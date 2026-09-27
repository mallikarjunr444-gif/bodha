package com.bodha;

import com.bodha.config.AiProperties;
import com.bodha.dto.*;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.*;
import com.bodha.repository.*;
import com.bodha.service.ai.AiPersonalizationService;
import com.bodha.service.ai.AiPromptBuilder;
import com.bodha.service.ai.AiProvider;
import com.bodha.service.ai.DeterministicAiFallback;
import com.bodha.service.ai.LearnerAiContextBuilder;
import com.bodha.service.ai.provider.MockAiProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests verifying BODHA's AI Personalization Foundation (Module H).
 *
 * Covers all 10 required verification scenarios:
 * 1. Learner context construction
 * 2. High-severity skill gap presence
 * 3. Mastered skill representation
 * 4. Current roadmap module identification
 * 5. Completed lesson identification
 * 6. Wrong-user access rejection
 * 7. Missing goal rejection
 * 8. Missing lesson rejection
 * 9. Resilient fallback activation on provider failure
 * 10. Privacy/data minimization (zero password, hash, or secret leakage)
 */
@ExtendWith(MockitoExtension.class)
class AiPersonalizationTests {

    @Mock private UserRepository userRepository;
    @Mock private LearnerProfileRepository learnerProfileRepository;
    @Mock private LearnerGoalRepository learnerGoalRepository;
    @Mock private AssessmentAttemptRepository assessmentAttemptRepository;
    @Mock private LearnerSkillGapRepository learnerSkillGapRepository;
    @Mock private RoadmapRepository roadmapRepository;
    @Mock private RoadmapModuleRepository roadmapModuleRepository;
    @Mock private RoadmapLessonRepository roadmapLessonRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private UserActivityLogRepository userActivityLogRepository;

    private ObjectMapper objectMapper;
    private LearnerAiContextBuilder contextBuilder;
    private AiPromptBuilder promptBuilder;
    private DeterministicAiFallback deterministicFallback;
    private AiProperties aiProperties;
    private MockAiProvider mockAiProvider;
    private AiPersonalizationService personalizationService;

    private User sampleUser;
    private Domain sampleDomain;
    private Subject sampleSubject;
    private LearnerGoal sampleGoal;
    private LearnerProfile sampleProfile;
    private Skill highGapSkill;
    private Skill masteredSkill;
    private LearnerSkillGap gapRecord;
    private LearnerSkillGap masteredRecord;
    private Roadmap sampleRoadmap;
    private RoadmapModule module1;
    private RoadmapModule module2;
    private RoadmapLesson lesson1;
    private RoadmapLesson lesson2;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        contextBuilder = new LearnerAiContextBuilder(
                userRepository,
                learnerProfileRepository,
                learnerGoalRepository,
                assessmentAttemptRepository,
                learnerSkillGapRepository,
                roadmapRepository,
                roadmapModuleRepository,
                roadmapLessonRepository,
                lessonProgressRepository,
                userActivityLogRepository
        );

        promptBuilder = new AiPromptBuilder(objectMapper);
        deterministicFallback = new DeterministicAiFallback();
        aiProperties = new AiProperties();
        aiProperties.setProvider("mock");
        mockAiProvider = new MockAiProvider();

        personalizationService = new AiPersonalizationService(
                contextBuilder,
                promptBuilder,
                deterministicFallback,
                List.of(mockAiProvider),
                aiProperties,
                objectMapper,
                roadmapLessonRepository
        );

        // Build domain models
        sampleUser = new User("arjun.patel@bodha.ai", "$2a$10$hashedPasswordValueXYZ", "Arjun K. Patel", UserRole.LEARNER);
        ReflectionTestUtils.setField(sampleUser, "id", 1L);

        sampleProfile = new LearnerProfile(sampleUser);
        sampleProfile.setCurrentStreakDays(4);
        sampleProfile.setTotalXp(250);
        sampleProfile.setBio("Backend developer preparing for system architecture.");

        sampleDomain = new Domain("programming", "Software & Engineering", "Backend tracks");
        sampleSubject = new Subject("java-backend", sampleDomain, "Full-Stack Java Architecture", DifficultyLevel.INTERMEDIATE);

        sampleGoal = new LearnerGoal(sampleUser, sampleSubject, GoalType.CAREER, BaselineLevel.INTERMEDIATE, 45);
        ReflectionTestUtils.setField(sampleGoal, "id", 1L);

        highGapSkill = new Skill(sampleSubject, "Spring Core & IoC", SkillCategory.CORE);
        ReflectionTestUtils.setField(highGapSkill, "id", 2L);

