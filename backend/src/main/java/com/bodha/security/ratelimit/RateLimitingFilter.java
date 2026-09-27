package com.bodha.security.ratelimit;

import com.bodha.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Servlet filter enforcing rate limits on authentication and AI endpoints (Module O).
 *
 * Protected routes:
 * - POST /api/auth/login : Per IP (mitigates brute-force credential stuffing)
 * - POST /api/auth/register : Per IP (mitigates automated fake account creation)
 * - /api/ai/** : Per authenticated userId (or IP if unauthenticated) (mitigates LLM quota abuse)
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final InMemoryRateLimiter rateLimiter;
    private final RateLimiterProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitingFilter(InMemoryRateLimiter rateLimiter, RateLimiterProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        String limitKey = null;
        int capacity = 0;
        int rpm = 0;
        String limitMessage = null;

        String clientIp = extractClientIp(request);

        if ("POST".equalsIgnoreCase(method) && path.equals("/api/auth/login")) {
            limitKey = "login:" + clientIp;
            capacity = properties.getLoginRpm();
            rpm = properties.getLoginRpm();
            limitMessage = "Too many login attempts. Please wait a moment before trying again.";
        } else if ("POST".equalsIgnoreCase(method) && path.equals("/api/auth/register")) {
            limitKey = "register:" + clientIp;
            capacity = properties.getRegisterRpm();
            rpm = properties.getRegisterRpm();
            limitMessage = "Too many registration attempts. Please wait a moment before trying again.";
        } else if (path.startsWith("/api/ai/")) {
            Long userId = extractAuthenticatedUserId();
            limitKey = "ai:" + (userId != null ? "user:" + userId : "ip:" + clientIp);
            capacity = properties.getAiRpm();
            rpm = properties.getAiRpm();
            limitMessage = "AI personalization request limit exceeded. Please wait a minute before requesting further AI insights.";
        }

        if (limitKey != null) {
            boolean permitted = rateLimiter.tryAcquire(limitKey, capacity, rpm);
            if (!permitted) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", "60");

                Map<String, Object> body = new LinkedHashMap<>();
                body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
                body.put("error", "Too Many Requests");
                body.put("message", limitMessage);
                body.put("path", path);

                objectMapper.writeValue(response.getOutputStream(), body);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Long extractAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user.getUserId();
        }
        return null;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
