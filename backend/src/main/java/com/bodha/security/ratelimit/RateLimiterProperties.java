package com.bodha.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for application rate limiting (Module O).
 */
@Component
public class RateLimiterProperties {

    @Value("${bodha.security.rate-limiting.enabled:true}")
    private boolean enabled = true;

    @Value("${bodha.security.rate-limiting.login-rpm:10}")
    private int loginRpm = 10;

    @Value("${bodha.security.rate-limiting.register-rpm:5}")
    private int registerRpm = 5;

    @Value("${bodha.security.rate-limiting.ai-rpm:20}")
    private int aiRpm = 20;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLoginRpm() {
        return loginRpm;
    }

    public void setLoginRpm(int loginRpm) {
        this.loginRpm = loginRpm;
    }

    public int getRegisterRpm() {
        return registerRpm;
    }

    public void setRegisterRpm(int registerRpm) {
        this.registerRpm = registerRpm;
    }

    public int getAiRpm() {
        return aiRpm;
    }

    public void setAiRpm(int aiRpm) {
        this.aiRpm = aiRpm;
    }
}
