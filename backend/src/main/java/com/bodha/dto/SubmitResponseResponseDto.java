package com.bodha.dto;

/**
 * Response payload acknowledging a recorded answer.
 * Does not expose the correct answer key until the assessment is formally completed.
 */
public record SubmitResponseResponseDto(
    Long attemptId,
    Long questionId,
    Long selectedOptionId,
    String message
) {}
