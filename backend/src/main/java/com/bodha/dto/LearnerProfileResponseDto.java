package com.bodha.dto;

import com.bodha.model.LearnerProfile;
import com.bodha.model.User;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object representing a learner's profile, study streak, and XP metrics (Module A).
 */
public record LearnerProfileResponseDto(
    Long profileId,
    Long userId,
    String email,
    String fullName,
    String role,
    String bio,
    String avatarUrl,
    int currentStreakDays,
    int totalXp,
    OffsetDateTime lastActiveAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
    public static LearnerProfileResponseDto fromEntity(LearnerProfile profile) {
        if (profile == null) return null;
        User user = profile.getUser();
        return new LearnerProfileResponseDto(
            profile.getId(),
            user != null ? user.getId() : null,
            user != null ? user.getEmail() : null,
            user != null ? user.getFullName() : null,
            user != null && user.getRole() != null ? user.getRole().name() : "LEARNER",
            profile.getBio(),
            profile.getAvatarUrl(),
            profile.getCurrentStreakDays(),
            profile.getTotalXp(),
            profile.getLastActiveAt(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
