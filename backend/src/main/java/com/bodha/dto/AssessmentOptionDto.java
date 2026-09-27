package com.bodha.dto;

import com.bodha.model.AssessmentOption;

/**
 * Learner-facing multiple-choice option DTO.
 * Excludes correctness flag to protect assessment integrity prior to submission.
 */
public record AssessmentOptionDto(
    Long id,
    String optionText,
    int orderIndex
) {
    public static AssessmentOptionDto fromEntity(AssessmentOption option) {
        if (option == null) return null;
        return new AssessmentOptionDto(
            option.getId(),
            option.getOptionText(),
            option.getOrderIndex()
        );
    }
}
