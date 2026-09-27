package com.bodha.service.ai.provider;

import com.bodha.service.ai.AiProvider;
import org.springframework.stereotype.Component;

/**
 * High-fidelity Mock AI Provider for local testing, development, and offline environments (Module H).
 *
 * Produces structured, domain-accurate JSON responses adhering strictly to BODHA's prompt schemas
 * without requiring external paid API keys or active internet access.
 */
@Component
public class MockAiProvider implements AiProvider {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String generateContent(String systemPrompt, String userPrompt) {
        if (userPrompt.contains("CURRENT LESSON")) {
            return """
                    {
                      "explanation": "This lesson explores core architectural mechanisms and practical implementations. Understanding these concepts eliminates common anti-patterns and establishes a foundation for production-ready backend design.",
                      "learningTips": [
                        "Trace the runtime lifecycle and interaction between framework components.",
                        "Emphasize clean separation of concerns and interface-driven abstractions."
                      ],
                      "commonMistakes": [
                        "Treating framework features as magical black boxes without understanding the underlying lifecycle.",
                        "Bypassing validation and error-handling in favor of quick prototyping workarounds."
                      ],
                      "practiceSuggestions": [
                        "Build an isolated unit test validating this lesson's behavior.",
                        "Inspect console logs and debug breakpoints to observe runtime object creation."
                      ]
                    }
                    """;
        }

        if (userPrompt.toLowerCase().contains("next step") || userPrompt.toLowerCase().contains("immediate next")) {
            return """
                    {
                      "title": "Next Step: Resume Active Milestone",
                      "summary": "Continue your sequential roadmap progression by studying the next uncompleted lesson in your current milestone.",
                      "currentModuleTitle": "Active Roadmap Milestone",
                      "nextLessonTitle": "Next Scheduled Lesson",
                      "suggestedAction": "Begin the next scheduled lesson",
                      "reason": "Sequential progression ensures all prerequisite concepts are satisfied before advancing to downstream topics."
                    }
                    """;
        }

        // Default: Recommendation prompt response
        return """
                {
                  "title": "Priority Focus: Target Critical Competencies",
                  "summary": "Your diagnostic results identified targeted areas for improvement. Your personalized roadmap schedules these as your primary learning focus.",
                  "reason": "Addressing diagnostic skill gaps before advancing builds durable full-stack competencies.",
                  "priority": "HIGH",
                  "relatedSkill": "Core Architecture",
                  "relatedLesson": "Next Scheduled Lesson",
                  "suggestedAction": "Work through scheduled lesson labs to solidify core concepts",
                  "learningApproach": "Dedicate consistent daily study blocks to hands-on coding and pattern review",
                  "practiceSuggestions": [
                    "Implement a minimal working example applying the targeted concept",
                    "Review framework documentation and community best practices",
                    "Complete checkpoint quizzes to measure retention"
                  ]
                }
                """;
    }
}
