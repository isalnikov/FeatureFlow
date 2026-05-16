import type { Assignment, Feature, Team, Sprint } from '../../types';
import { Badge } from '../common/Badge';

interface SprintTableProps {
  sprints: Sprint[];
  assignments: Assignment[];
  features: Feature[];
  teams: Team[];
}

const statusColors: Record<string, 'default' | 'info' | 'success' | 'warning'> = {
  PLANNED: 'info',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  LOCKED: 'default',
};

export function SprintTable({ sprints, assignments, features, teams }: SprintTableProps) {
  const featureMap = new Map(features.map((f) => [f.id, f]));
  const teamMap = new Map(teams.map((t) => [t.id, t]));

  const assignmentsBySprint = assignments.reduce((acc, a) => {
    if (!acc[a.sprintId]) acc[a.sprintId] = [];
    acc[a.sprintId].push(a);
    return acc;
  }, {} as Record<string, Assignment[]>);

  if (sprints.length === 0) {
    return (
      <div className="flex items-center justify-center py-8 text-gray-400">
        No sprints configured
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sprint</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Dates</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Features</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Teams</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {sprints.map((sprint) => {
            const sprintAssignments = assignmentsBySprint[sprint.id] || [];
            const uniqueTeams = new Set(sprintAssignments.map((a) => a.teamId));

            return (
              <tr key={sprint.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-sm font-medium text-gray-900">{sprint.label}</td>
                <td className="px-4 py-3 text-sm text-gray-500">
                  {sprint.startDate} → {sprint.endDate}
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {sprintAssignments.map((a) => (
                    <div key={a.id} className="truncate max-w-xs">
                      {featureMap.get(a.featureId)?.title || a.featureId}
                    </div>
                  ))}
                  {sprintAssignments.length === 0 && <span className="text-gray-400">—</span>}
                </td>
                <td className="px-4 py-3 text-sm text-gray-900">
                  {Array.from(uniqueTeams).map((teamId) => (
                    <Badge key={teamId} variant="info" size="sm" className="mr-1">
                      {teamMap.get(teamId)?.name || teamId}
                    </Badge>
                  ))}
                  {uniqueTeams.size === 0 && <span className="text-gray-400">—</span>}
                </td>
                <td className="px-4 py-3 text-sm">
                  {sprintAssignments.length > 0 && (
                    <Badge variant={statusColors[sprintAssignments[0].status] || 'default'}>
                      {sprintAssignments[0].status}
                    </Badge>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
