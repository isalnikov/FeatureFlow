import { useState } from 'react';
import { useSelector } from 'react-redux';
import type { RootState } from '../store';
import { useAppDispatch } from '../store';
import { setSelectedFeature } from '../store/uiSlice';
import { useFeatures } from '../hooks/useFeatures';
import { useAssignments } from '../hooks/usePlanning';
import { runPlanning } from '../store/planningSlice';
import { assignmentsApi } from '../api/assignments';
import { GanttChart } from '../components/gantt/GanttChart';
import { PlanningControls } from '../components/planning/PlanningControls';
import { TeamLoadChart } from '../components/planning/TeamLoadChart';
import { ConflictList } from '../components/planning/ConflictList';
import { FeatureDetail } from '../components/features/FeatureDetail';
import { FeatureForm } from '../components/features/FeatureForm';
import { Modal } from '../components/common/Modal';
import { Loading } from '../components/common/Loading';

export function PlanPage() {
  const dispatch = useAppDispatch();
  const selectedFeatureId = useSelector((state: RootState) => state.ui.selectedFeatureId);
  const planningState = useSelector((state: RootState) => state.planning);

  const { data: features, loading: featuresLoading } = useFeatures();
  const { data: assignments, loading: assignmentsLoading, refetch: refetchAssignments } = useAssignments();

  const [showCreateModal, setShowModal] = useState(false);
  const [dragError, setDragError] = useState<string | null>(null);

  const featureList = features?.content || [];
  const featureIds = featureList.map((f) => f.id);

  const handleDragEnd = async (featureId: string, newSprintId: string) => {
    setDragError(null);
    const featureAssignment = assignments?.find((a) => a.featureId === featureId);
    if (!featureAssignment) {
      setDragError('No assignment found for this feature');
      return;
    }
    try {
      await assignmentsApi.update(featureAssignment.id, {
        sprintId: newSprintId,
      });
      refetchAssignments();
    } catch (err) {
      setDragError(err instanceof Error ? err.message : 'Failed to update assignment');
    }
  };

  const handleRunPlanning = () => {
    dispatch(
      runPlanning({
        featureIds,
        planningWindowId: 'current',
        parameters: {
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
        },
        lockedAssignmentIds: [],
      }),
    );
  };

  const handlePlanningComplete = () => {
    refetchAssignments();
  };

  const selectedFeature = featureList.find((f) => f.id === selectedFeatureId);

  if (featuresLoading || assignmentsLoading) {
    return <Loading fullScreen label="Loading plan..." />;
  }

  return (
    <div className="h-full flex flex-col">
      <div className="mb-4 space-y-2">
        <PlanningControls
          featureIds={featureIds}
          planningWindowId="current"
          onPlanningComplete={handlePlanningComplete}
        />
        {dragError && (
          <div className="bg-red-50 border border-red-200 rounded-md p-3 text-sm text-red-700">
            {dragError}
          </div>
        )}
        {planningState.error && (
          <div className="bg-red-50 border border-red-200 rounded-md p-3 text-sm text-red-700">
            {planningState.error}
          </div>
        )}
      </div>

      <div className="flex-1 flex gap-4 min-h-0">
        <div className="flex-1 min-w-0">
          <GanttChart
            features={featureList}
            assignments={assignments || []}
            planningResult={planningState.currentResult}
            onDragEnd={handleDragEnd}
            onFeatureClick={(id) => dispatch(setSelectedFeature(id))}
            onRunPlanning={handleRunPlanning}
            isPlanning={planningState.isPlanning}
          />
        </div>

        <div className="w-80 flex-shrink-0 overflow-auto space-y-4">
          {selectedFeature && (
            <FeatureDetail
              feature={selectedFeature}
              onClose={() => dispatch(setSelectedFeature(null))}
            />
          )}
          {planningState.currentResult && (
            <>
              <TeamLoadChart
                reports={Object.values(planningState.currentResult.teamLoadReports)}
                title="Team Load"
              />
              <ConflictList conflicts={planningState.currentResult.conflicts} />
            </>
          )}
        </div>
      </div>

      <Modal
        isOpen={showCreateModal}
        onClose={() => setShowModal(false)}
        title="Create Feature"
        size="lg"
      >
        <FeatureForm
          products={[]}
          teams={[]}
          onSubmit={() => setShowModal(false)}
          onCancel={() => setShowModal(false)}
        />
      </Modal>
    </div>
  );
}
