#See https://aka.ms/customizecontainer to learn how to customize your debug container and how Visual Studio uses this Dockerfile to build your images for faster debugging.

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
USER app
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

# Copy solution and project files first to leverage Docker layer caching
COPY ["FundTransferService.sln", "FUND_TRANSFER/"]
COPY ["FundTransferService/Presentation/Presentation.csproj", "FUND_TRANSFER/FundTransferService/Presentation/"]
COPY ["FundTransferService/Application/Application.csproj", "FUND_TRANSFER/FundTransferService/Application/"]
COPY ["FundTransferService/Domain/Domain.csproj", "FUND_TRANSFER/FundTransferService/Domain/"]
COPY ["FundTransferService/Infrastructure/Infrastructure.csproj", "FUND_TRANSFER/FundTransferService/Infrastructure/"]

# Adjust paths if your .sln file is not in FUND_TRANSFER directory relative to DockerfileContext
# For example, if DockerfileContext is /WebBanking and sln is /WebBanking/FUND_TRANSFER/FundTransferService.sln:
# COPY ["FUND_TRANSFER/FundTransferService.sln", "FUND_TRANSFER/"] 
# COPY ["FUND_TRANSFER/FundTransferService/Presentation/Presentation.csproj", "FUND_TRANSFER/FundTransferService/Presentation/"]
# etc.

# Restore solution relative to where the .sln file is copied
RUN dotnet restore "FUND_TRANSFER/FundTransferService.sln"

# Copy the rest of the application code
COPY . .
WORKDIR "/src/FUND_TRANSFER/FundTransferService/Presentation"
RUN dotnet build "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "FundTransferService.Presentation.dll"] 