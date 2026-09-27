package com.bodha.controller;

import com.bodha.dto.LearnerProfileResponseDto;
import com.bodha.dto.UpdateProfileRequestDto;
import com.bodha.security.CurrentUserService;
import com.bodha.service.LearnerProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Learner Profile operations (Module A & N).
 * Enforces authenticated server-side identity via CurrentUserService.
 *
 * Endpoints:
 * - GET /api/profile/me     : Retrieve profile for authenticated learner
 * - GET /api/profile/{userId} : Retrieve profile verifying user ownership
 * - PUT /api/profile/{userId} : Update profile verifying user ownership
 */
@RestController
@RequestMapping("/api/profile")
public class LearnerProfileController {

    private final LearnerProfileService learnerProfileService;
    private final CurrentUserService currentUserService;

    public LearnerProfileController(LearnerProfileService learnerProfileService,
                                    CurrentUserService currentUserService) {
        this.learnerProfileService = learnerProfileService;
        this.currentUserService = currentUserService;
    }

    /**
     * GET /api/profile/me
     * Retrieves profile for the currently authenticated learner.
     */
    @GetMapping("/me")
    public ResponseEntity<LearnerProfileResponseDto> getMyProfile() {
        Long currentUserId = currentUserService.requireCurrentUserId();
        LearnerProfileResponseDto response = learnerProfileService.getProfileByUserId(currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/profile/{userId}
     * Retrieves the profile associated with the specified user ID, validating identity.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<LearnerProfileResponseDto> getProfile(@PathVariable Long userId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot access profile for user ID " + userId);
        }
        LearnerProfileResponseDto response = learnerProfileService.getProfileByUserId(currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/profile/{userId}
     * Updates profile details for the specified user ID, validating identity.
     */
    @PutMapping("/{userId}")
    public ResponseEntity<LearnerProfileResponseDto> updateProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new IllegalArgumentException("Unauthorized: Cannot update profile for user ID " + userId);
        }
        LearnerProfileResponseDto response = learnerProfileService.updateProfile(currentUserId, request);
        return ResponseEntity.ok(response);
    }
}
