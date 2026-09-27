package com.bodha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Cross-Origin Resource Sharing (CORS) configuration for BODHA REST APIs (Module O).
 *
 * Configures allowed origins, HTTP methods, headers, and credential handling.
 * In development, defaults to allowing http://localhost:5173.
 * In production, reads comma-separated origins from environment variable CORS_ALLOWED_ORIGINS.
 * Wildcard '*' origin is strictly forbidden when credentials are enabled.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${bodha.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins = "http://localhost:5173";

    public CorsConfig() {}

    public CorsConfig(String allowedOrigins) {
        this.allowedOrigins = (allowedOrigins != null && !allowedOrigins.isBlank())
                ? allowedOrigins
                : "http://localhost:5173";
    }

    public List<String> getAllowedOriginList() {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return List.of("http://localhost:5173");
        }
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty() && !origin.equals("*"))
                .toList();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = getAllowedOriginList();

        if (origins.isEmpty()) {
            configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = getAllowedOriginList();
        String[] originArray = origins.isEmpty()
                ? new String[]{"http://localhost:5173"}
                : origins.toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(originArray)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With")
                .exposedHeaders("Authorization", "Link", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
