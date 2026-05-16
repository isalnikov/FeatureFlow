import type { Feature, FeatureTimeline } from '../../types';

interface GanttDependenciesProps {
  features: Feature[];
  timelines: Record<string, FeatureTimeline>;
  dayWidth: number;
  startDate: Date;
}

export function GanttDependencies({ features, timelines, dayWidth, startDate }: GanttDependenciesProps) {
  const dependencies = features
    .filter((f) => f.dependencies.length > 0)
    .flatMap((f) =>
      f.dependencies.map((depId) => ({
        from: depId,
        to: f.id,
      })),
    );

  if (dependencies.length === 0) return null;

  const getBarPosition = (featureId: string) => {
    const timeline = timelines[featureId];
    if (!timeline) return null;
    const start = new Date(timeline.startDate);
    const offset = Math.floor((start.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
    return offset * dayWidth;
  };

  return (
    <svg className="absolute inset-0 pointer-events-none" style={{ zIndex: 0 }}>
      <defs>
        <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="10" refY="3.5" orient="auto">
          <polygon points="0 0, 10 3.5, 0 7" fill="#94a3b8" />
        </marker>
      </defs>
      {dependencies.map((dep, i) => {
        const fromX = getBarPosition(dep.from);
        const toX = getBarPosition(dep.to);
        if (fromX === null || toX === null) return null;

        const fromFeatureIndex = features.findIndex((f) => f.id === dep.from);
        const toFeatureIndex = features.findIndex((f) => f.id === dep.to);
        const fromY = fromFeatureIndex * 40 + 20;
        const toY = toFeatureIndex * 40 + 20;

        return (
          <path
            key={i}
            d={`M ${fromX + 80} ${fromY} C ${fromX + 120} ${fromY}, ${toX - 40} ${toY}, ${toX} ${toY}`}
            fill="none"
            stroke="#94a3b8"
            strokeWidth="1.5"
            strokeDasharray="4 2"
            markerEnd="url(#arrowhead)"
          />
        );
      })}
    </svg>
  );
}
