package com.bodha.dto;

import com.bodha.model.AssessmentQuestion;

import java.util.List;

/**
 * Learner-facing assessment question DTO during an active test session.
 * Excludes pedagogical explanation and answer keys to maintain test integrity.
 */
public record AssessmentQuestionDto(
    Long id,
    Long assessmentId,
    Long skillId,
    String skillName,
    String skillCategory,
    String questionText,
    int orderIndex,
    List<AssessmentOptionDto> options
) {
    public static AssessmentQuestionDto fromEntity(AssessmentQuestion question, List<AssessmentOptionDto> options) {
        if (question == null) return null;
        Long assessmentId = question.getAssessment() != null ? question.getAssessment().getId() : null;
        Long skillId = question.getSkill() != null ? question.getSkill().getId() : null;
        String skillName = question.getSkill() != null ? question.getSkill().getName() : null;
        String skillCategory = question.getSkill() != null && question.getSkill().getCategory() != null
                ? question.getSkill().getCategory().name() : null;

        return new AssessmentQuestionDto(
            question.getId(),
            assessmentId,
            skillId,
            skillName,
            skillCategory,
            question.getQuestionText(),
            question.getOrderIndex(),
            options != null ? options : List.of()
        );
    }
}
