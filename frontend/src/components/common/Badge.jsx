import React from 'react';

export default function Badge({
  children,
  variant = 'neutral',
  size = 'sm',
  icon = null,
  className = '',
}) {
  const sizeStyles = {
    xs: 'text-[10px] px-1.5 py-0.5 font-mono gap-1',
    sm: 'text-xs px-2.5 py-0.5 gap-1.5',
    md: 'text-xs sm:text-sm px-3 py-1 gap-2',
  };

  const variantStyles = {
    neutral:
      'bg-slate-800/80 text-slate-300 border border-slate-700/80',
    primary:
      'bg-blue-500/10 text-blue-400 border border-blue-500/25',
    ai:
      'bg-cyan-500/10 text-cyan-400 border border-cyan-500/25 font-mono',
    success:
      'bg-emerald-500/10 text-emerald-400 border border-emerald-500/25',
    warning:
      'bg-amber-500/10 text-amber-400 border border-amber-500/25',
    danger:
      'bg-rose-500/10 text-rose-400 border border-rose-500/25',
    outline:
      'bg-transparent text-slate-400 border border-slate-700/70',
  };

  const chosenSize = sizeStyles[size] || sizeStyles.sm;
  const chosenVariant = variantStyles[variant] || variantStyles.neutral;

  return (
    <span
      className={`inline-flex items-center font-medium rounded-md select-none shrink-0 ${chosenSize} ${chosenVariant} ${className}`}
    >
      {icon && <span className="shrink-0">{icon}</span>}
      <span>{children}</span>
    </span>
  );
}
