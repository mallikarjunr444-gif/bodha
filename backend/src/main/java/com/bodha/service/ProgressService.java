package com.bodha.service;

import com.bodha.dto.*;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing persistent lesson progress, milestone promotions,
 * overall roadmap completion, and user activity/streak telemetry (Module G).
 */
@Service
public class ProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final RoadmapLessonRepository roadmapLessonRepository;
    private final RoadmapModuleRepository roadmapModuleRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final UserActivityLogRepository userActivityLogRepository;

    public ProgressService(
            LessonProgressRepository lessonProgressRepository,
            RoadmapLessonRepository roadmapLessonRepository,
            RoadmapModuleRepository roadmapModuleRepository,
            RoadmapRepository roadmapRepository,
            UserRepository userRepository,
            LearnerProfileRepository learnerProfileRepository,
            UserActivityLogRepository userActivityLogRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.roadmapLessonRepository = roadmapLessonRepository;
        this.roadmapModuleRepository = roadmapModuleRepository;
        this.roadmapRepository = roadmapRepository;
        this.userRepository = userRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.userActivityLogRepository = userActivityLogRepository;
    }

    /**
     * Starts or resumes a lesson for a learner.
     * Idempotent: does not overwrite a COMPLETED lesson or create duplicate records.
     */
    @Transactional
    public LessonProgressResponseDto startLesson(Long lessonId, Long userId) {
        validateIds(lessonId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RoadmapLesson lesson = roadmapLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap lesson not found with id: " + lessonId));

        validateLessonOwnership(lesson, userId);

        Optional<LessonProgress> existing = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            LessonProgress current = existing.get();
            String status = current.isCompleted() ? "COMPLETED" : "IN_PROGRESS";
            return LessonProgressResponseDto.fromEntity(current, status);
        }

        // Create new progress record in IN_PROGRESS state (isCompleted = false)
        LessonProgress newProgress = new LessonProgress(user, lesson, false);
        LessonProgress saved = lessonProgressRepository.save(newProgress);
        return LessonProgressResponseDto.fromEntity(saved, "IN_PROGRESS");
    }

    /**
     * Marks a lesson as COMPLETED and updates module state, roadmap progress percentage,
     * daily user activity telemetry, and learner profile streaks/XP.
     * Idempotent: repeated completions of the same lesson do not double-count activity or progress.
     */
    @Transactional
    public LessonProgressResponseDto completeLesson(Long lessonId, Long userId) {
        validateIds(lessonId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RoadmapLesson lesson = roadmapLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap lesson not found with id: " + lessonId));

        validateLessonOwnership(lesson, userId);

        Optional<LessonProgress> existing = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        boolean wasAlreadyCompleted = false;
        LessonProgress progress;

        if (existing.isPresent()) {
            progress = existing.get();
            if (progress.isCompleted()) {
                wasAlreadyCompleted = true; // Idempotent no-op
            } else {
                progress.setCompleted(true);
                progress.setCompletedAt(OffsetDateTime.now());
                progress = lessonProgressRepository.save(progress);
            }
        } else {
            progress = new LessonProgress(user, lesson, true, OffsetDateTime.now());
            progress = lessonProgressRepository.save(progress);
        }

        // Only update dependent entities if the completion was newly recorded
        if (!wasAlreadyCompleted) {
            RoadmapModule module = lesson.getModule();
            Roadmap roadmap = module.getRoadmap();

            // 1. Recalculate module completion & sequential unlocking
            promoteModuleStateIfComplete(module, roadmap, userId);

            // 2. Recalculate roadmap overall progress percentage
            recalculateRoadmapProgress(roadmap, userId);

            // 3. Update daily activity logs (idempotent per day)
            updateDailyActivityLog(user);

            // 4. Update learner profile streaks & gamification XP
            updateLearnerProfileStreakAndXp(user);
        }

        return LessonProgressResponseDto.fromEntity(progress, "COMPLETED");
    }

    /**
     * Retrieves progress status for a specific lesson and learner.
     */
    @Transactional(readOnly = true)
    public LessonProgressResponseDto getLessonProgress(Long lessonId, Long userId) {
        validateIds(lessonId, userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        RoadmapLesson lesson = roadmapLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap lesson not found with id: " + lessonId));

        validateLessonOwnership(lesson, userId);

        Optional<LessonProgress> existing = lessonProgressRepository.findByUserIdAndLessonId(userId, lessonId);
        if (existing.isPresent()) {
            LessonProgress progress = existing.get();
            String status = progress.isCompleted() ? "COMPLETED" : "IN_PROGRESS";
            return LessonProgressResponseDto.fromEntity(progress, status);
        }

        return LessonProgressResponseDto.notStarted(lesson, userId);
    }

    /**
     * Calculates and retrieves complete progress breakdown across a roadmap.
     */
    @Transactional(readOnly = true)
    public RoadmapProgressResponseDto getRoadmapProgress(Long roadmapId, Long userId) {
        if (roadmapId == null) {
            throw new IllegalArgumentException("Roadmap ID is required");
        }

        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found with id: " + roadmapId));

        if (userId != null && !roadmap.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Roadmap " + roadmapId + " does not belong to user ID " + userId);
        }

        Long targetUserId = (userId != null) ? userId : roadmap.getUser().getId();
        return computeRoadmapProgress(roadmap, targetUserId);
    }

    /**
     * Retrieves high-level learner progress summary for the dashboard.
     */
    @Transactional(readOnly = true)
    public ProgressSummaryResponseDto getUserProgressSummary(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        LearnerProfile profile = learnerProfileRepository.findByUserId(userId).orElse(null);
        int currentStreak = (profile != null) ? profile.getCurrentStreakDays() : 0;
        int totalXp = (profile != null) ? profile.getTotalXp() : 0;
        OffsetDateTime lastActive = (profile != null) ? profile.getLastActiveAt() : null;

        int totalCompletedLessons = (int) lessonProgressRepository.countByUserIdAndIsCompletedTrue(userId);

        // Find active roadmap
        Optional<Roadmap> activeRoadmap = roadmapRepository.findByUserIdAndIsActiveTrue(userId);
        RoadmapProgressResponseDto roadmapProgress = activeRoadmap
                .map(r -> computeRoadmapProgress(r, userId))
                .orElse(null);

        // Fetch recent activity logs
        List<UserActivityLog> logs = userActivityLogRepository.findByUserIdOrderByActivityDateDesc(userId);
        List<UserActivityResponseDto> recentActivities = logs.stream()
                .limit(7)
                .map(UserActivityResponseDto::fromEntity)
                .toList();

        return new ProgressSummaryResponseDto(
                userId,
                currentStreak,
                totalXp,
                lastActive,
                totalCompletedLessons,
                roadmapProgress,
                recentActivities
        );
    }

    // -------------------------------------------------------------------------
    // Internal Helper Methods
    // -------------------------------------------------------------------------

    private void validateIds(Long lessonId, Long userId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    private void validateLessonOwnership(RoadmapLesson lesson, Long userId) {
        if (lesson.getModule() == null || lesson.getModule().getRoadmap() == null) {
            throw new IllegalStateException("Lesson " + lesson.getId() + " is orphaned from a roadmap");
        }
        Roadmap roadmap = lesson.getModule().getRoadmap();
        if (!roadmap.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Lesson " + lesson.getId() + " does not belong to user ID " + userId);
        }
    }

    private void promoteModuleStateIfComplete(RoadmapModule module, Roadmap roadmap, Long userId) {
        List<RoadmapLesson> moduleLessons = roadmapLessonRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());
        if (moduleLessons.isEmpty()) return;

        List<Long> lessonIds = moduleLessons.stream().map(RoadmapLesson::getId).toList();
        List<LessonProgress> completed = lessonProgressRepository.findByUserIdAndLessonIdIn(userId, lessonIds)
                .stream()
                .filter(LessonProgress::isCompleted)
                .toList();

        if (completed.size() == moduleLessons.size()) {
            module.setStatus(RoadmapModule.Status.COMPLETED);

            // Promote next sequential module if this was current
            if (module.isCurrent()) {
                List<RoadmapModule> allModules = roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
                Optional<RoadmapModule> nextOpt = allModules.stream()
                        .filter(m -> m.getOrderIndex() > module.getOrderIndex())
                        .findFirst();

                if (nextOpt.isPresent()) {
                    RoadmapModule next = nextOpt.get();
                    next.setStatus(RoadmapModule.Status.UNLOCKED);
                    next.setCurrent(true);
                    roadmapModuleRepository.save(next);
                }
                module.setCurrent(false);
            }
            roadmapModuleRepository.save(module);
        }
    }

    private void recalculateRoadmapProgress(Roadmap roadmap, Long userId) {
        List<RoadmapModule> allModules = roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
        List<Long> modIds = allModules.stream().map(RoadmapModule::getId).toList();
        if (modIds.isEmpty()) return;

        List<RoadmapLesson> allLessons = roadmapLessonRepository.findByModuleIdInOrderByOrderIndexAsc(modIds);
        List<Long> allLessonIds = allLessons.stream().map(RoadmapLesson::getId).toList();

        long completedCount = lessonProgressRepository.findByUserIdAndLessonIdIn(userId, allLessonIds)
                .stream()
                .filter(LessonProgress::isCompleted)
                .count();

        int overallPct = (allLessons.size() > 0)
                ? (int) Math.round(((double) completedCount / allLessons.size()) * 100)
                : 0;

        roadmap.setOverallProgressPercentage(overallPct);
        roadmapRepository.save(roadmap);
    }

    private void updateDailyActivityLog(User user) {
        LocalDate today = LocalDate.now();
        Optional<UserActivityLog> existingLog = userActivityLogRepository.findByUserIdAndActivityDate(user.getId(), today);
        int lessonStudyMinutes = 15; // Defensible bite-sized lesson duration

        if (existingLog.isPresent()) {
            UserActivityLog log = existingLog.get();
            log.setLessonsCompleted(log.getLessonsCompleted() + 1);
            log.setMinutesSpent(log.getMinutesSpent() + lessonStudyMinutes);
            userActivityLogRepository.save(log);
        } else {
            UserActivityLog newLog = new UserActivityLog(user, today, lessonStudyMinutes, 1, true);
            userActivityLogRepository.save(newLog);
        }
    }

    private void updateLearnerProfileStreakAndXp(User user) {
        Optional<LearnerProfile> profileOpt = learnerProfileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) return;

        LearnerProfile profile = profileOpt.get();
        LocalDate today = LocalDate.now();
        OffsetDateTime lastActive = profile.getLastActiveAt();

        if (lastActive == null) {
            profile.setCurrentStreakDays(1);
        } else {
            LocalDate lastDate = lastActive.toLocalDate();
            if (lastDate.equals(today)) {
                // Same day activity: maintain current streak
            } else if (lastDate.equals(today.minusDays(1))) {
                // Consecutive calendar day: increment streak
                profile.setCurrentStreakDays(profile.getCurrentStreakDays() + 1);
            } else {
                // Inactivity gap > 1 day: reset streak to 1
                profile.setCurrentStreakDays(1);
            }
        }

        profile.setTotalXp(profile.getTotalXp() + 50); // 50 XP per lesson completion
        profile.setLastActiveAt(OffsetDateTime.now());
        learnerProfileRepository.save(profile);
    }

    private RoadmapProgressResponseDto computeRoadmapProgress(Roadmap roadmap, Long userId) {
        List<RoadmapModule> modules = roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());
        if (modules.isEmpty()) {
            return new RoadmapProgressResponseDto(
                    roadmap.getId(),
                    userId,
                    roadmap.getLearnerGoal() != null ? roadmap.getLearnerGoal().getId() : null,
                    roadmap.getLearnerGoal() != null && roadmap.getLearnerGoal().getSubject() != null
                            ? roadmap.getLearnerGoal().getSubject().getId() : null,
                    roadmap.getTitle(),
                    0, 0, 0, 0.0,
                    List.of()
            );
        }

        List<Long> moduleIds = modules.stream().map(RoadmapModule::getId).toList();
        List<RoadmapLesson> allLessons = roadmapLessonRepository.findByModuleIdInOrderByOrderIndexAsc(moduleIds);
        List<Long> allLessonIds = allLessons.stream().map(RoadmapLesson::getId).toList();

        List<LessonProgress> userProgressList = lessonProgressRepository.findByUserIdAndLessonIdIn(userId, allLessonIds);
        Map<Long, LessonProgress> progressByLessonId = userProgressList.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p, (a, b) -> a));

        Map<Long, List<RoadmapLesson>> lessonsByModuleId = allLessons.stream()
                .collect(Collectors.groupingBy(l -> l.getModule().getId()));

        int totalRoadmapLessons = allLessons.size();
        int totalCompletedRoadmapLessons = 0;

        List<ModuleProgressResponseDto> moduleDtos = new ArrayList<>();
        for (RoadmapModule mod : modules) {
            List<RoadmapLesson> modLessons = lessonsByModuleId.getOrDefault(mod.getId(), List.of());
            int modTotal = modLessons.size();
            int modCompleted = 0;

            List<LessonProgressResponseDto> lessonDtos = new ArrayList<>();
            for (RoadmapLesson l : modLessons) {
                LessonProgress p = progressByLessonId.get(l.getId());
                if (p != null && p.isCompleted()) {
                    modCompleted++;
                    totalCompletedRoadmapLessons++;
                    lessonDtos.add(LessonProgressResponseDto.fromEntity(p, "COMPLETED"));
                } else if (p != null) {
                    lessonDtos.add(LessonProgressResponseDto.fromEntity(p, "IN_PROGRESS"));
                } else {
                    lessonDtos.add(LessonProgressResponseDto.notStarted(l, userId));
                }
            }

            double modPct = (modTotal > 0)
                    ? Math.round(((double) modCompleted / modTotal) * 10000.0) / 100.0
                    : 0.0;

            String statusStr = mod.getStatus() != null ? mod.getStatus().name().toLowerCase() : "locked";

            moduleDtos.add(new ModuleProgressResponseDto(
                    mod.getId(),
                    mod.getTitle(),
                    mod.getDurationLabel(),
                    mod.getOrderIndex(),
                    statusStr,
                    mod.isCurrent(),
                    modTotal,
                    modCompleted,
                    modPct,
                    lessonDtos
            ));
        }

        double exactOverallPct = (totalRoadmapLessons > 0)
                ? Math.round(((double) totalCompletedRoadmapLessons / totalRoadmapLessons) * 10000.0) / 100.0
                : 0.0;

        return new RoadmapProgressResponseDto(
                roadmap.getId(),
                userId,
                roadmap.getLearnerGoal() != null ? roadmap.getLearnerGoal().getId() : null,
                roadmap.getLearnerGoal() != null && roadmap.getLearnerGoal().getSubject() != null
                        ? roadmap.getLearnerGoal().getSubject().getId() : null,
                roadmap.getTitle(),
                totalRoadmapLessons,
                totalCompletedRoadmapLessons,
                roadmap.getOverallProgressPercentage(),
                exactOverallPct,
                moduleDtos
        );
    }
}
