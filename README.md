# GymTracker — Backend API

Spring Boot REST API behind the GymTracker app. Manages workout templates, training programs, live sessions and set-by-set logs, with PostgreSQL, Flyway migrations and Keycloak-issued JWTs.

## API Overview

| Resource | Base path | Description |
|----------|-----------|-------------|
| Workout Templates | `/api/workout-templates` | Reusable sessions you build yourself |
| Template Exercises | `/api/template-exercises` | Exercises inside a template, with default sets, reps and weight |
| Workout Sessions | `/api/workout-sessions` | Start from a template or a program day, finish, discard, fetch the last and the ongoing one |
| Exercise Logs | `/api/exercise-logs` | Log and delete single sets, reorder them, and read what was lifted last time |
| Training Programs | `/api/training-programs` | Persisted programs — list, edit, activate, archive, and read the next day due |
| AI Plans | `/api/ai-plans` | Start a generation, poll it, regenerate with feedback |

Full request/response schemas are in Swagger UI at `http://localhost:8080/swagger-ui.html` while the app runs.

## Authentication

Every endpoint except Swagger requires a Bearer JWT issued by Keycloak. The API is a pure OAuth2 resource server: stateless, CSRF disabled, no cookies.

Keycloak owns identity; the database owns your data. On the first request a user's `sub` claim is resolved to a local `users` row (`keycloak_id`, `email`), and every template, session, log and program hangs off that row — so a request can only ever reach its owner's records. Controllers receive the resolved user through a `@CurrentUser` argument resolver rather than digging into the security context themselves.

Failures come back as RFC 7807 `ProblemDetail` JSON, not HTML redirects: `401` when the token is missing or bad, `403` when it's valid but not allowed.

## AI-generated programs

Generation is asynchronous. `POST /api/ai-plans/generations` writes a row with status `PENDING` and returns immediately; a worker calls Claude in the background and flips the row to `SUCCEEDED` (with the generated program and token counts) or `FAILED` (with the reason). The frontend polls the generation by id.

The prompt is built from the user's actual training history — a summary service feeds recent sessions, weights and volumes in, so the plan is calibrated rather than generic. Generations are rate-limited per day.

`gymtracker.ai.enabled` picks the client: `true` uses the Anthropic API and needs `ANTHROPIC_API_KEY`, `false` (the default) swaps in a deterministic stub that returns a fixed program. The stub keeps the whole flow testable and costs nothing.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.5 (Java 21) |
| Database | PostgreSQL 16 (Docker) |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mapping | MapStruct + Lombok |
| Security | Spring Security, OAuth2 Resource Server + Keycloak |
| AI | Anthropic Java SDK |
| API docs | springdoc-openapi (Swagger UI) |
| Tests | JUnit 5 + Testcontainers |

## Getting Started

### Prerequisites

- Java 21
- Docker + Docker Compose

### Run locally

```bash
docker compose up -d      # PostgreSQL on 5432, Keycloak on 8081
./mvnw spring-boot:run    # API on 8080
```

Keycloak needs a realm `gymtracker` with a public client `gym-api` (Authorization Code + PKCE, redirect URI `http://localhost:3000`). Admin console is at `http://localhost:8081`.

To generate real plans instead of the stub, export `ANTHROPIC_API_KEY` and set `gymtracker.ai.enabled: true`.

### Build & test

```bash
./mvnw clean package
./mvnw test      # unit tests — classes named *Test, no containers needed
./mvnw verify    # integration tests — classes named *IT, spin up PostgreSQL via Testcontainers
```

## Database

Schema is managed exclusively through Flyway — Hibernate DDL auto stays at `validate`. Migrations live in `src/main/resources/db/migration/` as `V{n}__{description}.sql`, currently up to V16: templates and exercises, sessions and logs, users and the `user_id` columns that made the data per-owner, then the training-program tree and AI generation records. (V6 and V7 were never used — Flyway only cares that versions rise, so the gap is harmless.)

Never change the schema by hand; add a migration.

## Project Structure

```
src/main/java/com/gymtracker/gym/
├── workoutTemplates/   # Templates you build yourself
├── templateExercises/  # Exercises and defaults inside a template
├── workoutSessions/    # Session lifecycle: start, finish, discard
├── exerciseLogs/       # Per-set logging, reordering, previous-session lookup
├── trainingPrograms/   # Persisted programs — weeks, days, prescribed exercises
├── aiPlans/            # Claude client, prompt builder, async generation worker
├── users/              # Keycloak subject → local user, @CurrentUser resolver
├── config/             # Security filter chain, CORS, REST error entry points
└── exceptions/         # Domain exceptions and the global handler
```

Each domain package splits the same way: `controller`, `service`, `repository`, `model`, `dto`, `mapper`.

## Conventions

- Controllers never return JPA entities — everything goes out as a DTO mapped with MapStruct.
- In `pom.xml`, Lombok must stay ahead of MapStruct in `annotationProcessorPaths`, or the generated mappers won't see the builders.
- The active profile is `dev`, which enables Swagger UI and Flyway.
