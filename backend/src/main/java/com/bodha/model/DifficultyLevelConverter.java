package com.bodha.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter: translates between DifficultyLevel enum (Java)
 * and the VARCHAR string stored in the `difficulty_level` column (PostgreSQL).
 *
 * @Converter(autoApply = true)
 *   Hibernate automatically applies this converter to every field of type
 *   DifficultyLevel across ALL entities without needing a per-field @Convert
 *   annotation. This is safe here because DifficultyLevel is only used in
 *   the subjects table.
 *
 * Mapping:
 *   Java              <-->  PostgreSQL
 *   BEGINNER          <-->  'Beginner'
 *   INTERMEDIATE      <-->  'Intermediate'
 *   ADVANCED          <-->  'Advanced'
 *   ALL_LEVELS        <-->  'All Levels'
 */
@Converter(autoApply = true)
public class DifficultyLevelConverter implements AttributeConverter<DifficultyLevel, String> {

    /**
     * Java -> DB: called on INSERT and UPDATE.
     * Returns the exact string that PostgreSQL will store in the column.
     */
    @Override
    public String convertToDatabaseColumn(DifficultyLevel level) {
        if (level == null) return null;
        return level.getDbValue();
    }

    /**
     * DB -> Java: called on SELECT when Hibernate reads the column value.
     * Converts the raw PostgreSQL string back to the DifficultyLevel enum.
     */
    @Override
    public DifficultyLevel convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return DifficultyLevel.fromDbValue(dbData);
    }
}