        masteredSkill = new Skill(sampleSubject, "Core Java OOP", SkillCategory.CORE);
        ReflectionTestUtils.setField(masteredSkill, "id", 1L);

        gapRecord = new LearnerSkillGap(sampleUser, sampleGoal, highGapSkill, LearnerSkillGap.Status.GAP,
                LearnerSkillGap.Severity.HIGH, "Scored 0% in diagnostic assessment", null);

        masteredRecord = new LearnerSkillGap(sampleUser, sampleGoal, masteredSkill, LearnerSkillGap.Status.MASTERED,
                null, "Demonstrated 100% baseline mastery", null);

        sampleRoadmap = new Roadmap(sampleUser, sampleGoal, "Java Backend Roadmap");
        ReflectionTestUtils.setField(sampleRoadmap, "id", 1L);
        sampleRoadmap.setOverallProgressPercentage(27);

        module1 = new RoadmapModule(sampleRoadmap, "Module 1: Spring Core", "Week 1-2", 1);
        ReflectionTestUtils.setField(module1, "id", 1L);
        module1.setStatus(RoadmapModule.Status.COMPLETED);
        module1.setCurrent(false);

        module2 = new RoadmapModule(sampleRoadmap, "Module 2: RESTful APIs", "Week 3", 2);
        ReflectionTestUtils.setField(module2, "id", 2L);
        module2.setStatus(RoadmapModule.Status.UNLOCKED);
        module2.setCurrent(true);

        lesson1 = new RoadmapLesson(module1, "Why IoC matters", RoadmapLesson.LessonType.THEORY_AND_CODE, 1);
        ReflectionTestUtils.setField(lesson1, "id", 1L);

