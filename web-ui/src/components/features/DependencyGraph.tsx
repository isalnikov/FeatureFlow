import type { Feature } from '../../types';

interface DependencyGraphProps {
  features: Feature[];
  selectedFeatureId?: string | null;
  onFeatureClick?: (featureId: string) => void;
}

export function DependencyGraph({ features, selectedFeatureId, onFeatureClick }: DependencyGraphProps) {
  const featuresWithDeps = features.filter((f) => f.dependencies.length > 0);

  if (featuresWithDeps.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-4">
        <h3 className="text-sm font-medium text-gray-900 mb-2">Dependency Graph</h3>
        <div className="flex items-center justify-center py-8 text-gray-400">
          No dependencies defined
        </div>
      </div>
    );
  }

  const featureMap = new Map(features.map((f) => [f.id, f]));

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <h3 className="text-sm font-medium text-gray-900 mb-4">Dependency Graph</h3>
      <div className="space-y-3">
        {featuresWithDeps.map((feature) => (
          <div
            key={feature.id}
            className={`p-3 rounded-lg border transition-colors cursor-pointer ${
              selectedFeatureId === feature.id
                ? 'border-brand-500 bg-brand-50'
                : 'border-gray-200 hover:border-gray-300'
            }`}
            onClick={() => onFeatureClick?.(feature.id)}
          >
            <div className="font-medium text-sm text-gray-900">{feature.title}</div>
            <div className="mt-1 text-xs text-gray-500">
              Depends on:{' '}
              {feature.dependencies
                .map((depId) => featureMap.get(depId)?.title || depId)
                .join(', ')}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
