# Build stage
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src/TransactionService

# Copy only the project file for restore
COPY ["Presentation/Presentation.csproj", "Presentation/"]
COPY ["Application/Application.csproj", "Application/"]
COPY ["Domain/Domain.csproj", "Domain/"]
COPY ["Infrastructure/Infrastructure.csproj", "Infrastructure/"]

# Restore dependencies
RUN dotnet restore "Presentation/Presentation.csproj"

# Copy the rest of the application code
COPY . .

# Publish the application
RUN dotnet publish "Presentation/Presentation.csproj" -c Release -o /app/publish
RUN ls -l /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app

# Copy published output from build stage
COPY --from=build /app/publish .

EXPOSE 80
ENTRYPOINT ["sleep", "infinity"] 