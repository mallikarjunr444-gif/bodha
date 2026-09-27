package com.bodha.model;

/**
 * Enum for the `status` column in the `learner_goals` table.
 *
 * Schema: VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
 *         CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED'))
 *
 * Why @Enumerated(EnumType.STRING) IS sufficient here:
 *   All values are UPPERCASE and are valid Java identifiers — ACTIVE, PAUSED,
 *   COMPLETED — so @Enumerated(EnumType.STRING) stores exactly the right string
 *   without a custom converter. No AttributeConverter is needed.
 *
 * This is explicitly declared on the field in LearnerGoal.java via @Enumerated.
 */
public enum GoalStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}
