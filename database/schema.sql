-- =============================================================================
-- BODHA (बोध) — PostgreSQL Relational Schema
-- AI-Powered Personalized Learning & Skill-Development Platform
-- Architecture: PostgreSQL 14+ compatible
-- =============================================================================

-- Enable UUID extension if UUIDs are needed in the future
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================================================
-- MODULE A: IDENTITY & LEARNER PROFILE
-- =============================================================================

-- Table: users
-- Core authentication credentials and base user record.
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'LEARNER' CHECK (role IN ('LEARNER', 'MENTOR', 'ADMIN')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: learner_profiles
-- Persistent profile tracking overall learner metrics, XP, and streak.
CREATE TABLE learner_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bio TEXT,
    avatar_url VARCHAR(500),
    current_streak_days INTEGER NOT NULL DEFAULT 0 CHECK (current_streak_days >= 0),
    total_xp INTEGER NOT NULL DEFAULT 0 CHECK (total_xp >= 0),
    last_active_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- MODULE B: DOMAIN & SKILL TAXONOMY
-- Universal taxonomy for ANY discipline (Software, Math, Languages, Business)
-- =============================================================================

-- Table: domains
-- High-level subject categories.
CREATE TABLE domains (
    id VARCHAR(50) PRIMARY KEY, -- e.g. 'programming', 'mathematics', 'languages', 'business'
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: subjects
-- Specific curriculum tracks. Supports pre-seeded and user-created custom topics.
CREATE TABLE subjects (
    id VARCHAR(80) PRIMARY KEY, -- e.g. 'java-backend', 'linear-algebra', 'spanish'
    domain_id VARCHAR(50) NOT NULL REFERENCES domains(id) ON DELETE RESTRICT,
    title VARCHAR(200) NOT NULL,
    tagline VARCHAR(300),
    difficulty_level VARCHAR(50) NOT NULL CHECK (difficulty_level IN ('Beginner', 'Intermediate', 'Advanced', 'All Levels')),
    estimated_weeks INTEGER NOT NULL DEFAULT 6 CHECK (estimated_weeks > 0),
    is_popular BOOLEAN NOT NULL DEFAULT false,
    is_custom BOOLEAN NOT NULL DEFAULT false,
    created_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: skills
-- Granular competencies within a subject, used to measure exact skill gaps.
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    subject_id VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'core' CHECK (category IN ('prerequisite', 'core', 'advanced')),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- MODULE C: LEARNER GOALS & ENROLLMENTS
-- Multi-subject support: one user can have goals in multiple disciplines
-- =============================================================================

-- Table: learner_goals
-- Calibrates the learner's outcome target, baseline level, and daily commitment.
CREATE TABLE learner_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    goal_type VARCHAR(50) NOT NULL CHECK (goal_type IN ('career', 'exam', 'project', 'mastery')),
    baseline_level VARCHAR(50) NOT NULL CHECK (baseline_level IN ('beginner', 'intermediate', 'advanced')),
    daily_time_minutes INTEGER NOT NULL CHECK (daily_time_minutes IN (15, 30, 45, 60)),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_subject_goal UNIQUE (user_id, subject_id)
);

-- =============================================================================
-- MODULE D: DIAGNOSTIC ASSESSMENTS & ATTEMPTS
-- =============================================================================

-- Table: assessments
-- Tests associated with a subject (Diagnostic, Module Quizzes, Capstone).
CREATE TABLE assessments (
    id BIGSERIAL PRIMARY KEY,
    subject_id VARCHAR(80) NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    assessment_type VARCHAR(50) NOT NULL CHECK (assessment_type IN ('DIAGNOSTIC', 'MODULE_QUIZ', 'CHECKPOINT', 'FINAL_CAPSTONE')),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: assessment_questions
-- Questions within an assessment, directly tagged to a specific skill.
CREATE TABLE assessment_questions (
    id BIGSERIAL PRIMARY KEY,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    skill_id BIGINT REFERENCES skills(id) ON DELETE SET NULL, -- Tags question to granular skill gap
    question_text TEXT NOT NULL,
    explanation TEXT,
    order_index INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: assessment_options
-- Multiple-choice answer options for each question.
CREATE TABLE assessment_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false,
    order_index INTEGER NOT NULL DEFAULT 1
);

-- Table: assessment_attempts
-- Historical submission records for diagnostic or practice tests.
CREATE TABLE assessment_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assessment_id BIGINT NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    learner_goal_id BIGINT REFERENCES learner_goals(id) ON DELETE CASCADE,
    score_percentage NUMERIC(5,2) NOT NULL CHECK (score_percentage >= 0 AND score_percentage <= 100),
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: assessment_responses
-- Item-level responses recorded during an assessment attempt.
CREATE TABLE assessment_responses (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES assessment_questions(id) ON DELETE CASCADE,
    selected_option_id BIGINT NOT NULL REFERENCES assessment_options(id) ON DELETE CASCADE,
    is_correct BOOLEAN NOT NULL
);

-- =============================================================================
-- MODULE E: SKILL-GAP ANALYSIS
-- Core differentiator: persistent matrix of mastered vs gap competencies
-- =============================================================================

-- Table: learner_skill_gaps
-- Records diagnostic evaluation outcomes per skill for a given learner goal.
CREATE TABLE learner_skill_gaps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    learner_goal_id BIGINT NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE,
    skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('MASTERED', 'GAP')),
    gap_severity VARCHAR(30) CHECK (gap_severity IN ('HIGH', 'MEDIUM', 'LOW') OR gap_severity IS NULL),
    reason TEXT, -- Explanation of why this topic is prioritized or skipped
    evaluated_from_attempt_id BIGINT REFERENCES assessment_attempts(id) ON DELETE SET NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_goal_skill UNIQUE (user_id, learner_goal_id, skill_id)
);

-- =============================================================================
-- MODULE F: PERSONALIZED ROADMAPS & LEARNING PROGRESS
-- =============================================================================

-- Table: roadmaps
-- The dynamic learning path generated from goal setting and skill gaps.
CREATE TABLE roadmaps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    learner_goal_id BIGINT UNIQUE NOT NULL REFERENCES learner_goals(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    curated_recommendation TEXT, -- Diagnostic summary and pedagogical advice
    overall_progress_percentage INTEGER NOT NULL DEFAULT 0 CHECK (overall_progress_percentage >= 0 AND overall_progress_percentage <= 100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    ai_generated BOOLEAN NOT NULL DEFAULT false, -- Ready for LLM generator integration
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: roadmap_modules
-- Sequenced milestones within a personalized roadmap.
CREATE TABLE roadmap_modules (
    id BIGSERIAL PRIMARY KEY,
    roadmap_id BIGINT NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_label VARCHAR(50) NOT NULL, -- e.g. 'Week 1-2'
    order_index INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'UNLOCKED' CHECK (status IN ('UNLOCKED', 'LOCKED', 'COMPLETED')),
    is_current BOOLEAN NOT NULL DEFAULT false,
    prerequisite_summary VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: roadmap_lessons
-- Bite-sized learning units and exercises within each module.
CREATE TABLE roadmap_lessons (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES roadmap_modules(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    lesson_type VARCHAR(50) NOT NULL CHECK (lesson_type IN ('Theory + Code', 'Hands-on Lab', 'Best Practice', 'Quiz', 'Exercise', 'Capstone')),
    content_body TEXT, -- Bite-sized conceptual summary or code example
    order_index INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table: lesson_progress
-- Tracks individual lesson completion per user.
CREATE TABLE lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES roadmap_lessons(id) ON DELETE CASCADE,
    is_completed BOOLEAN NOT NULL DEFAULT true,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id)
);

-- Table: user_activity_logs
-- Daily activity logs powering consistency streaks and study metrics on Dashboard.
CREATE TABLE user_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    activity_date DATE NOT NULL,
    minutes_spent INTEGER NOT NULL DEFAULT 0 CHECK (minutes_spent >= 0),
    lessons_completed INTEGER NOT NULL DEFAULT 0 CHECK (lessons_completed >= 0),
    streak_maintained BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_activity_date UNIQUE (user_id, activity_date)
);

-- =============================================================================
-- INDEXES FOR OPTIMAL QUERY PERFORMANCE
-- =============================================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_subjects_domain ON subjects(domain_id);
CREATE INDEX idx_skills_subject ON skills(subject_id);
CREATE INDEX idx_learner_goals_user ON learner_goals(user_id);
CREATE INDEX idx_learner_goals_status ON learner_goals(user_id, status);

CREATE INDEX idx_assessments_subject ON assessments(subject_id);
CREATE INDEX idx_questions_assessment ON assessment_questions(assessment_id);
CREATE INDEX idx_questions_skill ON assessment_questions(skill_id);
CREATE INDEX idx_options_question ON assessment_options(question_id);

CREATE INDEX idx_attempts_user_goal ON assessment_attempts(user_id, learner_goal_id);
CREATE INDEX idx_responses_attempt ON assessment_responses(attempt_id);

CREATE INDEX idx_skill_gaps_lookup ON learner_skill_gaps(user_id, learner_goal_id);
CREATE INDEX idx_skill_gaps_status ON learner_skill_gaps(user_id, status);

CREATE INDEX idx_roadmaps_goal ON roadmaps(learner_goal_id);
CREATE INDEX idx_modules_roadmap ON roadmap_modules(roadmap_id);
CREATE INDEX idx_lessons_module ON roadmap_lessons(module_id);
CREATE INDEX idx_lesson_progress_user ON lesson_progress(user_id);
CREATE INDEX idx_activity_user_date ON user_activity_logs(user_id, activity_date);

-- =============================================================================
-- SEED DATA: CORE DOMAINS & SAMPLE TRACKS (Matches BODHA Frontend)
-- =============================================================================

INSERT INTO domains (id, name, description) VALUES
('programming', 'Software & Engineering', 'Software systems, backend architectures, web, and cloud'),
('mathematics', 'Mathematics & Data', 'Linear algebra, multivariable calculus, statistics, and discrete math'),
('languages', 'Languages & Linguistics', 'Conversational fluency, grammar frameworks, and vocabulary retention'),
('business', 'Business & Leadership', 'Product management, strategic roadmaps, metrics, and leadership');

INSERT INTO subjects (id, domain_id, title, tagline, difficulty_level, estimated_weeks, is_popular) VALUES
('java-backend', 'programming', 'Full-Stack Java & Spring Boot Architecture', 'Modern backend engineering, REST services, JPA, and Spring Security', 'Intermediate', 8, true),
('python-backend', 'programming', 'Python Backend Systems & FastAPI', 'Asynchronous APIs, Pydantic, SQLAlchemy, and Docker deployment', 'Beginner', 6, true),
('linear-algebra', 'mathematics', 'Linear Algebra for Machine Learning', 'Vector spaces, matrix decomposition, eigenvalues, and SVD applied to ML', 'Intermediate', 5, true),
('calculus', 'mathematics', 'Multivariable Calculus & Optimization', 'Gradients, Hessians, partial derivatives, and Lagrange multipliers', 'Advanced', 6, false),
('spanish', 'languages', 'Conversational Spanish (CEFR A1–B1)', 'Everyday dialogue, grammar essentials, verb conjugations, and active recall', 'Beginner', 7, true),
('product-mgmt', 'business', 'Product Management & Strategic Roadmapping', 'Discovery, customer research, North Star metrics, and RICE prioritization', 'All Levels', 4, false);
