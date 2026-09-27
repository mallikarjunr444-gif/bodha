# BODHA — Backend Service

The backend REST API service for **BODHA (बोध)**, an AI-powered personalized learning and skill-development platform.

## Tech Stack
- **Language:** Java 21 (LTS)
- **Framework:** Spring Boot 3.4.3
- **Build Tool:** Maven 3.9+
- **Database Layer:** Spring Data JPA + Hibernate + PostgreSQL Driver
- **Validation:** Spring Boot Starter Validation (Hibernate Validator)
- **Web Layer:** Spring Web (Embedded Tomcat)

## Project Structure
```
backend/
├── pom.xml                                      # Maven project dependencies & plugins
├── README.md                                    # Backend documentation & run guide
└── src
    ├── main
    │   ├── java/com/bodha
    │   │   ├── BodhaApplication.java           # Spring Boot application entry point
    │   │   ├── config/                          # Security, CORS, and Web MVC configs
    │   │   ├── controller/                      # REST API controllers
    │   │   │   └── HealthController.java        # /api/health monitoring endpoint
    │   │   ├── dto/                             # Request and response data transfer objects
    │   │   ├── model/                           # JPA entity models (matches database/schema.sql)
    │   │   ├── repository/                      # Spring Data JPA repositories
    │   │   └── service/                         # Business logic and adaptive roadmap engine
    │   └── resources
    │       └── application.yml                  # Server port, PostgreSQL datasource, and JPA configs
    └── test
        └── java/com/bodha
            └── BodhaApplicationTests.java      # Context loading test suite
```

## Running Locally

1. **Navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Compile and run tests:**
   ```bash
   mvn clean test
   ```

3. **Start the development server:**
   ```bash
   mvn spring-boot:run
   ```
   The service will start on `http://localhost:8080`.

4. **Verify health endpoint:**
   ```bash
   curl http://localhost:8080/api/health
   ```
   Expected response:
   ```json
   {
     "status": "UP",
     "service": "BODHA API",
     "version": "1.0.0",
     "timestamp": "2026-09-13T..."
   }
   ```
