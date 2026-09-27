package com.bodha.service.ai.provider;

import com.bodha.config.AiProperties;
import com.bodha.service.ai.AiProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Production implementation of AiProvider integrating with Google Gemini REST API (Module H).
 *
 * Uses standard Java 21 HttpClient and Jackson ObjectMapper with zero external heavy SDKs.
 * Secure: Never logs API keys, adheres strictly to timeout boundaries, and allows
 * automatic fallback on network/quota failure.
 */
@Component
public class GeminiAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiProvider.class);

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAiProvider(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                .build();
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return aiProperties.hasValidApiKey();
    }

    @Override
    public String generateContent(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            throw new IllegalStateException("Gemini API key is not configured. Supply via AI_API_KEY environment variable.");
        }

        try {
            String url = String.format("%s/%s:generateContent?key=%s",
                    aiProperties.getEndpoint(),
                    aiProperties.getModel(),
                    aiProperties.getApiKey());

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(Map.of("text", userPrompt))
                            )
                    ),
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.2
                    )
            );

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API request failed with HTTP status: {}", response.statusCode());
                throw new RuntimeException("Gemini API error (HTTP " + response.statusCode() + "): " + response.body());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }

            throw new RuntimeException("Gemini API returned empty candidate response");

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Exception during Gemini API invocation: {}", e.getMessage());
            throw new RuntimeException("Failed to generate content from Gemini API: " + e.getMessage(), e);
        }
    }
}
