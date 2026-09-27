package com.bodha.dto;

import com.bodha.model.User;

/**
 * Safe authenticated user response DTO.
 * Returns verified learner profile data and signed JWT token.
 * Guarantees password and password hashes are never exposed.
 */
public record AuthResponseDto(
    Long id,
    String email,
    String fullName,
    String role,
    String token,
    String message
) {
    public static AuthResponseDto fromUser(User user, String token, String message) {
        if (user == null) return null;
        return new AuthResponseDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole() != null ? user.getRole().name() : "LEARNER",
            token,
            message
        );
    }

    public static AuthResponseDto fromUser(User user, String message) {
        return fromUser(user, null, message);
    }
}
