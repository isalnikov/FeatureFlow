import type { PlanningResult } from '../../types';

interface PlanComparisonProps {
  baseline: PlanningResult;
  simulated: PlanningResult;
}

export function PlanComparison({ baseline, simulated }: PlanComparisonProps) {
  const metrics = [
    {
      label: 'Total Cost',
      baseline: baseline.totalCost.toFixed(0),
      simulated: simulated.totalCost.toFixed(0),
      better: (a: number, b: number) => a < b,
    },
    {
      label: 'Conflicts',
      baseline: baseline.conflicts.length.toString(),
      simulated: simulated.conflicts.length.toString(),
      better: (a: number, b: number) => a < b,
    },
    {
      label: 'Computation Time',
      baseline: `${baseline.computationTimeMs}ms`,
      simulated: `${simulated.computationTimeMs}ms`,
      better: (a: number, b: number) => a < b,
    },
    {
      label: 'Algorithm',
      baseline: baseline.algorithm,
      simulated: simulated.algorithm,
    },
  ];

  const baselineAssignments = baseline.assignments.length;
  const simulatedAssignments = simulated.assignments.length;
  const changes = simulatedAssignments - baselineAssignments;

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <h3 className="text-sm font-medium text-gray-900 mb-4">Plan Comparison</h3>

      <div className="grid grid-cols-3 gap-4 mb-4">
        <div className="text-xs font-medium text-gray-500 uppercase">Metric</div>
        <div className="text-xs font-medium text-gray-500 uppercase">Baseline</div>
        <div className="text-xs font-medium text-gray-500 uppercase">Simulated</div>

        {metrics.map((metric) => (
          <div key={metric.label} className="contents">
            <div className="text-sm text-gray-900">{metric.label}</div>
            <div className="text-sm text-gray-700">{metric.baseline}</div>
            <div className="text-sm text-gray-700">{metric.simulated}</div>
          </div>
        ))}
      </div>

      <div className="pt-4 border-t border-gray-200">
        <div className="text-sm text-gray-700">
          Assignments: {baselineAssignments} → {simulatedAssignments}
          <span className={`ml-2 ${changes > 0 ? 'text-amber-600' : changes < 0 ? 'text-green-600' : 'text-gray-500'}`}>
            ({changes > 0 ? '+' : ''}{changes})
          </span>
        </div>
      </div>
    </div>
  );
}
