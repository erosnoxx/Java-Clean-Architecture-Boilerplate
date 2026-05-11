# Java Clean Arch
A production-ready Java boilerplate built on **Clean Architecture** and **Domain-Driven Design** principles, modularized with Maven multi-module and powered by Spring Boot 3.
Licensed under the [MIT License](LICENSE).
---
## Philosophy
This boilerplate is opinionated where it matters and flexible where it counts.
The goal is a codebase where **business rules never depend on frameworks**, where each layer has a single, clear responsibility, and where adding a new domain module is a matter of following a well-defined pattern — not fighting the infrastructure.
Every architectural decision here was made deliberately. If something seems verbose, there's a reason. The [`docs/decisions.md`](2%20-%20docs/decisions.md) explains the thinking behind each one.
---
## Modules
```txt
backend-archetype/
├── domain/           Business rules, entities, value objects, domain events
├── application/      Use cases, ports, DTOs, mappers
├── infrastructure/   JPA, security, JWT, Flyway, adapters
├── web/              Controllers, exception handling, security annotations
└── boot/             Spring Boot entry point, bean wiring, seeders, config
```
Each module is a Maven artifact. The dependency flow is strictly one-directional:
```txt
domain ← application ← infrastructure
                    ← web
                         ← boot
```

`domain` knows nothing about Spring. `application` knows nothing about JPA or JWT. This is the core invariant of the architecture — never break it.

---
## Stack
| Concern | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 17 |
| Migrations | Flyway |
| Security | Spring Security + JWT (auth0/java-jwt) |
| Mapping | MapStruct + custom BaseMapper |
| Utilities | Lombok |
| HTTP Clients | Spring Cloud OpenFeign |
| Build | Maven 3.9 |
| Containerization | Docker + Docker Compose |
---
## Getting Started
### Prerequisites
- Java 21
- Maven 3.9+
- Docker + Docker Compose
### Running locally (IntelliJ)
A `.run/BootApplication.run.xml` configuration is included. Set the following environment variables in your run configuration:
```env
ACTIVE_PROFILE=dev
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=your_db
DB_SCHEMA=public
DB_USER=your_user
DB_PWD=your_password
SEC_KEY=your-secret-key
```
### Running with Docker
```cmd
cd 1 - enviroment/docker
run.cmd
```

Edit `run.cmd` to set your environment variables before running.

---
## Default Seeding
On startup, the application seeds initial data from JSON files located in `boot/src/main/resources/seeders/`.
The default `users.json` creates an admin user. Credentials can be changed directly in the file before the first run.
The seeder is idempotent — it skips records that already exist.
---
## Authentication
Authentication is JWT-based. The flow:
1. `POST /auth/login` — returns `access_token` and `refresh_token`
2. `POST /auth/refresh` — returns a new token pair
3. `POST /auth/register` — admin-only, creates a new user
All protected routes require `Authorization: Bearer <token>` header.
Route-level authorization is handled via annotations:
```java
@AdminOnly          // requires ROLE_ADMIN
@AuthenticatedOnly  // requires any authenticated user
```
---
## Module Documentation
- [`docs/domain.md`](2%20-%20docs/domain.md)
- [`docs/application.md`](2%20-%20docs/application.md)
- [`docs/infrastructure.md`](2%20-%20docs/infrastructure.md)
- [`docs/web.md`](2%20-%20docs/web.md)
- [`docs/boot.md`](2%20-%20docs/boot.md)
- [`docs/decisions.md`](2%20-%20docs/decisions.md)