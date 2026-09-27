package com.bodha.dto;

import com.bodha.model.Skill;

/**
 * Data Transfer Object representing a granular competency/skill within a subject (Module B).
 */
public record SkillResponseDto(
    Long id,
    String subjectId,
    String name,
    String category,
    String description
) {
    public static SkillResponseDto fromEntity(Skill skill) {
        if (skill == null) return null;
        return new SkillResponseDto(
            skill.getId(),
            skill.getSubject() != null ? skill.getSubject().getId() : null,
            skill.getName(),
            skill.getCategory() != null ? skill.getCategory().getDbValue() : null,
            skill.getDescription()
        );
    }
}
