# WebBanking Development Services Startup Script
# This script starts the essential services for local testing with Android Studio

Write-Host "Starting WebBanking Development Services..." -ForegroundColor Green
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker status..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "Docker is running" -ForegroundColor Green
} catch {
    Write-Host "Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Stop any existing containers
Write-Host "Stopping existing containers..." -ForegroundColor Yellow
docker-compose -f docker-compose.dev.yml down

# Build and start services
Write-Host "Building and starting services..." -ForegroundColor Yellow
docker-compose -f docker-compose.dev.yml up --build -d

# Wait for services to be ready
Write-Host "Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Check service health
Write-Host "Checking service health..." -ForegroundColor Yellow

# Check API Gateway
Write-Host "Checking API Gateway..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "API Gateway is healthy" -ForegroundColor Green
    } else {
        Write-Host "API Gateway health check failed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "API Gateway is not responding" -ForegroundColor Red
}

# Check User Service
Write-Host "Checking User Service..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8051/api/User/health" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "User Service is healthy" -ForegroundColor Green
    } else {
        Write-Host "User Service health check failed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "User Service is not responding" -ForegroundColor Red
}

# Check Account Service
Write-Host "Checking Account Service..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8052/api/health" -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Host "Account Service is healthy" -ForegroundColor Green
    } else {
        Write-Host "Account Service health check failed" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Account Service is not responding" -ForegroundColor Red
}

Write-Host ""
Write-Host "Development services are ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Mobile App Configuration:" -ForegroundColor Cyan
Write-Host "   API Gateway: http://localhost:8081" -ForegroundColor White
Write-Host "   User Service: http://localhost:8051" -ForegroundColor White
Write-Host "   Account Service: http://localhost:8052" -ForegroundColor White
Write-Host ""
Write-Host "Useful URLs:" -ForegroundColor Cyan
Write-Host "   API Gateway Swagger: http://localhost:8081/swagger-ui.html" -ForegroundColor White
Write-Host "   API Gateway Health: http://localhost:8081/actuator/health" -ForegroundColor White
Write-Host "   User Service Health: http://localhost:8051/api/User/health" -ForegroundColor White
Write-Host "   Account Service Health: http://localhost:8052/api/health" -ForegroundColor White
Write-Host ""
Write-Host "To stop services, run: docker-compose -f docker-compose.dev.yml down" -ForegroundColor Yellow
Write-Host "To view logs, run: docker-compose -f docker-compose.dev.yml logs -f" -ForegroundColor Yellow 