# BODHA (बोध) — Local Development & Setup Guide

This guide walks through setting up and running BODHA entirely on your local machine with zero external cloud dependencies or Railway credentials.

---

## 1. Prerequisites

Ensure you have the following installed on your system:

| Technology | Minimum Version | Verification Command |
|---|---|---|
| **Java Development Kit (JDK)** | 21 LTS | `java -version` |
| **Apache Maven** | 3.9+ | `mvn -version` |
| **Node.js** | 20+ LTS | `node -v` |
| **npm** | 10+ | `npm -v` |
| **PostgreSQL** | 16+ | `psql --version` |

---

## 2. Step 1: Database Setup

### 2.1 Start PostgreSQL
Ensure your local PostgreSQL server is active:
```bash
# macOS (Homebrew)
brew services start postgresql@16

# Linux (systemd)
sudo systemctl start postgresql
```

### 2.2 Create Database & User
Open the PostgreSQL command line:
```bash
psql -U postgres
```
Run the following SQL commands:
```sql
CREATE DATABASE bodha_db;
-- (Optional) If you want a dedicated user:
-- CREATE USER bodha_user WITH PASSWORD 'bodha_secret';
-- GRANT ALL PRIVILEGES ON DATABASE bodha_db TO bodha_user;
\q
```

### 2.3 Initialize Schema & Curriculum Seed Data
Load the relational schema into `bodha_db`:
```bash
psql -U postgres -d bodha_db -f database/schema.sql
```

