import type { Feature, ClassOfService } from '../../types';
import { Table, Column } from '../common/Table';
import { Badge } from '../common/Badge';

interface FeatureListProps {
  features: Feature[];
  onRowClick?: (feature: Feature) => void;
  onAdd?: () => void;
}

const classOfServiceLabels: Record<ClassOfService, string> = {
  EXPEDITE: 'Expedite',
  FIXED_DATE: 'Fixed Date',
  STANDARD: 'Standard',
  FILLER: 'Filler',
};

const classOfServiceVariants: Record<ClassOfService, 'error' | 'warning' | 'default' | 'info'> = {
  EXPEDITE: 'error',
  FIXED_DATE: 'warning',
  STANDARD: 'default',
  FILLER: 'info',
};

export function FeatureList({ features, onRowClick, onAdd }: FeatureListProps) {
  const columns: Column<Feature>[] = [
    {
      key: 'title',
      header: 'Title',
      render: (feature) => (
        <div>
          <div className="font-medium text-gray-900">{feature.title}</div>
          <div className="text-xs text-gray-500 truncate max-w-xs">{feature.description}</div>
        </div>
      ),
    },
    {
      key: 'businessValue',
      header: 'Value',
      render: (feature) => (
        <div className="text-center">
          <span className="font-medium">{feature.businessValue}</span>
        </div>
      ),
      sortable: true,
    },
    {
      key: 'classOfService',
      header: 'Class',
      render: (feature) => (
        <Badge variant={classOfServiceVariants[feature.classOfService]}>
          {classOfServiceLabels[feature.classOfService]}
        </Badge>
      ),
    },
    {
      key: 'effort',
      header: 'Effort (h)',
      render: (feature) => {
        const total =
          feature.effortEstimate.backendHours +
          feature.effortEstimate.frontendHours +
          feature.effortEstimate.qaHours +
          feature.effortEstimate.devopsHours;
        return <span className="font-mono text-sm">{total}</span>;
      },
      sortable: true,
    },
    {
      key: 'deadline',
      header: 'Deadline',
      render: (feature) => (
        <span className={feature.deadline ? 'text-gray-900' : 'text-gray-400'}>
          {feature.deadline || '—'}
        </span>
      ),
    },
    {
      key: 'dependencies',
      header: 'Deps',
      render: (feature) => (
        <span className="text-sm">{feature.dependencies.length > 0 ? feature.dependencies.length : '—'}</span>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-900">Features ({features.length})</h2>
        {onAdd && (
          <button
            onClick={onAdd}
            className="px-3 py-1.5 bg-brand-600 text-white text-sm font-medium rounded-md hover:bg-brand-700 transition-colors"
          >
            + Add Feature
          </button>
        )}
      </div>
      <Table columns={columns} data={features} onRowClick={onRowClick} emptyMessage="No features yet" />
    </div>
  );
}
