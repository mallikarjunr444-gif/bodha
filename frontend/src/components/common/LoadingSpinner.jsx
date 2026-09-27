import React from 'react';
import { Loader2 } from 'lucide-react';
import { JnanoraEmblem } from './BrandLogo';

export default function LoadingSpinner({
  message = 'Loading...',
  size = 'md',
  fullScreen = false,
  showEmblem = false,
  className = '',
}) {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };

  const shouldShowEmblem = showEmblem || fullScreen || size === 'lg';

  const content = (
    <div className={`flex flex-col items-center justify-center gap-3 text-[var(--text-muted)] ${className}`}>
      {shouldShowEmblem && (
        <div className="relative mb-1">
          <JnanoraEmblem className={size === 'lg' ? 'w-12 h-12' : 'w-10 h-10'} />
          <div className="absolute inset-0 bg-sky-500/20 blur-xl rounded-full pointer-events-none animate-pulse" />
        </div>
      )}
      <Loader2 className={`${sizeClasses[size] || sizeClasses.md} animate-spin text-sky-400`} />
      {message && (
        <p className="text-xs sm:text-sm font-medium animate-pulse text-[var(--text-secondary)] text-center max-w-sm">
          {message}
        </p>
      )}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="min-h-[400px] flex items-center justify-center w-full py-16">
        {content}
      </div>
    );
  }

  return content;
}