Then seed the curriculum skills and baseline diagnostic assessments:
```bash
psql -U postgres -d bodha_db << 'EOF'
BEGIN;

INSERT INTO skills (subject_id, name, category, description) VALUES
('java-backend', 'Core Java OOP', 'core', 'Object-oriented programming, inheritance, polymorphism, and encapsulation in Java'),
('java-backend', 'Spring Core & IoC', 'core', 'Dependency injection, application context, bean lifecycles, and Spring architecture'),
('java-backend', 'REST API Design', 'core', 'RESTful principles, HTTP methods, status codes, and API contract design'),
('java-backend', 'JPA & Database Performance', 'core', 'ORM mappings, EntityManager, Hibernate query optimization, and avoiding N+1 queries'),
('java-backend', 'Security & Authentication', 'advanced', 'Stateless security, JWT tokens, BCrypt hashing, and authorization filters')
ON CONFLICT DO NOTHING;

INSERT INTO assessments (subject_id, title, assessment_type, description) VALUES
('java-backend', 'Full-Stack Java Baseline Diagnostic', 'DIAGNOSTIC', 'Initial diagnostic evaluation to measure knowledge baseline and detect exact skill gaps across Java and Spring Boot.')
ON CONFLICT DO NOTHING;

INSERT INTO assessment_questions (assessment_id, skill_id, question_text, explanation, order_index)
SELECT a.id, s.id, 
  'In Java OOP, which concept allows a subclass to provide a specific implementation of a method declared in its superclass?',
  'Method overriding occurs when a subclass defines a method with the exact same signature and return type as in its parent class.',
  1
FROM assessments a, skills s 
WHERE a.subject_id = 'java-backend' AND a.assessment_type = 'DIAGNOSTIC' AND s.subject_id = 'java-backend' AND s.name = 'Core Java OOP'
AND NOT EXISTS (SELECT 1 FROM assessment_questions WHERE assessment_id = a.id AND order_index = 1);

INSERT INTO assessment_options (question_id, option_text, is_correct, order_index)
SELECT q.id, opt.text, opt.is_correct, opt.idx
FROM assessment_questions q,
(VALUES 
  ('Method Overloading', false, 1),
  ('Method Overriding (@Override)', true, 2),
  ('Data Encapsulation', false, 3),
  ('Object Serialization', false, 4)
) AS opt(text, is_correct, idx)
WHERE q.order_index = 1
AND NOT EXISTS (SELECT 1 FROM assessment_options WHERE question_id = q.id);

INSERT INTO assessment_questions (assessment_id, skill_id, question_text, explanation, order_index)
SELECT a.id, s.id,
  'In the Spring Framework, what does Inversion of Control (IoC) primarily achieve?',
  'IoC transfers the responsibility of managing object lifecycles and dependencies to the framework container.',
  2
FROM assessments a, skills s 
WHERE a.subject_id = 'java-backend' AND a.assessment_type = 'DIAGNOSTIC' AND s.subject_id = 'java-backend' AND s.name = 'Spring Core & IoC'
AND NOT EXISTS (SELECT 1 FROM assessment_questions WHERE assessment_id = a.id AND order_index = 2);

INSERT INTO assessment_options (question_id, option_text, is_correct, order_index)
SELECT q.id, opt.text, opt.is_correct, opt.idx
FROM assessment_questions q,
(VALUES 
  ('It speeds up database query execution', false, 1),
  ('It delegates object creation and dependency injection to the Spring IoC container', true, 2),
  ('It converts Java bytecode into native machine instructions', false, 3),
  ('It automatically encrypts network communications', false, 4)
) AS opt(text, is_correct, idx)
WHERE q.order_index = 2
AND NOT EXISTS (SELECT 1 FROM assessment_options WHERE question_id = q.id);

INSERT INTO assessment_questions (assessment_id, skill_id, question_text, explanation, order_index)
SELECT a.id, s.id,
  'Which HTTP method should be used for idempotent updates where the entire representation of a resource is replaced?',
  'PUT is idempotent and replaces the entire target resource; PATCH is typically used for partial modifications.',
  3
FROM assessments a, skills s 
WHERE a.subject_id = 'java-backend' AND a.assessment_type = 'DIAGNOSTIC' AND s.subject_id = 'java-backend' AND s.name = 'REST API Design'
AND NOT EXISTS (SELECT 1 FROM assessment_questions WHERE assessment_id = a.id AND order_index = 3);

INSERT INTO assessment_options (question_id, option_text, is_correct, order_index)
SELECT q.id, opt.text, opt.is_correct, opt.idx
FROM assessment_questions q,
(VALUES 
  ('POST', false, 1),
  ('GET', false, 2),
  ('PUT', true, 3),
  ('PATCH', false, 4)
) AS opt(text, is_correct, idx)
WHERE q.order_index = 3
AND NOT EXISTS (SELECT 1 FROM assessment_options WHERE question_id = q.id);

INSERT INTO assessment_questions (assessment_id, skill_id, question_text, explanation, order_index)
SELECT a.id, s.id,
  'What is the "N+1 query problem" in JPA / Hibernate ORM?',
  'The N+1 problem happens when lazy loading fetches a parent list with 1 query, then fires N additional queries for each child record.',
  4
FROM assessments a, skills s 
WHERE a.subject_id = 'java-backend' AND a.assessment_type = 'DIAGNOSTIC' AND s.subject_id = 'java-backend' AND s.name = 'JPA & Database Performance'
AND NOT EXISTS (SELECT 1 FROM assessment_questions WHERE assessment_id = a.id AND order_index = 4);

INSERT INTO assessment_options (question_id, option_text, is_correct, order_index)
SELECT q.id, opt.text, opt.is_correct, opt.idx
FROM assessment_questions q,
(VALUES 
  ('Running N queries inside a single database transaction', false, 1),
  ('Executing 1 initial query to fetch N entities, followed by N separate queries to fetch related child entities', true, 2),
  ('A constraint violation when inserting N+1 rows with duplicate primary keys', false, 3),
  ('Having N+1 threads attempting to write to the same table simultaneously', false, 4)
) AS opt(text, is_correct, idx)
WHERE q.order_index = 4
AND NOT EXISTS (SELECT 1 FROM assessment_options WHERE question_id = q.id);

INSERT INTO assessment_questions (assessment_id, skill_id, question_text, explanation, order_index)
SELECT a.id, s.id,
  'Why are JSON Web Tokens (JWT) commonly favored in scalable microservices architectures?',
  'JWTs carry verifiable claims signed cryptographically, allowing stateless verification across distributed services.',
  5
FROM assessments a, skills s 
WHERE a.subject_id = 'java-backend' AND a.assessment_type = 'DIAGNOSTIC' AND s.subject_id = 'java-backend' AND s.name = 'Security & Authentication'
AND NOT EXISTS (SELECT 1 FROM assessment_questions WHERE assessment_id = a.id AND order_index = 5);

INSERT INTO assessment_options (question_id, option_text, is_correct, order_index)
SELECT q.id, opt.text, opt.is_correct, opt.idx
FROM assessment_questions q,
(VALUES 
  ('They eliminate the need for HTTPS', false, 1),
  ('They are stateless and self-contained, allowing services to verify authentication without querying a shared session store', true, 2),
  ('They automatically encrypt the entire request payload', false, 3),
  ('They prevent all Cross-Site Scripting (XSS) vulnerabilities', false, 4)
) AS opt(text, is_correct, idx)
WHERE q.order_index = 5
AND NOT EXISTS (SELECT 1 FROM assessment_options WHERE question_id = q.id);

COMMIT;
EOF
```

