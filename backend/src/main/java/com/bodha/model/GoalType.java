package com.bodha.model;

/**
 * Enum for the `goal_type` column in the `learner_goals` table.
 *
 * Schema: CHECK (goal_type IN ('career', 'exam', 'project', 'mastery'))
 *
 * All values are LOWERCASE in PostgreSQL → @Enumerated(EnumType.STRING) would
 * store "CAREER" (uppercase) and violate the CHECK constraint.
 * GoalTypeConverter handles the bidirectional mapping.
 */
public enum GoalType {

    CAREER("career"),
    EXAM("exam"),
    PROJECT("project"),
    MASTERY("mastery");

    private final String dbValue;

    GoalType(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static GoalType fromDbValue(String value) {
        for (GoalType type : values()) {
            if (type.dbValue.equals(value)) return type;
        }
        throw new IllegalArgumentException("Unknown goal_type value: '" + value + "'");
    }
}
