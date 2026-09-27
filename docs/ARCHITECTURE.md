# BODHA (बोध) — System Architecture

## 1. System Vision & Architecture Philosophy

**BODHA** is an AI-powered personalized learning and skill-development platform designed to support any domain—from Software Engineering and Advanced Mathematics to Linguistics and Business Leadership.

Unlike generic LLM wrappers or conversational chatbots that provide ephemeral, unstructured text answers, BODHA is a **stateful, pedagogical learning system**:
- It maintains persistent learner state across multi-week curricula.
- It calibrates knowledge baselines using diagnostic tests.
- It isolates granular skill gaps at the concept level.
- It dynamically generates sequenced, milestone-based roadmaps tailored to each learner's time commitment and goal.
- It grounds AI recommendations in verified telemetry (score percentages, active gaps, completed lessons).

---

## 2. High-Level System Architecture

The following diagram illustrates the complete end-to-end architecture across client, API gateway/controllers, service domain layers, database, and AI providers:

```mermaid
graph TD
    Client[React 19 + Vite SPA]
    
    subgraph Edge & Security
        Caddy[Caddy Reverse Proxy / Railway Edge]
        Cors[CorsFilter - Strict Origin Match]
        RateLimit[RateLimitingFilter - Token Bucket]
        JwtAuth[JwtAuthenticationFilter - HMAC-SHA256]
        SecContext[SecurityContextHolder / CurrentUserService]
    end

    subgraph Spring Boot 3.4.3 Backend Services
        AuthController[AuthController]
        GoalController[LearnerGoalController]
        AssessController[AssessmentController]
        SkillGapController[SkillGapController]
        RoadmapController[RoadmapController]
        ProgressController[ProgressController]
        AiController[AiPersonalizationController]

        AuthService[AuthService + BCrypt]
        GoalService[LearnerGoalService]
        AssessService[AssessmentService]
        SkillGapService[SkillGapService]
        RoadmapService[RoadmapService]
        ProgressService[ProgressService]
        AiService[AiPersonalizationService]
    end

    subgraph AI Engine
        AiContext[LearnerAiContextBuilder]
        AiPrompt[AiPromptBuilder]
        AiProviderInterface[AiProvider Interface]
        MockProvider[MockAiProvider - Deterministic]
        GeminiProvider[GeminiAiProvider - Google Gemini 1.5]
        AiFallback[DeterministicAiFallback - Resilient]
    end

    subgraph Data Tier
        Hikari[HikariCP Connection Pool]
        Postgres[(PostgreSQL 16 - 17 Relational Tables)]
    end

    Client -->|HTTPS + Bearer JWT| Caddy
    Caddy --> Cors
    Cors --> RateLimit
    RateLimit --> JwtAuth
    JwtAuth --> SecContext

    SecContext --> AuthController
    SecContext --> GoalController
    SecContext --> AssessController
    SecContext --> SkillGapController
    SecContext --> RoadmapController
    SecContext --> ProgressController
    SecContext --> AiController

    AuthController --> AuthService
    GoalController --> GoalService
    AssessController --> AssessService
    SkillGapController --> SkillGapService
    RoadmapController --> RoadmapService
    ProgressController --> ProgressService
    AiController --> AiService

    AiService --> AiContext
    AiService --> AiPrompt
    AiService --> AiProviderInterface
    AiProviderInterface --> MockProvider
    AiProviderInterface --> GeminiProvider
    GeminiProvider -.->|Fallback on error/timeout| AiFallback

    AuthService & GoalService & AssessService & SkillGapService & RoadmapService & ProgressService & AiContext --> Hikari
    Hikari --> Postgres
```

---

## 3. Technology Stack & Component Details

### 3.1 Frontend Architecture
- **Framework:** React 19 SPA (Single Page Application)
- **Build Tool:** Vite 8.3
- **Styling:** Vanilla CSS design tokens + Tailwind CSS 3.4
- **Icons:** Lucide React
- **Routing:** React Router DOM 7
- **State Management:** React Context API (`LearnerContext.jsx`)
  - Centralizes authentication state, active goal, current roadmap, lesson progress, and answer key state.
  - Automatically loads telemetry upon login.
  - Manages session persistence in `localStorage` (`bodha_jwt_token`, `bodha_user_profile`).
- **HTTP Client:** Centralized `apiFetch` abstraction (`frontend/src/services/api.js`)
  - Automatically attaches `Authorization: Bearer <token>` to all protected requests.
  - Intercepts HTTP 401 Unauthorized responses and executes automated auto-logout.
  - Formats error payloads cleanly for UI display.
- **Production Web Server:** Caddy HTTP/2 reverse proxy on Railway providing SPA rewrite routing (`try_files {path} /index.html`).

