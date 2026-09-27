import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  CheckCircle2,
  XCircle,
  Brain,
  Target,
  BarChart3,
  GitFork,
  CheckSquare,
  Sparkles,
  BookOpen,
  Code,
  Sigma,
  Languages,
  Briefcase,
  Database,
  Server,
  ShieldCheck,
  Cpu,
  Layers,
  Flame,
  Zap,
} from 'lucide-react';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Card from '../components/common/Card';
import BrandLogo from '../components/common/BrandLogo';

export default function LandingPage() {
  const [activeDomain, setActiveDomain] = useState('programming');

  const pipelineStages = [
    {
      num: '01',
      title: 'Target Calibration',
      desc: 'Define goal outcome (Career, Exam, Project, Mastery) and realistic daily study time.',
      icon: Target,
      badge: 'Goal Calibration',
    },
    {
      num: '02',
      title: 'Diagnostic Baseline',
      desc: 'Take a rapid 5-question prerequisite test with zero answer-key leakage.',
      icon: CheckSquare,
      badge: 'Assessment',
    },
    {
      num: '03',
      title: 'Skill-Gap Matrix',
      desc: 'Server-side evaluation partitions skills into Mastered vs High/Medium Gaps.',
      icon: BarChart3,
      badge: 'Gap Detection',
    },
    {
      num: '04',
      title: 'Sequenced Roadmap',
      desc: 'Deterministic engine synthesizes prerequisite milestones with locked downstream modules.',
      icon: GitFork,
      badge: 'Roadmap',
    },
    {
      num: '05',
      title: 'Active Progression',
      desc: 'Complete bite-sized lesson labs, verify retention, earn XP, and build consistency streaks.',
      icon: Brain,
      badge: 'Telemetry',
    },
    {
      num: '06',
      title: 'AI Personalization',
      desc: 'Context-aware guidance recommending next actions and tutoring grounded in learner state.',
      icon: Sparkles,
      badge: 'AI Engine',
    },
  ];

  const comparisonData = [
    {
      feature: 'Pedagogical Philosophy',
      chatbot: 'Ad-hoc conversational prompt answering with no memory of learning trajectory',
      bodha: 'Structured, goal-oriented mastery pipeline backed by a persistent learner profile',
    },
    {
      feature: 'Curriculum & Sequence',
      chatbot: 'Passive text stream; learner must already know what questions to ask next',
      bodha: 'Prerequisite-enforced curriculum milestones with sequential unlocking',
    },
    {
      feature: 'Competency Evaluation',
      chatbot: 'Guesses user proficiency or requires repetitive manual prompt prompting',
      bodha: 'Rigorous diagnostic assessments detect exact concept deficiencies automatically',
    },
    {
      feature: 'Learner State Persistence',
      chatbot: 'Ephemeral chats that evaporate across sessions',
      bodha: 'PostgreSQL relational persistence tracking XP, streaks, module mastery, and attempts',
    },
    {
      feature: 'Learning Methodology',
      chatbot: 'Wall-of-text reading that causes cognitive overload and passive illusion of competence',
      bodha: 'Active recall: bite-sized theory + hands-on labs + checkpoint verification',
    },
    {
      feature: 'AI Role in Architecture',
      chatbot: 'The chatbot IS the product, hallucinating arbitrary educational claims',
      bodha: 'AI is an adaptive module grounded in deterministic learner gaps and roadmap position',
    },
  ];

  const sampleDomains = [
    {
      id: 'programming',
      name: 'Software Engineering',
      icon: Code,
      sampleTopic: 'Full-Stack Java & Spring Boot Architecture',
      roadmapSummary: [
        'Core Java OOP & Collections',
        'Spring Core & Dependency Injection',
        'RESTful API Design & Validation',
        'PostgreSQL Persistence with Spring Data JPA',
        'Authentication & Security with JWT',
      ],
    },
    {
      id: 'math',
      name: 'Mathematics',
      icon: Sigma,
      sampleTopic: 'Linear Algebra for Machine Learning',
      roadmapSummary: [
        'Vector Spaces & Projections',
        'Matrix Decomposition & Transformations',
        'Eigenvalues & Eigenvectors',
        'Singular Value Decomposition (SVD)',
        'Principal Component Analysis (PCA)',
      ],
    },
    {
      id: 'languages',
      name: 'Languages',
      icon: Languages,
      sampleTopic: 'Conversational Spanish (CEFR A1–B1)',
      roadmapSummary: [
        'Present & Past Tenses Frameworks',
        'Core Idiomatic Sentence Structures',
        'Active Recall Vocabulary Retention',
        'Contextual Auditory Dialogue',
        'Spontaneous Conversational Fluency',
      ],
    },
    {
      id: 'business',
      name: 'Business & Leadership',
      icon: Briefcase,
      sampleTopic: 'Product Management & Strategic Roadmapping',
      roadmapSummary: [
        'Problem Discovery & Customer Research',
        'User Journey Mapping & Segmentation',
        'North Star Metrics & KPI Telemetry',
        'RICE Prioritization Frameworks',
        'Agile Delivery & Product Backlogs',
      ],
    },
  ];

  const selectedDomainData =
    sampleDomains.find((d) => d.id === activeDomain) || sampleDomains[0];

  return (
    <div className="flex flex-col min-h-screen bg-[#080b11]">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-20 pb-24 md:pt-28 md:pb-36 border-b border-slate-800/80">
        {/* Glow background effects */}
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[380px] bg-gradient-to-tr from-blue-600/15 via-indigo-600/15 to-cyan-400/15 blur-[140px] pointer-events-none -z-10" />

        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center space-y-8">
          {/* Official Brand Lockup */}
          <div className="flex justify-center mb-2">
            <BrandLogo size="lg" showWordmark={true} showTagline={true} asLink={false} />
          </div>

          {/* Eyebrow Pill */}
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-500/10 border border-blue-500/25 text-blue-400 text-xs font-semibold shadow-inner">
            <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
            <span>AI-Powered Adaptive Learning • Beyond Generic Chatbots</span>
          </div>

          {/* Main Headline */}
          <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-white leading-[1.1]">
            Transform Learning from Chaos to{' '}
            <span className="bg-gradient-to-r from-blue-400 via-sky-300 to-cyan-400 bg-clip-text text-transparent">
              Verified Mastery
            </span>
          </h1>

          {/* Subtitle */}
          <p className="max-w-3xl mx-auto text-base sm:text-lg md:text-xl text-slate-300 leading-relaxed font-normal">
            BODHA systematically guides you through <strong className="text-white">diagnostic baseline evaluations</strong>,{' '}
            <strong className="text-white">skill-gap detection</strong>, and <strong className="text-white">prerequisite-sequenced roadmaps</strong>. AI serves as our context-aware tutor, not a hallucinating wrapper.
          </p>

          {/* Action CTAs */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-3">
            <Link to="/choose-subject" className="w-full sm:w-auto">
              <Button
                size="lg"
                variant="primary"
                className="w-full sm:w-auto"
                rightIcon={<ArrowRight className="w-4 h-4" />}
              >
                Start Learning Journey
              </Button>
            </Link>

            <Link to="/auth" className="w-full sm:w-auto">
              <Button
                size="lg"
                variant="secondary"
                className="w-full sm:w-auto"
              >
                Sign In / Demo Access
              </Button>
            </Link>
          </div>

          {/* Architectural Pillars Row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-12 text-left">
            <Card variant="default" className="p-4 sm:p-5">
              <div className="flex items-center gap-2 text-blue-400 text-xs font-semibold mb-1.5">
                <Target className="w-4 h-4" />
                <span>Goal Calibrated</span>
              </div>
              <p className="text-sm font-bold text-white">Outcome & Pace Focused</p>
              <p className="text-xs text-slate-400 mt-1">Calibrated to 15m, 30m, 45m, or 60m daily commitments</p>
            </Card>

            <Card variant="default" className="p-4 sm:p-5">
              <div className="flex items-center gap-2 text-emerald-400 text-xs font-semibold mb-1.5">
                <BarChart3 className="w-4 h-4" />
                <span>Zero Redundancy</span>
              </div>
              <p className="text-sm font-bold text-white">Targeted Skill Gaps</p>
              <p className="text-xs text-slate-400 mt-1">Fast-track mastered topics; prioritize high-severity gaps</p>
            </Card>

            <Card variant="default" className="p-4 sm:p-5">
              <div className="flex items-center gap-2 text-cyan-400 text-xs font-semibold mb-1.5">
                <GitFork className="w-4 h-4" />
                <span>Dynamic Roadmaps</span>
              </div>
              <p className="text-sm font-bold text-white">Prerequisite Sequencing</p>
              <p className="text-xs text-slate-400 mt-1">Modules unlock only as foundational concepts are verified</p>
            </Card>

            <Card variant="default" className="p-4 sm:p-5">
              <div className="flex items-center gap-2 text-amber-400 text-xs font-semibold mb-1.5">
                <Database className="w-4 h-4" />
                <span>Persistent Telemetry</span>
              </div>
              <p className="text-sm font-bold text-white">PostgreSQL Backed</p>
              <p className="text-xs text-slate-400 mt-1">Tracks actual streaks, XP, completed lessons, and skill state</p>
            </Card>
          </div>
        </div>
      </section>

      {/* Core User Journey Pipeline */}
      <section id="pipeline" className="py-20 md:py-28 border-b border-slate-800/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16 space-y-3">
            <Badge variant="primary" size="sm" icon={<Zap className="w-3.5 h-3.5" />}>
              The BODHA Engine
            </Badge>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              The End-to-End Mastery Pipeline
            </h2>
            <p className="text-slate-400 text-sm sm:text-base leading-relaxed">
              Every learner progresses through a systematic 6-stage engineering pipeline designed to eliminate passive browsing and produce verifiable competence.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {pipelineStages.map((stage) => {
              const Icon = stage.icon;
              return (
                <Card
                  key={stage.num}
                  variant="default"
                  interactive
                  className="p-6 sm:p-7 flex flex-col justify-between group"
                >
                  <div>
                    <div className="flex items-center justify-between mb-4">
                      <span className="text-xs font-mono font-bold px-2.5 py-1 rounded-lg bg-[#141f36] text-blue-400 border border-slate-700/80">
                        Stage {stage.num}
                      </span>
                      <div className="w-9 h-9 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400 group-hover:bg-blue-600 group-hover:text-white transition-all duration-200">
                        <Icon className="w-4 h-4" />
                      </div>
                    </div>
                    <Badge variant="outline" size="xs" className="mb-2">
                      {stage.badge}
                    </Badge>
                    <h3 className="text-base sm:text-lg font-bold text-white mb-2 group-hover:text-blue-300 transition-colors">
                      {stage.title}
                    </h3>
                    <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
                      {stage.desc}
                    </p>
                  </div>
                </Card>
              );
            })}
          </div>
        </div>
      </section>

      {/* Product Differentiation: Bodha vs Chatbots */}
      <section id="differentiation" className="py-20 md:py-28 bg-[#0a0f1d]/50 border-b border-slate-800/80">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-14 space-y-3">
            <Badge variant="ai" size="sm" icon={<Cpu className="w-3.5 h-3.5" />}>
              Architecture Differentiation
            </Badge>
            <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              Why BODHA is NOT a ChatGPT Wrapper
            </h2>
            <p className="text-slate-300 text-sm sm:text-base leading-relaxed">
              ChatGPT answers isolated queries with no tracking. BODHA is a persistent learning operating system with diagnostic rigor, sequence validation, and telemetry.
            </p>
          </div>

          {/* Comparison Table */}
          <div className="overflow-hidden rounded-2xl border border-slate-800 bg-[#0e1526]/80 shadow-2xl">
            <div className="grid grid-cols-12 bg-[#121c33] border-b border-slate-800 text-xs font-bold uppercase tracking-wider p-4 text-slate-300">
              <div className="col-span-3">Capability</div>
              <div className="col-span-4 text-slate-400 flex items-center gap-1.5">
                <XCircle className="w-4 h-4 text-rose-500" />
                <span>Generic AI Chatbot</span>
              </div>
              <div className="col-span-5 text-blue-300 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                <span>BODHA Structured Mastery Platform</span>
              </div>
            </div>

            <div className="divide-y divide-slate-800/80">
              {comparisonData.map((row, idx) => (
                <div
                  key={idx}
                  className="grid grid-cols-12 p-4 text-xs sm:text-sm hover:bg-slate-850/40 transition-colors"
                >
                  <div className="col-span-3 font-semibold text-slate-200 pr-2">
                    {row.feature}
                  </div>
                  <div className="col-span-4 text-slate-400 pr-4 leading-relaxed flex items-start gap-2">
                    <span className="text-rose-500 font-bold shrink-0 mt-0.5">•</span>
                    <span>{row.chatbot}</span>
                  </div>
                  <div className="col-span-5 text-slate-200 leading-relaxed flex items-start gap-2 bg-blue-950/20 -my-4 py-4 px-3 rounded-lg border-l-2 border-blue-500">
                    <span className="text-emerald-400 font-bold shrink-0 mt-0.5">✓</span>
                    <span className="font-medium text-blue-100">{row.bodha}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Universal Domain Showcase */}
      <section id="domains" className="py-20 md:py-28 border-b border-slate-800/80">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-12 space-y-3">
            <Badge variant="primary" size="sm" icon={<BookOpen className="w-3.5 h-3.5" />}>
              Universal Learning Scope
            </Badge>
            <h2 className="text-3xl font-extrabold text-white tracking-tight">
              Calibrated for Any Target Discipline
            </h2>
            <p className="text-slate-400 text-sm">
              Bodha's pedagogical diagnostic architecture applies to systems engineering, discrete mathematics, linguistic fluency, or strategic trades.
            </p>
          </div>

          {/* Domain Tabs */}
          <div className="flex flex-wrap items-center justify-center gap-2 mb-8">
            {sampleDomains.map((d) => {
              const Icon = d.icon;
              const isSelected = d.id === activeDomain;
              return (
                <button
                  key={d.id}
                  type="button"
                  onClick={() => setActiveDomain(d.id)}
                  className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-semibold transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-500/25 ring-1 ring-blue-400'
                      : 'bg-[#0e1526] text-slate-400 hover:text-slate-200 hover:bg-[#141f36] border border-slate-800'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{d.name}</span>
                </button>
              );
            })}
          </div>

          {/* Active Domain Preview Card */}
          <Card variant="elevated" className="p-6 sm:p-8">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-800">
              <div>
                <span className="text-xs font-mono uppercase tracking-wider text-blue-400 font-semibold">
                  Curriculum Architecture Preview
                </span>
                <h3 className="text-xl sm:text-2xl font-bold text-white mt-1">
                  {selectedDomainData.sampleTopic}
                </h3>
              </div>
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#0a0f1d] border border-slate-700 text-slate-300 text-xs">
                <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                <span>Sequenced with Prerequisite Checks</span>
              </div>
            </div>

            <div className="mt-6">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4">
                Sequenced Milestone Milestones:
              </h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3">
                {selectedDomainData.roadmapSummary.map((topic, i) => (
                  <div
                    key={i}
                    className="p-3.5 rounded-xl bg-[#0a0f1d] border border-slate-800/80 flex flex-col justify-between hover:border-slate-700 transition-colors"
                  >
                    <span className="text-[11px] font-mono text-blue-400 font-semibold mb-2">
                      Module 0{i + 1}
                    </span>
                    <p className="text-xs font-medium text-slate-200">{topic}</p>
                    <div className="mt-3 pt-2 border-t border-slate-850 flex items-center justify-between text-[10px] text-slate-500 font-mono">
                      <span>Diagnostic</span>
                      <span className="text-emerald-400 font-semibold">Ready</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </Card>
        </div>
      </section>

      {/* Production Stack Architecture */}
      <section id="architecture" className="py-20 md:py-28 bg-[#0a0f1d]/50 border-b border-slate-800/80">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-2xl mx-auto mb-12 space-y-2">
            <Badge variant="success" size="sm" icon={<Layers className="w-3.5 h-3.5" />}>
              Active Production Stack
            </Badge>
            <h2 className="text-3xl font-extrabold text-white tracking-tight">
              Built on Clean Enterprise Foundations
            </h2>
            <p className="text-slate-400 text-sm">
              Modules A through I are fully integrated, verified against PostgreSQL, and running live.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card variant="elevated" className="p-6">
              <div className="flex items-center justify-between mb-4">
                <Badge variant="primary" size="xs">Tier 1 • Client</Badge>
                <Code className="w-5 h-5 text-blue-400" />
              </div>
              <h3 className="text-lg font-bold text-white mb-2">React 19 & Vite</h3>
              <p className="text-xs text-slate-300 leading-relaxed">
                Clean component hierarchy, central API client, responsive glassmorphism design tokens, and real-time state synchronization.
              </p>
              <div className="mt-4 pt-3 border-t border-slate-800 text-xs font-semibold text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4" />
                <span>Production Verified</span>
              </div>
            </Card>

            <Card variant="elevated" className="p-6">
              <div className="flex items-center justify-between mb-4">
                <Badge variant="success" size="xs">Tier 2 • Service</Badge>
                <Server className="w-5 h-5 text-emerald-400" />
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Spring Boot 3 REST APIs</h3>
              <p className="text-xs text-slate-300 leading-relaxed">
                Deterministic roadmap sequencing, assessment scoring, skill-gap detection, streak tracking, and AI tutoring fallbacks.
              </p>
              <div className="mt-4 pt-3 border-t border-slate-800 text-xs font-semibold text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4" />
                <span>23/23 Maven Tests Passing</span>
              </div>
            </Card>

            <Card variant="elevated" className="p-6">
              <div className="flex items-center justify-between mb-4">
                <Badge variant="ai" size="xs">Tier 3 • Storage</Badge>
                <Database className="w-5 h-5 text-cyan-400" />
              </div>
              <h3 className="text-lg font-bold text-white mb-2">PostgreSQL 16 Telemetry</h3>
              <p className="text-xs text-slate-300 leading-relaxed">
                17 relational entities managing persistent learner profiles, diagnostic question banks, progress checkpoints, and daily streaks.
              </p>
              <div className="mt-4 pt-3 border-t border-slate-800 text-xs font-semibold text-emerald-400 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4" />
                <span>Live PostgreSQL State</span>
              </div>
            </Card>
          </div>
        </div>
      </section>

      {/* Call to Action Footer Banner */}
      <section className="py-20 text-center">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
          <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
            Ready to Calibrate Your Personalized Roadmap?
          </h2>
          <p className="text-slate-300 text-sm sm:text-base max-w-xl mx-auto leading-relaxed">
            Take the initial 5-question baseline assessment to uncover your exact competency gaps and start mastering your chosen skill.
          </p>
          <div className="pt-2">
            <Link to="/choose-subject">
              <Button size="lg" variant="primary" rightIcon={<ArrowRight className="w-4 h-4" />}>
                Launch Step-by-Step Journey
              </Button>
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
