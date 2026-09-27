# BODHA (बोध) — REST API Documentation

This document provides a comprehensive reference for all REST API endpoints provided by the BODHA Spring Boot backend.

---

## 1. Global API Standards

- **Base URL (Production):** `https://bodha-backend-production.up.railway.app`
- **Base URL (Local Development):** `http://localhost:8080`
- **Content-Type:** `application/json`
- **Authentication Scheme:** `Authorization: Bearer <JWT>`
- **Identity Enforcement:** For all protected endpoints, the calling user's identity is derived server-side from the verified JWT token (`SecurityContextHolder`). Client-supplied `userId` parameters cannot override authenticated identity.

### Common HTTP Status Codes
- `200 OK`: Request succeeded.
- `201 Created`: Resource successfully created.
- `400 Bad Request`: Input validation failed or malformed request.
- `401 Unauthorized`: Token missing, invalid, or expired.
- `403 Forbidden`: Resource belongs to another user (cross-tenant access denied).
- `404 Not Found`: Target entity does not exist.
- `429 Too Many Requests`: Rate limit threshold exceeded. Includes `Retry-After: 60`.
- `500 Internal Server Error`: Sanitized server error.

---

## 2. Authentication & Identity Endpoints (`/api/auth`)

### 2.1 Register New Account
- **Endpoint:** `POST /api/auth/register`
- **Rate Limit:** 5 requests / minute per client IP.
- **Request Headers:** `Content-Type: application/json`
- **Request Body:**
  ```json
  {
    "email": "learner@example.com",
    "password": "StrongPassword123!",
    "fullName": "Jane Doe"
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "id": 1,
    "email": "learner@example.com",
    "fullName": "Jane Doe",
    "role": "LEARNER",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "message": "User registered successfully"
  }
  ```

### 2.2 User Login
- **Endpoint:** `POST /api/auth/login`
- **Rate Limit:** 10 requests / minute per client IP.
- **Request Body:**
  ```json
  {
    "email": "learner@example.com",
    "password": "StrongPassword123!"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "email": "learner@example.com",
    "fullName": "Jane Doe",
    "role": "LEARNER",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "message": "Authentication successful"
  }
  ```

---

## 3. Knowledge Catalog Endpoints (`/api`)

Public catalog endpoints (no authentication required).

### 3.1 List Knowledge Domains
- **Endpoint:** `GET /api/domains`
- **Response (200 OK):**
  ```json
  [
    {
      "id": "programming",
      "name": "Software & Engineering",
      "description": "Software systems, backend architectures, web, and cloud"
    },
    {
      "id": "mathematics",
      "name": "Mathematics & Data",
      "description": "Linear algebra, multivariable calculus, statistics, and discrete math"
    }
  ]
  ```

### 3.2 List All Subjects
- **Endpoint:** `GET /api/subjects`
- **Query Parameters:** `domainId` (optional filter, e.g., `?domainId=programming`)
- **Response (200 OK):**
  ```json
  [
    {
      "id": "java-backend",
      "domainId": "programming",
      "title": "Full-Stack Java & Spring Boot Architecture",
      "tagline": "Modern backend engineering, REST services, JPA, and Spring Security",
      "difficultyLevel": "Intermediate",
      "estimatedWeeks": 8,
      "isPopular": true,
      "skillsCovered": ["Core Java OOP", "Spring Core & IoC", "REST API Design"]
    }
  ]
  ```

### 3.3 List Popular Subjects
- **Endpoint:** `GET /api/subjects/popular`
- **Response (200 OK):** Array of curated popular subjects.

---

## 4. Learner Goals Endpoints (`/api/goals`)

