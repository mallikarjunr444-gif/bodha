package com.bodha.service.ai;

import com.bodha.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, rule-based fallback generator for BODHA's AI Personalization Engine (Module H).
 *
 * Guarantees that BODHA remains 100% resilient and functional even if external LLMs
 * are unconfigured, rate-limited, unreachable, or encounter network timeouts.
 */
@Component
public class DeterministicAiFallback {

    public AiRecommendationResponseDto createFallbackRecommendation(LearnerAiContext context, String providerNote) {
        String highGapSkill = null;
        String highGapReason = null;

        if (context.skillGaps() != null && !context.skillGaps().highSeverityGaps().isEmpty()) {
            LearnerAiContext.SkillSummary topGap = context.skillGaps().highSeverityGaps().get(0);
            highGapSkill = topGap.name();
            highGapReason = topGap.reason();
        }

        LearnerAiContext.CurrentModuleContext currentModule = context.roadmap() != null ? context.roadmap().currentModule() : null;
        String currentModTitle = (currentModule != null) ? currentModule.title() : "Current Milestone";

        // Find next incomplete lesson
        String nextLessonTitle = "Next Curriculum Lesson";
        if (currentModule != null && currentModule.lessons() != null) {
            for (LearnerAiContext.LessonSummary l : currentModule.lessons()) {
                if (!l.isCompleted()) {
                    nextLessonTitle = l.title();
                    break;
                }
            }
        }

        String title;
        String summary;
        String reason;
        String priority;
        String relatedSkill;
        String suggestedAction;
        String learningApproach;
        List<String> practiceSuggestions = new ArrayList<>();

        if (highGapSkill != null) {
            priority = "HIGH";
            relatedSkill = highGapSkill;

            boolean inFirstModule = (currentModule != null && currentModule.orderIndex() == 1);
            if (inFirstModule) {
                title = "Priority Focus: " + highGapSkill;
                summary = "Your diagnostic assessment identified a critical competency gap in " + highGapSkill
                        + ". Your personalized roadmap prioritizes this milestone.";
                reason = (highGapReason != null) ? highGapReason
                        : "Addressing this foundational gap is mandatory before moving to subsequent milestones.";
                suggestedAction = "Work through " + nextLessonTitle + " to solidify core concepts.";
                learningApproach = "Dedicate your " + (context.goal() != null ? context.goal().dailyStudyMinutes() : 45)
                        + " daily study minutes to focused coding labs and architectural principles.";
                practiceSuggestions.add("Build a minimal proof-of-concept project demonstrating " + highGapSkill);
                practiceSuggestions.add("Review common anti-patterns and edge-case exceptions for " + highGapSkill);
                practiceSuggestions.add("Complete the milestone quiz to benchmark your retention");
            } else {
                title = "Continue Progression: " + currentModTitle;
                summary = "You have addressed your initial critical gap in " + highGapSkill
                        + ". Advance your competencies in " + currentModTitle + ".";
                reason = "Your roadmap has promoted you to " + currentModTitle + " based on your verified progress.";
                suggestedAction = "Complete the upcoming lesson: " + nextLessonTitle;
                learningApproach = "Focus on hands-on implementations and best practice reviews.";
                practiceSuggestions.add("Implement practical code examples from " + nextLessonTitle);
                practiceSuggestions.add("Integrate your new milestone concepts with previous modules");
                practiceSuggestions.add("Review API contracts and error-handling strategies");
            }
        } else {
            priority = "MEDIUM";
            relatedSkill = (context.goal() != null) ? context.goal().subjectTitle() : "General Competencies";
            title = "Advance in " + currentModTitle;
            summary = "Keep progressing through your scheduled milestone lessons in " + currentModTitle + ".";
            reason = "Steady progression maintains your consistency streak and builds full-stack mastery.";
            suggestedAction = "Study " + nextLessonTitle;
            learningApproach = "Consistent daily study sessions aligned with your target pace.";
            practiceSuggestions.add("Complete hands-on exercises in the current module");
            practiceSuggestions.add("Review code examples in " + nextLessonTitle);
            practiceSuggestions.add("Run practice tests to reinforce conceptual retention");
        }

        return new AiRecommendationResponseDto(
                title,
                summary,
                reason,
                priority,
                relatedSkill,
                nextLessonTitle,
                suggestedAction,
                learningApproach,
                practiceSuggestions,
                true,
                "deterministic-fallback"
        );
    }

