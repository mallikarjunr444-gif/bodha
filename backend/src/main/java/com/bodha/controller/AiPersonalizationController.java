package com.bodha.controller;

import com.bodha.dto.AiLessonAssistanceResponseDto;
import com.bodha.dto.AiNextStepResponseDto;
import com.bodha.dto.AiRecommendationResponseDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.ai.AiPersonalizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for BODHA's AI Personalization Engine (Module H & N).
 * Enforces authenticated server-side identity so AI queries are grounded strictly in the caller's state.
 */
@RestController
@RequestMapping("/api/ai")
public class AiPersonalizationController {

    private final AiPersonalizationService aiPersonalizationService;
    private final CurrentUserService currentUserService;

    public AiPersonalizationController(AiPersonalizationService aiPersonalizationService,
                                       CurrentUserService currentUserService) {
        this.aiPersonalizationService = aiPersonalizationService;
        this.currentUserService = currentUserService;
    }

    /**
     * Generates a personalized learning recommendation grounded in the authenticated learner's state.
     * Overrides any client-supplied userId with verified authentication identity.
     */
    @RequestMapping(value = "/recommendations", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<AiRecommendationResponseDto> getRecommendation(
            @RequestParam(required = false) Long userId,
            @RequestParam Long goalId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AiRecommendationResponseDto dto = aiPersonalizationService.generateRecommendation(currentUserId, goalId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Generates context-aware tutoring for a lesson, validating lesson ownership.
     */
    @PostMapping("/lessons/{lessonId}/assist")
    public ResponseEntity<AiLessonAssistanceResponseDto> getLessonAssistance(
            @PathVariable Long lessonId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AiLessonAssistanceResponseDto dto = aiPersonalizationService.generateLessonAssistance(lessonId, currentUserId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Recommends the authenticated learner's immediate next learning action.
     */
    @GetMapping("/next-step")
    public ResponseEntity<AiNextStepResponseDto> getNextStep(
            @RequestParam(required = false) Long userId,
            @RequestParam Long goalId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AiNextStepResponseDto dto = aiPersonalizationService.generateNextStep(currentUserId, goalId);
        return ResponseEntity.ok(dto);
    }
}
