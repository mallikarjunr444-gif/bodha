package com.bodha.model;

/**
 * Enum for the `difficulty_level` column in the `subjects` table.
 *
 * Schema: CHECK (difficulty_level IN ('Beginner', 'Intermediate', 'Advanced', 'All Levels'))
 *
 * Why NOT @Enumerated(EnumType.STRING)?
 *   "All Levels" contains a space — it is not a valid Java identifier.
 *   We therefore use a custom AttributeConverter (DifficultyLevelConverter)
 *   that maps each enum constant to its exact database string representation.
 */
public enum DifficultyLevel {

    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
    ALL_LEVELS("All Levels");

    /** The exact string stored in the PostgreSQL column. */
    private final String dbValue;

    DifficultyLevel(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    /**
     * Reverse-lookup: converts a database string back to the enum constant.
     * Called by DifficultyLevelConverter.convertToEntityAttribute().
     */
    public static DifficultyLevel fromDbValue(String value) {
        for (DifficultyLevel level : values()) {
            if (level.dbValue.equals(value)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty_level value: '" + value + "'");
    }
}
