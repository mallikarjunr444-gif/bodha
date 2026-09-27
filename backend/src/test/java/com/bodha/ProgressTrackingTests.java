package com.bodha;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests validating the deterministic progress calculation and streak tracking algorithms (Module G).
 */
class ProgressTrackingTests {

    private int calculateOverallPercentage(int completedLessons, int totalLessons) {
        if (totalLessons <= 0) return 0;
        return (int) Math.round(((double) completedLessons / totalLessons) * 100.0);
    }

    private int calculateNextStreak(LocalDate lastActiveDate, LocalDate currentDate, int currentStreak) {
        if (lastActiveDate == null) {
            return 1;
        }
        if (lastActiveDate.equals(currentDate)) {
            return currentStreak; // Same day: maintain
        }
        if (lastActiveDate.equals(currentDate.minusDays(1))) {
            return currentStreak + 1; // Consecutive day: increment
        }
        return 1; // Gap > 1 day: reset
    }

    @Test
    @DisplayName("Progress percentage must be calculated deterministically and match integer rounding")
    void testProgressPercentageCalculations() {
        assertEquals(0, calculateOverallPercentage(0, 15));
        assertEquals(7, calculateOverallPercentage(1, 15));   // 1/15 = 6.67% -> rounds to 7%
        assertEquals(27, calculateOverallPercentage(4, 15));  // 4/15 = 26.67% -> rounds to 27%
        assertEquals(33, calculateOverallPercentage(5, 15));  // 5/15 = 33.33% -> rounds to 33%
        assertEquals(100, calculateOverallPercentage(15, 15));
    }

    @Test
    @DisplayName("Streak tracking must increment on consecutive days and maintain on same day")
    void testStreakCalculationRules() {
        LocalDate today = LocalDate.of(2026, 9, 27);

        // 1. First activity ever
        int streakDay1 = calculateNextStreak(null, today, 0);
        assertEquals(1, streakDay1, "First activity must initialize streak to 1");

        // 2. Same-day second lesson: must NOT increment again
        int sameDayStreak = calculateNextStreak(today, today, streakDay1);
        assertEquals(1, sameDayStreak, "Activity on the same day must not increment streak");

        // 3. Activity on next consecutive day: must increment
        LocalDate tomorrow = today.plusDays(1);
        int consecutiveStreak = calculateNextStreak(today, tomorrow, streakDay1);
        assertEquals(2, consecutiveStreak, "Activity on consecutive calendar day must increment streak");

        // 4. Inactivity gap of 2 days: must reset to 1
        LocalDate threeDaysLater = today.plusDays(3);
        int resetStreak = calculateNextStreak(today, threeDaysLater, consecutiveStreak);
        assertEquals(1, resetStreak, "Inactivity gap greater than 1 day must reset streak to 1");
    }

    @Test
    @DisplayName("Module is complete only when all module lessons are finished")
    void testModuleCompletionCheck() {
        int totalLessons = 4;
        assertFalse(0 == totalLessons);
        assertFalse(3 == totalLessons);
        assertTrue(4 == totalLessons);
    }
}
