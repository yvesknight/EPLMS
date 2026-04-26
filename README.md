# EPLMS — Employee Performance & Leave Management System

A production-ready Spring Boot application implementing Leave Management, Performance Management, Approval Workflow, and Notifications.

---

## Quick Start (H2 Prototype)

```bash
cd EPLMS
mvn spring-boot:run
```

Open: http://localhost:8080

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:eplmsdb`)

---

## Demo Accounts (auto-seeded)

| Role        | Email                  | Password   |
|-------------|------------------------|------------|
| HR Manager  | hr@eplms.com           | password   |
| Manager     | manager@eplms.com      | password   |
| Employee    | carol@eplms.com        | password   |
| Employee    | dave@eplms.com         | password   |

---

## Docker (MySQL)

```bash
docker-compose up --build
```

---

## Project Structure

```
com.eplms
├── controller        # REST endpoints (Auth, Leave, Performance, User)
├── service           # Business logic (Auth, Leave, Performance, User, Notification)
├── repository        # Spring Data JPA interfaces
├── model             # JPA entities (User, Employee, Manager, HRManager, LeaveRequest, ...)
├── dto               # Request/Response DTOs
├── strategy          # Approval Strategy Pattern (Auto, Standard, HREscalation, Engine)
├── security          # JWT provider + filter
├── config            # SecurityConfig, SwaggerConfig, DataSeeder
└── exception         # GlobalExceptionHandler, custom exceptions
```

---

## Approval Workflow (Strategy Pattern)

| Days Requested | Strategy           | Resulting Status |
|----------------|--------------------|------------------|
| ≤ 2 days       | AutoApproval       | AUTO_APPROVED    |
| 3 – 5 days     | StandardApproval   | PENDING (Team Lead) |
| > 5 days       | HREscalation       | ESCALATED        |

---

## Key API Endpoints

### Auth
| Method | Endpoint              | Description        |
|--------|-----------------------|--------------------|
| POST   | /api/auth/register    | Register user      |
| POST   | /api/auth/login       | Login → JWT token  |

### Leave
| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| POST   | /api/leave/request              | Submit leave request     |
| GET    | /api/leave/balance/{employeeId} | Get leave balance        |
| GET    | /api/leave/employee/{id}        | Get employee's leaves    |
| GET    | /api/leave/pending              | Get pending approvals    |
| PUT    | /api/leave/approve/{id}         | Approve / Reject leave   |

### Performance
| Method | Endpoint                                  | Description           |
|--------|-------------------------------------------|-----------------------|
| POST   | /api/performance/goals                    | Create goal           |
| PUT    | /api/performance/goals/{id}/status        | Update goal status    |
| GET    | /api/performance/goals/employee/{id}      | Get employee goals    |
| POST   | /api/performance/review/self              | Submit self-review    |
| POST   | /api/performance/review/manager           | Submit manager review |
| GET    | /api/performance/review/employee/{id}     | Get employee reviews  |

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
- H2 (prototype) / MySQL (production)
- Springdoc OpenAPI (Swagger UI)
- HTML + CSS + Vanilla JS frontend
- Docker + Docker Compose
