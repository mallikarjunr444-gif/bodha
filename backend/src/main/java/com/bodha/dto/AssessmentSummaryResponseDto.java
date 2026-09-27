package com.bodha.dto;

import com.bodha.model.Assessment;

/**
 * Data Transfer Object representing an assessment overview (Module D).
 */
public record AssessmentSummaryResponseDto(
    Long id,
    String subjectId,
    String subjectTitle,
    String title,
    String assessmentType,
    String description,
    int questionCount
) {
    public static AssessmentSummaryResponseDto fromEntity(Assessment assessment, int questionCount) {
        if (assessment == null) return null;
        String sId = assessment.getSubject() != null ? assessment.getSubject().getId() : null;
        String sTitle = assessment.getSubject() != null ? assessment.getSubject().getTitle() : null;
        String type = assessment.getAssessmentType() != null ? assessment.getAssessmentType().name() : null;

        return new AssessmentSummaryResponseDto(
            assessment.getId(),
            sId,
            sTitle,
            assessment.getTitle(),
            type,
            assessment.getDescription(),
            questionCount
        );
    }
}