### 4.1 Create Learner Goal
- **Endpoint:** `POST /api/goals`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "subjectId": "java-backend",
    "goalType": "career",
    "baselineLevel": "beginner",
    "dailyTimeMinutes": 30
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "id": 10,
    "userId": 1,
    "subjectId": "java-backend",
    "subjectTitle": "Full-Stack Java & Spring Boot Architecture",
    "goalType": "career",
    "baselineLevel": "beginner",
    "dailyTimeMinutes": 30,
    "status": "ACTIVE",
    "createdAt": "2026-09-27T13:12:00Z"
  }
  ```

### 4.2 Get Active Goal for Authenticated User
- **Endpoint:** `GET /api/goals/user/{userId}/active`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** Active `LearnerGoalResponseDto`.

### 4.3 Get Goal by ID
- **Endpoint:** `GET /api/goals/{goalId}`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** Goal details (enforces user ownership).

---

## 5. Diagnostic Assessment Engine (`/api`)

### 5.1 Get Diagnostic Assessment for Subject
- **Endpoint:** `GET /api/assessments/subject/{subjectId}/diagnostic`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "subjectId": "java-backend",
    "title": "Full-Stack Java Baseline Diagnostic",
    "assessmentType": "DIAGNOSTIC",
    "description": "Initial diagnostic evaluation to measure knowledge baseline and detect exact skill gaps.",
    "totalQuestions": 5
  }
  ```

### 5.2 Start Assessment Attempt
- **Endpoint:** `POST /api/assessments/{assessmentId}/attempts`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "learnerGoalId": 10
  }
  ```
- **Response (201 Created):**
  ```json
  {
    "id": 25,
    "assessmentId": 1,
    "learnerGoalId": 10,
    "status": "IN_PROGRESS",
    "questions": [
      {
        "id": 101,
        "questionText": "In Java OOP, which concept allows a subclass to provide a specific implementation...",
        "skillId": 1,
        "skillName": "Core Java OOP",
        "orderIndex": 1,
        "options": [
          { "id": 1001, "optionText": "Method Overloading", "orderIndex": 1 },
          { "id": 1002, "optionText": "Method Overriding (@Override)", "orderIndex": 2 }
        ]
      }
    ]
  }
  ```
  *(Note: `isCorrect` and explanations are concealed during the active attempt)*

### 5.3 Submit Answer to Question
- **Endpoint:** `POST /api/attempts/{attemptId}/responses`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "questionId": 101,
    "selectedOptionId": 1002
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "responseId": 301,
    "attemptId": 25,
    "questionId": 101,
    "recorded": true
  }
  ```

### 5.4 Complete & Evaluate Assessment
- **Endpoint:** `POST /api/attempts/{attemptId}/complete`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "attemptId": 25,
    "scorePercentage": 80.0,
    "totalQuestions": 5,
    "correctAnswers": 4,
    "completedAt": "2026-09-27T13:15:00Z"
  }
  ```

---

## 6. Skill-Gap Engine (`/api`)

### 6.1 Analyze Completed Attempt
- **Endpoint:** `POST /api/attempts/{attemptId}/skill-gaps/analyze`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "learnerGoalId": 10,
    "totalSkillsEvaluated": 5,
    "skillGaps": [
      {
        "skillId": 1,
        "skillName": "Core Java OOP",
        "category": "core",
        "status": "MASTERED",
        "proficiencyScore": 100.0
      },
      {
        "skillId": 4,
        "skillName": "JPA & Database Performance",
        "category": "core",
        "status": "NEEDS_REVIEW",
        "proficiencyScore": 0.0
      }
    ]
  }
  ```

### 6.2 Get Learner Skill Gaps
- **Endpoint:** `GET /api/skill-gaps/user/{userId}`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** Array of all persistent skill gaps for the authenticated user.

---

## 7. Personalized Roadmap Engine (`/api`)

