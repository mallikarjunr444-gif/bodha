package com.bodha.dto;

import java.time.LocalDate;

/**
 * Request payload for updating an existing learner goal (PUT /api/goals/{goalId}).
 * Supports modifying goal type, baseline level, study time, and goal status.
 */
public record UpdateGoalRequestDto(
    String goalType,
    String baselineLevel,
    Integer dailyTimeMinutes,
    String dailyTime,
    String status,
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
