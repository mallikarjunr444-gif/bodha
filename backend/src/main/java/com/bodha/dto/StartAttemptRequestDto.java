package com.bodha.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for starting an assessment attempt (POST /api/assessments/{assessmentId}/attempts).
 */
public record StartAttemptRequestDto(
    @NotNull(message = "User ID is required")
    Long userId,

    Long learnerGoalId
) {}
