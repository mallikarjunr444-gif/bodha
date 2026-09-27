import React from 'react';
import { Database, Server, Code2, Cpu } from 'lucide-react';
import BrandLogo from './common/BrandLogo';

export default function Footer() {
  return (
    <footer className="border-t border-[var(--border-subtle)] bg-[var(--bg-canvas)] text-[var(--text-muted)] text-sm py-14">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10 pb-12 border-b border-[var(--border-subtle)]">
          {/* Brand Info & Logo Slot */}
          <div className="md:col-span-2 space-y-4">
            <BrandLogo size="md" showWordmark={true} showTagline={true} />
            <p className="text-xs sm:text-sm text-[var(--text-secondary)] max-w-md leading-relaxed">
              BODHA is an AI-powered personalized learning and skill-development platform. It combines diagnostic assessments, skill-gap analysis, dynamic sequenced roadmaps, and persistent learner profiling.
            </p>
            <div className="text-xs text-blue-400 font-medium pt-1 flex items-center gap-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse" />
              <span>AI is an adaptive module in BODHA, not the entire product.</span>
            </div>
          </div>

          {/* Architecture & Tech Stack */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white">
              Production Architecture
            </h4>
            <ul className="space-y-2.5 text-xs">
              <li className="flex items-center gap-2.5 text-[var(--text-secondary)]">
                <Code2 className="w-4 h-4 text-blue-400 shrink-0" />
                <span>Frontend: React 19 + Vite</span>
              </li>
              <li className="flex items-center gap-2.5 text-[var(--text-secondary)]">
                <Server className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Backend: Spring Boot 3 REST</span>
              </li>
              <li className="flex items-center gap-2.5 text-[var(--text-secondary)]">
                <Database className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>State: PostgreSQL 16</span>
              </li>
              <li className="flex items-center gap-2.5 text-[var(--text-secondary)]">
                <Cpu className="w-4 h-4 text-amber-400 shrink-0" />
                <span>AI: Gemini 2.5 + Deterministic Fallback</span>
              </li>
            </ul>
          </div>

          {/* Navigation & Learning Paths */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-white">
              Learning Domains
            </h4>
            <ul className="space-y-2 text-xs text-[var(--text-secondary)]">
              <li className="hover:text-white transition-colors">Software Engineering & Systems</li>
              <li className="hover:text-white transition-colors">Mathematics & Algorithms</li>
              <li className="hover:text-white transition-colors">Data Science & AI Engineering</li>
              <li className="hover:text-white transition-colors">Foreign Languages & Communication</li>
              <li className="hover:text-white transition-colors">Design, UI/UX & Systems Architecture</li>
            </ul>
          </div>
        </div>

        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between text-xs text-[var(--text-muted)] gap-4">
          <p>© {new Date().getFullYear()} BODHA Platform. Portfolio-grade autonomous learning architecture.</p>
          <p className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-emerald-400" />
            <span>PostgreSQL & Spring Boot Online</span>
          </p>
        </div>
      </div>
    </footer>
  );
}
