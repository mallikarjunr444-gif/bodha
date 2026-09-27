import React from 'react';
import { Link } from 'react-router-dom';

/**
 * BodhaEmblem: Standalone vectorized BODHA architectural emblem.
 *
 * Represents:
 * 1. Base spine: Structured foundational curriculum & knowledge baseline
 * 2. Top tier: Prerequisite diagnostic milestone
 * 3. Bottom tier: Ascending roadmap towards verified mastery
 * 4. Interlocking chevron notch: Step-by-step upward trajectory
 * 5. Cyan intelligence core: Context-aware AI tutoring nexus
 */
export function BodhaEmblem({ className = 'w-9 h-9', animated = true }) {
  return (
    <svg
      viewBox="0 0 100 100"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`${className} shrink-0 transition-transform duration-300 ${
        animated ? 'group-hover:scale-105' : ''
      }`}
      aria-hidden="true"
    >
      <defs>
        <linearGradient id="bodha-primary-grad" x1="16%" y1="12%" x2="88%" y2="88%">
          <stop offset="0%" stopColor="#3b82f6" />
          <stop offset="55%" stopColor="#6366f1" />
          <stop offset="100%" stopColor="#06b6d4" />
        </linearGradient>

        <linearGradient id="bodha-accent-grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#06b6d4" />
          <stop offset="100%" stopColor="#10b981" />
        </linearGradient>

        <filter id="bodha-emblem-shadow" x="-20%" y="-20%" width="140%" height="140%">
          <feDropShadow dx="0" dy="3" stdDeviation="5" floodColor="#3b82f6" floodOpacity="0.4" />
        </filter>
      </defs>

      <g filter="url(#bodha-emblem-shadow)">
        {/* Base Pillar (Foundation Spine) */}
        <rect x="18" y="16" width="13" height="68" rx="6.5" fill="url(#bodha-primary-grad)" />

        {/* Top Progression Tier (Milestone 1 / Prerequisite Baseline) */}
        <path
          d="M31 16 H 55 C 68.25 16 76 23.5 76 33.5 C 76 43 68.5 48.5 56 48.5 H 31 V 36 H 54 C 60 36 63.5 34.5 63.5 32 C 63.5 29.5 60 28 54 28 H 31 Z"
          fill="url(#bodha-primary-grad)"
        />

        {/* Bottom Progression Tier (Milestone 2 / Mastery Roadmap) */}
        <path
          d="M31 46 H 58 C 72 46 82 52.5 82 64 C 82 75.5 72.5 84 57 84 H 31 V 71.5 H 56 C 63.5 71.5 68.5 69.5 68.5 65 C 68.5 60.5 63.5 58.5 56 58.5 H 31 Z"
          fill="url(#bodha-primary-grad)"
        />

        {/* Upward Progression Chevron Notch */}
        <path
          d="M48 48.5 L 58 38.5 L 67 48.5 L 57 58.5 Z"
          fill="url(#bodha-accent-grad)"
          opacity="0.95"
        />

        {/* AI Nexus Core Spark */}
        <circle cx="57.5" cy="48.5" r="4.5" fill="#ffffff" />
        <circle cx="57.5" cy="48.5" r="2.5" fill="#06b6d4" />
      </g>
    </svg>
  );
}

/**
 * Official BODHA BrandLogo component.
 *
 * Supports:
 * - full logo (emblem + wordmark + AI badge)
 * - icon-only logo (variant="symbol" or showWordmark={false})
 * - responsive size variants: 'xs', 'sm', 'md', 'lg', 'xl'
 * - accessible semantic labelling
 * - linkable or static rendering
 */
export default function BrandLogo({
  size = 'md',
  variant = 'full',
  showWordmark = true,
  showTagline = false,
  className = '',
  asLink = true,
  ariaLabel = 'BODHA - AI-Powered Personalized Learning Platform',
}) {
  const isSymbolOnly = variant === 'symbol' || !showWordmark;

  const sizeMap = {
    xs: {
      emblem: 'w-5 h-5',
      title: 'text-sm font-extrabold',
      badge: 'text-[8px] px-1 py-0.2',
      tagline: 'text-[8px]',
      gap: 'gap-1.5',
    },
    sm: {
      emblem: 'w-7 h-7',
      title: 'text-base font-extrabold',
      badge: 'text-[9px] px-1.5 py-0.5',
      tagline: 'text-[9px]',
      gap: 'gap-2.5',
    },
    md: {
      emblem: 'w-9 h-9',
      title: 'text-xl font-black',
      badge: 'text-[9px] px-2 py-0.5',
      tagline: 'text-[10px]',
      gap: 'gap-3',
    },
    lg: {
      emblem: 'w-12 h-12',
      title: 'text-2xl sm:text-3xl font-black',
      badge: 'text-[10px] px-2 py-0.5',
      tagline: 'text-xs',
      gap: 'gap-3.5',
    },
    xl: {
      emblem: 'w-16 h-16',
      title: 'text-3xl sm:text-4xl font-black',
      badge: 'text-xs px-2.5 py-1',
      tagline: 'text-sm',
      gap: 'gap-4',
    },
  };

  const currentSize = sizeMap[size] || sizeMap.md;

  const content = (
    <div
      className={`inline-flex items-center ${currentSize.gap} group select-none ${className}`}
      aria-label={ariaLabel}
      role="img"
    >
      {/* Official BODHA Emblem */}
      <BodhaEmblem className={currentSize.emblem} />

      {/* Official BODHA Wordmark */}
      {!isSymbolOnly && (
        <div className="flex flex-col justify-center leading-tight">
          <div className="flex items-center gap-2">
            <span
              className={`${currentSize.title} tracking-wider text-white font-sans uppercase group-hover:text-blue-200 transition-colors drop-shadow-sm`}
            >
              BODHA
            </span>
            <span
              className={`${currentSize.badge} hidden sm:inline-flex font-mono font-bold uppercase tracking-wider rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/25 shadow-sm shadow-cyan-500/10`}
            >
              AI Platform
            </span>
          </div>
          {showTagline && (
            <p className={`${currentSize.tagline} text-[var(--text-muted)] font-medium tracking-normal mt-0.5`}>
              Personalized Learning Architecture
            </p>
          )}
        </div>
      )}
    </div>
  );

  if (asLink) {
    return (
      <Link
        to="/"
        className="inline-block cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/60 rounded-xl"
        title="BODHA Homepage"
      >
        {content}
      </Link>
    );
  }

  return content;
}

