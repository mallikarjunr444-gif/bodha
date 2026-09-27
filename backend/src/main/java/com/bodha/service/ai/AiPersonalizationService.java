package com.bodha.service.ai;

import com.bodha.config.AiProperties;
import com.bodha.dto.*;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.RoadmapLesson;
import com.bodha.repository.RoadmapLessonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business orchestrator for BODHA's AI Personalization Engine (Module H).
 *
 * Coordinates context gathering, prompt construction, provider invocation, JSON deserialization,
 * and resilient deterministic fallbacks.
 */
@Service
@Transactional(readOnly = true)
public class AiPersonalizationService {

    private static final Logger log = LoggerFactory.getLogger(AiPersonalizationService.class);

    private final LearnerAiContextBuilder contextBuilder;
    private final AiPromptBuilder promptBuilder;
    private final DeterministicAiFallback deterministicFallback;
    private final Map<String, AiProvider> providers;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RoadmapLessonRepository roadmapLessonRepository;

    public AiPersonalizationService(
            LearnerAiContextBuilder contextBuilder,
            AiPromptBuilder promptBuilder,
            DeterministicAiFallback deterministicFallback,
            List<AiProvider> providerList,
            AiProperties aiProperties,
            ObjectMapper objectMapper,
            RoadmapLessonRepository roadmapLessonRepository) {
        this.contextBuilder = contextBuilder;
        this.promptBuilder = promptBuilder;
        this.deterministicFallback = deterministicFallback;
        this.providers = providerList.stream()
                .collect(Collectors.toMap(p -> p.getProviderName().toLowerCase(), p -> p, (a, b) -> a));
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.roadmapLessonRepository = roadmapLessonRepository;
    }

    /**
     * Generates a context-driven learning recommendation tailored to diagnostic gaps,
     * baseline knowledge, and current roadmap progression.
     */
    public AiRecommendationResponseDto generateRecommendation(Long userId, Long goalId) {
        LearnerAiContext context = contextBuilder.buildContext(userId, goalId);

        AiProvider provider = resolveActiveProvider();
        if (provider != null && provider.isAvailable()) {
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt();
                String userPrompt = promptBuilder.buildRecommendationPrompt(context);
                String rawResponse = provider.generateContent(systemPrompt, userPrompt);

                JsonNode root = objectMapper.readTree(cleanJsonMarkdown(rawResponse));
                String title = root.path("title").asText("Personalized Learning Focus");
                String summary = root.path("summary").asText("Immediate learning recommendation based on your progress.");
                String reason = root.path("reason").asText("Derived from your diagnostic gaps and roadmap state.");
                String priority = root.path("priority").asText("HIGH");
                String relatedSkill = root.path("relatedSkill").asText("Core Architecture");
                String relatedLesson = root.path("relatedLesson").asText("Next Scheduled Lesson");
                String suggestedAction = root.path("suggestedAction").asText("Work through upcoming exercises.");
                String learningApproach = root.path("learningApproach").asText("Consistent daily practice.");

                List<String> practiceSuggestions = new ArrayList<>();
                JsonNode suggestionsNode = root.path("practiceSuggestions");
                if (suggestionsNode.isArray()) {
                    for (JsonNode item : suggestionsNode) {
                        practiceSuggestions.add(item.asText());
                    }
                }

                if (context.roadmap() != null && context.roadmap().currentModule() != null) {
                    for (LearnerAiContext.LessonSummary l : context.roadmap().currentModule().lessons()) {
                        if (!l.isCompleted()) {
                            if ("Next Scheduled Lesson".equalsIgnoreCase(relatedLesson) || "Next Lesson".equalsIgnoreCase(relatedLesson)) {
                                relatedLesson = l.title();
                            }
                            break;
                        }
                    }
                }
                if (context.skillGaps() != null && !context.skillGaps().highSeverityGaps().isEmpty()) {
                    if ("Core Architecture".equalsIgnoreCase(relatedSkill)) {
                        relatedSkill = context.skillGaps().highSeverityGaps().get(0).name();
                        title = "Priority Focus: " + relatedSkill;
                    }
                }

                return new AiRecommendationResponseDto(
                        title,
                        summary,
                        reason,
                        priority,
                        relatedSkill,
                        relatedLesson,
                        suggestedAction,
                        learningApproach,
                        practiceSuggestions,
                        false,
                        provider.getProviderName()
                );
            } catch (Exception e) {
                log.warn("AI generation failed for recommendation (userId={}, goalId={}): {}. Activating deterministic fallback.",
                        userId, goalId, e.getMessage());
            }
        }

