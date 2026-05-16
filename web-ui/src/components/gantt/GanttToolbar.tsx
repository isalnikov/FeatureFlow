import { GanttZoomLevel } from '../../store/uiSlice';

interface GanttToolbarProps {
  zoom: GanttZoomLevel;
  onZoomChange: (zoom: GanttZoomLevel) => void;
  onRunPlanning?: () => void;
  isPlanning?: boolean;
}

const zoomLevels: { value: GanttZoomLevel; label: string }[] = [
  { value: 'day', label: 'Day' },
  { value: 'week', label: 'Week' },
  { value: 'sprint', label: 'Sprint' },
  { value: 'month', label: 'Month' },
];

export function GanttToolbar({ zoom, onZoomChange, onRunPlanning, isPlanning }: GanttToolbarProps) {
  return (
    <div className="flex items-center justify-between px-4 py-2 bg-white border-b border-gray-200">
      <div className="flex items-center gap-2">
        <span className="text-sm text-gray-500">Zoom:</span>
        <div className="flex rounded-md shadow-sm">
          {zoomLevels.map((level) => (
            <button
              key={level.value}
              onClick={() => onZoomChange(level.value)}
              className={`px-3 py-1.5 text-xs font-medium border transition-colors ${
                zoom === level.value
                  ? 'bg-brand-600 text-white border-brand-600'
                  : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
              } ${level.value === 'day' ? 'rounded-l-md' : ''} ${level.value === 'month' ? 'rounded-r-md' : ''}`}
            >
              {level.label}
            </button>
          ))}
        </div>
      </div>

      {onRunPlanning && (
        <button
          onClick={onRunPlanning}
          disabled={isPlanning}
          className="px-4 py-2 bg-brand-600 text-white text-sm font-medium rounded-md hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {isPlanning ? 'Planning...' : 'Run Planning'}
        </button>
      )}
    </div>
  );
}
