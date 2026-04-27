# EPLMS — Employee Performance & Leave Management System

A production-ready Spring Boot application implementing Leave Management, Performance Management, Approval Workflow, and Notifications.

---

## Prerequisites

Make sure you have the following installed:

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17+ | https://adoptium.net |
| Git | Any | https://git-scm.com |

> **Maven is NOT required** — the project includes `mvnw` / `mvnw.cmd` wrappers that download Maven automatically.

---

## Quick Start (Clone & Run)

### Windows

```bash
git clone https://github.com/YOUR_USERNAME/EPLMS.git
cd EPLMS
mvnw.cmd spring-boot:run
```

### Linux / Mac

```bash
git clone https://github.com/YOUR_USERNAME/EPLMS.git
cd EPLMS
chmod +x mvnw
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**

---

## Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8080 | Frontend UI |
| http://localhost:8080/swagger-ui.html | Swagger API Docs |
| http://localhost:8080/h2-console | H2 Database Console |

**H2 Console settings:**
- JDBC URL: `jdbc:h2:mem:eplmsdb`
- Username: `sa`
- Password: *(leave blank)*

---

## Demo Accounts (auto-seeded on startup)

| Role | Email | Password |
|------|-------|----------|
| HR Manager | hr@eplms.com | password |
| Manager | manager@eplms.com | password |
| Employee | carol@eplms.com | password |
| Employee | dave@eplms.com | password |

---

## Run with Docker (MySQL)

Requires Docker Desktop: https://www.docker.com/products/docker-desktop

```bash
git clone https://github.com/YOUR_USERNAME/EPLMS.git
cd EPLMS
docker-compose up --build
```

App starts on **http://localhost:8080** backed by MySQL.

---

## Project Structure

```
com.eplms
├── controller        # REST endpoints (Auth, Leave, Performance, User)
├── service           # Business logic
├── repository        # Spring Data JPA interfaces
├── model             # JPA entities
├── dto               # Request/Response DTOs
├── strategy          # Approval Strategy Pattern
├── security          # JWT provider + filter
├── config            # SecurityConfig, SwaggerConfig, DataSeeder
└── exception         # GlobalExceptionHandler, custom exceptions
```

---

## Approval Workflow (Strategy Pattern)

| Days Requested | Strategy | Resulting Status |
|----------------|----------|-----------------|
| ≤ 2 days | AutoApproval | AUTO_APPROVED |
| 3 – 5 days | StandardApproval | PENDING (Team Lead) |
| > 5 days | HREscalation | ESCALATED |

---

## Key API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login → JWT token |

### Leave
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/leave/request | Submit leave request |
| GET | /api/leave/balance/{employeeId} | Get leave balance |
| GET | /api/leave/employee/{id} | Get employee's leaves |
| GET | /api/leave/pending | Get pending approvals |
| PUT | /api/leave/approve/{id} | Approve / Reject leave |

### Performance
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/performance/goals | Create goal |
| PUT | /api/performance/goals/{id}/status | Update goal status |
| GET | /api/performance/goals/employee/{id} | Get employee goals |
| POST | /api/performance/review/self | Submit self-review |
| POST | /api/performance/review/manager | Submit manager review |
| GET | /api/performance/review/employee/{id} | Get employee reviews |

---

## Final Rating Formula

```
finalRating = (selfRating × 0.4) + (managerRating × 0.6)
```

---

## Tech Stack

- Java 17 + Spring Boot 3.2
- Spring Security + JWT (jjwt 0.11.5)
- Spring Data JPA + Hibernate
- H2 (prototype) / MySQL (production via Docker)
- Springdoc OpenAPI (Swagger UI)
- HTML + CSS + Vanilla JS frontend
- Docker + Docker Compose

---

## Importing Postman Collection

1. Open Postman
2. Click **Import**
3. Select `EPLMS_Postman_Collection.json` from the project root
4. Run **Login as Employee** first — the token is auto-saved to collection variables
5. All other requests use the saved token automatically

---

## Switching to MySQL (without Docker)

1. Install MySQL and create a database named `eplmsdb`
2. Add the MySQL driver to `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

3. Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/eplmsdb
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```
