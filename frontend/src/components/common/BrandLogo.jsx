import React from 'react';
import { Link } from 'react-router-dom';

/**
 * JnanoraEmblem: Standalone brand mark using the exact project owner logo asset (/jnanora-logo.png).
 * Source of truth: frontend/public/jnanora-logo.png (bear reading book on sky-blue ground).
 */
export function JnanoraEmblem({
  className = 'w-9 h-9',
  alt = 'Jnanora',
  animated = true,
}) {
  return (
    <img
      src="/jnanora-logo.png"
      alt={alt}
      className={`${className} rounded-xl object-contain shadow-md shadow-sky-950/40 border border-sky-400/25 shrink-0 transition-transform duration-300 ${
        animated ? 'group-hover:scale-105' : ''
      }`}
      loading="eager"
    />
  );
}

// Backward-compatible alias
export const BodhaEmblem = JnanoraEmblem;

/**
 * Official JNANORA BrandLogo component.
 *
 * Supports:
 * - full logo (official emblem + wordmark + AI badge)
 * - emblem-only logo (variant="symbol" or showWordmark={false})
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
  ariaLabel = 'Jnanora - AI-Powered Personalized Learning Platform',
}) {
  const isSymbolOnly = variant === 'symbol' || !showWordmark;

  const sizeMap = {
    xs: {
      emblem: 'w-6 h-6',
      title: 'text-sm font-extrabold',
      badge: 'text-[8px] px-1 py-0.2',
      tagline: 'text-[8px]',
      gap: 'gap-1.5',
    },
    sm: {
      emblem: 'w-8 h-8',
      title: 'text-base font-extrabold',
      badge: 'text-[9px] px-1.5 py-0.5',
      tagline: 'text-[9px]',
      gap: 'gap-2.5',
    },
    md: {
      emblem: 'w-10 h-10',
      title: 'text-xl font-black',
      badge: 'text-[9px] px-2 py-0.5',
      tagline: 'text-[10px]',
      gap: 'gap-3',
    },
    lg: {
      emblem: 'w-14 h-14',
      title: 'text-2xl sm:text-3xl font-black',
      badge: 'text-[10px] px-2 py-0.5',
      tagline: 'text-xs',
      gap: 'gap-3.5',
    },
    xl: {
      emblem: 'w-20 h-20',
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
      {/* Official Jnanora Emblem */}
      <JnanoraEmblem className={currentSize.emblem} alt="Jnanora" />

      {/* Official Jnanora Wordmark */}
      {!isSymbolOnly && (
        <div className="flex flex-col justify-center leading-tight">
          <div className="flex items-center gap-2">
            <span
              className={`${currentSize.title} tracking-wider text-white font-sans uppercase group-hover:text-sky-200 transition-colors drop-shadow-sm`}
            >
              JNANORA
            </span>
            <span
              className={`${currentSize.badge} hidden sm:inline-flex font-mono font-bold uppercase tracking-wider rounded bg-sky-500/10 text-sky-300 border border-sky-500/25 shadow-sm shadow-sky-500/10`}
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
        className="inline-block cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-sky-500/60 rounded-xl"
        title="Jnanora Homepage"
      >
        {content}
      </Link>
    );
  }

  return content;
}
