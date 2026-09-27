import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart3,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  Sparkles,
  BookOpen,
  ShieldCheck,
  Zap,
} from 'lucide-react';

import { useLearner } from '../context/LearnerContext';
import { skillGapApi } from '../services/api';
import JourneyStepper from '../components/JourneyStepper';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import SectionHeader from '../components/common/SectionHeader';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';

export default function SkillGapPage() {
  const {
    user,
    selectedSubject,
    activeGoal,
    assessmentResult,
    diagnosticScore,
    skillGaps,
    setSkillGaps,
    fetchOrGenerateRoadmap,
  } = useLearner();

  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [navigating, setNavigating] = useState(false);
  const [error, setError] = useState(null);

  // Load persisted skill gaps if not already in context
  const loadSkillGaps = useCallback(async () => {
    if (!user?.id) return;
    if (skillGaps && skillGaps.length > 0) return;

    setLoading(true);
    setError(null);
    try {
      let gaps = [];
      if (activeGoal?.id) {
        gaps = await skillGapApi.getUserGoalSkillGaps(user.id, activeGoal.id);
      } else if (selectedSubject?.id) {
        gaps = await skillGapApi.getUserSubjectSkillGaps(user.id, selectedSubject.id);
      } else {
        gaps = await skillGapApi.getUserSkillGaps(user.id);
      }
      setSkillGaps(gaps || []);
    } catch (err) {
      setError(err.message || 'Failed to retrieve skill gap analysis from backend.');
    } finally {
      setLoading(false);
    }
  }, [user?.id, activeGoal?.id, selectedSubject?.id, skillGaps, setSkillGaps]);

  useEffect(() => {
    loadSkillGaps();
  }, [loadSkillGaps]);

  // Partition real backend gaps into Mastered vs Gaps
  const rawList = Array.isArray(skillGaps) ? skillGaps : [];

  const masteredSkills = rawList.filter(
    (item) => item.status === 'MASTERED' || (!item.gapSeverity && item.status !== 'GAP')
  );

  const gapSkills = rawList.filter(
    (item) => item.status === 'GAP' || item.gapSeverity
  );

  // Severity ordering: HIGH > MEDIUM > LOW
  const severityRank = { HIGH: 1, MEDIUM: 2, LOW: 3 };
  const sortedGaps = [...gapSkills].sort((a, b) => {
    const rankA = severityRank[a.gapSeverity?.toUpperCase()] || 99;
    const rankB = severityRank[b.gapSeverity?.toUpperCase()] || 99;
    return rankA - rankB;
  });

  const readinessScore =
    diagnosticScore ||
    (assessmentResult?.scorePercentage != null ? Math.round(Number(assessmentResult.scorePercentage)) : null) ||
    (rawList.length > 0
      ? Math.round((masteredSkills.length / rawList.length) * 100)
      : 80);

  const handleProceed = async () => {
    setNavigating(true);
    try {
      if (activeGoal?.id) {
        await fetchOrGenerateRoadmap(activeGoal.id);
      }
      navigate('/roadmap');
    } catch (err) {
      console.warn('Roadmap synthesis warning:', err);
      navigate('/roadmap');
    } finally {
      setNavigating(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[var(--bg-canvas)]">
      <JourneyStepper />

      <main className="flex-1 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-8">
        {/* Header */}
        <SectionHeader
          eyebrow="Competency Telemetry"
          eyebrowIcon={BarChart3}
          title="Skill-Gap Analysis & Baseline Calibration"
          description={`Diagnostic results for ${selectedSubject?.title || 'Selected Track'}. Unlike generic tools that guess your capabilities, Jnanora calculates precisely where your foundation is solid and where targeted milestones are required.`}
        />

        {/* Error alert */}
        {error && (
          <ErrorMessage
            title="Skill Gap Telemetry Error"
            message={error}
            onRetry={loadSkillGaps}
          />
        )}

        {/* Loading state */}
        {loading ? (
          <div className="py-20">
            <LoadingSpinner
              message="Calculating competency matrix and evaluating persistent skill gaps..."
              size="lg"
            />
          </div>
        ) : rawList.length === 0 ? (
          <EmptyState
            icon={BookOpen}
            title="No Skill Gaps Recorded Yet"
            description="Take the diagnostic assessment to evaluate prerequisites and uncover exact skill gaps."
            action={
              <Button
                variant="primary"
                size="md"
                onClick={() => navigate('/assessment')}
              >
                Take Diagnostic Assessment
              </Button>
            }
          />
        ) : (
          <>
            {/* Overview Score Card */}
            <Card variant="glow" className="p-6 sm:p-8 flex flex-col md:flex-row items-center justify-between gap-6">
              <div className="space-y-2 text-center md:text-left">
                <div className="flex items-center justify-center md:justify-start gap-2">
                  <Badge variant="primary" size="sm">
                    Baseline Calibration
                  </Badge>
                  <span className="text-xs text-[var(--text-muted)] font-mono">
                    PostgreSQL Persisted State
                  </span>
                </div>
                <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                  Initial Domain Readiness: {readinessScore}%
                </h2>
                <p className="text-xs sm:text-sm text-[var(--text-secondary)] max-w-xl leading-relaxed">
                  {assessmentResult?.scorePercentage != null
                    ? `Authoritative backend evaluation complete. You correctly demonstrated understanding for ${assessmentResult.correctAnswers} of ${assessmentResult.totalQuestions} questions.`
                    : 'Diagnostic evaluation mapped against target curriculum. Prerequisite competencies will be accelerated, while identified skill gaps form your personalized milestones.'}
                </p>
              </div>

              <div className="flex items-center gap-4 shrink-0">
                <div className="h-24 w-24 rounded-2xl bg-[var(--bg-canvas)] border border-blue-500/40 flex flex-col items-center justify-center shadow-inner relative overflow-hidden">
                  <div className="absolute inset-0 bg-blue-500/10 pointer-events-none" />
                  <span className="text-3xl font-black text-white">{readinessScore}%</span>
                  <span className="text-[10px] text-blue-400 font-mono uppercase tracking-wider mt-0.5">Readiness</span>
                </div>
              </div>
            </Card>

            {/* Mastered vs Gaps Columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Mastered Column */}
              <Card variant="default" className="p-6 space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-[var(--border-subtle)]">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-5 h-5 text-emerald-400" />
                    <h3 className="text-base font-bold text-white tracking-tight">Mastered Competencies</h3>
                  </div>
                  <Badge variant="success" size="sm">
                    {masteredSkills.length} Verified
                  </Badge>
                </div>

                <p className="text-xs text-[var(--text-muted)] leading-relaxed">
                  These prerequisite topics will be <strong className="text-[var(--text-secondary)]">accelerated or marked complete</strong> in your roadmap so you focus on new material:
                </p>

                <div className="space-y-2.5">
                  {masteredSkills.length > 0 ? (
                    masteredSkills.map((item, idx) => (
                      <div
                        key={item.id || idx}
                        className="p-3.5 rounded-xl bg-[var(--bg-canvas)]/70 border border-[var(--border-subtle)] flex items-center justify-between hover:border-emerald-500/30 transition-colors"
                      >
                        <div className="flex items-center gap-2.5">
                          <span className="w-2 h-2 rounded-full bg-emerald-400 shadow-sm shadow-emerald-400/50" />
                          <span className="text-xs font-semibold text-white">
                            {item.skillName || item.name}
                          </span>
                        </div>
                        <Badge variant="success" size="xs">
                          {item.category || 'Mastered'}
                        </Badge>
                      </div>
                    ))
                  ) : (
                    <div className="p-4 rounded-xl bg-[var(--bg-canvas)]/40 border border-[var(--border-subtle)] text-center text-xs text-[var(--text-muted)]">
                      No prerequisites marked as mastered yet.
                    </div>
                  )}
                </div>
              </Card>

              {/* Identified Gaps Column */}
              <Card variant="default" className="p-6 space-y-4">
                <div className="flex items-center justify-between pb-3 border-b border-[var(--border-subtle)]">
                  <div className="flex items-center gap-2">
                    <AlertTriangle className="w-5 h-5 text-amber-400" />
                    <h3 className="text-base font-bold text-white tracking-tight">Targeted Skill Gaps</h3>
                  </div>
                  <Badge variant="warning" size="sm">
                    {sortedGaps.length} Action Items
                  </Badge>
                </div>

                <p className="text-xs text-[var(--text-muted)] leading-relaxed">
                  These identified deficiencies directly shape the sequencing and prioritization of your roadmap:
                </p>

                <div className="space-y-2.5">
                  {sortedGaps.length > 0 ? (
                    sortedGaps.map((item, idx) => {
                      const sev = (item.gapSeverity || 'MEDIUM').toUpperCase();
                      const isHigh = sev === 'HIGH';
                      const isMed = sev === 'MEDIUM';

                      return (
                        <div
                          key={item.id || idx}
                          className="p-3.5 rounded-xl bg-[var(--bg-canvas)]/70 border border-[var(--border-subtle)] space-y-1.5 hover:border-[var(--border-default)] transition-colors"
                        >
                          <div className="flex items-center justify-between">
                            <span className="text-xs font-bold text-white">
                              {item.skillName || item.name}
                            </span>
                            <Badge
                              variant={isHigh ? 'danger' : isMed ? 'warning' : 'primary'}
                              size="xs"
                            >
                              {sev} Priority
                            </Badge>
                          </div>
                          {item.reason && (
                            <p className="text-[11px] text-[var(--text-muted)] leading-relaxed">
                              {item.reason}
                            </p>
                          )}
                        </div>
                      );
                    })
                  ) : (
                    <div className="p-4 rounded-xl bg-[var(--bg-canvas)]/40 border border-[var(--border-subtle)] text-center text-xs text-[var(--text-muted)]">
                      No skill gaps detected. You are ready to start with advanced modules!
                    </div>
                  )}
                </div>
              </Card>
            </div>

            {/* Bottom Proceed Box */}
            <Card variant="elevated" className="p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-3.5">
                <div className="w-11 h-11 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
                  <Sparkles className="w-5 h-5 text-cyan-400" />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-white tracking-tight">
                    Dynamic Roadmap Ready for Synthesis
                  </h4>
                  <p className="text-xs text-[var(--text-muted)] mt-0.5">
                    Jnanora synthesizes your diagnostic profile into an adaptive learning path with topological sequencing.
                  </p>
                </div>
              </div>

              <Button
                variant="ai"
                size="lg"
                loading={navigating}
                disabled={navigating}
                onClick={handleProceed}
                iconRight={ArrowRight}
                className="w-full sm:w-auto shrink-0"
              >
                {navigating ? 'Synthesizing Roadmap...' : 'View Personalized Roadmap'}
              </Button>
            </Card>
          </>
        )}
      </main>
    </div>
  );
}
