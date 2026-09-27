package com.bodha.service.ai;

import com.bodha.dto.LearnerAiContext;
import com.bodha.dto.LessonAiContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Constructs prompt templates enforcing strict behavioral guardrails, data boundaries,
 * and JSON output schemas for BODHA's AI engine (Module H).
 */
@Component
public class AiPromptBuilder {

    private final ObjectMapper objectMapper;

    public AiPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Builds the foundational system prompt governing AI persona, factual boundaries,
     * and anti-hallucination guardrails.
     */
    public String buildSystemPrompt() {
        return """
                You are BODHA's dedicated AI learning assistant.
                You are NOT a generic open-domain chatbot.
                
                CRITICAL INSTRUCTIONS:
                1. Use ONLY the supplied structured learner context.
                2. Do NOT invent learner scores, assessment attempts, or completed lessons.
                3. Do NOT claim a skill is mastered unless the supplied context explicitly lists it under 'masteredSkills'.
                4. Always prioritize actual identified skill gaps, particularly 'highSeverityGaps'.
                5. Respect the learner's daily study time commitment (e.g. 15, 30, 45, or 60 minutes).
                6. Respect the sequential roadmap progression; do NOT recommend skipping prerequisite modules.
                7. If information is unavailable or unverified, state that clearly rather than hallucinating details.
                8. You must respond ONLY with a valid, parseable JSON object matching the requested schema.
                9. Do NOT wrap your JSON in markdown code blocks like ```json or ```. Return pure JSON text only.
                """;
    }

    /**
     * Constructs prompt payload for personalized learning recommendations.
     */
    public String buildRecommendationPrompt(LearnerAiContext context) {
        String contextJson = serializeContext(context);
        return """
                ==================================================
                LEARNER CONTEXT
                ==================================================
                %s
                
                ==================================================
                USER REQUEST / TASK
                ==================================================
                Based exclusively on the learner's goal, baseline level, diagnostic assessment results,
                skill gaps (especially high-severity gaps), and current roadmap progress:
                Generate a personalized learning recommendation indicating what they should focus on next,
                why it matters, which skill gap it targets, and actionable practice suggestions.
                
                Respond ONLY with a JSON object conforming exactly to this structure:
                {
                  "title": "Concise headline (e.g. Focus on Spring Core & Dependency Injection)",
                  "summary": "1-2 sentence overview of the immediate pedagogical priority",
                  "reason": "Explain how this connects to their diagnostic gap score and roadmap position",
                  "priority": "HIGH" | "MEDIUM" | "LOW",
                  "relatedSkill": "Name of the target skill competency",
                  "relatedLesson": "Title of the recommended lesson to study next",
                  "suggestedAction": "Concrete immediate action the learner should take",
                  "learningApproach": "Recommended learning strategy tailored to their daily study time",
                  "practiceSuggestions": [
                    "Actionable practice exercise 1",
                    "Actionable practice exercise 2",
                    "Actionable practice exercise 3"
                  ]
                }
                """.formatted(contextJson);
    }

    /**
     * Constructs prompt payload for lesson-specific tutoring and assistance.
     */
    public String buildLessonAssistancePrompt(LearnerAiContext context, LessonAiContext lessonContext) {
        String contextJson = serializeContext(context);
        String lessonJson = serializeContext(lessonContext);

        return """
                ==================================================
                LEARNER CONTEXT
                ==================================================
                %s
                
                ==================================================
                CURRENT LESSON
                ==================================================
                %s
                
                ==================================================
                USER REQUEST / TASK
                ==================================================
                Provide pedagogical tutoring assistance tailored specifically to the CURRENT LESSON above,
                taking into consideration the learner's baseline knowledge, diagnostic skill gaps, and target goal.
                
                Explain the key concept clearly, provide learning tips, highlight common traps/mistakes learners make,
                and give hands-on practice ideas.
                
                Respond ONLY with a JSON object conforming exactly to this structure:
                {
                  "explanation": "Clear, intuitive pedagogical explanation of the lesson topic",
                  "learningTips": [
                    "Practical tip 1 to master this topic",
                    "Practical tip 2 to master this topic"
                  ],
                  "commonMistakes": [
                    "Common pitfall 1 to avoid",
                    "Common pitfall 2 to avoid"
                  ],
                  "practiceSuggestions": [
                    "Hands-on coding exercise 1",
                    "Hands-on coding exercise 2"
                  ]
                }
                """.formatted(contextJson, lessonJson);
    }

    /**
     * Constructs prompt payload for immediate next-step recommendation.
     */
    public String buildNextStepPrompt(LearnerAiContext context) {
        String contextJson = serializeContext(context);

        return """
                ==================================================
                LEARNER CONTEXT
                ==================================================
                %s
                
                ==================================================
                USER REQUEST / TASK
                ==================================================
                Determine the learner's single immediate next step based on their active roadmap, current milestone,
                and lesson completion state.
                
                Respond ONLY with a JSON object conforming exactly to this structure:
                {
                  "title": "Next Step: Action Title",
                  "summary": "1-2 sentence description of what to do next",
                  "currentModuleTitle": "Name of the active milestone module",
                  "nextLessonTitle": "Name of the specific lesson to start or continue",
                  "suggestedAction": "Direct call to action (e.g. Start lesson: ...)",
                  "reason": "Pedagogical explanation linking this step to prerequisites and daily study time"
                }
                """.formatted(contextJson);
    }

    private String serializeContext(Object context) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return String.valueOf(context);
        }
    }
}
