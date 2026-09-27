package com.bodha.dto;

/**
 * Question-level performance breakdown returned upon assessment completion.
 * Details selected option, correct answer key, and pedagogical rationale.
 */
public record QuestionResultDto(
    Long questionId,
    String questionText,
    Long skillId,
    String skillName,
    String skillCategory,
    Long selectedOptionId,
    String selectedOptionText,
    Long correctOptionId,
    String correctOptionText,
    boolean isCorrect,
    String explanation
) {}
