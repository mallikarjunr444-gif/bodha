package com.bodha.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for submitting an answer to a question within an attempt
 * (POST /api/attempts/{attemptId}/responses).
 */
public record SubmitResponseRequestDto(
    @NotNull(message = "User ID is required for ownership verification")
    Long userId,

    @NotNull(message = "Question ID is required")
    Long questionId,

    @NotNull(message = "Selected option ID is required")
    Long selectedOptionId
) {}
