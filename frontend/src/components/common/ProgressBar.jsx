import React from 'react';

export default function ProgressBar({
  value = 0,
  max = 100,
  size = 'md',
  variant = 'primary',
  showLabel = false,
  label = '',
  className = '',
}) {
  const percentage = Math.min(100, Math.max(0, Math.round((value / max) * 100)));

  const sizeStyles = {
    sm: 'h-1.5',
    md: 'h-2.5',
    lg: 'h-4',
  };

  const fillVariants = {
    primary: 'bg-gradient-to-r from-blue-600 via-blue-500 to-cyan-400',
    success: 'bg-gradient-to-r from-emerald-600 to-teal-400',
    ai: 'bg-gradient-to-r from-cyan-500 to-blue-500',
    warning: 'bg-gradient-to-r from-amber-500 to-orange-400',
  };

  const heightClass = sizeStyles[size] || sizeStyles.md;
  const fillClass = fillVariants[variant] || fillVariants.primary;

  return (
    <div className={`w-full space-y-1.5 ${className}`}>
      {showLabel && (
        <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
          <span>{label || 'Progress'}</span>
          <span className="font-bold text-slate-200">{percentage}%</span>
        </div>
      )}
      <div className={`w-full bg-[#0a0f1d] ${heightClass} rounded-full overflow-hidden border border-slate-800/80`}>
        <div
          className={`${heightClass} ${fillClass} transition-all duration-500 ease-out rounded-full`}
          style={{ width: `${percentage}%` }}
          role="progressbar"
          aria-valuenow={percentage}
          aria-valuemin={0}
          aria-valuemax={100}
        />
      </div>
    </div>
  );
}
