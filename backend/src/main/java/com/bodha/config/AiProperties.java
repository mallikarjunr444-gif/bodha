package com.bodha.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for BODHA's AI Personalization Engine (Module H).
 * Binds properties defined under `bodha.ai` in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "bodha.ai")
public class AiProperties {

    /**
     * Active AI provider: 'mock', 'gemini', or 'fallback'.
     */
    private String provider = "mock";

    /**
     * API key for external LLM provider. Bound from AI_API_KEY environment variable.
     * Never hard-coded or logged.
     */
    private String apiKey = "";

    /**
     * LLM model identifier (e.g. 'gemini-1.5-flash').
     */
    private String model = "gemini-1.5-flash";

    /**
     * Base endpoint URI for the LLM REST API.
     */
    private String endpoint = "https://generativelanguage.googleapis.com/v1beta/models";

    /**
     * Network request timeout in milliseconds.
     */
    private int timeoutMs = 5000;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean hasValidApiKey() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
