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
COPY ["OrchestrationService.sln", "./"]
COPY ["Domain/OrchestrationService.Domain.csproj", "Domain/"]
COPY ["Application/OrchestrationService.Application.csproj", "Application/"]
COPY ["Infrastructure/OrchestrationService.Infrastructure.csproj", "Infrastructure/"]
COPY ["Presentation/OrchestrationService.Presentation.csproj", "Presentation/"]

RUN dotnet restore "OrchestrationService.sln"
COPY . .
WORKDIR "/src/Presentation"
RUN dotnet build "OrchestrationService.Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "OrchestrationService.Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "OrchestrationService.Presentation.dll"] 