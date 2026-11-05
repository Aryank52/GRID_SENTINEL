import { forwardRef } from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
type ButtonSize = 'sm' | 'md' | 'lg' | 'icon';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  asChild?: boolean;
}

const variantClasses: Record<ButtonVariant, string> = {
  primary: 'bg-grid-600 text-white hover:bg-grid-700 focus-visible:ring-grid-500',
  secondary: 'bg-grid-100 text-grid-700 hover:bg-grid-200 focus-visible:ring-grid-400',
  danger: 'bg-alert-600 text-white hover:bg-alert-700 focus-visible:ring-alert-500',
  ghost: 'bg-transparent text-grid-600 hover:bg-grid-100 focus-visible:ring-grid-400',
  outline: 'border border-grid-300 bg-white text-grid-700 hover:bg-grid-50 focus-visible:ring-grid-400',
};

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs',
  md: 'px-4 py-2 text-sm',
  lg: 'px-6 py-3 text-base',
  icon: 'p-2',
};

function Spinner({ className }: { className?: string }) {
  return <span className={`spinner ${className || ''}`} aria-hidden="true" />;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'primary', size = 'md', loading = false, leftIcon, rightIcon, children, disabled, asChild = false, ...props }, ref) => {
    const isDisabled = disabled || loading;

    const baseClass = 'btn';
    const variantClass = variantClasses[variant];
    const sizeClass = sizeClasses[size];
    const disabledClass = isDisabled ? 'opacity-50 cursor-not-allowed' : '';
    const combinedClass = `${baseClass} ${variantClass} ${sizeClass} ${disabledClass} ${className || ''}`.trim();

    const ChildComponent = asChild ? 'span' : 'button';

    return (
      <ChildComponent
        ref={ref}
        className={combinedClass}
        disabled={isDisabled}
        {...props}
      >
        {loading && <Spinner className="w-4 h-4" />}
        {!loading && leftIcon && <span className="flex-shrink-0">{leftIcon}</span>}
        <span className={loading ? 'flex-1 invisible' : 'flex-1'}>
          {children}
        </span>
        {!loading && rightIcon && <span className="flex-shrink-0">{rightIcon}</span>}
      </ChildComponent>
    );
  }
);

Button.displayName = 'Button';