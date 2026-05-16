import { format, eachDayOfInterval } from 'date-fns';
import { GanttZoomLevel } from '../../store/uiSlice';

interface GanttTimelineProps {
  startDate: Date;
  endDate: Date;
  zoom: GanttZoomLevel;
}

export function GanttTimeline({ startDate, endDate, zoom }: GanttTimelineProps) {
  const days = eachDayOfInterval({ start: startDate, end: endDate });

  const getHeaderLabel = (date: Date) => {
    switch (zoom) {
      case 'day':
        return format(date, 'dd');
      case 'week':
        return format(date, 'ww');
      case 'sprint':
        return `S${Math.ceil((date.getDate()) / 14)}`;
      case 'month':
        return format(date, 'MMM');
      default:
        return format(date, 'dd');
    }
  };

  const getCellWidth = () => {
    switch (zoom) {
      case 'day':
        return 'w-8 min-w-[2rem]';
      case 'week':
        return 'w-16 min-w-[4rem]';
      case 'sprint':
        return 'w-24 min-w-[6rem]';
      case 'month':
        return 'w-32 min-w-[8rem]';
      default:
        return 'w-8 min-w-[2rem]';
    }
  };

  return (
    <div className="flex border-b border-gray-200 bg-gray-50 sticky top-0 z-10">
      <div className="w-64 flex-shrink-0 px-4 py-2 text-xs font-medium text-gray-500 border-r border-gray-200">
        Feature
      </div>
      <div className="flex-1 overflow-hidden">
        <div className="flex">
          {days.map((day, i) => (
            <div
              key={i}
              className={`${getCellWidth()} text-center text-xs text-gray-500 py-2 border-r border-gray-100 ${
                day.getDay() === 0 || day.getDay() === 6 ? 'bg-gray-100' : ''
              }`}
            >
              {getHeaderLabel(day)}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
