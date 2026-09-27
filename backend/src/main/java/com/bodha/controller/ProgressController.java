package com.bodha.controller;

import com.bodha.dto.LessonProgressResponseDto;
import com.bodha.dto.ProgressSummaryResponseDto;
import com.bodha.dto.RoadmapProgressResponseDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Lesson Progress & Study Tracking (Module G & N).
 * Enforces authenticated server-side identity on all lesson study and progress metrics.
 */
@RestController
@RequestMapping("/api")
public class ProgressController {

    private final ProgressService progressService;
    private final CurrentUserService currentUserService;

    public ProgressController(ProgressService progressService,
                              CurrentUserService currentUserService) {
        this.progressService = progressService;
        this.currentUserService = currentUserService;
    }

    /**
     * POST /api/lessons/{lessonId}/start
     * Transitions a lesson from NOT_STARTED to IN_PROGRESS, verifying lesson ownership.
     */
    @PostMapping("/lessons/{lessonId}/start")
    public ResponseEntity<LessonProgressResponseDto> startLesson(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LessonProgressResponseDto response = progressService.startLesson(lessonId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/lessons/{lessonId}/progress
     * Alias endpoint to start/record lesson progress.
     */
    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressResponseDto> updateProgress(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LessonProgressResponseDto response = progressService.startLesson(lessonId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/lessons/{lessonId}/complete
     * Marks a lesson as COMPLETED, verifying lesson ownership.
     */
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<LessonProgressResponseDto> completeLesson(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LessonProgressResponseDto response = progressService.completeLesson(lessonId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/lessons/{lessonId}/progress
     * Retrieves current progress state for a specific lesson, verifying ownership.
     */
    @GetMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressResponseDto> getLessonProgress(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LessonProgressResponseDto response = progressService.getLessonProgress(lessonId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/roadmaps/{roadmapId}/progress
     * Retrieves full roadmap progress breakdown, verifying roadmap ownership.
     */
    @GetMapping("/roadmaps/{roadmapId}/progress")
    public ResponseEntity<RoadmapProgressResponseDto> getRoadmapProgress(
            @PathVariable Long roadmapId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        RoadmapProgressResponseDto response = progressService.getRoadmapProgress(roadmapId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/progress/user/{userId}
     * Retrieves dashboard progress summary for the authenticated user, validating identity.
     */
    @GetMapping("/progress/user/{userId}")
    public ResponseEntity<ProgressSummaryResponseDto> getUserProgressSummary(
            @PathVariable Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access progress summary for user ID " + userId);
        }
        ProgressSummaryResponseDto response = progressService.getUserProgressSummary(currentUserId);
        return ResponseEntity.ok(response);
    }
}
