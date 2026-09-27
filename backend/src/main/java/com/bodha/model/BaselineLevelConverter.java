package com.bodha.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter: BaselineLevel enum <-> lowercase PostgreSQL VARCHAR.
 *
 * Mapping:
 *   BEGINNER      <-->  'beginner'
 *   INTERMEDIATE  <-->  'intermediate'
 *   ADVANCED      <-->  'advanced'
 */
@Converter(autoApply = true)
public class BaselineLevelConverter implements AttributeConverter<BaselineLevel, String> {

    @Override
    public String convertToDatabaseColumn(BaselineLevel level) {
        if (level == null) return null;
        return level.getDbValue();
    }

    @Override
    public BaselineLevel convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return BaselineLevel.fromDbValue(dbData);
    }
}
