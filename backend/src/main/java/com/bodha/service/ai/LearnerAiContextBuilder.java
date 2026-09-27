package com.bodha.service.ai;

import com.bodha.dto.LearnerAiContext;
import com.bodha.dto.LessonAiContext;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gathers and sanitizes comprehensive learner state across all completed modules
 * (Identity, Goals, Diagnostics, Skill Gaps, Roadmaps, Progress, Activity) into an immutable,
 * privacy-conscious LearnerAiContext (Module H).
 */
@Component
public class LearnerAiContextBuilder {

    private final UserRepository userRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final LearnerGoalRepository learnerGoalRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final LearnerSkillGapRepository learnerSkillGapRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapModuleRepository roadmapModuleRepository;
    private final RoadmapLessonRepository roadmapLessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserActivityLogRepository userActivityLogRepository;

    public LearnerAiContextBuilder(
            UserRepository userRepository,
            LearnerProfileRepository learnerProfileRepository,
            LearnerGoalRepository learnerGoalRepository,
            AssessmentAttemptRepository assessmentAttemptRepository,
            LearnerSkillGapRepository learnerSkillGapRepository,
            RoadmapRepository roadmapRepository,
            RoadmapModuleRepository roadmapModuleRepository,
            RoadmapLessonRepository roadmapLessonRepository,
            LessonProgressRepository lessonProgressRepository,
            UserActivityLogRepository userActivityLogRepository) {
        this.userRepository = userRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.learnerGoalRepository = learnerGoalRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.learnerSkillGapRepository = learnerSkillGapRepository;
        this.roadmapRepository = roadmapRepository;
        this.roadmapModuleRepository = roadmapModuleRepository;
        this.roadmapLessonRepository = roadmapLessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userActivityLogRepository = userActivityLogRepository;
    }