        return deterministicFallback.createFallbackRecommendation(context, "Provider unavailable or generation failed");
    }

    /**
     * Generates context-aware tutoring assistance for a specific roadmap lesson.
     */
    public AiLessonAssistanceResponseDto generateLessonAssistance(Long lessonId, Long userId) {
        LessonAiContext lessonContext = contextBuilder.buildLessonContext(lessonId, userId);

        RoadmapLesson lesson = roadmapLessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap lesson not found with id: " + lessonId));
        Long goalId = lesson.getModule().getRoadmap().getLearnerGoal().getId();
        LearnerAiContext learnerContext = contextBuilder.buildContext(userId, goalId);

        AiProvider provider = resolveActiveProvider();
        if (provider != null && provider.isAvailable()) {
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt();
                String userPrompt = promptBuilder.buildLessonAssistancePrompt(learnerContext, lessonContext);
                String rawResponse = provider.generateContent(systemPrompt, userPrompt);

                JsonNode root = objectMapper.readTree(cleanJsonMarkdown(rawResponse));
                String explanation = root.path("explanation").asText(
                        "Core conceptual breakdown and implementation patterns for " + lessonContext.title() + ".");

                List<String> tips = extractStringList(root.path("learningTips"));
                List<String> mistakes = extractStringList(root.path("commonMistakes"));
                List<String> practice = extractStringList(root.path("practiceSuggestions"));

                return new AiLessonAssistanceResponseDto(
                        lessonContext.lessonId(),
                        lessonContext.title(),
                        lessonContext.lessonType(),
                        lessonContext.moduleTitle(),
                        explanation,
                        tips,
                        mistakes,
                        practice,
                        false,
                        provider.getProviderName()
                );
            } catch (Exception e) {
                log.warn("AI generation failed for lesson assistance (lessonId={}, userId={}): {}. Activating deterministic fallback.",
                        lessonId, userId, e.getMessage());
            }
        }

        return deterministicFallback.createFallbackLessonAssistance(learnerContext, lessonContext, "Provider unavailable or generation failed");
    }

    /**
     * Determines and returns the immediate next pedagogical step for the learner.
     */
    public AiNextStepResponseDto generateNextStep(Long userId, Long goalId) {
        LearnerAiContext context = contextBuilder.buildContext(userId, goalId);

        AiProvider provider = resolveActiveProvider();
        if (provider != null && provider.isAvailable()) {
            try {
                String systemPrompt = promptBuilder.buildSystemPrompt();
                String userPrompt = promptBuilder.buildNextStepPrompt(context);
                String rawResponse = provider.generateContent(systemPrompt, userPrompt);

                JsonNode root = objectMapper.readTree(cleanJsonMarkdown(rawResponse));
                String title = root.path("title").asText("Next Step");
                String summary = root.path("summary").asText("Continue with your roadmap progression.");
                String currentModuleTitle = root.path("currentModuleTitle").asText(
                        context.roadmap() != null && context.roadmap().currentModule() != null
                                ? context.roadmap().currentModule().title() : "Current Milestone");
                String nextLessonTitle = root.path("nextLessonTitle").asText("Next Lesson");
                String suggestedAction = root.path("suggestedAction").asText("Resume active lesson");
                String reason = root.path("reason").asText("Sequential prerequisite progression");

                // Derive exact lesson ID and progress from deterministic context
                Long nextLessonId = null;
                String actualLessonTitle = null;
                int remainingInModule = 0;
                if (context.roadmap() != null && context.roadmap().currentModule() != null) {
                    for (LearnerAiContext.LessonSummary l : context.roadmap().currentModule().lessons()) {
                        if (!l.isCompleted()) {
                            if (nextLessonId == null) {
                                nextLessonId = l.lessonId();
                                actualLessonTitle = l.title();
                            }
                            remainingInModule++;
                        }
                    }
                }

                if (actualLessonTitle != null && ("Next Lesson".equalsIgnoreCase(nextLessonTitle) || "Next Scheduled Lesson".equalsIgnoreCase(nextLessonTitle))) {
                    nextLessonTitle = actualLessonTitle;
                    suggestedAction = "Start lesson: " + actualLessonTitle;
                    title = "Next Step: " + actualLessonTitle;
                }

                int overallPct = (context.roadmap() != null) ? context.roadmap().overallProgressPercentage() : 0;

                return new AiNextStepResponseDto(
                        title,
                        summary,
                        currentModuleTitle,
                        nextLessonId,
                        nextLessonTitle,
                        suggestedAction,
                        reason,
                        overallPct,
                        remainingInModule,
                        false,
                        provider.getProviderName()
                );
            } catch (Exception e) {
                log.warn("AI generation failed for next-step (userId={}, goalId={}): {}. Activating deterministic fallback.",
                        userId, goalId, e.getMessage());
            }
        }

        return deterministicFallback.createFallbackNextStep(context, "Provider unavailable or generation failed");
    }

    private AiProvider resolveActiveProvider() {
        String configuredName = aiProperties.getProvider() != null ? aiProperties.getProvider().toLowerCase().trim() : "mock";
        AiProvider selected = providers.get(configuredName);

        if (selected == null || !selected.isAvailable()) {
            // Fall back to mock if available
            return providers.get("mock");
        }

        return selected;
    }

    private List<String> extractStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private String cleanJsonMarkdown(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
