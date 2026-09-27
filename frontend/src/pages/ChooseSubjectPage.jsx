import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Search,
  BookOpen,
  ArrowRight,
  CheckCircle2,
  Sparkles,
  PlusCircle,
  Clock,
} from 'lucide-react';

import { useLearner } from '../context/LearnerContext';
import { catalogApi } from '../services/api';
import JourneyStepper from '../components/JourneyStepper';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import EmptyState from '../components/common/EmptyState';
import SectionHeader from '../components/common/SectionHeader';
import Card from '../components/common/Card';
import Badge from '../components/common/Badge';
import Button from '../components/common/Button';

export default function ChooseSubjectPage() {
  const { selectedSubject, selectSubject } = useLearner();
  const navigate = useNavigate();

  const [domains, setDomains] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [selectedDomain, setSelectedDomain] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [customSubjectInput, setCustomSubjectInput] = useState('');
  const [isCustomCreated, setIsCustomCreated] = useState(false);

  // Load domains & subjects from backend
  const loadCatalogData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [domainsRes, subjectsRes] = await Promise.all([
        catalogApi.getDomains(),
        catalogApi.getSubjects(),
      ]);

      const allDomains = [
        { id: 'all', name: 'All Domains', description: 'All academic and industry disciplines' },
        ...(domainsRes || []),
      ];

      setDomains(allDomains);
      setSubjects(subjectsRes || []);
    } catch (err) {
      setError(err.message || 'Failed to load domain and subject catalog from backend.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCatalogData();
  }, [loadCatalogData]);

  // Client-side filtering across selected domain and search text
  const filteredSubjects = subjects.filter((sub) => {
    const subDomain = sub.domainId || sub.domain;
    const matchesDomain = selectedDomain === 'all' || subDomain === selectedDomain;

    const query = searchQuery.toLowerCase().trim();
    if (!query) return matchesDomain;

    const matchesTitle = sub.title?.toLowerCase().includes(query);
    const matchesTagline = sub.tagline?.toLowerCase().includes(query);
    const matchesSkills = Array.isArray(sub.skillsCovered) &&
      sub.skillsCovered.some((s) => s.toLowerCase().includes(query));

    return matchesDomain && (matchesTitle || matchesTagline || matchesSkills);
  });

  const handleSelectSubject = (subjectObj) => {
    selectSubject(subjectObj);
    setIsCustomCreated(false);
  };

  const handleCreateCustom = (e) => {
    e.preventDefault();
    if (!customSubjectInput.trim()) return;

    const customObj = {
      id: 'custom-' + Date.now(),
      domainId: 'custom',
      domain: 'custom',
      domainName: 'Custom Skill Track',
      title: customSubjectInput.trim(),
      tagline: 'Custom learning track synthesized with Jnanora adaptive diagnostic frameworks',
      difficultyLevel: 'Tailored Baseline',
      difficulty: 'Tailored Baseline',
      estimatedWeeks: 6,
      popular: false,
      isCustom: true,
      skillsCovered: ['Foundational Concepts', 'Core Patterns', 'Problem Solving', 'Mastery Assessment'],
    };

    selectSubject(customObj);
    setIsCustomCreated(true);
  };

  const handleProceed = () => {
    navigate('/goal-setting');
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex flex-col bg-[#080b11]">
      <JourneyStepper />

      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full">
        {/* Header */}
        <div className="mb-8">
          <SectionHeader
            badgeText="Universal Learning Scope"
            badgeIcon={<Sparkles className="w-3.5 h-3.5 text-cyan-400" />}
            badgeVariant="primary"
            title="What skill or subject do you want to master?"
            description="Choose from curated foundational tracks or enter ANY custom discipline — from advanced software architectures to discrete mathematics, languages, or specialized trades."
          />
        </div>

        {/* Error Notification */}
        {error && (
          <div className="mb-6">
            <ErrorMessage
              title="Catalog Connection Error"
              message={error}
              onRetry={loadCatalogData}
            />
          </div>
        )}

        {/* Search and Domain Filters */}
        <div className="space-y-4 mb-8">
          <div className="relative max-w-md">
            <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by topic, keyword, or skill (e.g., Spring Boot, Calculus)..."
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-[#0e1526] border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-colors shadow-inner"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {domains.map((domain) => {
              const isSelected = selectedDomain === domain.id;
              return (
                <button
                  key={domain.id}
                  type="button"
                  onClick={() => setSelectedDomain(domain.id)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-500/25 ring-1 ring-blue-400'
                      : 'bg-[#0e1526] text-slate-400 hover:text-slate-200 border border-slate-800 hover:border-slate-700'
                  }`}
                >
                  {domain.name}
                </button>
              );
            })}
          </div>
        </div>

        {/* Loading Spinner */}
        {loading ? (
          <div className="py-20">
            <LoadingSpinner message="Fetching verified learning tracks from Jnanora catalog..." size="lg" />
          </div>
        ) : filteredSubjects.length === 0 ? (
          <div className="mb-10">
            <EmptyState
              icon={BookOpen}
              title="No subjects match your criteria"
              description={`No topics found for "${searchQuery}" in ${
                selectedDomain === 'all' ? 'any domain' : selectedDomain
              }. You can enter a custom skill below.`}
            />
          </div>
        ) : (
          /* Grid of Subjects */
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 mb-10">
            {filteredSubjects.map((sub) => {
              const isSelected = selectedSubject?.id === sub.id && !isCustomCreated;
              const difficulty = sub.difficulty || sub.difficultyLevel || 'All Levels';
              const weeks = sub.estimatedWeeks || 6;
              const skills = sub.skillsCovered || [];

              return (
                <Card
                  key={sub.id}
                  variant={isSelected ? 'glow' : 'default'}
                  interactive
                  onClick={() => handleSelectSubject(sub)}
                  className="p-6 flex flex-col justify-between group"
                >
                  <div>
                    <div className="flex items-center justify-between mb-3">
                      <Badge variant="outline" size="xs">
                        {difficulty}
                      </Badge>
                      {isSelected && (
                        <Badge variant="success" size="xs" icon={<CheckCircle2 className="w-3 h-3" />}>
                          Selected
                        </Badge>
                      )}
                    </div>

                    <h3 className="text-base font-bold text-white group-hover:text-blue-300 transition-colors mb-1.5">
                      {sub.title}
                    </h3>
                    <p className="text-xs text-slate-400 leading-relaxed mb-4">
                      {sub.tagline}
                    </p>

                    {skills.length > 0 && (
                      <div className="flex flex-wrap gap-1.5 mb-4">
                        {skills.map((skill, i) => (
                          <span
                            key={i}
                            className="text-[10px] px-2 py-0.5 rounded-md bg-[#080b11] text-slate-400 border border-slate-800/80 font-mono"
                          >
                            {skill}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="pt-3.5 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
                    <span className="flex items-center gap-1 font-mono text-[11px]">
                      <Clock className="w-3.5 h-3.5 text-slate-500" />
                      <span>~{weeks} weeks</span>
                    </span>
                    <span className="font-semibold text-blue-400 group-hover:translate-x-1 transition-transform inline-flex items-center gap-1">
                      <span>Select</span>
                      <ArrowRight className="w-3 h-3" />
                    </span>
                  </div>
                </Card>
              );
            })}
          </div>
        )}

        {/* Custom Subject Box */}
        <Card variant="elevated" className="p-6 sm:p-8 mb-10">
          <div className="max-w-2xl space-y-4">
            <div className="flex items-center gap-2">
              <PlusCircle className="w-5 h-5 text-blue-400" />
              <h3 className="text-base font-bold text-white">
                Don't see your topic? Enter ANY Custom Skill
              </h3>
            </div>
            <p className="text-xs sm:text-sm text-slate-400 leading-relaxed">
              Jnanora's pedagogical engine can construct diagnostic assessments and roadmaps for arbitrary disciplines (e.g. "Quantum Computing", "Organic Chemistry", "Japanese CEFR B1").
            </p>

            <form onSubmit={handleCreateCustom} className="flex flex-col sm:flex-row gap-3">
              <input
                type="text"
                value={customSubjectInput}
                onChange={(e) => setCustomSubjectInput(e.target.value)}
                placeholder="Type any subject or target skill..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-[#080b11] border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 shadow-inner"
              />
              <Button type="submit" size="sm" variant="secondary" className="shrink-0">
                Set Custom Skill
              </Button>
            </form>

            {isCustomCreated && (
              <div className="flex items-center gap-2 text-xs text-emerald-400 font-medium">
                <CheckCircle2 className="w-4 h-4" />
                <span>Custom skill "{selectedSubject?.title}" is selected for diagnostic assessment!</span>
              </div>
            )}
          </div>
        </Card>

        {/* Floating / Sticky Bottom Bar */}
        <div className="sticky bottom-4 z-30 p-4 rounded-2xl jnanora-glass border border-slate-700/80 shadow-2xl flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3 text-sm text-slate-300">
            <div className="w-9 h-9 rounded-xl bg-blue-500/10 border border-blue-500/30 flex items-center justify-center text-blue-400 shrink-0">
              <BookOpen className="w-4 h-4" />
            </div>
            <div>
              <span className="text-xs text-slate-400 block font-mono">Selected Focus:</span>
              <strong className="text-white font-bold">{selectedSubject?.title}</strong>
            </div>
          </div>

          <Button
            size="md"
            variant="primary"
            onClick={handleProceed}
            rightIcon={<ArrowRight className="w-4 h-4" />}
            className="w-full sm:w-auto"
          >
            Continue to Goal & Time Setting
          </Button>
        </div>
      </main>
    </div>
  );
}
