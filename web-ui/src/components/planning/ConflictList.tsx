import type { Conflict } from '../../types';
import { Badge } from '../common/Badge';
import { Button } from '../common/Button';

interface ConflictListProps {
  conflicts: Conflict[];
  onResolve?: (conflict: Conflict) => void;
  title?: string;
}

const conflictSeverity: Record<Conflict['type'], 'error' | 'warning' | 'info'> = {
  CAPACITY_EXCEEDED: 'error',
  DEPENDENCY_VIOLATION: 'error',
  OWNERSHIP_VIOLATION: 'error',
  DEADLINE_MISSED: 'warning',
  PARALLELISM_EXCEEDED: 'warning',
  DEPENDENCY_CYCLE: 'error',
};

const conflictLabels: Record<Conflict['type'], string> = {
  CAPACITY_EXCEEDED: 'Capacity Exceeded',
  DEPENDENCY_VIOLATION: 'Dependency Violation',
  OWNERSHIP_VIOLATION: 'Ownership Violation',
  DEADLINE_MISSED: 'Deadline Missed',
  PARALLELISM_EXCEEDED: 'Parallelism Exceeded',
  DEPENDENCY_CYCLE: 'Dependency Cycle',
};

export function ConflictList({ conflicts, onResolve, title = 'Conflicts' }: ConflictListProps) {
  if (conflicts.length === 0) {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-4">
        <h3 className="text-sm font-medium text-gray-900 mb-2">{title}</h3>
        <div className="flex items-center gap-2 py-4 text-green-600">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          <span className="text-sm">No conflicts detected</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-medium text-gray-900">{title}</h3>
        <Badge variant="error">{conflicts.length}</Badge>
      </div>
      <ul className="space-y-2">
        {conflicts.map((conflict, index) => (
          <li
            key={index}
            className="flex items-start gap-3 p-3 rounded-md bg-gray-50 border border-gray-100"
          >
            <Badge variant={conflictSeverity[conflict.type]} size="sm">
              {conflictLabels[conflict.type]}
            </Badge>
            <span className="flex-1 text-sm text-gray-700">{conflict.message}</span>
            {onResolve && (
              <Button variant="ghost" size="sm" onClick={() => onResolve(conflict)}>
                Resolve
              </Button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
