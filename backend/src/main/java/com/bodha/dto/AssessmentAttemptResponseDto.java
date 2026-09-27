package com.bodha.dto;

import com.bodha.model.AssessmentAttempt;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Data Transfer Object representing an assessment attempt session (Module D).
 */
public record AssessmentAttemptResponseDto(
    Long id,
    Long userId,
    Long assessmentId,
    String assessmentTitle,
    String assessmentType,
    Long learnerGoalId,
    String status,
    int totalQuestions,
    int answeredQuestions,
    OffsetDateTime completedAt,
    List<AssessmentQuestionDto> questions
) {
    public static AssessmentAttemptResponseDto fromEntity(
            AssessmentAttempt attempt,
            int questionCount,
            int answeredCount,
            List<AssessmentQuestionDto> questions) {
        if (attempt == null) return null;

        Long userId = attempt.getUser() != null ? attempt.getUser().getId() : null;
        Long assessmentId = attempt.getAssessment() != null ? attempt.getAssessment().getId() : null;
        String assessmentTitle = attempt.getAssessment() != null ? attempt.getAssessment().getTitle() : null;
        String assessmentType = attempt.getAssessment() != null && attempt.getAssessment().getAssessmentType() != null
                ? attempt.getAssessment().getAssessmentType().name() : null;
        Long goalId = attempt.getLearnerGoal() != null ? attempt.getLearnerGoal().getId() : null;

        String statusStr;
        if (attempt.isCompleted()) {
            statusStr = "COMPLETED";
        } else if (answeredCount > 0) {
            statusStr = "IN_PROGRESS";
        } else {
            statusStr = "STARTED";
        }

        int total = attempt.isCompleted() ? attempt.getTotalQuestions() : questionCount;

        return new AssessmentAttemptResponseDto(
            attempt.getId(),
            userId,
            assessmentId,
            assessmentTitle,
            assessmentType,
            goalId,
            statusStr,
            total,
            answeredCount,
            attempt.getCompletedAt(),
            questions != null ? questions : List.of()
        );
    }
}
