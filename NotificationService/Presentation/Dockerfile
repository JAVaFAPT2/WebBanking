FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

# Creates a non-root user with an explicit UID and adds permission to access the /app folder
RUN adduser -u 5678 --disabled-password --gecos "" appuser && chown -R appuser /app
USER appuser

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["NotificationService/Presentation/Presentation.csproj", "NotificationService/Presentation/"]
COPY ["NotificationService/Application/Application.csproj", "NotificationService/Application/"]
COPY ["NotificationService/Domain/Domain.csproj", "NotificationService/Domain/"]
COPY ["NotificationService/Infrastructure/Infrastructure.csproj", "NotificationService/Infrastructure/"]
# Copy other .csproj files if the solution structure is different or they are directly referenced

# If you have a solution file (.sln), you might copy and restore it first
# COPY ["NotificationService.sln", "./"]
# RUN dotnet restore "NotificationService.sln"

RUN dotnet restore "NotificationService/Presentation/Presentation.csproj"
COPY . .
WORKDIR "/src/NotificationService/Presentation"
RUN dotnet build "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Presentation.dll"] 