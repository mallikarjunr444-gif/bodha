package com.bodha.controller;

import com.bodha.dto.SkillGapAnalysisResponseDto;
import com.bodha.dto.SkillGapResponseDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Skill Gap Engine (Module E & N).
 * Enforces authenticated server-side user identity for skill-gap telemetry.
 */
@RestController
@RequestMapping("/api")
public class SkillGapController {

    private final SkillGapService skillGapService;
    private final CurrentUserService currentUserService;

    public SkillGapController(SkillGapService skillGapService,
                              CurrentUserService currentUserService) {
        this.skillGapService = skillGapService;
        this.currentUserService = currentUserService;
    }

    /**
     * POST /api/attempts/{attemptId}/skill-gaps/analyze
     * Analyzes a completed assessment attempt and persists or updates the learner's skill-gap matrix,
     * validating attempt ownership against the authenticated user.
     */
    @PostMapping("/attempts/{attemptId}/skill-gaps/analyze")
    public ResponseEntity<SkillGapAnalysisResponseDto> analyzeSkillGaps(
            @PathVariable Long attemptId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        SkillGapAnalysisResponseDto response = skillGapService.analyzeAttempt(attemptId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/skill-gaps/user/{userId}
     * Retrieves all persistent skill gaps for the specified learner, validating identity.
     */
    @GetMapping("/skill-gaps/user/{userId}")
    public ResponseEntity<List<SkillGapResponseDto>> getSkillGapsByUser(@PathVariable Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access skill gaps for user ID " + userId);
        }
        List<SkillGapResponseDto> response = skillGapService.getSkillGapsByUserId(currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/skill-gaps/user/{userId}/subject/{subjectId}
     * Retrieves skill gaps for a learner filtered by subject track, validating identity.
     */
    @GetMapping("/skill-gaps/user/{userId}/subject/{subjectId}")
    public ResponseEntity<List<SkillGapResponseDto>> getSkillGapsByUserAndSubject(
            @PathVariable Long userId,
            @PathVariable String subjectId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access skill gaps for user ID " + userId);
        }
        List<SkillGapResponseDto> response = skillGapService.getSkillGapsByUserIdAndSubjectId(currentUserId, subjectId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/skill-gaps/user/{userId}/goal/{goalId}
     * Retrieves skill gaps for a learner filtered by goal ID, validating identity.
     */
    @GetMapping("/skill-gaps/user/{userId}/goal/{goalId}")
    public ResponseEntity<List<SkillGapResponseDto>> getSkillGapsByUserAndGoal(
            @PathVariable Long userId,
            @PathVariable Long goalId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access skill gaps for user ID " + userId);
        }
        List<SkillGapResponseDto> response = skillGapService.getSkillGapsByUserIdAndGoalId(currentUserId, goalId);
        return ResponseEntity.ok(response);
    }
}
