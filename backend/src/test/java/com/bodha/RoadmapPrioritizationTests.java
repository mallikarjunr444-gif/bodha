package com.bodha;

import com.bodha.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused unit tests validating the deterministic skill-gap prioritization rules for Module F.
 */
class RoadmapPrioritizationTests {

    record SkillPriority(Skill skill, int tierRank, String tierLabel) {}

    private int calculateTierRank(LearnerSkillGap.Status status, LearnerSkillGap.Severity severity) {
        if (status == LearnerSkillGap.Status.GAP) {
            if (severity == LearnerSkillGap.Severity.HIGH) return 1;
            if (severity == LearnerSkillGap.Severity.MEDIUM) return 2;
            if (severity == LearnerSkillGap.Severity.LOW) return 3;
            return 3;
        } else if (status == null) {
            return 4; // Unevaluated required skill
        } else if (status == LearnerSkillGap.Status.MASTERED) {
            return 5; // Mastered (Accelerated track)
        }
        return 6;
    }

    @Test
    @DisplayName("High severity GAP must receive highest priority (Rank 1) over Mastered skills (Rank 5)")
    void testHighGapPriorityOverMastered() {
        int highGapRank = calculateTierRank(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.HIGH);
        int masteredRank = calculateTierRank(LearnerSkillGap.Status.MASTERED, null);

        assertEquals(1, highGapRank, "High GAP should be Tier 1");
        assertEquals(5, masteredRank, "Mastered should be Tier 5");
        assertTrue(highGapRank < masteredRank, "High GAP must take precedence over Mastered skills");
    }

    @Test
    @DisplayName("Severity hierarchy must be strictly deterministic: HIGH < MEDIUM < LOW < UNEVALUATED < MASTERED")
    void testStrictTierHierarchy() {
        int high = calculateTierRank(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.HIGH);
        int medium = calculateTierRank(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.MEDIUM);
        int low = calculateTierRank(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.LOW);
        int unevaluated = calculateTierRank(null, null);
        int mastered = calculateTierRank(LearnerSkillGap.Status.MASTERED, null);

        assertTrue(high < medium);
        assertTrue(medium < low);
        assertTrue(low < unevaluated);
        assertTrue(unevaluated < mastered);
    }

    @Test
    @DisplayName("Spring Core & IoC (GAP/HIGH) must sort to Module 1 when other skills are MASTERED")
    void testBODHATestScenarioPrioritization() {
        Domain domain = new Domain("programming", "Programming", "Software systems");
        Subject subject = new Subject("java-backend", domain, "Full-Stack Java", DifficultyLevel.INTERMEDIATE);

        Skill s1 = new Skill(subject, "Core Java OOP", SkillCategory.CORE);
        Skill s2 = new Skill(subject, "Spring Core & IoC", SkillCategory.CORE);
        Skill s3 = new Skill(subject, "REST API Design", SkillCategory.CORE);
        Skill s4 = new Skill(subject, "JPA & Database Performance", SkillCategory.CORE);
        Skill s5 = new Skill(subject, "Security & Authentication", SkillCategory.ADVANCED);

        List<SkillPriority> list = new ArrayList<>();
        list.add(new SkillPriority(s1, calculateTierRank(LearnerSkillGap.Status.MASTERED, null), "MASTERED"));
        list.add(new SkillPriority(s2, calculateTierRank(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.HIGH), "GAP_HIGH"));
        list.add(new SkillPriority(s3, calculateTierRank(LearnerSkillGap.Status.MASTERED, null), "MASTERED"));
        list.add(new SkillPriority(s4, calculateTierRank(LearnerSkillGap.Status.MASTERED, null), "MASTERED"));
        list.add(new SkillPriority(s5, calculateTierRank(LearnerSkillGap.Status.MASTERED, null), "MASTERED"));

        // Sort by tierRank ascending
        list.sort(Comparator.comparingInt(SkillPriority::tierRank));

        assertEquals("Spring Core & IoC", list.get(0).skill().getName(),
                "Spring Core & IoC with HIGH GAP must be sorted as the very first module");
        assertEquals(1, list.get(0).tierRank());
    }
}
