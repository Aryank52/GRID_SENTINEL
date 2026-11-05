import { clsx } from 'clsx';

interface PageLoaderProps {
  fullScreen?: boolean;
  size?: 'sm' | 'md' | 'lg';
  text?: string;
}

export function PageLoader({ fullScreen = false, size = 'md', text = 'Loading...' }: PageLoaderProps) {
  const sizeClasses = {
    sm: 'h-4 w-4 border-2',
    md: 'h-8 w-8 border-3',
    lg: 'h-12 w-12 border-4',
  };

  if (fullScreen) {
    return (
      <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-white/90 backdrop-blur-sm">
        <div className={clsx('animate-spin rounded-full border-grid-200 border-t-grid-600', sizeClasses[size])} />
        <p className="mt-4 text-sm text-grid-500">{text}</p>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center py-8">
      <div className={clsx('animate-spin rounded-full border-grid-200 border-t-grid-600', sizeClasses[size])} />
      {text && <span className="ml-3 text-sm text-grid-500">{text}</span>}
    </div>
  );
}

export function Skeleton({ className = '' }: { className?: string }) {
  return (
    <div className={clsx('skeleton', className)} aria-hidden="true" />
  );
}

export function SkeletonCard() {
  return (
    <div className="card p-6 space-y-4">
      <Skeleton className="h-6 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
      <Skeleton className="h-4 w-1/3" />
      <div className="grid grid-cols-3 gap-4">
        <Skeleton className="h-16 rounded-lg" />
        <Skeleton className="h-16 rounded-lg" />
        <Skeleton className="h-16 rounded-lg" />
      </div>
    </div>
  );
}

export function SkeletonTable(rows = 5) {
  return (
    <div className="card overflow-hidden">
      <div className="table-container">
        <table className="table">
          <thead>
            <tr>
              <th><Skeleton className="h-4 w-full" /></th>
              <th><Skeleton className="h-4 w-full" /></th>
              <th><Skeleton className="h-4 w-full" /></th>
              <th><Skeleton className="h-4 w-full" /></th>
            </tr>
          </thead>
          <tbody>
            {Array.from({ length: rows }).map((_, i) => (
              <tr key={i}>
                <td><Skeleton className="h-4 w-3/4" /></td>
                <td><Skeleton className="h-4 w-1/2" /></td>
                <td><Skeleton className="h-4 w-1/3" /></td>
                <td><Skeleton className="h-4 w-1/4" /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export function SkeletonMap() {
  return (
    <div className="map-container aspect-video">
      <div className="absolute inset-0 bg-grid-200 animate-pulse" />
    </div>
  );
}