package com.bodha.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter: GoalType enum <-> lowercase PostgreSQL VARCHAR.
 *
 * Mapping:
 *   CAREER   <-->  'career'
 *   EXAM     <-->  'exam'
 *   PROJECT  <-->  'project'
 *   MASTERY  <-->  'mastery'
 */
@Converter(autoApply = true)
public class GoalTypeConverter implements AttributeConverter<GoalType, String> {

    @Override
    public String convertToDatabaseColumn(GoalType goalType) {
        if (goalType == null) return null;
        return goalType.getDbValue();
    }

    @Override
    public GoalType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return GoalType.fromDbValue(dbData);
    }
}
