package com.bodha.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service managing JWT generation, cryptographic signing (HMAC-SHA256),
 * claim extraction, and token signature/expiration validation (Module N & O).
 *
 * Production security guarantees:
 * - Requires at least 256-bit (32 character) signing secret.
 * - Rejects development fallback secret when running in production profile.
 * - Never logs or exposes raw secrets in exceptions or APIs.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    public static final int MIN_SECRET_LENGTH = 32; // 256-bit minimum key size
    public static final String DEV_FALLBACK_SECRET = "bodha-dev-secret-key-must-be-at-least-256-bits-long-32-chars";

    @Value("${bodha.jwt.secret:}")
    private String secret;

    @Value("${bodha.jwt.expiration-ms:86400000}")
    private long expirationMs = 86400000L; // 24 hours default

    @Autowired(required = false)
    private Environment environment;

    private SecretKey signingKey;

    public JwtService() {}

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    public JwtService(String secret, long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        initSigningKey();
    }

    public JwtService(String secret, long expirationMs, Environment environment) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.environment = environment;
        initSigningKey();
    }

    @PostConstruct
    public void init() {
        initSigningKey();
    }

    private void initSigningKey() {
        if (secret == null || secret.trim().length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT configuration error: 'bodha.jwt.secret' (JWT_SECRET) is missing or shorter than "
                + MIN_SECRET_LENGTH + " characters (256 bits). "
                + "Please configure the JWT_SECRET environment variable with an adequate production secret."
            );
        }

        boolean isProduction = false;
        if (environment != null && environment.getActiveProfiles() != null) {
            for (String profile : environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(profile) || "production".equalsIgnoreCase(profile)) {
                    isProduction = true;
                    break;
                }
            }
        }

        if (isProduction && DEV_FALLBACK_SECRET.equals(secret.trim())) {
            throw new IllegalStateException(
                "JWT security violation: The development fallback secret cannot be used in production profile. "
                + "A unique, cryptographically random JWT_SECRET must be supplied via environment variable."
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT with minimal identity claims (userId, email, role).
     * Excludes passwords, hashes, and unnecessary metadata.
     */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role != null ? role : "LEARNER")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates signature and checks expiration of a given JWT token string.
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("Invalid JWT signature or malformed structure: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.debug("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT token format: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("Empty or null JWT claims string: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Parses and extracts claims payload from a signed JWT.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts authenticated userId from token subject or userId claim.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number num) {
            return num.longValue();
        }
        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extracts authenticated email from token claim.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    /**
     * Extracts user role from token claim.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extracts expiration timestamp from token.
     */
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    /**
     * Checks if token has passed its expiration time.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
