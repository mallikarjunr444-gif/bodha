package com.bodha.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Comprehensive assessment result payload generated upon completion.
 * Encapsulates deterministic backend score calculation, question-level verification,
 * and skill-level evidence ready for the Skill-Gap Engine.
 */
public record AssessmentResultResponseDto(
    Long attemptId,
    Long userId,
    Long assessmentId,
    String assessmentTitle,
    String assessmentType,
    Long learnerGoalId,
    String status,
    int totalQuestions,
    int answeredQuestions,
    int correctAnswers,
    int incorrectAnswers,
    BigDecimal scorePercentage,
    OffsetDateTime completedAt,
    List<SkillPerformanceDto> skillBreakdown,
    List<QuestionResultDto> questionResults
) {}
