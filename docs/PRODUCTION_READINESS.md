# BODHA — Production Readiness Guide

> Module O completed: 2026-09-27  
> Status: **SECURITY HARDENED — READY FOR PRODUCTION DEPLOYMENT (Module P)**

---

## 1. Current Architecture

```
┌────────────────────────────────────────────────────────┐
│  React 19 + Vite + Tailwind CSS (SPA)                  │
│  Serves on: http://localhost:5173 (dev)                │
│  Auto-attaches Authorization: Bearer <JWT> via api.js   │
│  Centralized 401 handler triggers automatic logout    │
│  Demo access: controlled via VITE_ENABLE_DEMO_ACCESS   │
└───────────────────────┬────────────────────────────────┘
                        │ REST + Bearer JWT
                        ▼
┌────────────────────────────────────────────────────────┐
│  Spring Boot 3.4.3 / Java 21 Backend                   │
│  Serves on: http://localhost:8080                      │
│  CorsFilter (reads CORS_ALLOWED_ORIGINS, rejects *)   │
│  Security Headers (X-Content-Type, Frame, Referrer)    │
│  JwtAuthenticationFilter (HMAC-SHA256, >=256-bit key)  │
│  RateLimitingFilter (Token bucket: Auth & AI limits)   │
│  CurrentUserService (server-side identity from context)│
│  Spring Data JPA + Hibernate                           │
│  GlobalExceptionHandler (sanitized SQL / 400 / 429)    │
└───────────────────────┬────────────────────────────────┘
                        │ JDBC (HikariCP)
                        ▼
┌────────────────────────────────────────────────────────┐
│  PostgreSQL 16                                         │
│  Database: bodha_db (17 tables, zero data resets)      │
│  BCrypt hashed passwords ($2a$ format, 10 rounds)      │
│  Schema managed by schema.sql                          │
└────────────────────────────────────────────────────────┘
```

**Authentication Layer (Module N & O):**
- **Library:** JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- **Signature Algorithm:** HMAC-SHA256 (`Keys.hmacShaKeyFor`) with >= 256-bit (32 character) secret.
- **Production Secret Enforcement:** `JwtService` checks Spring `Environment`. In `prod` or `production` profiles, using the development fallback key throws `IllegalStateException` preventing startup. Secrets are never logged or exposed.
- **Claims:** `sub` (userId string), `userId` (Long), `email` (String), `role` (String), `iat` (Date), `exp` (Date). Passwords and password hashes are strictly excluded.
- **Expiration:** Configurable via `bodha.jwt.expiration-ms` / `JWT_EXPIRATION_MS` (default 86,400,000 ms = 24 hours).
- **Security Context:** `JwtAuthenticationFilter` validates token, instantiates `AuthenticatedUser` principal, and populates `SecurityContextHolder`.
- **Identity Enforcement:** All 7 domain controllers use `CurrentUserService.requireCurrentUserId()`. Client-supplied user ID parameters cannot override or forge authentication.
- **Error Responses:** `JwtAuthenticationEntryPoint` renders uniform JSON 401 Unauthorized responses.

**Rate Limiting Layer (Module O):**
- **Implementation:** `InMemoryRateLimiter` (Thread-safe Token Bucket with automated 15-minute idle eviction).
- **Endpoints Protected:**
  - `POST /api/auth/login`: 10 requests / minute (keyed by client IP / X-Forwarded-For).
  - `POST /api/auth/register`: 5 requests / minute (keyed by client IP / X-Forwarded-For).
  - `/api/ai/**`: 20 requests / minute (keyed by authenticated `userId` or client IP fallback).
- **Rejection Response:** HTTP 429 Too Many Requests with `Retry-After: 60` header and structured JSON body:
  `{"status":429,"error":"Too Many Requests","message":"Rate limit exceeded. Please try again later.","timestamp":"..."}`.

**Security Headers (Module O):**
- `X-Content-Type-Options: nosniff` (prevents MIME sniffing).
- `X-Frame-Options: DENY` (clickjacking protection).
- `Referrer-Policy: strict-origin-when-cross-origin` (prevents referrer leakage).
- `Permissions-Policy: camera=(), microphone=(), geolocation=()` (disables unused browser capabilities).

**Error Response Sanitization (Module O):**
- `GlobalExceptionHandler` explicitly catches `DataAccessException` and `SQLException`, logging the trace internally while returning a sanitized HTTP 500 without leaking SQL syntax, table names, or database credentials.
- `HttpMessageNotReadableException` returns a sanitized HTTP 400 without revealing internal parser classes or stack traces.

**Public Endpoints (No JWT Required):**
- `POST /api/auth/register` (Protected by IP rate limit: 5 RPM)
- `POST /api/auth/login` (Protected by IP rate limit: 10 RPM)
- `GET /api/domains/**` (Public subject domain catalog)
- `GET /api/subjects/**` (Public learning subjects catalog)
- `GET /api/health`, `/actuator/health` (Health telemetry)

