# See https://aka.ms/customizecontainer to learn how to customize your debug container and how Visual Studio uses this Dockerfile to build your images for faster debugging.

# This stage is used when running from VS in fast mode (Default for Debug configuration)
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081 # Assuming gRPC might also be on 8081, adjust if needed

# Creates a non-root user with an explicit UID and adds permission to access the /app folder
RUN adduser -u 5680 --disabled-password --gecos "" appuser && chown -R appuser /app
USER appuser

# This stage is used to build the service project
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["AccountService/Presentation/Presentation.csproj", "AccountService/Presentation/"]
COPY ["AccountService/Application/Application.csproj", "AccountService/Application/"]
COPY ["AccountService/Domain/Domain.csproj", "AccountService/Domain/"]
COPY ["AccountService/Infrastructure/Infrastructure.csproj", "AccountService/Infrastructure/"]
COPY ["AccountService/Shared/Shared.csproj", "AccountService/Shared/"]

# If you have a solution file (.sln) for AccountService, uncomment and use:
# COPY ["AccountService/AccountService.sln", "AccountService/"] 
# RUN dotnet restore "AccountService/AccountService.sln"

RUN dotnet restore "AccountService/Presentation/Presentation.csproj"
COPY . .
WORKDIR "/src/AccountService/Presentation"
RUN dotnet build "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

# This stage is used to publish the service project to be copied to the final stage
FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Presentation.dll"]