# Web Banking Project

## Overview

This project consists of several microservices (UserService, OrchestrationService, ApiGateway, etc.) built using Docker.  
Below are the commands to build and run the project.

## Prerequisites

- Docker and Docker Compose installed on your machine.
- (Optional) .NET SDK (for local development) and Java (for Spring Boot services).

## Build and Run

### Build All Services

To build all services (including UserService, OrchestrationService, ApiGateway, etc.), run:

```bash
docker-compose build
```

### Start All Services

To start all services (in the foreground), run:

```bash
docker-compose up
```

If you want to run the services in detached mode (in the background), run:

```bash
docker-compose up -d
```

### Stop All Services

To stop all running services, run:

```bash
docker-compose down
```

## Additional Commands

- **Rebuild a Single Service (e.g., UserService):**

  ```bash
  docker-compose build user-service
  ```

- **View Logs (if running in detached mode):**

  ```bash
  docker-compose logs -f
  ```

- **Restart a Service (e.g., UserService):**

  ```bash
  docker-compose restart user-service
  ```

- **Remove All Containers, Volumes, and Images (if you want to clean up):**

  ```bash
  docker-compose down --volumes --rmi all
  ```

## Notes

- Ensure that your Docker daemon is running.
- If you run into issues (for example, port conflicts or missing dependencies), please check your Docker logs or the service's Dockerfile. 