import clsx from 'clsx';

interface LoadingProps {
  size?: 'sm' | 'md' | 'lg';
  fullScreen?: boolean;
  label?: string;
  className?: string;
}

const sizeClasses = {
  sm: 'w-4 h-4',
  md: 'w-8 h-8',
  lg: 'w-12 h-12',
};

export function Loading({ size = 'md', fullScreen, label, className }: LoadingProps) {
  const spinner = (
    <div
      className={clsx(
        'animate-spin rounded-full border-2 border-gray-200 border-t-brand-600',
        sizeClasses[size],
      )}
    />
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 flex items-center justify-center bg-white/80 z-50">
        <div className="flex flex-col items-center gap-3">
          {spinner}
          {label && <p className="text-sm text-gray-600">{label}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className={clsx('flex items-center gap-3', className)}>
      {spinner}
      {label && <p className="text-sm text-gray-600">{label}</p>}
    </div>
  );
}
