import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Target,
  Clock,
  ArrowRight,
  CheckCircle2,
  Zap,
  GraduationCap,
  Hammer,
  BookOpen,
} from 'lucide-react';

import { useLearner } from '../context/LearnerContext';
import JourneyStepper from '../components/JourneyStepper';
import ErrorMessage from '../components/common/ErrorMessage';
import SectionHeader from '../components/common/SectionHeader';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';

export default function GoalSettingPage() {
  const { selectedSubject, goal, level, dailyTime, saveGoalAndPace } = useLearner();
  const navigate = useNavigate();

  const [selectedGoal, setSelectedGoal] = useState((goal || 'CAREER').toUpperCase());
  const [selectedLevel, setSelectedLevel] = useState((level || 'INTERMEDIATE').toUpperCase());
  const [selectedTime, setSelectedTime] = useState(dailyTime || '45m');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  const goals = [
    {
      id: 'CAREER',
      title: 'Career & Industry Readiness',
      desc: 'Build portfolio-grade skills, understand architecture, and pass technical interviews.',
      icon: Target,
    },
    {
      id: 'EXAM',
      title: 'Academic & Exam Preparation',
      desc: 'Master theoretical foundations, proofs, and rigorous problem-solving benchmarks.',
      icon: GraduationCap,
    },
    {
      id: 'PROJECT',
      title: 'Build a Tangible System',
      desc: 'Pragmatic focus on end-to-end execution, APIs, deployment, and practical patterns.',
      icon: Hammer,
    },
    {
      id: 'MASTERY',
      title: 'Lifelong Deep Mastery',
      desc: 'Understand the first principles and historical context without artificial shortcuts.',
      icon: BookOpen,
    },
  ];

  const levels = [
    {
      id: 'BEGINNER',
      title: 'Absolute Beginner',
      subtitle: 'Starting from scratch',
      desc: 'Needs introductory analogies and step-by-step foundation building.',
    },
    {
      id: 'INTERMEDIATE',
      title: 'Intermediate Foundation',
      subtitle: 'Knows basics & syntax',
      desc: 'Ready for structural design patterns, deep diagnostics, and real problems.',
    },
    {
      id: 'ADVANCED',
      title: 'Advanced / Specializing',
      subtitle: 'Comfortable with core topics',
      desc: 'Focus exclusively on architecture, edge cases, and performance optimizations.',
    },
  ];

  const timePaces = [
    { id: '15m', minutes: 15, label: '15 min/day', pace: 'Light Pace (~12-14 weeks)' },
    { id: '30m', minutes: 30, label: '30 min/day', pace: 'Steady Pace (~8-10 weeks)' },
    { id: '45m', minutes: 45, label: '45 min/day', pace: 'Optimal Standard (~6-7 weeks)' },
    { id: '60m', minutes: 60, label: '60+ min/day', pace: 'Intensive Sprint (~4-5 weeks)' },
  ];

  const handleProceed = async () => {
    setIsSubmitting(true);
    setSubmitError(null);

    try {
      await saveGoalAndPace({
        goal: selectedGoal,
        level: selectedLevel,
        dailyTime: selectedTime,
      });
      navigate('/assessment');
    } catch (err) {
      setSubmitError(
        err.message || 'Failed to calibrate learner goal with backend. Please retry.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[#080b11]">
      <JourneyStepper />

      <main className="flex-1 max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-10">
        {/* Header */}
        <SectionHeader
          badgeText="Target Calibration"
          badgeIcon={<Target className="w-3.5 h-3.5 text-blue-400" />}
          badgeVariant="primary"
          title="Calibrate your goals & constraints"
          description={`Configuring learning trajectory for ${selectedSubject?.title || 'your selected focus'}. Bodha adjusts roadmap depth and diagnostic rigor based on your primary objective, current familiarity, and real schedule.`}
        />

        {submitError && (
          <ErrorMessage
            title="Goal Calibration Error"
            message={submitError}
            onRetry={handleProceed}
          />
        )}

        {/* Section 1: Learning Goal */}
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20 text-xs flex items-center justify-center font-bold font-mono">
              1
            </span>
            <h2 className="text-base sm:text-lg font-bold text-white">
              What is your primary learning goal?
            </h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {goals.map((g) => {
              const Icon = g.icon;
              const isSelected = selectedGoal === g.id;

              return (
                <Card
                  key={g.id}
                  variant={isSelected ? 'glow' : 'default'}
                  interactive
                  onClick={() => setSelectedGoal(g.id)}
                  className="p-5 flex gap-4"
                >
                  <div
                    className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 transition-colors ${
                      isSelected
                        ? 'bg-blue-600 text-white shadow-md shadow-blue-500/30'
                        : 'bg-[#141f36] text-slate-400 border border-slate-700/60'
                    }`}
                  >
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-white mb-1">{g.title}</h3>
                    <p className="text-xs text-slate-400 leading-relaxed">{g.desc}</p>
                  </div>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Section 2: Current Level */}
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20 text-xs flex items-center justify-center font-bold font-mono">
              2
            </span>
            <h2 className="text-base sm:text-lg font-bold text-white">
              Where do you consider your current baseline?
            </h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {levels.map((lvl) => {
              const isSelected = selectedLevel === lvl.id;
              return (
                <Card
                  key={lvl.id}
                  variant={isSelected ? 'glow' : 'default'}
                  interactive
                  onClick={() => setSelectedLevel(lvl.id)}
                  className="p-5"
                >
                  <div className="flex items-center justify-between mb-2">
                    <Badge variant={isSelected ? 'primary' : 'neutral'} size="xs">
                      {lvl.subtitle}
                    </Badge>
                    {isSelected && <CheckCircle2 className="w-4 h-4 text-emerald-400" />}
                  </div>
                  <h3 className="text-sm font-bold text-white mb-1.5">{lvl.title}</h3>
                  <p className="text-xs text-slate-400 leading-relaxed">{lvl.desc}</p>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Section 3: Available Time */}
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="w-6 h-6 rounded-lg bg-blue-500/10 text-blue-400 border border-blue-500/20 text-xs flex items-center justify-center font-bold font-mono">
              3
            </span>
            <h2 className="text-base sm:text-lg font-bold text-white">
              How much time can you realistically invest daily?
            </h2>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            {timePaces.map((pace) => {
              const isSelected = selectedTime === pace.id;
              return (
                <Card
                  key={pace.id}
                  variant={isSelected ? 'glow' : 'default'}
                  interactive
                  onClick={() => setSelectedTime(pace.id)}
                  className="p-4 text-center"
                >
                  <Clock
                    className={`w-5 h-5 mx-auto mb-2 ${
                      isSelected ? 'text-blue-400' : 'text-slate-500'
                    }`}
                  />
                  <div className="text-sm font-bold text-white mb-0.5">{pace.label}</div>
                  <div className="text-[11px] text-slate-400 font-mono">{pace.pace}</div>
                </Card>
              );
            })}
          </div>
        </div>

        {/* Milestone Projection Banner */}
        <Card variant="elevated" className="p-5 sm:p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <div className="w-11 h-11 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
              <Zap className="w-5 h-5 text-cyan-400" />
            </div>
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-300 font-mono">
                Next: Diagnostic Baseline Evaluation
              </h4>
              <p className="text-xs text-slate-400 mt-0.5">
                Take a rapid diagnostic test to detect what you already know and uncover exact skill gaps.
              </p>
            </div>
          </div>

          <Button
            size="md"
            variant="primary"
            isLoading={isSubmitting}
            onClick={handleProceed}
            rightIcon={<ArrowRight className="w-4 h-4" />}
            className="w-full sm:w-auto shrink-0"
          >
            Start Diagnostic Test
          </Button>
        </Card>
      </main>
    </div>
  );
}
