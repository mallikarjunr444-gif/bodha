package com.bodha.dto;

import java.util.List;

/**
 * Structured, sanitized learning context representation passed to AI personalization engines (Module H).
 *
 * Privacy & Data Minimization:
 * - Omits sensitive credentials (passwords, password hashes, auth secrets, emails).
 * - Transmits ONLY pedagogical signals: profile metrics, goals, diagnostic gaps, roadmap progress.
 */
public record LearnerAiContext(
        Long userId,
        String displayName,
        ProfileContext profile,
        GoalContext goal,
        AssessmentContext assessment,
        SkillGapsContext skillGaps,
        RoadmapContext roadmap,
        ActivityContext activity
) {
    public record ProfileContext(
            int currentStreakDays,
            int totalXp,
            String bio
    ) {}

    public record GoalContext(
            Long goalId,
            String subjectId,
            String subjectTitle,
            String goalType,
            String baselineLevel,
            int dailyStudyMinutes,
            String status
    ) {}

    public record AssessmentContext(
            Long attemptId,
            Double scorePercentage,
            int totalQuestions,
            int correctAnswers,
            String completedAt
    ) {}

    public record SkillGapsContext(
            List<SkillSummary> highSeverityGaps,
            List<SkillSummary> mediumSeverityGaps,
            List<SkillSummary> lowSeverityGaps,
            List<SkillSummary> masteredSkills,
            int totalSkillsEvaluated
    ) {}

    public record SkillSummary(
            Long skillId,
            String name,
            String category,
            String status, // "GAP" or "MASTERED"
            String severity, // "HIGH", "MEDIUM", "LOW", or null
            String reason
    ) {}

    public record RoadmapContext(
            Long roadmapId,
            String title,
            int overallProgressPercentage,
            int totalLessons,
            int completedLessonsCount,
            int remainingLessonsCount,
            CurrentModuleContext currentModule,
            List<ModuleSummary> allModules
    ) {}

    public record CurrentModuleContext(
            Long moduleId,
            String title,
            String durationLabel,
            int orderIndex,
            String status,
            int totalLessons,
            int completedLessons,
            List<LessonSummary> lessons
    ) {}

    public record ModuleSummary(
            Long moduleId,
            String title,
            int orderIndex,
            String status,
            boolean isCurrent,
            int totalLessons,
            int completedLessons
    ) {}

    public record LessonSummary(
            Long lessonId,
            String title,
            String lessonType,
            int orderIndex,
            boolean isCompleted
    ) {}

    public record ActivityContext(
            int streakDays,
            int totalCompletedLessons,
            String lastActiveDate,
            int minutesSpentToday,
            int lessonsCompletedToday
    ) {}
}
