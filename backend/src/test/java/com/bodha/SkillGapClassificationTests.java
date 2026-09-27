package com.bodha;

import com.bodha.model.LearnerSkillGap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkillGapClassificationTests {

    private record ClassificationResult(LearnerSkillGap.Status status, LearnerSkillGap.Severity severity) {}

    private ClassificationResult classify(double percentage) {
        if (percentage >= 80.0) {
            return new ClassificationResult(LearnerSkillGap.Status.MASTERED, null);
        } else if (percentage >= 60.0) {
            return new ClassificationResult(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.LOW);
        } else if (percentage >= 40.0) {
            return new ClassificationResult(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.MEDIUM);
        } else {
            return new ClassificationResult(LearnerSkillGap.Status.GAP, LearnerSkillGap.Severity.HIGH);
        }
    }

    @Test
    @DisplayName("0% score is classified as GAP with HIGH severity")
    void testZeroScoreClassification() {
        ClassificationResult result = classify(0.0);
        assertEquals(LearnerSkillGap.Status.GAP, result.status());
        assertEquals(LearnerSkillGap.Severity.HIGH, result.severity());
    }

    @Test
    @DisplayName("35% score is classified as GAP with HIGH severity")
    void testLowScoreClassification() {
        ClassificationResult result = classify(35.0);
        assertEquals(LearnerSkillGap.Status.GAP, result.status());
        assertEquals(LearnerSkillGap.Severity.HIGH, result.severity());
    }

    @Test
    @DisplayName("50% score is classified as GAP with MEDIUM severity")
    void testMediumScoreClassification() {
        ClassificationResult result = classify(50.0);
        assertEquals(LearnerSkillGap.Status.GAP, result.status());
        assertEquals(LearnerSkillGap.Severity.MEDIUM, result.severity());
    }

    @Test
    @DisplayName("70% score is classified as GAP with LOW severity")
    void testLowGapScoreClassification() {
        ClassificationResult result = classify(70.0);
        assertEquals(LearnerSkillGap.Status.GAP, result.status());
        assertEquals(LearnerSkillGap.Severity.LOW, result.severity());
    }

    @Test
    @DisplayName("80% to 100% score is classified as MASTERED with null severity")
    void testMasteredClassification() {
        ClassificationResult result80 = classify(80.0);
        assertEquals(LearnerSkillGap.Status.MASTERED, result80.status());
        assertNull(result80.severity());

        ClassificationResult result100 = classify(100.0);
        assertEquals(LearnerSkillGap.Status.MASTERED, result100.status());
        assertNull(result100.severity());
    }
}
