import { LucideIcon } from 'lucide-react';

interface PageTemplateProps {
  title: string;
  description?: string;
  icon?: LucideIcon;
  children: React.ReactNode;
  actions?: React.ReactNode;
}

export function PageTemplate({ title, description, icon: Icon, children, actions }: PageTemplateProps) {
  return (
    <div className="space-y-6">
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
        <div className="flex items-center gap-4">
          {Icon && (
            <div className="w-12 h-12 bg-grid-100 rounded-xl flex items-center justify-center">
              <Icon className="w-7 h-7 text-grid-600" />
            </div>
          )}
          <div>
            <h1 className="text-2xl font-bold text-grid-900">{title}</h1>
            {description && <p className="text-grid-500 mt-1">{description}</p>}
          </div>
        </div>
        {actions && <div className="flex items-center gap-3">{actions}</div>}
      </div>
      {children}
    </div>
  );
}