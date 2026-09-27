package com.bodha.model;

/**
 * Enum for the `category` column in the `skills` table.
 *
 * Schema: CHECK (category IN ('prerequisite', 'core', 'advanced')) DEFAULT 'core'
 *
 * Why NOT @Enumerated(EnumType.STRING)?
 *   The database stores lowercase values ('prerequisite', 'core', 'advanced').
 *   Java enum constants by convention are UPPERCASE (PREREQUISITE, CORE, ADVANCED).
 *   @Enumerated(EnumType.STRING) would try to store "PREREQUISITE" (uppercase)
 *   which would violate the PostgreSQL CHECK constraint.
 *   We use SkillCategoryConverter to map correctly in both directions.
 */
public enum SkillCategory {

    PREREQUISITE("prerequisite"),
    CORE("core"),
    ADVANCED("advanced");

    /** The exact lowercase string stored in PostgreSQL. */
    private final String dbValue;

    SkillCategory(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    /**
     * Reverse-lookup: converts a database string back to the enum constant.
     * Called by SkillCategoryConverter.convertToEntityAttribute().
     */
    public static SkillCategory fromDbValue(String value) {
        for (SkillCategory cat : values()) {
            if (cat.dbValue.equals(value)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown skill category value: '" + value + "'");
    }
}
