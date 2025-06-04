#See https://aka.ms/customizecontainer to learn how to customize your debug container and how Visual Studio uses this Dockerfile to build your images for faster debugging.

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
WORKDIR /app
EXPOSE 8080
EXPOSE 8081 # Assuming gRPC might also be on 8081, adjust if needed

# Creates a non-root user with an explicit UID and adds permission to access the /app folder
RUN adduser -u 5681 --disabled-password --gecos "" appuser && chown -R appuser /app
USER appuser

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["FundTransferService/Presentation/Presentation.csproj", "FundTransferService/Presentation/"]
COPY ["FundTransferService/Application/Application.csproj", "FundTransferService/Application/"]
COPY ["FundTransferService/Domain/Domain.csproj", "FundTransferService/Domain/"]
COPY ["FundTransferService/Infrastructure/Infrastructure.csproj", "FundTransferService/Infrastructure/"]

# If you have a solution file (.sln) for FundTransferService, uncomment and use:
# COPY ["FundTransferService/FundTransferService.sln", "FundTransferService/"] 
# RUN dotnet restore "FundTransferService/FundTransferService.sln"

RUN dotnet restore "FundTransferService/Presentation/Presentation.csproj"
COPY . .
WORKDIR "/src/FundTransferService/Presentation"
RUN dotnet build "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "Presentation.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "Presentation.dll"] 