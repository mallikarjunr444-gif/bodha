package com.bodha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request payload for creating a learner goal (POST /api/goals).
 *
 * Captures target outcome, baseline familiarity, and daily time commitment.
 */
public record CreateGoalRequestDto(
    @NotNull(message = "User ID is required")
    Long userId,

    @NotBlank(message = "Subject ID is required")
    String subjectId,

    @NotBlank(message = "Goal type is required")
    String goalType,

    @NotBlank(message = "Baseline level is required")
    String baselineLevel,

    Integer dailyTimeMinutes,

    String dailyTime,

    String targetLevel,

    LocalDate targetDate
) {
    /**
     * Resolves the daily time in minutes, supporting either an integer
     * or a frontend time string like "15m", "30m", "45m", "60m".
     */
    public Integer resolveDailyMinutes() {
        if (dailyTimeMinutes != null) {
            return dailyTimeMinutes;
        }
        if (dailyTime != null && !dailyTime.isBlank()) {
            String cleaned = dailyTime.trim().toLowerCase().replace("m", "").replace("min", "");
            try {
                return Integer.parseInt(cleaned);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
