import React, { useState, useEffect, useCallback } from 'react';

/**
 * JnanoraSplash: Dedicated full-viewport initial loading/splash screen.
 *
 * Implements the brand visual reveal using the exact project logo asset (/jnanora-logo.png).
 * Sequence:
 *   Phase 1 (0.0s - 0.4s): Sky-blue backdrop with logo initializing at scale 0.88
 *   Phase 2 (0.4s - 1.8s): Smooth ease-out fade-in and scale to 1.0
 *   Phase 3 (1.2s - 3.2s): Luminous ambient glow gently expands behind the logo
 *   Phase 4 (2.6s - 3.2s): Hold phase with crisp, stable centered logo
 *   Phase 5 (3.2s - 3.8s): Smooth 600ms fade-out transition, seamlessly revealing the app
 *
 * Reduced Motion: Respects prefers-reduced-motion with shortened duration (1.2s) and no scaling.
 */
export default function JnanoraSplash({ onFinish }) {
  const [isExiting, setIsExiting] = useState(false);

  const handleFinish = useCallback(() => {
    setIsExiting(true);
    const exitTimer = setTimeout(() => {
      if (onFinish) onFinish();
    }, 600);
    return () => clearTimeout(exitTimer);
  }, [onFinish]);

  useEffect(() => {
    // Check if user prefers reduced motion
    const prefersReducedMotion =
      typeof window !== 'undefined' &&
      window.matchMedia &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // Target duration: ~3.8s total (3.2s active + 0.6s exit fade); 1.2s if reduced motion
    const activeDuration = prefersReducedMotion ? 800 : 3200;

    const timer = setTimeout(() => {
      handleFinish();
    }, activeDuration);

    // Allow user to dismiss with Escape key
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' || e.key === 'Enter') {
        handleFinish();
      }
    };
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      clearTimeout(timer);
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [handleFinish]);

  return (
    <div
      role="status"
      aria-label="Loading Jnanora"
      aria-live="polite"
      tabIndex={-1}
      onClick={handleFinish}
      className={`fixed inset-0 z-[99999] flex flex-col items-center justify-center select-none cursor-default transition-opacity duration-600 ease-in-out ${
        isExiting ? 'opacity-0 pointer-events-none' : 'opacity-100'
      }`}
      style={{
        background: 'radial-gradient(circle at 50% 50%, #6dc2f5 0%, #5eafe5 45%, #499ed8 100%)',
      }}
    >
      <div className="relative flex flex-col items-center justify-center p-6 max-w-sm sm:max-w-md w-full">
        {/* Ambient Glowing Halo (Phase 3: Expands behind the logo) */}
        <div
          className="absolute -inset-4 sm:-inset-8 rounded-full blur-2xl pointer-events-none jnanora-splash-glow"
          style={{
            background:
              'radial-gradient(circle, rgba(255, 255, 255, 0.65) 0%, rgba(165, 222, 255, 0.45) 45%, transparent 70%)',
          }}
          aria-hidden="true"
        />

        {/* Central Logo Asset (Phase 1, 2, 4: Exact /jnanora-logo.png) */}
        <div className="relative z-10 overflow-hidden rounded-3xl shadow-2xl shadow-sky-950/25 border border-white/25 jnanora-splash-logo bg-[#5eafe5]">
          <img
            src="/jnanora-logo.png"
            alt="Jnanora"
            className="w-56 h-56 sm:w-64 sm:h-64 md:w-72 md:h-72 object-contain block"
            loading="eager"
            decoding="async"
          />

          {/* Soft light shimmer sweep across the mark */}
          <div
            className="absolute inset-y-0 w-1/2 pointer-events-none jnanora-splash-shimmer"
            style={{
              background:
                'linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.35) 50%, transparent 100%)',
            }}
            aria-hidden="true"
          />
        </div>

        {/* Subtle Minimalist Status Indicator */}
        <div
          className="mt-8 flex items-center gap-2 text-white/80 text-xs font-medium tracking-wider uppercase font-mono"
          aria-hidden="true"
        >
          <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" style={{ animationDelay: '0ms' }} />
          <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" style={{ animationDelay: '200ms' }} />
          <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" style={{ animationDelay: '400ms' }} />
        </div>
      </div>
    </div>
  );
}
