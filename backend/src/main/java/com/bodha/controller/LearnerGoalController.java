package com.bodha.controller;

import com.bodha.dto.CreateGoalRequestDto;
import com.bodha.dto.LearnerGoalResponseDto;
import com.bodha.dto.UpdateGoalRequestDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.LearnerGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Learner Goal Setting and Calibration (Module C & N).
 * Enforces server-side authenticated identity so users can only manage their own goals.
 */
@RestController
@RequestMapping("/api/goals")
public class LearnerGoalController {

    private final LearnerGoalService learnerGoalService;
    private final CurrentUserService currentUserService;

    public LearnerGoalController(LearnerGoalService learnerGoalService,
                                 CurrentUserService currentUserService) {
        this.learnerGoalService = learnerGoalService;
        this.currentUserService = currentUserService;
    }

    /**
     * POST /api/goals
     * Creates a new learner goal, binding it exclusively to the authenticated user ID.
     */
    @PostMapping
    public ResponseEntity<LearnerGoalResponseDto> createGoal(@Valid @RequestBody CreateGoalRequestDto request) {
        Long currentUserId = currentUserService.requireCurrentUserId();

        CreateGoalRequestDto authenticatedRequest = new CreateGoalRequestDto(
                currentUserId,
                request.subjectId(),
                request.goalType(),
                request.baselineLevel(),
                request.dailyTimeMinutes(),
                request.dailyTime(),
                request.targetLevel(),
                request.targetDate()
        );

        LearnerGoalResponseDto created = learnerGoalService.createGoal(authenticatedRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/goals/user/{userId}
     * Retrieves all goals set by the user, validating identity.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearnerGoalResponseDto>> getGoalsByUser(@PathVariable Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access goals for user ID " + userId);
        }
        List<LearnerGoalResponseDto> goals = learnerGoalService.getGoalsByUserId(currentUserId);
        return ResponseEntity.ok(goals);
    }

    /**
     * GET /api/goals/user/{userId}/active
     * Retrieves current active goal for the user, validating identity.
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<LearnerGoalResponseDto> getActiveGoalByUser(@PathVariable Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access active goal for user ID " + userId);
        }
        LearnerGoalResponseDto activeGoal = learnerGoalService.getActiveGoalByUserId(currentUserId);
        return ResponseEntity.ok(activeGoal);
    }

    /**
     * GET /api/goals/{goalId}
     * Retrieves a goal by ID, ensuring it belongs to the authenticated user.
     */
    @GetMapping("/{goalId}")
    public ResponseEntity<LearnerGoalResponseDto> getGoalById(@PathVariable Long goalId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LearnerGoalResponseDto goal = learnerGoalService.getGoalById(goalId);
        if (goal.userId() != null && !goal.userId().equals(currentUserId)) {
            throw new IllegalArgumentException("Unauthorized: Goal " + goalId + " does not belong to user ID " + currentUserId);
        }
        return ResponseEntity.ok(goal);
    }

    /**
     * PUT /api/goals/{goalId}
     * Updates an existing goal, ensuring it belongs to the authenticated user.
     */
    @PutMapping("/{goalId}")
    public ResponseEntity<LearnerGoalResponseDto> updateGoal(
            @PathVariable Long goalId,
            @RequestBody UpdateGoalRequestDto request) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LearnerGoalResponseDto existing = learnerGoalService.getGoalById(goalId);
        if (existing.userId() != null && !existing.userId().equals(currentUserId)) {
            throw new IllegalArgumentException("Unauthorized: Goal " + goalId + " does not belong to user ID " + currentUserId);
        }
        LearnerGoalResponseDto updated = learnerGoalService.updateGoal(goalId, request);
        return ResponseEntity.ok(updated);
    }
}