**Protected Endpoints (Bearer JWT Required + AI Rate Limiting):**
- `GET /api/profile/me`, `GET /api/profile/{userId}`, `PUT /api/profile/{userId}`
- `POST /api/goals`, `GET /api/goals/user/{userId}`, `GET /api/goals/{goalId}`, etc.
- `GET /api/assessments/{id}`, `POST /api/assessments/{id}/attempts`, `POST /api/attempts/{id}/responses`, `POST /api/attempts/{id}/complete`, `GET /api/attempts/{id}/result`
- `POST /api/attempts/{id}/skill-gaps/analyze`, `GET /api/skill-gaps/user/{userId}/**`
- `POST /api/goals/{id}/roadmap/generate`, `GET /api/goals/{id}/roadmap`, `GET /api/roadmaps/{id}`
- `POST /api/lessons/{id}/start`, `POST /api/lessons/{id}/complete`, `GET /api/progress/user/{userId}`
- `POST /api/ai/recommendations`, `POST /api/ai/lessons/{id}/assist`, `GET /api/ai/next-step` (Protected by AI rate limit: 20 RPM)

**Frontend Token Management:**
- Stored under `localStorage` key `bodha_jwt_token`.
- Centralized `apiFetch` in `frontend/src/services/api.js` attaches `Authorization: Bearer <token>` to all protected calls.
- HTTP 401 status intercepts automatically, clears token, and invokes registered `logout()` callback to restore unauthenticated state.
- Demo access: Controlled via `VITE_ENABLE_DEMO_ACCESS=false` for production.

---

## 2. Required Environment Variables

### Backend

| Variable | Description | Default (dev) | Production Requirement |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `default` | Set to `prod` or `production` |
| `JWT_SECRET` | 256-bit (>=32 char) signing key | Built-in dev fallback | **Mandatory in prod: startup fails if missing/weak** |
| `JWT_EXPIRATION_MS` | Token lifespan in ms | `86400000` (24h) | Set to desired session policy |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/bodha_db` | Production DB connection string |
| `SPRING_DATASOURCE_USERNAME` | DB username | Current OS user (`$USER`) | Production DB role |
| `SPRING_DATASOURCE_PASSWORD` | DB password | *(empty)* | Strong password |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins | `http://localhost:5173` | Production domain (wildcards forbidden) |
| `AI_PROVIDER` | `mock` or `gemini` | `mock` | `gemini` |
| `AI_API_KEY` | Gemini API key | *(empty)* | Production Google AI Studio key |
| `AI_MODEL` | Gemini model name | `gemini-1.5-flash` | Production model selection |
| `AI_ENDPOINT` | Gemini API base URL | Google Generative Language API | Standard endpoint |
| `AI_TIMEOUT_MS` | AI request timeout (ms) | `5000` | Adjust per SLA requirements |

### Frontend (Vite — prefix with `VITE_`)

| Variable | Description | Default (dev) | Production Requirement |
|---|---|---|---|
| `VITE_API_BASE_URL` | Backend REST API base URL | `http://localhost:8080` | `https://api.your-domain.com` |
| `VITE_ENABLE_DEMO_ACCESS` | Show demo login shortcuts | `true` | **Must be set to `false` in production** |
| `VITE_DEMO_EMAIL` | Optional demo login email | `arjun.patel@bodha.ai` | Disabled in production |
| `VITE_DEMO_PASSWORD` | Optional demo login password | *(empty)* | Disabled in production |

---

## 3. Local Development Setup

### Prerequisites
- Java 21+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 16

### Database
```bash
psql -U your_user -c "CREATE DATABASE bodha_db;"
psql -U your_user -d bodha_db -f database/schema.sql
# Optional seed data:
psql -U your_user -d bodha_db -f database/seed_data.sql
```

### Backend
```bash
# Set env vars (example):
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password

cd backend
mvn spring-boot:run
# Starts on http://localhost:8080
# Health check: GET http://localhost:8080/api/health
```

### Frontend
```bash
cd frontend
cp .env.example .env
# Edit .env if needed
npm install
npm run dev
# Starts on http://localhost:5173
```

### Run Tests
```bash
# Backend (70/70 passing)
mvn test -f backend/pom.xml

# Frontend (0 errors)
cd frontend && npm run lint && npm run build
```

---

## 4. Production Readiness Status

### 🟢 COMPLETED IN MODULES M, N, & O

