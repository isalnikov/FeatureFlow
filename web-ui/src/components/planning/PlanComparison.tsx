import type { PlanningResult } from '../../types';

interface PlanComparisonProps {
  baseline: PlanningResult;
  simulated: PlanningResult;
}

export function PlanComparison({ baseline, simulated }: PlanComparisonProps) {
  const costDelta = simulated.totalCost - baseline.totalCost;
  const conflictDelta = simulated.conflicts.length - baseline.conflicts.length;
  const assignmentDelta = simulated.assignments.length - baseline.assignments.length;

  const timelineEntries = Object.entries(simulated.featureTimelines);
  const changedTimelines = timelineEntries.filter(([id, simTimeline]) => {
    const baseTimeline = baseline.featureTimelines[id];
    if (!baseTimeline) return true;
    return baseTimeline.startDate !== simTimeline.startDate || baseTimeline.endDate !== simTimeline.endDate;
  });

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <h3 className="text-sm font-medium text-gray-900 mb-4">Plan Comparison</h3>

      <div className="grid grid-cols-4 gap-4 mb-6">
        <div className="bg-gray-50 rounded-md p-3">
          <div className="text-xs text-gray-500">Total Cost</div>
          <div className="text-lg font-bold">{baseline.totalCost.toFixed(0)} → {simulated.totalCost.toFixed(0)}</div>
          <div className={`text-xs font-medium ${costDelta < 0 ? 'text-green-600' : costDelta > 0 ? 'text-red-600' : 'text-gray-500'}`}>
            {costDelta > 0 ? '+' : ''}{costDelta.toFixed(0)}
          </div>
        </div>
        <div className="bg-gray-50 rounded-md p-3">
          <div className="text-xs text-gray-500">Conflicts</div>
          <div className="text-lg font-bold">{baseline.conflicts.length} → {simulated.conflicts.length}</div>
          <div className={`text-xs font-medium ${conflictDelta < 0 ? 'text-green-600' : conflictDelta > 0 ? 'text-red-600' : 'text-gray-500'}`}>
            {conflictDelta > 0 ? '+' : ''}{conflictDelta}
          </div>
        </div>
        <div className="bg-gray-50 rounded-md p-3">
          <div className="text-xs text-gray-500">Assignments</div>
          <div className="text-lg font-bold">{baseline.assignments.length} → {simulated.assignments.length}</div>
          <div className="text-xs font-medium text-gray-500">
            {assignmentDelta > 0 ? '+' : ''}{assignmentDelta}
          </div>
        </div>
        <div className="bg-gray-50 rounded-md p-3">
          <div className="text-xs text-gray-500">Timeline Changes</div>
          <div className="text-lg font-bold">{changedTimelines.length}</div>
          <div className="text-xs font-medium text-gray-500">features shifted</div>
        </div>
      </div>

      {changedTimelines.length > 0 && (
        <div className="pt-4 border-t border-gray-200">
          <h4 className="text-xs font-medium text-gray-500 uppercase mb-2">Feature Timeline Diffs</h4>
          <div className="space-y-1 max-h-48 overflow-y-auto">
            {changedTimelines.slice(0, 10).map(([id, simTimeline]) => {
              const baseTimeline = baseline.featureTimelines[id];
              const shifted = baseTimeline
                ? Math.round((new Date(simTimeline.startDate).getTime() - new Date(baseTimeline.startDate).getTime()) / 86400000)
                : 0;
              return (
                <div key={id} className="flex items-center gap-3 text-sm">
                  <span className="text-gray-700 font-mono text-xs w-20 truncate">{id.slice(0, 8)}</span>
                  <span className="text-gray-500 text-xs">
                    {baseTimeline?.startDate} → {simTimeline.startDate}
                  </span>
                  <span className={`text-xs font-medium ${shifted > 0 ? 'text-red-600' : shifted < 0 ? 'text-green-600' : 'text-gray-500'}`}>
                    {shifted > 0 ? '+' : ''}{shifted}d
                  </span>
                </div>
              );
            })}
            {changedTimelines.length > 10 && (
              <div className="text-xs text-gray-400">...and {changedTimelines.length - 10} more</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
