import clsx from 'clsx';
import { LucideIcon } from 'lucide-react';
import { TrendingUp } from 'lucide-react';

type StatCardColor = 'safe' | 'warning' | 'alert' | 'grid';

interface StatCardProps {
  label: string;
  value: string;
  trend?: string;
  trendUp?: boolean | null;
  icon: LucideIcon;
  color: StatCardColor;
}

export function StatCard({ label, value, trend, trendUp, icon: Icon, color }: StatCardProps) {
  const colorClasses: Record<StatCardColor, string> = {
    safe: 'bg-safe-50 text-safe-700 border-safe-200',
    warning: 'bg-warning-50 text-warning-700 border-warning-200',
    alert: 'bg-alert-50 text-alert-700 border-alert-200',
    grid: 'bg-grid-50 text-grid-700 border-grid-200',
  };

  const iconColorClasses: Record<StatCardColor, string> = {
    safe: 'text-safe-600',
    warning: 'text-warning-600',
    alert: 'text-alert-600',
    grid: 'text-grid-600',
  };

  return (
    <div className={clsx('card p-5', colorClasses[color])}>
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <p className="text-sm font-medium text-grid-500 truncate">{label}</p>
          <p className="text-2xl font-bold text-grid-900 mt-1">{value}</p>
          {trend && (
            <p className={clsx('text-xs mt-2 flex items-center gap-1', trendUp === true ? 'text-safe-600' : trendUp === false ? 'text-alert-600' : 'text-grid-500')}>
              {trendUp === true && <TrendingUp className="w-3 h-3" />}
              {trendUp === false && <TrendingUp className="w-3 h-3 rotate-180" />}
              {trendUp === null && <span>•</span>}
              {trend}
            </p>
          )}
        </div>
        <div className={clsx('flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center', `bg-${color}-100`, iconColorClasses[color])}>
          <Icon className="w-5 h-5" aria-hidden="true" />
        </div>
      </div>
    </div>
  );
}