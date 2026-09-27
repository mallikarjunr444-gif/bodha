package com.bodha.service;

import com.bodha.dto.CreateGoalRequestDto;
import com.bodha.dto.LearnerGoalResponseDto;
import com.bodha.dto.UpdateGoalRequestDto;
import com.bodha.exception.DuplicateGoalException;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.BaselineLevel;
import com.bodha.model.GoalStatus;
import com.bodha.model.GoalType;
import com.bodha.model.LearnerGoal;
import com.bodha.model.Subject;
import com.bodha.model.User;
import com.bodha.repository.LearnerGoalRepository;
import com.bodha.repository.SubjectRepository;
import com.bodha.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing learner goals, learning pace calibrations, and active goal states (Module C).
 *
 * Implements strict domain validations, checks existence of foreign user/subject entities,
 * enforces allowed enum/time ranges, and guarantees unique active goals per subject.
 */
@Service
@Transactional(readOnly = true)
public class LearnerGoalService {

    private final LearnerGoalRepository learnerGoalRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public LearnerGoalService(LearnerGoalRepository learnerGoalRepository,
                              UserRepository userRepository,
                              SubjectRepository subjectRepository) {
        this.learnerGoalRepository = learnerGoalRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Creates a new calibrated goal for a learner and subject.
     */
    @Transactional
    public LearnerGoalResponseDto createGoal(CreateGoalRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Goal request body cannot be null");
        }

        // 1. Verify user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        // 2. Verify subject exists
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + request.subjectId()));

        // 3. Validate and parse goalType
        GoalType goalType;
        try {
            goalType = GoalType.fromDbValue(request.goalType().trim().toLowerCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid goal type: '" + request.goalType()
                    + "'. Allowed values: career, exam, project, mastery");
        }

        // 4. Validate and parse baselineLevel
        BaselineLevel baselineLevel;
        try {
            baselineLevel = BaselineLevel.fromDbValue(request.baselineLevel().trim().toLowerCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid baseline level: '" + request.baselineLevel()
                    + "'. Allowed values: beginner, intermediate, advanced");
        }

        // 5. Validate daily study time commitment
        Integer dailyMinutes = request.resolveDailyMinutes();
        if (dailyMinutes == null || (dailyMinutes != 15 && dailyMinutes != 30 && dailyMinutes != 45 && dailyMinutes != 60)) {
            throw new IllegalArgumentException("Invalid daily time commitment: " + dailyMinutes
                    + ". Allowed values: 15, 30, 45, 60 minutes.");
        }

        // 6. Validate optional targetDate and targetLevel if provided
        if (request.targetDate() != null && request.targetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be today or in the future: " + request.targetDate());
        }
        if (request.targetLevel() != null && !request.targetLevel().isBlank()) {
            try {
                BaselineLevel.fromDbValue(request.targetLevel().trim().toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid target level: '" + request.targetLevel()
                        + "'. Allowed values: beginner, intermediate, advanced");
            }
        }

        // 7. Check for duplicate or conflicting goal for this user & subject
        Optional<LearnerGoal> existingGoalOpt = learnerGoalRepository.findByUserIdAndSubjectId(user.getId(), subject.getId());
        if (existingGoalOpt.isPresent()) {
            LearnerGoal existing = existingGoalOpt.get();
            if (existing.getStatus() == GoalStatus.ACTIVE) {
                throw new DuplicateGoalException("An active goal already exists for user ID " + user.getId()
                        + " and subject '" + subject.getId() + "'. Update or complete the existing goal instead.");
            }
            throw new DuplicateGoalException("A goal already exists for user ID " + user.getId()
                    + " and subject '" + subject.getId() + "' with status " + existing.getStatus()
                    + ". Please update the existing goal (id: " + existing.getId() + ").");
        }

        // 8. Instantiate and persist
        LearnerGoal goal = new LearnerGoal(user, subject, goalType, baselineLevel, dailyMinutes);
        goal.setStatus(GoalStatus.ACTIVE);

        LearnerGoal saved = learnerGoalRepository.save(goal);
        return LearnerGoalResponseDto.fromEntity(saved);
    }

    /**
     * Retrieves all goals established by a specific user.
     */
    public List<LearnerGoalResponseDto> getGoalsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return learnerGoalRepository.findByUserId(userId)
                .stream()
                .map(LearnerGoalResponseDto::fromEntity)
                .toList();
    }

    /**
     * Retrieves the most recently updated ACTIVE goal for a user.
     */
    public LearnerGoalResponseDto getActiveGoalByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        List<LearnerGoal> activeGoals = learnerGoalRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(userId, GoalStatus.ACTIVE);
        if (activeGoals.isEmpty()) {
            throw new ResourceNotFoundException("No active goal found for user with id: " + userId);
        }
        return LearnerGoalResponseDto.fromEntity(activeGoals.get(0));
    }

    /**
     * Retrieves a goal by its primary key ID.
     */
    public LearnerGoalResponseDto getGoalById(Long goalId) {
        LearnerGoal goal = learnerGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));
        return LearnerGoalResponseDto.fromEntity(goal);
    }

    /**
     * Updates an existing learner goal (pace, level, type, or status).
     */
    @Transactional
    public LearnerGoalResponseDto updateGoal(Long goalId, UpdateGoalRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Update request body cannot be null");
        }

        LearnerGoal goal = learnerGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + goalId));

        if (request.goalType() != null && !request.goalType().isBlank()) {
            try {
                goal.setGoalType(GoalType.fromDbValue(request.goalType().trim().toLowerCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid goal type: '" + request.goalType()
                        + "'. Allowed values: career, exam, project, mastery");
            }
        }

        if (request.baselineLevel() != null && !request.baselineLevel().isBlank()) {
            try {
                goal.setBaselineLevel(BaselineLevel.fromDbValue(request.baselineLevel().trim().toLowerCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid baseline level: '" + request.baselineLevel()
                        + "'. Allowed values: beginner, intermediate, advanced");
            }
        }

        Integer dailyMinutes = request.resolveDailyMinutes();
        if (dailyMinutes != null) {
            if (dailyMinutes != 15 && dailyMinutes != 30 && dailyMinutes != 45 && dailyMinutes != 60) {
                throw new IllegalArgumentException("Invalid daily time commitment: " + dailyMinutes
                        + ". Allowed values: 15, 30, 45, 60 minutes.");
            }
            goal.setDailyTimeMinutes(dailyMinutes);
        }

        if (request.status() != null && !request.status().isBlank()) {
            try {
                GoalStatus status = GoalStatus.valueOf(request.status().trim().toUpperCase());
                goal.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid goal status: '" + request.status()
                        + "'. Allowed values: ACTIVE, PAUSED, COMPLETED");
            }
        }

        if (request.targetDate() != null && request.targetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be today or in the future: " + request.targetDate());
        }

        if (request.targetLevel() != null && !request.targetLevel().isBlank()) {
            try {
                BaselineLevel.fromDbValue(request.targetLevel().trim().toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid target level: '" + request.targetLevel()
                        + "'. Allowed values: beginner, intermediate, advanced");
            }
        }

        LearnerGoal updated = learnerGoalRepository.save(goal);
        return LearnerGoalResponseDto.fromEntity(updated);
    }
}
