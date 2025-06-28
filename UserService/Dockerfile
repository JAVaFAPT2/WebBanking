# Build stage
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY . .
RUN dotnet restore ./Presentation/Presentation.csproj
RUN dotnet publish ./Presentation/Presentation.csproj -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:9.0-bookworm-slim-amd64 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
ENV Kafka__BootstrapServers=kafka:9092
ENV DOTNET_SYSTEM_GLOBALIZATION_INVARIANT=false
ENV KAFKA_CLIENT_DNS_LOOKUP=resolve_canonical_bootstrap_servers_only
ENV ASPNETCORE_URLS=http://+:8051
ENV ConnectionStrings__DefaultConnection=Server=db;Database=UserService_db;Uid=root;Pwd=root_password;Port=3306;
EXPOSE 80
ENTRYPOINT ["dotnet", "Presentation.dll", "--Kafka:BootstrapServers=kafka:9092"] 