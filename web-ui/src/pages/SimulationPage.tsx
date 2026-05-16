import { useState } from 'react';
import { PlanComparison } from '../components/planning/PlanComparison';
import { Button } from '../components/common/Button';
import { planningApi } from '../api/planning';
import type { PlanningResult, PlanningRunRequest } from '../types';
import type { ComparisonResult } from '../api/planning';

export function SimulationPage() {
  const [baselinePlan, setBaselinePlan] = useState<PlanningResult | null>(null);
  const [simulatedPlan, setSimulatedPlan] = useState<PlanningResult | null>(null);
  const [comparison, setComparison] = useState<ComparisonResult | null>(null);
  const [priorityChangesJson, setPriorityChangesJson] = useState('');
  const [capacityChangesJson, setCapacityChangesJson] = useState('');
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRunSimulation = async () => {
    try {
      setIsRunning(true);
      setError(null);
      setComparison(null);

      let priorityChanges: Record<string, number> = {};
      try {
        if (priorityChangesJson.trim()) priorityChanges = JSON.parse(priorityChangesJson);
      } catch { /* ignore */ }
      try {
        if (capacityChangesJson.trim()) JSON.parse(capacityChangesJson);
      } catch { /* ignore */ }

      const request: PlanningRunRequest = {
        featureIds: [],
        teamIds: [],
        planningWindowId: 'current',
        parameters: {
          w1Ttm: Object.keys(priorityChanges).length > 0 ? 1.5 : 1.0,
          w2Underutilization: 0.5,
          w3DeadlinePenalty: 2.0,
          maxParallelFeatures: 3,
          annealing: {
            initialTemperature: 1000.0,
            coolingRate: 0.95,
            minTemperature: 0.1,
            maxIterations: 10000,
          },
          monteCarlo: {
            iterations: 1000,
            confidenceLevel: 0.95,
          },
        },
        lockedAssignmentIds: [],
      };

      const job = await planningApi.run(request);

      while (true) {
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const status = await planningApi.getJobStatus(job.jobId);
        if (status.status === 'DONE' || status.status === 'FAILED') break;
      }

      const result = await planningApi.getResult(job.jobId);

      if (!baselinePlan) {
        setBaselinePlan(result);
      } else {
        setSimulatedPlan(result);
        const comp = await planningApi.compareResults(baselinePlan.assignments[0]?.id || '', job.jobId);
        setComparison(comp);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Simulation failed');
    } finally {
      setIsRunning(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">What-If Simulation</h1>

      <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
        <h3 className="text-sm font-medium text-gray-900 mb-4">Simulation Parameters</h3>

        <div className="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Priority Changes</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm font-mono"
              rows={3}
              placeholder='{"feature-id": 75.0}'
              value={priorityChangesJson}
              onChange={(e) => setPriorityChangesJson(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Capacity Multipliers</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm font-mono"
              rows={3}
              placeholder='{"team-id": 1.2}'
              value={capacityChangesJson}
              onChange={(e) => setCapacityChangesJson(e.target.value)}
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button onClick={handleRunSimulation} loading={isRunning}>
            {baselinePlan ? 'Run Simulation' : 'Run Baseline'}
          </Button>
          {baselinePlan && (
            <Button variant="ghost" size="sm" onClick={() => {
              setBaselinePlan(null);
              setSimulatedPlan(null);
              setComparison(null);
            }}>
              Reset
            </Button>
          )}
          {error && <span className="text-sm text-red-600">{error}</span>}
        </div>
      </div>

      {comparison && (
        <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
          <h3 className="text-sm font-medium text-gray-900 mb-4">Comparison Results</h3>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <div className="text-xs text-gray-500">Cost Delta</div>
              <div className={`text-lg font-bold ${comparison.costDelta < 0 ? 'text-green-600' : 'text-red-600'}`}>
                {comparison.costDelta > 0 ? '+' : ''}{comparison.costDelta.toFixed(1)}
              </div>
            </div>
            <div>
              <div className="text-xs text-gray-500">Conflicts</div>
              <div className="text-lg font-bold">
                {comparison.baselineConflicts} → {comparison.simulationConflicts}
              </div>
            </div>
            <div>
              <div className="text-xs text-gray-500">Timeline Shifts</div>
              <div className="text-lg font-bold">{comparison.timelineDiffs.length} features</div>
            </div>
          </div>
        </div>
      )}

      {baselinePlan && simulatedPlan && (
        <PlanComparison baseline={baselinePlan} simulated={simulatedPlan} />
      )}

      {baselinePlan && !simulatedPlan && (
        <div className="mt-6 text-sm text-gray-500">
          Baseline plan loaded. Adjust parameters and run again to compare.
        </div>
      )}

      {!baselinePlan && (
        <div className="mt-6 text-sm text-gray-400 italic">
          Run a baseline plan first, then adjust parameters and run a simulation to compare.
        </div>
      )}
    </div>
  );
}
