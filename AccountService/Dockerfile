# Build stage
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY . .
RUN dotnet restore ./Presentation/Presentation.csproj
RUN dotnet publish ./Presentation/Presentation.csproj -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 80
ENTRYPOINT ["dotnet", "Presentation.dll"]

ENV ConnectionStrings__DefaultConnection=Server=db;Database=AccountService_db;Uid=root;Pwd=root_password;Port=3306; 