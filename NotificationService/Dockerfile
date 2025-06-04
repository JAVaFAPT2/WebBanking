FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

# Creates a non-root user with an explicit UID and adds permission to access the /app folder
RUN adduser -u 5678 --disabled-password --gecos "" appuser && chown -R appuser /app
USER appuser

FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

# Copy csproj files and restore as distinct layers
COPY ["NotificationService/Presentation/Presentation.csproj", "NotificationService/Presentation/"]
COPY ["NotificationService/Application/Application.csproj", "NotificationService/Application/"]
COPY ["NotificationService/Domain/Domain.csproj", "NotificationService/Domain/"]
COPY ["NotificationService/Infrastructure/Infrastructure.csproj", "NotificationService/Infrastructure/"]
# If you had a Shared project for NotificationService, you would copy its csproj here too.

# Restore solution or individual projects. Restoring the solution is often cleaner if all projects are within it.
# COPY ["NotificationService/NotificationService.sln", "NotificationService/"]
# RUN dotnet restore "NotificationService/NotificationService.sln"
# Altenatively, restore the presentation project which should pull other dependencies.
RUN dotnet restore "NotificationService/Presentation/Presentation.csproj"

# Copy the Sdk files
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