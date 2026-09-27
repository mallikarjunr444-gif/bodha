import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  GitFork,
  ArrowRight,
  Lock,
  Unlock,
  ShieldCheck,
  CheckCircle2,
  TrendingUp,
  BookOpen,
} from 'lucide-react';

import { useLearner } from '../context/LearnerContext';
import JourneyStepper from '../components/JourneyStepper';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import SectionHeader from '../components/common/SectionHeader';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';
import ProgressBar from '../components/common/ProgressBar';

export default function RoadmapPage() {
  const {
    user,
    selectedSubject,
    activeGoal,
    roadmap,
    fetchOrGenerateRoadmap,
    dailyTime,
    completedLessons,
    toggleLessonComplete,
  } = useLearner();

  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadRoadmap = useCallback(async () => {
    if (!user?.id || !activeGoal?.id) return;
    if (roadmap) return;

    setLoading(true);
    setError(null);
    try {
      await fetchOrGenerateRoadmap(activeGoal.id);
    } catch (err) {
      setError(err.message || 'Failed to synthesize or retrieve personalized roadmap.');
    } finally {
      setLoading(false);
    }
  }, [user?.id, activeGoal?.id, roadmap, fetchOrGenerateRoadmap]);

  useEffect(() => {
    loadRoadmap();
  }, [loadRoadmap]);

  const handleLaunchDashboard = () => {
    navigate('/dashboard');
  };

  const modules = roadmap?.modules || [];
  const progressPct = roadmap?.overallProgressPercentage ?? 0;

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[var(--bg-canvas)]">
      <JourneyStepper />

      <main className="flex-1 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-8">
        {/* Header */}
        <SectionHeader
          eyebrow="Dynamic Sequenced Curriculum"
          eyebrowIcon={GitFork}
          title={roadmap?.title || 'Your Personalized Roadmap'}
          description={`Customized specifically for ${selectedSubject?.title || 'Selected Track'} based on your diagnostic skill gaps and ${dailyTime}/day commitment.`}
          action={
            <Button
              variant="primary"
              size="md"
              onClick={handleLaunchDashboard}
              iconRight={ArrowRight}
            >
              Enter Dashboard
            </Button>
          }
        />

        {/* Overall Progress Bar */}
        {roadmap && (
          <Card variant="elevated" className="p-5 flex flex-col sm:flex-row items-center justify-between gap-6">
            <div className="flex items-center gap-3.5 w-full sm:w-auto">
              <div className="w-11 h-11 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
                <TrendingUp className="w-5 h-5 text-blue-400" />
              </div>
              <div>
                <span className="text-xs font-semibold text-[var(--text-muted)] block">Curriculum Completion</span>
                <span className="text-xl font-black text-white">{progressPct}% Mastered</span>
              </div>
            </div>

            <div className="w-full sm:max-w-md space-y-1">
              <ProgressBar
                progress={progressPct}
                variant="primary"
                size="md"
              />
            </div>
          </Card>
        )}

        {/* Error notification */}
        {error && (
          <ErrorMessage
            title="Roadmap Synthesis Error"
            message={error}
            onRetry={loadRoadmap}
          />
        )}

        {/* Loading state */}
        {loading ? (
          <div className="py-20">
            <LoadingSpinner
              message="Synthesizing personalized prerequisite graph and learning milestones..."
              size="lg"
            />
          </div>
        ) : modules.length === 0 ? (
          <EmptyState
            icon={BookOpen}
            title="No Personalized Roadmap Found"
            description="Complete the diagnostic assessment and goal calibration to generate your sequenced roadmap."
            action={
              <Button
                variant="primary"
                size="md"
                onClick={() => navigate('/goal-setting')}
              >
                Set Learning Goal
              </Button>
            }
          />
        ) : (
          /* Roadmap Module Timeline */
          <div className="space-y-6 relative before:absolute before:inset-0 before:left-6 before:w-0.5 before:bg-[var(--border-subtle)] before:hidden md:before:block">
            {modules.map((module) => {
              const status = module.status?.toLowerCase() || 'locked';
              const isUnlocked = status === 'unlocked' || status === 'completed';
              const isCompleted = status === 'completed';
              const isCurrent = Boolean(module.isCurrent);

              return (
                <div
                  key={module.id}
                  className={`relative md:pl-16 transition-all duration-300 ${
                    isCurrent ? 'scale-[1.01]' : ''
                  }`}
                >
                  {/* Node icon in timeline */}
                  <div
                    className={`hidden md:flex absolute left-3 -translate-x-1/2 top-6 w-8 h-8 rounded-full items-center justify-center text-xs font-bold ring-4 ring-[var(--bg-canvas)] z-10 shadow-lg ${
                      isCompleted
                        ? 'bg-emerald-600 text-white shadow-emerald-500/20'
                        : isCurrent
                        ? 'bg-blue-600 text-white shadow-blue-500/30'
                        : isUnlocked
                        ? 'bg-[var(--bg-surface-elevated)] text-[var(--text-secondary)] border border-[var(--border-default)]'
                        : 'bg-[var(--bg-surface)] text-[var(--text-muted)] border border-[var(--border-subtle)]'
                    }`}
                  >
                    {isCompleted ? (
                      <CheckCircle2 className="w-4 h-4" />
                    ) : isUnlocked ? (
                      <Unlock className="w-4 h-4" />
                    ) : (
                      <Lock className="w-4 h-4" />
                    )}
                  </div>

                  {/* Card */}
                  <Card
                    variant={isCurrent ? 'glow' : 'default'}
                    className={`p-6 sm:p-7 transition-all ${
                      isCompleted
                        ? 'border-emerald-500/30 bg-[var(--bg-surface)]/80'
                        : isCurrent
                        ? 'ring-1 ring-blue-500/50 bg-[var(--bg-surface-elevated)]'
                        : isUnlocked
                        ? 'bg-[var(--bg-surface)]'
                        : 'bg-[var(--bg-surface)]/50 opacity-60'
                    }`}
                  >
                    {/* Module Header */}
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-[var(--border-subtle)]">
                      <div>
                        <div className="flex items-center gap-2 mb-1.5">
                          <span className="text-xs font-mono font-bold uppercase tracking-wider text-blue-400">
                            {module.durationLabel || module.duration || `Milestone ${module.orderIndex}`}
                          </span>
                          {isCurrent && (
                            <Badge variant="ai" size="xs" className="animate-pulse">
                              Current Focus
                            </Badge>
                          )}
                          <Badge
                            variant={
                              isCompleted
                                ? 'success'
                                : isUnlocked
                                ? 'primary'
                                : 'neutral'
                            }
                            size="xs"
                          >
                            {isCompleted
                              ? 'Completed'
                              : isUnlocked
                              ? 'Unlocked'
                              : 'Prerequisites Required'}
                          </Badge>
                        </div>
                        <h3 className="text-lg sm:text-xl font-bold text-white tracking-tight">{module.title}</h3>
                      </div>

                      {module.prerequisiteSummary && (
                        <div className="text-xs text-[var(--text-muted)] font-medium flex items-center gap-1.5 self-start sm:self-center">
                          <ShieldCheck className="w-4 h-4 text-blue-400" />
                          <span>{module.prerequisiteSummary}</span>
                        </div>
                      )}
                    </div>

                    <p className="text-xs sm:text-sm text-[var(--text-secondary)] my-4 leading-relaxed">
                      {module.description}
                    </p>

                    {/* Lessons List */}
                    <div className="space-y-2 pt-2">
                      <span className="text-[11px] font-bold uppercase tracking-wider text-[var(--text-muted)] block mb-2">
                        Sequenced Lessons & Mastery Checks:
                      </span>
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                        {module.lessons?.map((lesson) => {
                          const isDone =
                            lesson.completed || completedLessons.includes(lesson.id);

                          return (
                            <div
                              key={lesson.id}
                              onClick={() => toggleLessonComplete(lesson.id)}
                              className={`p-3.5 rounded-xl border flex items-center justify-between text-xs transition-all cursor-pointer ${
                                isDone
                                  ? 'bg-[var(--bg-canvas)]/70 border-emerald-500/30 text-[var(--text-secondary)]'
                                  : 'bg-[var(--bg-canvas)]/40 border-[var(--border-subtle)] text-[var(--text-secondary)] hover:border-[var(--border-default)] hover:text-white'
                              }`}
                            >
                              <div className="flex items-center gap-2.5">
                                <span
                                  className={`w-2 h-2 rounded-full shrink-0 ${
                                    isDone ? 'bg-emerald-400 shadow-sm shadow-emerald-400/50' : 'bg-slate-600'
                                  }`}
                                />
                                <span
                                  className={
                                    isDone
                                      ? 'line-through text-[var(--text-muted)]'
                                      : 'font-medium text-white'
                                  }
                                >
                                  {lesson.title}
                                </span>
                              </div>
                              <Badge variant="neutral" size="xs">
                                {lesson.type || lesson.lessonType || 'Lesson'}
                              </Badge>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </Card>
                </div>
              );
            })}
          </div>
        )}

        {/* Bottom Navigation CTA */}
        <Card variant="glow" className="p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            <h4 className="text-base font-bold text-white tracking-tight">
              Ready to Advance Your Mastery?
            </h4>
            <p className="text-xs sm:text-sm text-[var(--text-secondary)] mt-0.5">
              Launch the learner dashboard to begin studying your active lesson with AI tutoring assistance.
            </p>
          </div>

          <Button
            variant="primary"
            size="lg"
            onClick={handleLaunchDashboard}
            iconRight={ArrowRight}
            className="w-full sm:w-auto shrink-0"
          >
            Launch Learning Dashboard
          </Button>
        </Card>
      </main>
    </div>
  );
}