### 3.2 Backend Architecture
- **Framework:** Spring Boot 3.4.3
- **Runtime:** Java 21 LTS
- **Build System:** Apache Maven 3.9+
- **Security & Identity:**
  - Spring Security 6.4 with stateless session management (`SessionCreationPolicy.STATELESS`).
  - JJWT 0.12.6 for cryptographically signed HMAC-SHA256 tokens.
  - BCrypt password encoder (10 rounds).
  - `CurrentUserService`: Extracts authenticated user ID directly from `SecurityContextHolder`, preventing client-side ID spoofing or cross-tenant parameter tampering.
- **Data Access:** Spring Data JPA + Hibernate 6.6 with explicit `PostgreSQLDialect`.
- **Connection Pool:** HikariCP with connection validation and optimized pool sizing (5 connections in production for minimal memory footprint).
- **Rate Limiting:** In-memory token bucket rate limiter (`InMemoryRateLimiter`) with automatic 15-minute idle bucket eviction:
  - `POST /api/auth/login`: 10 requests / minute (IP-based).
  - `POST /api/auth/register`: 5 requests / minute (IP-based).
  - `/api/ai/**`: 20 requests / minute (User ID or IP-based).
- **Error Handling:** `GlobalExceptionHandler` with `@RestControllerAdvice`:
  - Sanitizes SQL and database exceptions (`DataAccessException`, `SQLException`), returning HTTP 500 without leaking schema or credentials.
  - Handles bean validation errors (`MethodArgumentNotValidException`), returning structured field-level 400 responses.
  - Handles rate limiting violations, returning HTTP 429 with `Retry-After: 60`.

### 3.3 Database Tier
- **Database Engine:** PostgreSQL 16
- **Architecture:** 17 relational tables with strict referential integrity (`ON DELETE CASCADE` / `ON DELETE RESTRICT`), foreign key indexes, and enum check constraints.
- **Data Zero-Reset Policy:** All migrations and schema structures are designed to be backward compatible and non-destructive.

---

## 4. End-to-End Learner Journey (Data Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Learner
    participant UI as Frontend (React 19)
    participant API as Spring Boot API
    participant DB as PostgreSQL
    participant AI as AI Engine

    Learner->>UI: 1. Register / Login
    UI->>API: POST /api/auth/login
    API->>DB: Verify BCrypt Hash
    API-->>UI: Return JWT Token (HMAC-SHA256)

    Learner->>UI: 2. Browse Catalog & Set Goal
    UI->>API: POST /api/goals (subjectId, dailyMinutes)
    API->>DB: Insert learner_goals (bound to JWT identity)
    API-->>UI: Goal Created

    Learner->>UI: 3. Take Diagnostic Assessment
    UI->>API: POST /api/assessments/{id}/attempts
    API->>DB: Load Questions (isCorrect concealed)
    API-->>UI: Deliver Questions

    Learner->>UI: 4. Submit Answers
    UI->>API: POST /api/attempts/{id}/complete
    API->>DB: Evaluate Server-Side & Score
    API-->>UI: Score % & Breakdown

    UI->>API: 5. Trigger Skill-Gap Analysis
    API->>DB: Compute Gap Status (MASTERED / NEEDS_REVIEW / CRITICAL_GAP)
    API-->>UI: Skill-Gap Matrix

    UI->>API: 6. Synthesize Personalized Roadmap
    API->>DB: Generate roadmap_modules & lessons prioritized by skill gaps
    API-->>UI: Personalized Roadmap

    Learner->>UI: 7. Study Lesson & Mark Complete
    UI->>API: POST /api/lessons/{id}/complete
    API->>DB: Record lesson_progress & update XP/Streak
    API-->>UI: Progress Updated

    Learner->>UI: 8. Request Next Step / AI Tutor
    UI->>API: GET /api/ai/next-step?goalId={id}
    API->>AI: Build Learner Context (Gaps + Roadmap State)
    AI-->>API: Synthesize Contextual Advice
    API-->>UI: Structured Next Step Action
```

---

## 5. Security & Isolation Architecture

1. **Stateless Authentication:** Bearer JWT tokens signed with HMAC-SHA256. Passwords, hashes, and secrets are strictly excluded from token claims and serialized payloads.
2. **Server-Side Identity:** Endpoints never trust client-supplied `userId` query parameters or JSON fields. Identity is derived directly from the verified token principal via `CurrentUserService.requireCurrentUserId()`.
3. **Cross-Tenant Isolation:** Resource ownership checks verify that assessment attempts, goals, skill gaps, roadmaps, and progress records belong to the calling user before executing mutations or reads.
4. **Defense in Depth:**
   - Preflight CORS checks with explicit origin whitelisting (`CORS_ALLOWED_ORIGINS`).
   - Rate limiting on vulnerable authentication and AI endpoints.
   - Standard security headers (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`, `Permissions-Policy`).
   - SQL exception sanitization preventing database metadata exposure.
