package com.bodha.dto;

import java.util.List;

/**
 * Summary analysis response produced after evaluating an assessment attempt for skill gaps (Module E).
 */
public record SkillGapAnalysisResponseDto(
    Long attemptId,
    Long userId,
    Long learnerGoalId,
    String subjectId,
    String subjectTitle,
    int totalSkillsEvaluated,
    int masteredSkillsCount,
    int gapSkillsCount,
    int highSeverityGapsCount,
    List<SkillGapResponseDto> skillGaps
) {}