- [x] **BCrypt Password Hashing (Module M)** — Verified 10-round BCrypt; database stores only 60-character `$2a$` hashes. Passwords never appear in plaintext or API responses.
- [x] **Cryptographic JWT Authentication (Module N)** — JJWT HMAC-SHA256 tokens issued on login/registration with minimal identity claims (`sub`, `userId`, `email`, `role`).
- [x] **Server-Side Identity Verification (Module N)** — All domain controllers enforce identity via `CurrentUserService.requireCurrentUserId()` from `SecurityContext`. Client-supplied user IDs cannot forge identity or access cross-user resources.
- [x] **Centralized 401 & Auto-Logout (Module N)** — Frontend `apiFetch` automatically injects Bearer JWT and triggers auto-logout upon 401 token expiration/rejection.
- [x] **Production CORS Configuration (Module O)** — Backend dynamically configures allowed origins via `CORS_ALLOWED_ORIGINS`. Wildcards (`*`) with credentials are explicitly rejected. Preflight OPTIONS requests supported.
- [x] **Production JWT Secret Enforcement (Module O)** — `JwtService` detects `prod` or `production` profiles and aborts startup if the default fallback secret is used. Secret length >= 256 bits enforced.
- [x] **Application-Level Rate Limiting (Module O)** — Thread-safe in-memory token bucket rate limiting on `POST /api/auth/login` (10 RPM), `POST /api/auth/register` (5 RPM), and `/api/ai/**` (20 RPM). Returns HTTP 429 with `Retry-After: 60`.
- [x] **Security Headers (Module O)** — Enabled `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy`.
- [x] **Error Response Sanitization (Module O)** — Catches `DataAccessException` and `HttpMessageNotReadableException`, preventing SQL leakage, stack traces, and internal class names.
- [x] **Demo Access Production Boundary (Module O)** — Documented `VITE_ENABLE_DEMO_ACCESS=false` for production. Demo login never bypasses JWT verification.
- [x] **AI API Key Isolation (Module O)** — Google Gemini API keys are backend-only, read via `AI_API_KEY`, and never exposed in frontend code, git, or responses.
- [x] **Database Safety (Modules M, N, O)** — All 17 tables and existing data retained without reset or reseed.

### 🟡 NOT YET ACTIVE (Targeted for Module P — Deployment Phase)

1. **HTTPS / TLS Termination**  
   Must terminate at the cloud load balancer, reverse proxy, or hosting platform TLS layer (HTTP → HTTPS automatic redirect).
2. **Actual Cloud Deployment**  
   Backend container / JVM deployment to cloud provider (AWS, GCP, Railway, Render, etc.).
3. **Production Domain & DNS**  
   Configure production domain records and supply final URL to `CORS_ALLOWED_ORIGINS` and `VITE_API_BASE_URL`.
4. **Real Production Secrets**  
   Inject cryptographically generated random string for `JWT_SECRET` in production cloud environment.
5. **Real Gemini API Key**  
   Inject live Google AI Studio API key in production cloud environment.

---

## 5. Security Considerations

### What is verified safe right now
- **Stateless cryptographic authentication** — Bearer JWT with HMAC-SHA256 signature verification.
- **Server-side identity binding** — Resource authorization checked via `CurrentUserService` / SecurityContext.
- **Passwords securely hashed** — One-way BCrypt ($2a$ format) with work factor 10.
- **Passwords never returned in API responses** — Excluded from all DTOs and models.
- **No stack traces leaked to clients** — `GlobalExceptionHandler` sanitizes all error responses including database and serialization errors.
- **CORS restricted** — Restricted to configured origin(s); wildcard `*` is forbidden when credentials are enabled.
- **Rate limiting active** — Login, register, and AI endpoints reject burst traffic with HTTP 429.
- **localStorage** — Stores only signed JWT token and non-sensitive learner session profile (no passwords, no hashes).
- **AI prompts** — Never include credentials, password hashes, or secret tokens.
- **Assessment answer key** — `isCorrect` field excluded from pre-submission questions.

### Files that must NEVER be committed
```
.env
frontend/.env
backend/.env
frontend/.env.local
any file containing AI_API_KEY, database passwords, or production JWT secrets
```

---

## 6. Pre-Deployment Verification Checklist

- [x] Passwords hashed with BCrypt in the database (Module M)
- [x] JWT authentication & server-side identity implemented and tested (Module N)
- [x] Centralized frontend Bearer token injection and 401 auto-logout (Module N)
- [x] Dynamic production CORS with wildcard prevention (Module O)
- [x] JWT production secret enforcement in prod profile (Module O)
- [x] In-memory rate limiting on login, registration, and AI endpoints (Module O)
- [x] Security headers: X-Content-Type-Options, Frame-Options, Referrer-Policy, Permissions-Policy (Module O)
- [x] Error responses sanitized against SQL/serialization leakage (Module O)
- [x] Backend tests: 70/70 passing (`ProductionSecurityTests`, `AuthenticationSecurityTests`, `OwnershipAuthorizationTests`)
- [x] Frontend lint: 0 errors
- [x] Frontend build: SUCCESS
- [x] PostgreSQL database intact: 0 dropped tables, 0 resets
- [ ] Production cloud environment configured with `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `AI_API_KEY` (Module P)
- [ ] TLS termination configured (HTTP -> HTTPS) (Module P)

