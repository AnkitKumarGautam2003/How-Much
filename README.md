# How-Much Backend (P2P Lending)

Production-grade Spring Boot 3 backend for a peer-to-peer lending platform.

## Stack
- Java 17
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- JPA (Hibernate)
- Maven
- Docker (Render compatible)

## Security and Fintech Guardrails
- Money-safe design: currency values are required to be `long` (cents) only (no `float`/`double`).
- JWT stateless authentication with BCrypt password hashing.
- Role-based access control (LENDER, BORROWER, ADMIN).
- KYC status lifecycle (PENDING, VERIFIED, REJECTED).
- DTO-based request/response contracts to prevent mass assignment and entity exposure.
- Global exception handling and strict request validation.
- Idempotency-Key support for POST endpoints with DB-backed persistence.

## Run Locally
```bash
mvn spring-boot:run
```

## Required Environment Variables
- `DB_URL` (e.g. `jdbc:postgresql://host:5432/dbname`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET` (Base64-encoded key, at least 256-bit)
- `JWT_EXPIRATION_SECONDS` (optional, default `3600`)
- `PORT` (optional, default `8080`)

## Render Deployment
1. Push this repository to GitHub.
2. In Render, create a **PostgreSQL** service.
3. Create a new **Web Service** and connect this repository.
4. Use the provided `Dockerfile` for deployment.
5. Set environment variables in Render:
   - `DB_URL`: from Render PostgreSQL **Internal Database URL** translated to JDBC, format:
     `jdbc:postgresql://<host>:<port>/<database>`
   - `DB_USERNAME`: Render DB username
   - `DB_PASSWORD`: Render DB password
   - `JWT_SECRET`: secure Base64 secret
   - `JWT_EXPIRATION_SECONDS`: optional
6. Deploy.

## Health Endpoint
- `GET /api/health`

## Auth Endpoints
- `POST /api/auth/register`
- `POST /api/auth/login`

Include `Idempotency-Key` header for POST endpoints to prevent duplicate execution.