    /**
     * Builds structured, sanitized learner context for a specific user and goal.
     */
    @Transactional(readOnly = true)
    public LearnerAiContext buildContext(Long userId, Long goalId) {
        validateIds(userId, goalId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        LearnerGoal goal = learnerGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner goal not found with id: " + goalId));

        if (!goal.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Learner goal " + goalId + " does not belong to user ID " + userId);
        }

        // 1. Learner Profile metrics (XP, streak, bio)
        LearnerProfile profile = learnerProfileRepository.findByUserId(userId).orElse(null);
        int currentStreak = (profile != null) ? profile.getCurrentStreakDays() : 0;
        int totalXp = (profile != null) ? profile.getTotalXp() : 0;
        String bio = (profile != null) ? profile.getBio() : null;
        LearnerAiContext.ProfileContext profileCtx = new LearnerAiContext.ProfileContext(currentStreak, totalXp, bio);

        // 2. Goal calibration details
        String subjectId = (goal.getSubject() != null) ? goal.getSubject().getId() : "";
        String subjectTitle = (goal.getSubject() != null) ? goal.getSubject().getTitle() : "";
        String goalType = (goal.getGoalType() != null) ? goal.getGoalType().getDbValue() : "career";
        String baselineLevel = (goal.getBaselineLevel() != null) ? goal.getBaselineLevel().getDbValue() : "beginner";
        int dailyMinutes = goal.getDailyTimeMinutes();
        String goalStatus = (goal.getStatus() != null) ? goal.getStatus().name() : "ACTIVE";
        LearnerAiContext.GoalContext goalCtx = new LearnerAiContext.GoalContext(
                goal.getId(), subjectId, subjectTitle, goalType, baselineLevel, dailyMinutes, goalStatus);

        // 3. Latest diagnostic assessment attempt
        List<AssessmentAttempt> attempts = assessmentAttemptRepository
                .findByUserIdAndLearnerGoalIdOrderByCompletedAtDesc(userId, goalId);
        LearnerAiContext.AssessmentContext assessmentCtx = null;
        if (!attempts.isEmpty()) {
            AssessmentAttempt latest = attempts.get(0);
            if (latest.isCompleted()) {
                Double score = (latest.getScorePercentage() != null) ? latest.getScorePercentage().doubleValue() : 0.0;
                String completedAtStr = (latest.getCompletedAt() != null) ? latest.getCompletedAt().toString() : null;
                assessmentCtx = new LearnerAiContext.AssessmentContext(
                        latest.getId(), score, latest.getTotalQuestions(), latest.getCorrectAnswers(), completedAtStr);
            }
        }

        // 4. Evaluated skill gaps & competencies
        List<LearnerSkillGap> gaps = learnerSkillGapRepository.findByUserIdAndLearnerGoalId(userId, goalId);
        List<LearnerAiContext.SkillSummary> highGaps = new ArrayList<>();
        List<LearnerAiContext.SkillSummary> mediumGaps = new ArrayList<>();
        List<LearnerAiContext.SkillSummary> lowGaps = new ArrayList<>();
        List<LearnerAiContext.SkillSummary> mastered = new ArrayList<>();

        for (LearnerSkillGap g : gaps) {
            String skillName = (g.getSkill() != null) ? g.getSkill().getName() : "Skill";
            String cat = (g.getSkill() != null && g.getSkill().getCategory() != null) ? g.getSkill().getCategory().name() : "core";
            String status = (g.getStatus() != null) ? g.getStatus().name() : "GAP";
            String severity = (g.getGapSeverity() != null) ? g.getGapSeverity().name() : null;
            String reason = g.getReason();

            LearnerAiContext.SkillSummary summary = new LearnerAiContext.SkillSummary(
                    g.getSkill() != null ? g.getSkill().getId() : null,
                    skillName, cat, status, severity, reason);

            if (g.getStatus() == LearnerSkillGap.Status.MASTERED) {
                mastered.add(summary);
            } else if (g.getGapSeverity() == LearnerSkillGap.Severity.HIGH) {
                highGaps.add(summary);
            } else if (g.getGapSeverity() == LearnerSkillGap.Severity.MEDIUM) {
                mediumGaps.add(summary);
            } else {
                lowGaps.add(summary);
            }
        }

        LearnerAiContext.SkillGapsContext skillGapsCtx = new LearnerAiContext.SkillGapsContext(
                highGaps, mediumGaps, lowGaps, mastered, gaps.size());

        // 5. Active Roadmap, modules, lessons, and progress
        Optional<Roadmap> roadmapOpt = roadmapRepository.findByLearnerGoalId(goalId);
        LearnerAiContext.RoadmapContext roadmapCtx = null;

        if (roadmapOpt.isPresent()) {
            Roadmap roadmap = roadmapOpt.get();
            List<RoadmapModule> modules = roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
            List<Long> modIds = modules.stream().map(RoadmapModule::getId).toList();

            List<RoadmapLesson> lessons = modIds.isEmpty()
                    ? List.of()
                    : roadmapLessonRepository.findByModuleIdInOrderByOrderIndexAsc(modIds);
            List<Long> lessonIds = lessons.stream().map(RoadmapLesson::getId).toList();

            List<LessonProgress> progresses = lessonIds.isEmpty()
                    ? List.of()
                    : lessonProgressRepository.findByUserIdAndLessonIdIn(userId, lessonIds);
            Set<Long> completedLessonIds = progresses.stream()
                    .filter(LessonProgress::isCompleted)
                    .map(p -> p.getLesson().getId())
                    .collect(Collectors.toSet());

            Map<Long, List<RoadmapLesson>> lessonsByModuleId = lessons.stream()
                    .collect(Collectors.groupingBy(l -> l.getModule().getId()));

            List<LearnerAiContext.ModuleSummary> allModuleSummaries = new ArrayList<>();
            LearnerAiContext.CurrentModuleContext currentModuleCtx = null;

            for (RoadmapModule m : modules) {
                List<RoadmapLesson> mLessons = lessonsByModuleId.getOrDefault(m.getId(), List.of());
                int totalInMod = mLessons.size();
                int completedInMod = (int) mLessons.stream().filter(l -> completedLessonIds.contains(l.getId())).count();
                String mStatus = (m.getStatus() != null) ? m.getStatus().name() : "LOCKED";

                LearnerAiContext.ModuleSummary modSummary = new LearnerAiContext.ModuleSummary(
                        m.getId(), m.getTitle(), m.getOrderIndex(), mStatus, m.isCurrent(), totalInMod, completedInMod);
                allModuleSummaries.add(modSummary);

                if (m.isCurrent() || (currentModuleCtx == null && !"COMPLETED".equalsIgnoreCase(mStatus))) {
                    List<LearnerAiContext.LessonSummary> lessonSummaries = mLessons.stream()
                            .map(l -> new LearnerAiContext.LessonSummary(
                                    l.getId(),
                                    l.getTitle(),
                                    l.getLessonType() != null ? l.getLessonType().getDbValue() : "Theory + Code",
                                    l.getOrderIndex(),
                                    completedLessonIds.contains(l.getId())
                            ))
                            .toList();

                    currentModuleCtx = new LearnerAiContext.CurrentModuleContext(
                            m.getId(), m.getTitle(), m.getDurationLabel(), m.getOrderIndex(),
                            mStatus, totalInMod, completedInMod, lessonSummaries);
                }
            }

            int totalLessonsCount = lessons.size();
            int completedLessonsCount = completedLessonIds.size();
            int remainingLessonsCount = Math.max(0, totalLessonsCount - completedLessonsCount);

            roadmapCtx = new LearnerAiContext.RoadmapContext(
                    roadmap.getId(),
                    roadmap.getTitle(),
                    roadmap.getOverallProgressPercentage(),
                    totalLessonsCount,
                    completedLessonsCount,
                    remainingLessonsCount,
                    currentModuleCtx,
                    allModuleSummaries
            );
        }

        // 6. User Activity & Streak Telemetry
        LocalDate today = LocalDate.now();
        Optional<UserActivityLog> todayLog = userActivityLogRepository.findByUserIdAndActivityDate(userId, today);
        int minsToday = todayLog.map(UserActivityLog::getMinutesSpent).orElse(0);
        int lessonsToday = todayLog.map(UserActivityLog::getLessonsCompleted).orElse(0);
        int totalCompletedLessonsOverall = (int) lessonProgressRepository.countByUserIdAndIsCompletedTrue(userId);

        LearnerAiContext.ActivityContext activityCtx = new LearnerAiContext.ActivityContext(
                currentStreak,
                totalCompletedLessonsOverall,
                today.toString(),
                minsToday,
                lessonsToday
        );

        return new LearnerAiContext(
                user.getId(),
                user.getFullName(),
                profileCtx,
                goalCtx,
                assessmentCtx,
                skillGapsCtx,
                roadmapCtx,
                activityCtx
        );
    }

    /**
     * Builds focused lesson context, enforcing ownership and retrieving module hierarchy.
     */
    @Transactional(readOnly = true)
    public LessonAiContext buildLessonContext(Long lessonId, Long userId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        RoadmapLesson lesson = roadmapLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap lesson not found with id: " + lessonId));

        if (lesson.getModule() == null || lesson.getModule().getRoadmap() == null) {
            throw new IllegalStateException("Lesson " + lessonId + " is orphaned from a roadmap");
        }

        Roadmap roadmap = lesson.getModule().getRoadmap();
        if (!roadmap.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Lesson " + lessonId + " does not belong to user ID " + userId);
        }

        boolean isCompleted = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .map(LessonProgress::isCompleted)
                .orElse(false);

        String lessonTypeStr = (lesson.getLessonType() != null)
                ? lesson.getLessonType().getDbValue()
                : "Theory + Code";

        return new LessonAiContext(
                lesson.getId(),
                lesson.getTitle(),
                lessonTypeStr,
                lesson.getContentBody(),
                lesson.getOrderIndex(),
                isCompleted,
                lesson.getModule().getId(),
                lesson.getModule().getTitle(),
                lesson.getModule().getDurationLabel()
        );
    }

    private void validateIds(Long userId, Long goalId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (goalId == null) {
            throw new IllegalArgumentException("Learner goal ID is required");
        }
    }
}
