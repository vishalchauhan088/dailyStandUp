# Daily Stand-Up Management System - Deployment Guide

## Quick Start

This application is ready to deploy and run. Simply start the Spring Boot application and it will:

1. **Automatically initialize the database** with required system constants (Roles and Statuses)
2. **Create database schema** automatically (H2 file-based database)
3. **Be ready to use** immediately

## Prerequisites

- Java 17 or higher
- Maven (included via Maven Wrapper)

## Running the Application

### Option 1: Using Maven Wrapper (Recommended)
```bash
./mvnw spring-boot:run
```

### Option 2: Using Maven
```bash
mvn spring-boot:run
```

### Option 3: Build and Run JAR
```bash
./mvnw clean package
java -jar target/dailyStandUp-0.0.1-SNAPSHOT.jar
```

## Database

- **Type**: H2 File Database
- **Location**: `./data/dailyStandUpDB.mv.db`
- **Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/dailyStandUpDB`
  - Username: `sa`
  - Password: (empty)

## Initial Data

On first startup, the system automatically creates:

### Roles:
- ADMIN
- OWNER
- MANAGER
- TEAM_ADMIN
- MEMBER

### Statuses:
- TO_DO
- IN_PROGRESS
- BLOCKED
- REVIEW
- DONE

## API Endpoints

### Public Endpoints (No Authentication Required)
- `GET /api/v1/public/health` - Health check
- `GET /api/v1/public/roles` - List all roles
- `GET /api/v1/public/status` - List all statuses
- `GET /api/v1/public/teams` - List all teams
- `POST /api/v1/auth/signup` - User registration
- `POST /api/v1/auth/login` - User login

### Protected Endpoints (JWT Token Required)
- All other endpoints under `/api/v1/*`

## Configuration

Edit `src/main/resources/application.properties` to customize:

- JWT Secret: `app.jwt.secret`
- JWT Expiration: `app.jwt.expiration` (in milliseconds)
- Database: Change `spring.datasource.url` for production database

## Production Deployment

For production, update `application.properties`:

1. Change database from H2 to PostgreSQL/MySQL:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/dailystandup
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   ```

2. Update JWT secret to a secure random string

3. Set `spring.jpa.hibernate.ddl-auto=validate` for production

## Features

✅ Role-based access control (RBAC)
✅ Team management with join requests
✅ Project management
✅ Ticket tracking with dependencies
✅ Daily stand-up posts (one per employee per team per day)
✅ Soft deletes for data integrity
✅ Comprehensive reporting

## Troubleshooting

- **Port already in use**: Change `server.port` in `application.properties`
- **Database locked**: Close H2 console or other connections
- **JWT errors**: Check JWT secret in `application.properties`

