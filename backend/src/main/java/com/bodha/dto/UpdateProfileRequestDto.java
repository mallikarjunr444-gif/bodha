package com.bodha.dto;

import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a learner's profile (PUT /api/profile/{userId}).
 */
public record UpdateProfileRequestDto(
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    String fullName,

    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    String bio,

    @Size(max = 500, message = "Avatar URL cannot exceed 500 characters")
    String avatarUrl
) {}
