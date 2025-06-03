# Use the official .NET SDK image for building the application
# Make sure to use the correct version matching your project's target framework (net9.0)
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /source

# Copy solution file and all project files first to leverage Docker layer caching for dependencies
# Copy .sln file (assuming it's in the root of the WebBanking solution)
# Adjust the path ../../WebBanking.sln if your solution file is located elsewhere relative to this Dockerfile
# For now, let's assume the Docker build context is TransactionService/ and the sln is one level up.
# If the build context is WebBanking/, then it would be just WebBanking.sln
# Given the Dockerfile is in TransactionService/Presentation, and sln is at WebBanking/ (two levels up from Presentation)
COPY ["../../WebBanking.sln", "WebBanking.sln"] 

# Copy all .csproj files for TransactionService and its dependencies
# Adjust paths as necessary based on your solution structure and build context
COPY ["Application/Application.csproj", "Application/Application.csproj"]
COPY ["Domain/Domain.csproj", "Domain/Domain.csproj"]
COPY ["Infrastructure/Infrastructure.csproj", "Infrastructure/Infrastructure.csproj"]
COPY ["Presentation/Presentation.csproj", "Presentation/Presentation.csproj"]

# Restore dependencies for all projects in the solution context defined by the .sln
RUN dotnet restore "WebBanking.sln"

# Copy the rest of the source code
# Copy the entire TransactionService directory into the /source/TransactionService directory in the image
COPY ["../", "/source/TransactionService/"]

# Set the working directory to the Presentation project
WORKDIR "/source/TransactionService/Presentation"

# Build and publish the Presentation project
# Output will be in /app/publish
RUN dotnet publish -c Release -o /app/publish --no-restore

# Use the official .NET ASP.NET runtime image for the final application
# Make sure to use the correct version matching your project's target framework (net9.0)
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app

# Copy the published application from the build stage
COPY --from=build /app/publish .

# Expose the ports the application listens on (as per appsettings.json)
# HTTP port
EXPOSE 5203
# gRPC port
EXPOSE 5204

# Set the entry point for the application
ENTRYPOINT ["dotnet", "TransactionService.Presentation.dll"] 