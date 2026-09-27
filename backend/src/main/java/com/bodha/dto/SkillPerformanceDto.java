package com.bodha.dto;

/**
 * Aggregated performance evidence for a specific granular skill.
 * Directly feeds into Module E (Skill-Gap Engine) for isolating learning gaps.
 */
public record SkillPerformanceDto(
    Long skillId,
    String skillName,
    String skillCategory,
    int totalQuestions,
    int correctAnswers,
    double scorePercentage
) {}