Verify that 17 tables are created:
```bash
psql -U postgres -d bodha_db -c "\dt"
```

---

## 3. Step 2: Backend Setup (Spring Boot)

### 3.1 Configure Environment Variables
You can run with default values directly or configure custom variables in `backend/.env`:
```bash
cd backend
cp .env.example .env
```
Default settings in `application.yml` automatically connect to:
- Database: `jdbc:postgresql://localhost:5432/bodha_db`
- Username: `$USER` (or `postgres`)
- Password: *(empty)*
- AI Provider: `mock` (no API key required)

### 3.2 Run Backend Service
```bash
cd backend
mvn spring-boot:run
```
The backend starts on port **8080**.

### 3.3 Verify Health Check
In another terminal:
```bash
curl http://localhost:8080/api/health
# Output: {"status":"UP","service":"BODHA API","version":"1.0.0",...}
```

---

## 4. Step 3: Frontend Setup (React / Vite)

### 4.1 Install Dependencies
```bash
cd frontend
npm install
```

### 4.2 Configure Environment
```bash
cd frontend
cp .env.example .env
```
Ensure `frontend/.env` contains:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_ENABLE_DEMO_ACCESS=true
```

### 4.3 Start Development Server
```bash
npm run dev
```
The frontend is accessible at **`http://localhost:5173`**.

---

## 5. Step 4: Run Test Suites

Verify everything is functioning properly:

### 5.1 Backend Test Suite (70 Tests)
```bash
cd backend
mvn test
```
Expected output:
```
[INFO] Tests run: 70, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 5.2 Frontend Lint & Production Build
```bash
cd frontend
npm run lint
npm run build
```
Expected output:
```
Found 0 errors.
✓ built in ~200ms
```

---

## 6. Troubleshooting Common Issues

### Issue 1: PostgreSQL Connection Refused (`port 5432`)
- Ensure PostgreSQL is running (`brew services list` or `sudo systemctl status postgresql`).
- Check database exists: `psql -l`.

### Issue 2: CORS Error in Browser Console
- Check `backend/src/main/resources/application.yml` for `CORS_ALLOWED_ORIGINS`.
- Ensure it includes `http://localhost:5173`.

### Issue 3: JWT Secret Rejection in Production Profile
- If testing with `-Dspring.profiles.active=prod`, set a strong `>=256-bit` secret:
  `export JWT_SECRET=c2VjdXJlLXJhbmRvbS1wcm9kdWN0aW9uLXNlY3JldC1rZXktMjU2LWJpdHM=`
