package com.bodha.model;

/**
 * Enum for the `assessment_type` column in the `assessments` table.
 *
 * Schema: VARCHAR(50) NOT NULL
 *         CHECK (assessment_type IN ('DIAGNOSTIC', 'MODULE_QUIZ', 'CHECKPOINT', 'FINAL_CAPSTONE'))
 *
 * Why @Enumerated(EnumType.STRING) IS sufficient here:
 *   All values are UPPERCASE with underscores — valid Java enum identifiers.
 *   MODULE_QUIZ and FINAL_CAPSTONE in Java match 'MODULE_QUIZ' and 'FINAL_CAPSTONE'
 *   in PostgreSQL exactly. No custom AttributeConverter is required.
 *
 * Lifecycle of each type in the BODHA flow:
 *   DIAGNOSTIC     — Taken once at the start (step 4 of the core workflow)
 *   MODULE_QUIZ    — Practice recall inside an active roadmap module
 *   CHECKPOINT     — Mid-roadmap skill validation
 *   FINAL_CAPSTONE — End-of-roadmap mastery verification
 */
public enum AssessmentType {
    DIAGNOSTIC,
    MODULE_QUIZ,
    CHECKPOINT,
    FINAL_CAPSTONE
}
