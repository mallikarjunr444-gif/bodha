import React from 'react';
import Badge from './Badge';

export default function SectionHeader({
  badgeText = '',
  badgeIcon = null,
  badgeVariant = 'primary',
  title,
  description = '',
  action = null,
  className = '',
}) {
  return (
    <div className={`flex flex-col sm:flex-row sm:items-end justify-between gap-4 ${className}`}>
      <div className="space-y-2 max-w-3xl">
        {badgeText && (
          <div>
            <Badge variant={badgeVariant} icon={badgeIcon} size="sm">
              {badgeText}
            </Badge>
          </div>
        )}
        <h1 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-white tracking-tight leading-tight">
          {title}
        </h1>
        {description && (
          <p className="text-slate-400 text-xs sm:text-sm md:text-base leading-relaxed">
            {description}
          </p>
        )}
      </div>

      {action && <div className="shrink-0">{action}</div>}
    </div>
  );
}