        lesson2 = new RoadmapLesson(module2, "@RestController and Request Mappings", RoadmapLesson.LessonType.HANDS_ON_LAB, 1);
        ReflectionTestUtils.setField(lesson2, "id", 2L);
    }

    private void stubStandardContext() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(learnerGoalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));
        when(learnerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(sampleProfile));
        when(assessmentAttemptRepository.findByUserIdAndLearnerGoalIdOrderByCompletedAtDesc(1L, 1L))
                .thenReturn(List.of(new AssessmentAttempt(sampleUser, null, sampleGoal, BigDecimal.valueOf(80.0), 5, 4)));
        when(learnerSkillGapRepository.findByUserIdAndLearnerGoalId(1L, 1L))
                .thenReturn(List.of(gapRecord, masteredRecord));
        when(roadmapRepository.findByLearnerGoalId(1L)).thenReturn(Optional.of(sampleRoadmap));
        when(roadmapModuleRepository.findByRoadmapIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(module1, module2));
        when(roadmapLessonRepository.findByModuleIdInOrderByOrderIndexAsc(List.of(1L, 2L)))
                .thenReturn(List.of(lesson1, lesson2));

        LessonProgress progress1 = new LessonProgress(sampleUser, lesson1, true, OffsetDateTime.now());
        when(lessonProgressRepository.findByUserIdAndLessonIdIn(eq(1L), anyList()))
                .thenReturn(List.of(progress1));
        when(lessonProgressRepository.countByUserIdAndIsCompletedTrue(1L)).thenReturn(1L);
        when(userActivityLogRepository.findByUserIdAndActivityDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(new UserActivityLog(sampleUser, LocalDate.now(), 45, 1, true)));
    }

    @Test
    @DisplayName("1. Learner context construction builds correct multi-module structure")
    void testLearnerContextConstruction() {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);

        assertNotNull(context);
        assertEquals(1L, context.userId());
        assertEquals("Arjun K. Patel", context.displayName());
        assertEquals(4, context.profile().currentStreakDays());
        assertEquals(250, context.profile().totalXp());
        assertEquals("java-backend", context.goal().subjectId());
        assertEquals(45, context.goal().dailyStudyMinutes());
        assertNotNull(context.assessment());
        assertEquals(80.0, context.assessment().scorePercentage());
        assertEquals(1, context.activity().totalCompletedLessons());
    }

    @Test
    @DisplayName("2. High-severity skill gap appears accurately in AI context")
    void testHighSeveritySkillGapAppearsInAiContext() {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);

        assertNotNull(context.skillGaps());
        assertEquals(1, context.skillGaps().highSeverityGaps().size());
        LearnerAiContext.SkillSummary highGap = context.skillGaps().highSeverityGaps().get(0);
        assertEquals("Spring Core & IoC", highGap.name());
        assertEquals("GAP", highGap.status());
        assertEquals("HIGH", highGap.severity());
    }

    @Test
    @DisplayName("3. Mastered skill appears accurately in AI context")
    void testMasteredSkillAppearsCorrectly() {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);

        assertNotNull(context.skillGaps());
        assertEquals(1, context.skillGaps().masteredSkills().size());
        LearnerAiContext.SkillSummary mastered = context.skillGaps().masteredSkills().get(0);
        assertEquals("Core Java OOP", mastered.name());
        assertEquals("MASTERED", mastered.status());
        assertNull(mastered.severity());
    }

    @Test
    @DisplayName("4. Current roadmap module is correctly identified in AI context")
    void testCurrentRoadmapModuleIsCorrectlyIdentified() {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);

        assertNotNull(context.roadmap());
        assertNotNull(context.roadmap().currentModule());
        assertEquals(2L, context.roadmap().currentModule().moduleId());
        assertEquals("Module 2: RESTful APIs", context.roadmap().currentModule().title());
        assertEquals("UNLOCKED", context.roadmap().currentModule().status());
    }

    @Test
    @DisplayName("5. Completed vs remaining lessons are correctly identified in AI context")
    void testCompletedLessonsAreCorrectlyIdentified() {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);

        assertEquals(2, context.roadmap().totalLessons());
        assertEquals(1, context.roadmap().completedLessonsCount());
        assertEquals(1, context.roadmap().remainingLessonsCount());
        assertEquals(27, context.roadmap().overallProgressPercentage());
    }

    @Test
    @DisplayName("6. Wrong-user access is rejected with Unauthorized error")
    void testWrongUserAccessIsRejected() {
        when(userRepository.findById(999L)).thenReturn(Optional.of(new User("other@bodha.ai", "hash", "Other", UserRole.LEARNER)));
        when(learnerGoalRepository.findById(1L)).thenReturn(Optional.of(sampleGoal));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                contextBuilder.buildContext(999L, 1L));

        assertTrue(ex.getMessage().contains("Unauthorized"));
    }

    @Test
    @DisplayName("7. Missing goal is rejected with ResourceNotFoundException")
    void testMissingGoalIsRejected() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(learnerGoalRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                contextBuilder.buildContext(1L, 9999L));
    }

    @Test
    @DisplayName("8. Missing lesson is rejected with ResourceNotFoundException")
    void testMissingLessonIsRejected() {
        when(roadmapLessonRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                contextBuilder.buildLessonContext(9999L, 1L));
    }

    @Test
    @DisplayName("9. Provider failure triggers deterministic fallback seamlessly")
    void testProviderFailureTriggersDeterministicFallback() {
        stubStandardContext();

        // Create a failing provider
        AiProvider failingProvider = mock(AiProvider.class);
        when(failingProvider.getProviderName()).thenReturn("failing");
        when(failingProvider.isAvailable()).thenReturn(true);
        when(failingProvider.generateContent(anyString(), anyString()))
                .thenThrow(new RuntimeException("Simulated upstream network timeout"));

        AiPersonalizationService serviceWithFailingProvider = new AiPersonalizationService(
                contextBuilder,
                promptBuilder,
                deterministicFallback,
                List.of(failingProvider),
                aiProperties,
                objectMapper,
                roadmapLessonRepository
        );
        aiProperties.setProvider("failing");

        AiRecommendationResponseDto result = serviceWithFailingProvider.generateRecommendation(1L, 1L);

        assertNotNull(result);
        assertTrue(result.fallbackUsed());
        assertEquals("deterministic-fallback", result.provider());
        assertNotNull(result.title());
        assertNotNull(result.suggestedAction());
        assertFalse(result.practiceSuggestions().isEmpty());
    }

    @Test
    @DisplayName("10. No password, hash, or security secrets appear in serialized AI context")
    void testNoPasswordOrHashInAiContext() throws Exception {
        stubStandardContext();

        LearnerAiContext context = contextBuilder.buildContext(1L, 1L);
        String json = objectMapper.writeValueAsString(context).toLowerCase();

        assertFalse(json.contains("password"), "Context must not contain 'password'");
        assertFalse(json.contains("hash"), "Context must not contain 'hash'");
        assertFalse(json.contains("secret"), "Context must not contain 'secret'");
        assertFalse(json.contains("token"), "Context must not contain 'token'");
        assertFalse(json.contains("email"), "Context must not contain 'email'");
    }
}
