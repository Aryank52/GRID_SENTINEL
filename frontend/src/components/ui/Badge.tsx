import clsx from 'clsx';
import { forwardRef } from 'react';

type BadgeVariant = 'default' | 'info' | 'warning' | 'danger' | 'success' | 'secondary' | 'alert';

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
  size?: 'sm' | 'md' | 'lg';
}

const variantClasses: Record<BadgeVariant, string> = {
  default: 'bg-grid-100 text-grid-700',
  info: 'bg-grid-100 text-grid-700',
  warning: 'bg-warning-100 text-warning-800',
  danger: 'bg-alert-100 text-alert-800',
  success: 'bg-safe-100 text-safe-800',
  secondary: 'bg-gray-100 text-gray-700',
  alert: 'bg-alert-100 text-alert-800',
};

const sizeClasses = {
  sm: 'px-1.5 py-0.5 text-xs',
  md: 'px-2 py-0.5 text-xs',
  lg: 'px-2.5 py-1 text-sm',
};

export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ className, variant = 'default', size = 'md', children, ...props }, ref) => {
    return (
      <span
        ref={ref}
        className={clsx(
          'badge',
          variantClasses[variant],
          sizeClasses[size],
          className
        )}
        {...props}
      >
        {children}
      </span>
    );
  }
);

Badge.displayName = 'Badge';