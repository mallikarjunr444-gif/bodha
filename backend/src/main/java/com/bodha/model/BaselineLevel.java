package com.bodha.model;

/**
 * Enum for the `baseline_level` column in the `learner_goals` table.
 *
 * Schema: CHECK (baseline_level IN ('beginner', 'intermediate', 'advanced'))
 *
 * All values are LOWERCASE in PostgreSQL → needs BaselineLevelConverter.
 *
 * Note: This is distinct from DifficultyLevel (used in subjects).
 *   - DifficultyLevel describes a subject's overall complexity ('Beginner' with capital B).
 *   - BaselineLevel describes the learner's self-reported starting familiarity ('beginner' lowercase).
 */
public enum BaselineLevel {

    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String dbValue;

    BaselineLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static BaselineLevel fromDbValue(String value) {
        for (BaselineLevel level : values()) {
            if (level.dbValue.equals(value)) return level;
        }
        throw new IllegalArgumentException("Unknown baseline_level value: '" + value + "'");
    }
}
