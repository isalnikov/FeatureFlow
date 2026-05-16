import { useState, useEffect } from 'react';
import { planningApi } from '../../api/planning';
import { useSSE } from '../../hooks/useSSE';
import type { PlanningParameters, PlanningRunRequest } from '../../types';

interface PlanningControlsProps {
  featureIds: string[];
  planningWindowId: string;
  onPlanningComplete: (result: unknown) => void;
}

const defaultParameters: PlanningParameters = {
  w1Ttm: 1.0,
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
};

export function PlanningControls({ featureIds, planningWindowId, onPlanningComplete }: PlanningControlsProps) {
  const [isPlanning, setIsPlanning] = useState(false);
  const [jobId, setJobId] = useState<string | null>(null);
  const [progress, setProgress] = useState(0);
  const [phase, setPhase] = useState<string | null>(null);

  const { status } = useSSE(jobId ? `/planning/jobs/${jobId}/sse` : null);

  useEffect(() => {
    if (status) {
      setProgress(status.progressPercent || 0);
      setPhase(status.phase || null);

      if (status.phase === 'DONE') {
        setIsPlanning(false);
        onPlanningComplete(status);
      }
    }
  }, [status, onPlanningComplete]);

  const handleRunPlanning = async () => {
    setIsPlanning(true);
    setProgress(0);
    setPhase('Starting...');

    const request: PlanningRunRequest = {
      featureIds,
      planningWindowId,
      parameters: defaultParameters,
      lockedAssignmentIds: [],
    };

    try {
      const job = await planningApi.run(request);
      setJobId(job.jobId);
    } catch {
      setIsPlanning(false);
      setPhase('Failed to start planning');
    }
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div className="flex items-center gap-4">
        <button
          onClick={handleRunPlanning}
          disabled={isPlanning || featureIds.length === 0}
          className="px-4 py-2 bg-brand-600 text-white text-sm font-medium rounded-md hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isPlanning ? 'Planning...' : 'Run Planning'}
        </button>

        {isPlanning && (
          <div className="flex-1">
            <div className="flex items-center gap-3">
              <div className="flex-1 bg-gray-200 rounded-full h-2">
                <div
                  className="bg-brand-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <span className="text-sm text-gray-600 whitespace-nowrap">
                {phase}: {progress}%
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
