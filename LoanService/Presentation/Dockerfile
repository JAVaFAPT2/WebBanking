FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081 # Assuming gRPC might also be on 8081 like other services, adjust if needed

# Creates a non-root user with an explicit UID and adds permission to access the /app folder
RUN adduser -u 5679 --disabled-password --gecos "" appuser && chown -R appuser /app
USER appuser

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["LoanService/Presentation/Presentation.csproj", "LoanService/Presentation/"]
COPY ["LoanService/Application/Application.csproj", "LoanService/Application/"]
COPY ["LoanService/Domain/Domain.csproj", "LoanService/Domain/"]
COPY ["LoanService/Infrastructure/Infrastructure.csproj", "LoanService/Infrastructure/"]
COPY ["LoanService/Shared/Shared.csproj", "LoanService/Shared/"]

# If you have a solution file (.sln) for LoanService, uncomment and use:
# COPY ["LoanService/LoanService.sln", "LoanService/"]
# RUN dotnet restore "LoanService/LoanService.sln"

RUN dotnet restore "LoanService/Presentation/Presentation.csproj"
COPY . .
WORKDIR "/src/LoanService/Presentation"
RUN dotnet build "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Presentation.dll"] 