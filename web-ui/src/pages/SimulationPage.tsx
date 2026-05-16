import { useState } from 'react';
import { PlanningControls } from '../components/planning/PlanningControls';
import { PlanComparison } from '../components/planning/PlanComparison';
import { TeamLoadChart } from '../components/planning/TeamLoadChart';
import { Button } from '../components/common/Button';
import type { PlanningResult, SimulationChanges } from '../types';

export function SimulationPage() {
  const [baselinePlan, setBaselinePlan] = useState<PlanningResult | null>(null);
  const [simulatedPlan, setSimulatedPlan] = useState<PlanningResult | null>(null);
  const [changes, setChanges] = useState<SimulationChanges>({});
  const [isRunning, setIsRunning] = useState(false);

  const handleRunSimulation = async () => {
    setIsRunning(true);
    // TODO: Implement simulation logic
    // 1. Clone current plan as baseline
    // 2. Apply changes
    // 3. Run planning with modified parameters
    // 4. Compare results
    setTimeout(() => {
      setIsRunning(false);
    }, 2000);
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

        <Button onClick={handleRunSimulation} loading={isRunning}>
          Run Simulation
        </Button>
      </div>

      {baselinePlan && simulatedPlan && (
        <PlanComparison baseline={baselinePlan} simulated={simulatedPlan} />
      )}

      <div className="mt-6">
        <TeamLoadChart reports={[]} title="Simulated Team Load" />
      </div>
    </div>
  );
}
