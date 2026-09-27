package com.bodha.controller;

import com.bodha.dto.*;
import com.bodha.security.CurrentUserService;
import com.bodha.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Assessment Engine (Module D & N).
 * Enforces authenticated server-side user identity for test attempts and evaluations.
 */
@RestController
@RequestMapping("/api")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final CurrentUserService currentUserService;

    public AssessmentController(AssessmentService assessmentService,
                                CurrentUserService currentUserService) {
        this.assessmentService = assessmentService;
        this.currentUserService = currentUserService;
    }

    /**
     * GET /api/assessments/{assessmentId}
     * Retrieves high-level assessment information and question count.
     */
    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<AssessmentSummaryResponseDto> getAssessment(@PathVariable Long assessmentId) {
        AssessmentSummaryResponseDto response = assessmentService.getAssessmentById(assessmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/assessments/subject/{subjectId}
     * Retrieves all assessments associated with a specific subject.
     */
    @GetMapping("/assessments/subject/{subjectId}")
    public ResponseEntity<List<AssessmentSummaryResponseDto>> getAssessmentsBySubject(@PathVariable String subjectId) {
        List<AssessmentSummaryResponseDto> responses = assessmentService.getAssessmentsBySubjectId(subjectId);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/assessments/subject/{subjectId}/diagnostic
     * Retrieves the primary diagnostic baseline assessment for a subject.
     */
    @GetMapping("/assessments/subject/{subjectId}/diagnostic")
    public ResponseEntity<AssessmentSummaryResponseDto> getDiagnosticAssessment(@PathVariable String subjectId) {
        AssessmentSummaryResponseDto response = assessmentService.getDiagnosticAssessmentBySubjectId(subjectId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/assessments/{assessmentId}/attempts
     * Initiates a new assessment attempt session for the authenticated learner.
     */
    @PostMapping("/assessments/{assessmentId}/attempts")
    public ResponseEntity<AssessmentAttemptResponseDto> startAttempt(
            @PathVariable Long assessmentId,
            @Valid @RequestBody StartAttemptRequestDto request) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        StartAttemptRequestDto authenticatedRequest = new StartAttemptRequestDto(currentUserId, request.learnerGoalId());
        AssessmentAttemptResponseDto response = assessmentService.startAttempt(assessmentId, authenticatedRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/attempts/{attemptId}
     * Retrieves attempt session status and questions, enforcing user ownership.
     */
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<AssessmentAttemptResponseDto> getAttempt(
            @PathVariable Long attemptId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AssessmentAttemptResponseDto response = assessmentService.getAttemptById(attemptId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/attempts/{attemptId}/responses
     * Records or updates an answer to an assessment question.
     */
    @PostMapping("/attempts/{attemptId}/responses")
    public ResponseEntity<SubmitResponseResponseDto> submitResponse(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitResponseRequestDto request) {
        // AssessmentService.submitResponse verifies attempt ownership through question/attempt mapping
        SubmitResponseResponseDto response = assessmentService.submitResponse(attemptId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/attempts/{attemptId}/complete
     * Finalizes the attempt and calculates results, enforcing user ownership.
     */
    @PostMapping("/attempts/{attemptId}/complete")
    public ResponseEntity<AssessmentResultResponseDto> completeAssessment(
            @PathVariable Long attemptId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AssessmentResultResponseDto response = assessmentService.completeAssessment(attemptId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/attempts/{attemptId}/result
     * Retrieves final completed assessment results and skill performance breakdown, enforcing ownership.
     */
    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<AssessmentResultResponseDto> getAssessmentResult(
            @PathVariable Long attemptId,
            @RequestParam(required = false) Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        AssessmentResultResponseDto response = assessmentService.getAssessmentResult(attemptId, currentUserId);
        return ResponseEntity.ok(response);
    }
}
