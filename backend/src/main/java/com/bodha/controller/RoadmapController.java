package com.bodha.controller;

import com.bodha.dto.RoadmapResponseDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Personalized Roadmap Engine (Module F & N).
 * Enforces authenticated server-side identity on roadmap generation and retrieval.
 */
@RestController
@RequestMapping("/api")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final CurrentUserService currentUserService;

    public RoadmapController(RoadmapService roadmapService,
                             CurrentUserService currentUserService) {
        this.roadmapService = roadmapService;
        this.currentUserService = currentUserService;
    }

    /**
     * POST /api/goals/{goalId}/roadmap/generate
     * Synthesizes or retrieves an existing personalized roadmap for a learner goal,
     * enforcing goal ownership against the authenticated user.
     */
    @PostMapping("/goals/{goalId}/roadmap/generate")
    public ResponseEntity<RoadmapResponseDto> generateRoadmap(
            @PathVariable Long goalId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        RoadmapResponseDto response = roadmapService.generateRoadmap(goalId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/goals/{goalId}/roadmap
     * Retrieves the personalized roadmap associated with a specific learner goal,
     * enforcing goal ownership against the authenticated user.
     */
    @GetMapping("/goals/{goalId}/roadmap")
    public ResponseEntity<RoadmapResponseDto> getRoadmapByGoalId(
            @PathVariable Long goalId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        RoadmapResponseDto response = roadmapService.getRoadmapByGoalId(goalId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/roadmaps/{roadmapId}
     * Retrieves a personalized roadmap by its primary key ID,
     * enforcing roadmap ownership against the authenticated user.
     */
    @GetMapping("/roadmaps/{roadmapId}")
    public ResponseEntity<RoadmapResponseDto> getRoadmapById(
            @PathVariable Long roadmapId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        RoadmapResponseDto response = roadmapService.getRoadmapById(roadmapId, currentUserId);
        return ResponseEntity.ok(response);
    }
}
