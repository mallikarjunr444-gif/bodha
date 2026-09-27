package com.bodha.config;

import com.bodha.security.JwtAuthenticationEntryPoint;
import com.bodha.security.JwtAuthenticationFilter;
import com.bodha.security.ratelimit.RateLimitingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security configuration providing BCrypt password hashing,
 * stateless JWT authentication, rate limiting, and HTTP security headers (Module N & O).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          RateLimitingFilter rateLimitingFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    /**
     * Standard BCrypt password encoder for secure one-way credential hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Disables default Spring Boot generated password logging by providing a no-op UserDetailsService.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Direct HTTP basic/form authentication is disabled");
        };
    }

    /**
     * Stateless HTTP security filter chain.
     * Public endpoints:
     * - /api/auth/** (Registration, Login)
     * - GET /api/domains/** (Public catalog)
     * - GET /api/subjects/** (Public catalog)
     * - GET /api/health, /actuator/health (Health telemetry)
     *
     * Protected endpoints:
     * - /api/** (All learner profile, goal, assessment, skill gap, roadmap, progress, AI endpoints)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(permissions -> permissions.policy("camera=(), microphone=(), geolocation=()"))
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication and onboarding endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Public catalog browse endpoints
                .requestMatchers(HttpMethod.GET, "/api/domains/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                // Public health monitoring endpoints
                .requestMatchers(HttpMethod.GET, "/api/health", "/actuator/health").permitAll()
                // All other API endpoints require authenticated JWT identity
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
