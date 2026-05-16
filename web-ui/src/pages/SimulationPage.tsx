import { useState } from 'react';
import { PlanComparison } from '../components/planning/PlanComparison';
import { Button } from '../components/common/Button';
import { planningApi } from '../api/planning';
import type { PlanningResult, PlanningJob, SimulationChanges, PlanningRunRequest } from '../types';

export function SimulationPage() {
  const [baselinePlan, setBaselinePlan] = useState<PlanningResult | null>(null);
  const [simulatedPlan, setSimulatedPlan] = useState<PlanningResult | null>(null);
  const [changes, setChanges] = useState<SimulationChanges>({});
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleRunSimulation = async () => {
    try {
      setIsRunning(true);
      setError(null);

      const request: PlanningRunRequest = {
        featureIds: [],
        teamIds: [],
        parameters: {
          w1Ttm: changes.priorityChanges ? 1.5 : 1.0,
          w2Underutilization: 0.5,
          w3DeadlinePenalty: 2.0,
        },
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
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              rows={3}
              placeholder="JSON: {&quot;feature-id&quot;: newPriority}"
              value={JSON.stringify(changes.priorityChanges || {}, null, 2)}
              onChange={(e) => {
                try {
                  setChanges((prev) => ({ ...prev, priorityChanges: JSON.parse(e.target.value) }));
                } catch {
                  // Invalid JSON, ignore
                }
              }}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Capacity Changes (%)</label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
              rows={3}
              placeholder="JSON: {&quot;team-id&quot;: capacityMultiplier}"
              value={JSON.stringify(changes.capacityChanges || {}, null, 2)}
              onChange={(e) => {
                try {
                  setChanges((prev) => ({ ...prev, capacityChanges: JSON.parse(e.target.value) }));
                } catch {
                  // Invalid JSON, ignore
                }
              }}
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          <Button onClick={handleRunSimulation} loading={isRunning}>
            Run Simulation
          </Button>
          {error && <span className="text-sm text-red-600">{error}</span>}
        </div>
      </div>

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
          Run a simulation to see team load comparison
        </div>
      )}
    </div>
  );
}
