package com.bodha.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service providing clean, server-side retrieval of the authenticated user's identity.
 * Replaces client-supplied userId with trusted authentication context.
 */
@Service
public class CurrentUserService {

    /**
     * Retrieves the authenticated user principal from the security context if present.
     */
    public Optional<AuthenticatedUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /**
     * Retrieves the authenticated user ID if an authenticated session exists.
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(AuthenticatedUser::getUserId);
    }

    /**
     * Retrieves the authenticated user ID, throwing an exception if not authenticated.
     */
    public Long requireCurrentUserId() {
        return getCurrentUserId().orElseThrow(() ->
                new IllegalArgumentException("Unauthorized: Authentication required to perform this action"));
    }

    /**
     * Retrieves the authenticated user's email if available.
     */
    public Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(AuthenticatedUser::getEmail);
    }

    /**
     * Checks whether the given targetUserId matches the authenticated user's ID.
     */
    public boolean isCurrentUser(Long targetUserId) {
        return targetUserId != null && getCurrentUserId().map(targetUserId::equals).orElse(false);
    }
}
