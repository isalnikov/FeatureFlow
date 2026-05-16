import { useRef, useState, useMemo } from 'react';
import { format, addDays } from 'date-fns';
import type { Feature, Assignment, PlanningResult } from '../../types';
import { GanttBar } from './GanttBar';
import { GanttTimeline } from './GanttTimeline';
import { GanttDependencies } from './GanttDependencies';
import { GanttToolbar } from './GanttToolbar';
import { useSelector, useDispatch } from 'react-redux';
import type { RootState } from '../../store';
import { setGanttZoom } from '../../store/uiSlice';

interface GanttChartProps {
  features: Feature[];
  assignments: Assignment[];
  planningResult: PlanningResult | null;
  onDragEnd: (featureId: string, newSprintId: string) => void;
  onFeatureClick: (featureId: string) => void;
  onRunPlanning?: () => void;
  isPlanning?: boolean;
}

export function GanttChart({
  features,
  assignments,
  planningResult,
  onDragEnd,
  onFeatureClick,
  onRunPlanning,
  isPlanning,
}: GanttChartProps) {
  const dispatch = useDispatch();
  const zoom = useSelector((state: RootState) => state.ui.ganttZoom);
  const containerRef = useRef<HTMLDivElement>(null);
  const [draggedFeature, setDraggedFeature] = useState<string | null>(null);

  const dayWidth = useMemo(() => {
    switch (zoom) {
      case 'day':
        return 32;
      case 'week':
        return 64;
      case 'sprint':
        return 96;
      case 'month':
        return 128;
      default:
        return 32;
    }
  }, [zoom]);

  const { startDate, endDate, totalDays } = useMemo(() => {
    if (!planningResult || features.length === 0) {
      const start = new Date();
      const end = addDays(start, 90);
      return { startDate: start, endDate: end, totalDays: 90 };
    }

    const timelines = Object.values(planningResult.featureTimelines);
    if (timelines.length === 0) {
      const start = new Date();
      const end = addDays(start, 90);
      return { startDate: start, endDate: end, totalDays: 90 };
    }

    const dates = timelines.flatMap((t) => [new Date(t.startDate), new Date(t.endDate)]);
    const start = new Date(Math.min(...dates.map((d) => d.getTime())));
    const end = new Date(Math.max(...dates.map((d) => d.getTime())));
    const paddedEnd = addDays(end, 14);
    return {
      startDate: start,
      endDate: paddedEnd,
      totalDays: Math.ceil((paddedEnd.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)),
    };
  }, [planningResult, features]);

  const featureAssignments = useMemo(() => {
    return assignments.reduce((acc, assignment) => {
      if (!acc[assignment.featureId]) {
        acc[assignment.featureId] = [];
      }
      acc[assignment.featureId].push(assignment);
      return acc;
    }, {} as Record<string, Assignment[]>);
  }, [assignments]);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    const featureId = e.dataTransfer.getData('featureId');
    if (featureId) {
      onDragEnd(featureId, 'dropped-sprint');
    }
    setDraggedFeature(null);
  };

  return (
    <div className="flex flex-col h-full bg-white rounded-lg shadow-sm border border-gray-200">
      <GanttToolbar
        zoom={zoom}
        onZoomChange={(z) => dispatch(setGanttZoom(z))}
        onRunPlanning={onRunPlanning}
        isPlanning={isPlanning}
      />

      <div ref={containerRef} className="flex-1 overflow-auto" onDrop={handleDrop} onDragOver={(e) => e.preventDefault()}>
        <div className="min-w-fit">
          <GanttTimeline startDate={startDate} endDate={endDate} zoom={zoom} />

          <div className="relative">
            {features.map((feature) => {
              const timeline = planningResult?.featureTimelines[feature.id];
              const assigns = featureAssignments[feature.id] || [];

              return (
                <GanttBar
                  key={feature.id}
                  feature={feature}
                  timeline={timeline}
                  assignments={assigns}
                  isDragged={draggedFeature === feature.id}
                  onDragStart={() => setDraggedFeature(feature.id)}
                  onDragEnd={(sprintId) => onDragEnd(feature.id, sprintId)}
                  onClick={() => onFeatureClick(feature.id)}
                  dayWidth={dayWidth}
                  totalDays={totalDays}
                  startDate={startDate}
                />
              );
            })}

            {planningResult && (
              <GanttDependencies
                features={features}
                timelines={planningResult.featureTimelines}
                dayWidth={dayWidth}
                startDate={startDate}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
