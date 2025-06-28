# WebBanking Docker Testing Guide

This guide will help you set up and test the WebBanking project using Docker containers with Android Studio.

## 🚀 Quick Start

### Prerequisites
- Docker Desktop installed and running
- Android Studio with Android Emulator
- PowerShell (for Windows)

### 1. Start Development Services

Run the development startup script:

```powershell
.\start-dev-services.ps1
```

This script will:
- Check Docker status
- Build and start all essential services
- Verify service health
- Display connection information

### 2. Verify Services are Running

Check that all services are healthy:

```powershell
# Check API Gateway
curl http://localhost:8088/actuator/health

# Check User Service
curl http://localhost:8051/api/User/health

# Check Account Service
curl http://localhost:8052/api/health
```

### 3. Test with Android Studio

1. **Open Android Studio**
2. **Open the WebBankingMobileApp project**
3. **Run the app on an Android Emulator**
4. **Test the registration and login functionality**

## 📱 Mobile App Configuration

The mobile app is already configured to connect to the Docker services:

- **API Gateway**: `http://10.0.2.2:8088` (Android emulator localhost)
- **User Service**: `http://10.0.2.2:8051`
- **Account Service**: `http://10.0.2.2:8052`

## 🔧 Service Architecture

### Development Services (docker-compose.dev.yml)

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8088 | Main entry point, routing, authentication |
| User Service | 8051 | User management, authentication |
| Account Service | 8052 | Account management |
| Kafka | 9092 | Message broker |
| MySQL | 3306 | Database |
| Redis | 6379 | Caching |

### API Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

#### User Management
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user

#### Account Management
- `GET /api/accounts/{id}` - Get account details
- `POST /api/accounts` - Create account

## 🧪 Testing Scenarios

### 1. User Registration Test

```bash
curl -X POST http://localhost:8088/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPassword123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "1234567890",
    "dateOfBirth": "1990-01-01T00:00:00Z",
    "address": {
      "street": "123 Test St",
      "city": "Test City",
      "state": "Test State",
      "zipCode": "12345",
      "country": "Test Country"
    }
  }'
```

### 2. User Login Test

```bash
curl -X POST http://localhost:8088/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPassword123!"
  }'
```

### 3. Mobile App Testing

1. **Launch the app in Android Studio**
2. **Navigate to the registration screen**
3. **Fill in the registration form**
4. **Submit and verify successful registration**
5. **Navigate to login screen**
6. **Login with registered credentials**
7. **Verify successful login and token generation**

## 🔍 Monitoring and Debugging

### View Service Logs

```powershell
# View all service logs
docker-compose -f docker-compose.dev.yml logs -f

# View specific service logs
docker-compose -f docker-compose.dev.yml logs -f api-gateway
docker-compose -f docker-compose.dev.yml logs -f user-service
docker-compose -f docker-compose.dev.yml logs -f account-service
```

### Health Checks

```powershell
# API Gateway Health
curl http://localhost:8088/actuator/health

# User Service Health
curl http://localhost:8051/api/User/health

# Account Service Health
curl http://localhost:8052/api/health
```

### Swagger Documentation

- **API Gateway Swagger**: http://localhost:8088/swagger-ui.html
- **User Service Swagger**: http://localhost:8051/swagger
- **Account Service Swagger**: http://localhost:8052/swagger

## ��️ Troubleshooting

### Common Issues

#### 1. Port Already in Use
```powershell
# Check what's using the port
netstat -ano | findstr :8088

# Kill the process if needed
taskkill /PID <PID> /F
```

#### 2. Docker Build Failures
```powershell
# Clean Docker cache
docker system prune -a

# Rebuild without cache
docker-compose -f docker-compose.dev.yml build --no-cache
```

#### 3. Database Connection Issues
```powershell
# Check database container
docker-compose -f docker-compose.dev.yml logs db

# Restart database
docker-compose -f docker-compose.dev.yml restart db
```

#### 4. Kafka Connection Issues
```powershell
# Check Kafka container
docker-compose -f docker-compose.dev.yml logs kafka

# Restart Kafka
docker-compose -f docker-compose.dev.yml restart kafka
```

### Service Dependencies

The services start in this order:
1. **Database (MySQL)** - Required by all services
2. **Kafka** - Required for messaging
3. **User Service** - Core authentication
4. **Account Service** - Account management
5. **API Gateway** - Main entry point

## 🧹 Cleanup

### Stop All Services

```powershell
docker-compose -f docker-compose.dev.yml down
```

### Remove All Data

```powershell
docker-compose -f docker-compose.dev.yml down -v
docker volume prune
```

## 📋 Development Workflow

1. **Start services**: `.\start-dev-services.ps1`
2. **Make code changes** in your IDE
3. **Rebuild services**: `docker-compose -f docker-compose.dev.yml up --build -d`
4. **Test changes** in Android Studio
5. **View logs** for debugging
6. **Stop services** when done: `docker-compose -f docker-compose.dev.yml down`

## 🔗 Useful Commands

```powershell
# Start services
.\start-dev-services.ps1

# Stop services
docker-compose -f docker-compose.dev.yml down

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Rebuild specific service
docker-compose -f docker-compose.dev.yml up --build -d api-gateway

# Check service status
docker-compose -f docker-compose.dev.yml ps

# Access service shell
docker-compose -f docker-compose.dev.yml exec user-service /bin/bash
```

## 📞 Support

If you encounter issues:

1. Check the service logs
2. Verify all prerequisites are met
3. Ensure Docker Desktop is running
4. Check port availability
5. Review the troubleshooting section above 