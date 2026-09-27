package com.bodha.security;

import java.security.Principal;

/**
 * Represents the server-verified user principal extracted from a cryptographically signed JWT.
 * Encapsulates authenticated identity without relying on client-supplied parameters.
 */
public class AuthenticatedUser implements Principal {

    private final Long userId;
    private final String email;
    private final String role;

    public AuthenticatedUser(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role != null ? role : "LEARNER";
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getName() {
        return email != null ? email : String.valueOf(userId);
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{userId=" + userId + ", email='" + email + "', role='" + role + "'}";
    }
}
