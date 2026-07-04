# services/backend

REST API backend for FS-Iris. Handles authentication, user management, and exposes HTTP endpoints consumed by the web frontend.

---

## Stack

| Category | Technology |
|---|---|
| Framework | Spring Boot 4.1.0 |
| Language | Java 21 |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Auth | JJWT 0.12.3 + BCrypt (spring-security-crypto) |
| API Docs | springdoc-openapi 2.8.13 (Swagger UI at `/swagger-ui.html`) |
| Build | Maven |

---

## Architecture

Follows a layered architecture where outer layers depend on inner layers, never the reverse.

```
┌──────────────────────────────────────┐
│             Controller               │
├──────────────────────────────────────┤
│              Service                 │
├──────────────────────────────────────┤
│            Repository                │
├──────────────────────────────────────┤
│              Entity                  │
└──────────────────────────────────────┘
         Config / Util (cross-cutting)
```

**Auth:** manual `HandlerInterceptor` pattern — no Spring Security filter chain. JWT validation happens in `AuthInterceptor` before any protected controller is reached.

---

## Directory Structure

```
services/backend/
├── src/main/java/com/iris/backend/
│   ├── config/        — CORS, BCrypt, Swagger
│   ├── controller/    — HTTP handlers, global exception handler
│   ├── entity/        — JPA entities
│   ├── interceptor/   — Auth guard
│   └── util/          — Shared utilities
└── src/main/resources/
    ├── application.properties
    ├── database.example.properties   — copy to database.properties and fill in credentials
    └── jwt.example.properties        — copy to jwt.properties and fill in secret
```

---

## Setup

```bash
# 1. Copy config templates
cp src/main/resources/database.example.properties src/main/resources/database.properties
cp src/main/resources/jwt.example.properties src/main/resources/jwt.properties

# 2. Fill in database.properties and jwt.properties

# 3. Run
mvn spring-boot:run
```

Server starts on `http://localhost:8080`.

---

## What's Next

- Auth endpoints — `POST /auth/login`, `POST /auth/register`
- `AuthInterceptor` implementation and route wiring
- Service layer per domain
- Additional entities