    public AiLessonAssistanceResponseDto createFallbackLessonAssistance(LearnerAiContext context, LessonAiContext lesson, String providerNote) {
        String explanation = (lesson.contentBody() != null && !lesson.contentBody().isBlank())
                ? "This lesson covers " + lesson.title() + ". Core concept: " + lesson.contentBody()
                : "This lesson covers foundational and applied principles of " + lesson.title()
                + " within " + lesson.moduleTitle() + ". Focus on understanding the core mechanisms and industry patterns.";

        List<String> tips = new ArrayList<>();
        List<String> mistakes = new ArrayList<>();
        List<String> practice = new ArrayList<>();

        String type = (lesson.lessonType() != null) ? lesson.lessonType() : "Theory + Code";

        if (type.contains("Lab") || type.contains("Code")) {
            tips.add("Trace execution flow step-by-step through the code examples.");
            tips.add("Write out the syntax by hand or in your IDE rather than copy-pasting.");
            mistakes.add("Overlooking edge cases, null pointer risks, or unhandled exceptions.");
            mistakes.add("Treating framework abstractions as black boxes without inspecting the underlying lifecycle.");
            practice.add("Create an isolated unit test validating this lesson's primary behavior.");
            practice.add("Introduce a deliberate bug or faulty input to verify how the system reacts.");
        } else if (type.contains("Best Practice")) {
            tips.add("Pay close attention to why the recommended approach scales better than naive alternatives.");
            tips.add("Notice the trade-offs between performance, maintainability, and code complexity.");
            mistakes.add("Premature optimization before establishing clean separation of concerns.");
            mistakes.add("Bypassing architectural conventions for quick temporary workarounds.");
            practice.add("Refactor an existing piece of code to adopt the best practice introduced here.");
            practice.add("Document the rationale behind choosing this pattern over alternatives.");
        } else if (type.contains("Quiz")) {
            tips.add("Read each question thoroughly and identify the underlying principle being tested.");
            tips.add("Eliminate options that violate framework constraints or clean code principles.");
            mistakes.add("Rushing through questions without verifying subtle constraint differences.");
            mistakes.add("Guessing rather than reviewing the preceding theory and lab lessons.");
            practice.add("Re-read earlier lesson summaries if you are unsure of any conceptual distinctions.");
            practice.add("Explain the correct answer in your own words before looking at options.");
        } else {
            tips.add("Connect this lesson's concept back to your overall goal: " + (context.goal() != null ? context.goal().subjectTitle() : "Mastery"));
            tips.add("Take concise notes on new terminology and patterns introduced.");
            mistakes.add("Skipping foundational theory and attempting advanced labs prematurely.");
            mistakes.add("Passive reading without active recall or mental modeling.");
            practice.add("Write a 2-sentence summary explaining this topic to a beginner.");
            practice.add("Identify where this pattern is used in real-world production systems.");
        }

        return new AiLessonAssistanceResponseDto(
                lesson.lessonId(),
                lesson.title(),
                lesson.lessonType(),
                lesson.moduleTitle(),
                explanation,
                tips,
                mistakes,
                practice,
                true,
                "deterministic-fallback"
        );
    }

    public AiNextStepResponseDto createFallbackNextStep(LearnerAiContext context, String providerNote) {
        LearnerAiContext.RoadmapContext roadmap = context.roadmap();
        LearnerAiContext.CurrentModuleContext currentModule = (roadmap != null) ? roadmap.currentModule() : null;

        String currentModuleTitle = (currentModule != null) ? currentModule.title() : "Personalized Roadmap";
        int overallPct = (roadmap != null) ? roadmap.overallProgressPercentage() : 0;

        Long nextLessonId = null;
        String nextLessonTitle = null;
        int remainingInModule = 0;

        if (currentModule != null && currentModule.lessons() != null) {
            for (LearnerAiContext.LessonSummary l : currentModule.lessons()) {
                if (!l.isCompleted()) {
                    if (nextLessonId == null) {
                        nextLessonId = l.lessonId();
                        nextLessonTitle = l.title();
                    }
                    remainingInModule++;
                }
            }
        }

        String title;
        String summary;
        String suggestedAction;
        String reason;

        if (nextLessonId != null) {
            title = "Next Step: " + nextLessonTitle;
            summary = "Resume your progress in " + currentModuleTitle + ". You have "
                    + remainingInModule + " lesson" + (remainingInModule > 1 ? "s" : "") + " remaining in this milestone.";
            suggestedAction = "Start lesson: " + nextLessonTitle;
            reason = "Sequential milestone progression guarantees all conceptual prerequisites are satisfied before moving forward.";
        } else if (overallPct >= 100) {
            title = "Roadmap Completed!";
            summary = "Outstanding work! You have finished 100% of the lessons in your personalized roadmap.";
            suggestedAction = "Review portfolio projects or take a checkpoint assessment";
            reason = "You have achieved all scheduled milestones for your target goal.";
        } else {
            title = "Next Milestone Ready";
            summary = "You have completed your current module. Advance to the next unlocked milestone in your roadmap.";
            suggestedAction = "Begin the next unlocked roadmap module";
            reason = "All child lessons in the previous module are complete, unlocking the subsequent milestone.";
        }

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
                true,
                "deterministic-fallback"
        );
    }
}