### 7.1 Generate Personalized Roadmap
- **Endpoint:** `POST /api/goals/{goalId}/roadmap/generate`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "id": 12,
    "learnerGoalId": 10,
    "title": "Full-Stack Java & Spring Boot Architecture Personalized Roadmap",
    "totalEstimatedHours": 32,
    "modules": [
      {
        "id": 51,
        "title": "Module 1: Remedial Foundations & Core Concepts",
        "orderIndex": 1,
        "lessons": [
          {
            "id": 201,
            "title": "Deep Dive into JPA N+1 Query Resolution",
            "lessonType": "PRACTICE",
            "estimatedMinutes": 30,
            "orderIndex": 1
          }
        ]
      }
    ]
  }
  ```

### 7.2 Get Roadmap by Goal ID
- **Endpoint:** `GET /api/goals/{goalId}/roadmap`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** Active roadmap for the specified goal.

---

## 8. Lesson Progress & Study Tracking (`/api`)

### 8.1 Start Lesson
- **Endpoint:** `POST /api/lessons/{lessonId}/start`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** `LessonProgressResponseDto` (`status: "IN_PROGRESS"`).

### 8.2 Mark Lesson Completed
- **Endpoint:** `POST /api/lessons/{lessonId}/complete`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "id": 401,
    "userId": 1,
    "lessonId": 201,
    "lessonTitle": "Deep Dive into JPA N+1 Query Resolution",
    "status": "COMPLETED",
    "isCompleted": true,
    "completedAt": "2026-09-27T13:20:00Z"
  }
  ```

### 8.3 Get User Progress Summary (Dashboard)
- **Endpoint:** `GET /api/progress/user/{userId}`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "userId": 1,
    "currentStreakDays": 1,
    "totalXp": 50,
    "totalCompletedLessons": 1,
    "lastActiveAt": "2026-09-27T13:20:00Z"
  }
  ```

---

## 9. AI Personalization Engine (`/api/ai`)

Rate limited at 20 requests / minute per user.

### 9.1 Immediate Next Step Recommendation
- **Endpoint:** `GET /api/ai/next-step?goalId={goalId}`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "title": "Next Step: Spring Beans & ApplicationContext",
    "summary": "You have completed your diagnostic assessment. Let's tackle your first core lesson.",
    "currentModuleTitle": "Module 1: Foundations",
    "nextLessonId": 202,
    "nextLessonTitle": "Spring Beans & ApplicationContext",
    "suggestedAction": "Start lesson: Spring Beans & ApplicationContext",
    "reason": "Prioritizes your active roadmap milestone while addressing unmastered competencies.",
    "overallProgressPercentage": 6,
    "remainingLessonsInModule": 2,
    "fallbackUsed": false,
    "provider": "mock"
  }
  ```

### 9.2 Personalized Remediation Recommendation
- **Endpoint:** `GET /api/ai/recommendations?goalId={goalId}`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "title": "Priority Focus: JPA & Database Performance",
    "summary": "Diagnostic telemetry identified a gap in JPA query efficiency.",
    "reason": "Mastering the N+1 problem is essential before advancing to Spring Security.",
    "priority": "HIGH",
    "relatedSkill": "JPA & Database Performance",
    "relatedLesson": "Deep Dive into JPA N+1 Query Resolution",
    "suggestedAction": "Complete the practice lab on EntityManager batch fetching",
    "learningApproach": "hands-on",
    "practiceSuggestions": [
      "Experiment with JOIN FETCH in JPQL",
      "Enable Hibernate SQL logging with show-sql: true"
    ],
    "fallbackUsed": false,
    "provider": "mock"
  }
  ```

### 9.3 Lesson Socratic Assistant
- **Endpoint:** `POST /api/ai/lessons/{lessonId}/assist`
- **Headers:** `Authorization: Bearer <token>`
- **Response (200 OK):** Context-grounded tutoring assistance tailored to the lesson.

---

## 10. System Health Endpoint

### 10.1 Service Health Check
- **Endpoint:** `GET /api/health`
- **Authentication:** None (Public)
- **Response (200 OK):**
  ```json
  {
    "status": "UP",
    "service": "BODHA API",
    "version": "1.0.0",
    "timestamp": "2026-09-27T13:12:25.370432085Z"
  }
  ```
