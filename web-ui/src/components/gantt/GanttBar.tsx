import { useState, DragEvent } from 'react';
import { addDays, format } from 'date-fns';
import type { Feature, Assignment, FeatureTimeline } from '../../types';
import clsx from 'clsx';

interface GanttBarProps {
  feature: Feature;
  timeline: FeatureTimeline | undefined;
  assignments: Assignment[];
  isDragged: boolean;
  onDragStart: () => void;
  onDragEnd: (sprintId: string) => void;
  onClick: () => void;
  dayWidth: number;
  totalDays: number;
  startDate: Date;
}

const classOfServiceColors: Record<string, string> = {
  EXPEDITE: 'bg-red-500',
  FIXED_DATE: 'bg-amber-500',
  STANDARD: 'bg-blue-500',
  FILLER: 'bg-gray-400',
};

const statusBorderColors: Record<string, string> = {
  onTrack: 'border-green-500',
  atRisk: 'border-amber-500',
  blocked: 'border-red-500',
  completed: 'border-indigo-500',
};

export function GanttBar({
  feature,
  timeline,
  assignments,
  isDragged,
  onDragStart,
  onDragEnd,
  onClick,
  dayWidth,
  totalDays,
  startDate,
}: GanttBarProps) {
  const [isDragging, setIsDragging] = useState(false);

  if (!timeline) {
    return (
      <div className="flex items-center h-10 border-b border-gray-100">
        <div className="w-64 flex-shrink-0 px-4 py-2 text-sm text-gray-400 truncate border-r border-gray-200">
          {feature.title}
        </div>
        <div className="flex-1 relative h-full">
          <div className="absolute left-4 top-1/2 -translate-y-1/2">
            <span className="text-xs text-gray-400 italic">Unscheduled</span>
          </div>
        </div>
      </div>
    );
  }

  const start = new Date(timeline.startDate);
  const end = new Date(timeline.endDate);
  const startOffset = Math.max(0, Math.floor((start.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)));
  const duration = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));

  const left = startOffset * dayWidth;
  const width = Math.max(duration * dayWidth, dayWidth);

  const probabilityColor =
    timeline.probabilityOfMeetingDeadline >= 0.8
      ? 'text-green-600'
      : timeline.probabilityOfMeetingDeadline >= 0.5
        ? 'text-amber-600'
        : 'text-red-600';

  const handleDragStart = (e: DragEvent) => {
    setIsDragging(true);
    onDragStart();
    e.dataTransfer.setData('featureId', feature.id);
  };

  const handleDragEnd = (e: DragEvent) => {
    setIsDragging(false);
    const dropX = e.clientX;
    const dropDate = addDays(startDate, Math.floor((dropX - 256) / dayWidth));
    onDragEnd(format(dropDate, 'yyyy-MM-dd'));
  };

  return (
    <div className="flex items-center h-10 border-b border-gray-100">
      <div className="w-64 flex-shrink-0 px-4 py-2 text-sm text-gray-900 truncate border-r border-gray-200">
        {feature.title}
      </div>
      <div className="flex-1 relative h-full">
        <div
          draggable
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onClick={onClick}
          className={clsx(
            'absolute top-1/2 -translate-y-1/2 h-6 rounded-sm cursor-pointer transition-shadow border-l-2',
            classOfServiceColors[feature.classOfService] || 'bg-blue-500',
            statusBorderColors['onTrack'] || 'border-blue-500',
            isDragged && 'opacity-50',
            isDragging && 'shadow-lg ring-2 ring-brand-500',
            'hover:shadow-md',
          )}
          style={{ left: `${left}px`, width: `${width}px` }}
          title={`${feature.title}\n${timeline.startDate} → ${timeline.endDate}\nDeadline probability: ${(timeline.probabilityOfMeetingDeadline * 100).toFixed(0)}%`}
        >
          {width > 60 && (
            <span className="px-1 text-xs text-white truncate block leading-6">
              {feature.title}
            </span>
          )}
        </div>
        {timeline.probabilityOfMeetingDeadline < 1 && (
          <div
            className={clsx('absolute top-1/2 -translate-y-1/2 text-xs font-medium', probabilityColor)}
            style={{ left: `${left + width + 4}px` }}
          >
            {Math.round(timeline.probabilityOfMeetingDeadline * 100)}%
          </div>
        )}
      </div>
    </div>
  );
}
