# GymTracker — Backend API

Spring Boot REST API powering the GymTracker app. Manages workout templates, active sessions, and exercise logs, with a PostgreSQL database and schema migrations via Flyway.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.5 (Java 21) |
| Database | PostgreSQL (Docker) |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct + Lombok |
| Security | Spring Security |
| API docs | Swagger UI (OpenAPI) |

## Getting Started

### Prerequisites

- Java 21
- Docker + Docker Compose

### Run locally

```bash
# 1. Start the database
docker compose up -d

# 2. Start the API
./mvnw spring-boot:run
```

API runs at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Build & test

```bash
./mvnw clean package
./mvnw test           # uses H2 in-memory DB
```

## API Overview

| Resource | Base path | Description |
|----------|-----------|-------------|
| Workout Templates | `/api/workout-templates` | Create and manage reusable workout plans |
| Template Exercises | `/api/template-exercises` | Add/edit/delete exercises within a template |
| Workout Sessions | `/api/workout-sessions` | Start, finish, and list workout sessions |
| Exercise Logs | `/api/exercise-logs` | Log individual sets during an active session |

Full endpoint documentation with request/response schemas is available in Swagger UI when the app is running.

## Database

Schema is managed exclusively through Flyway migrations — Hibernate DDL auto is disabled (`validate` only). Migration files live in `src/main/resources/db/migration/` and follow the naming convention `V{n}__{description}.sql`.

Never modify the schema directly; always add a new migration file.

## Project Structure

```
src/main/java/com/gymtracker/gym/
├── workoutTemplates/     # Template entity, DTO, controller, service, repository
├── templateExercrcises/  # Exercise config per template (note: typo is intentional — matches existing package)
├── workoutSessions/      # Session lifecycle management
└── exerciseLogs/         # Per-set logging during a session
```

Controllers never expose JPA entities directly — all responses go through DTOs mapped with MapStruct.
