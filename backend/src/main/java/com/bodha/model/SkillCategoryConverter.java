package com.bodha.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter: translates between SkillCategory enum (Java)
 * and the lowercase VARCHAR string stored in the `category` column (PostgreSQL).
 *
 * @Converter(autoApply = true)
 *   Automatically applied to every SkillCategory field across all entities.
 *
 * Mapping:
 *   Java          <-->  PostgreSQL
 *   PREREQUISITE  <-->  'prerequisite'
 *   CORE          <-->  'core'
 *   ADVANCED      <-->  'advanced'
 */
@Converter(autoApply = true)
public class SkillCategoryConverter implements AttributeConverter<SkillCategory, String> {

    /** Java -> DB: called on INSERT and UPDATE. */
    @Override
    public String convertToDatabaseColumn(SkillCategory category) {
        if (category == null) return null;
        return category.getDbValue();
    }

    /** DB -> Java: called on SELECT. */
    @Override
    public SkillCategory convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return SkillCategory.fromDbValue(dbData);
    }
}
