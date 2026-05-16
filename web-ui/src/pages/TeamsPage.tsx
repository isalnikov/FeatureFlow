import { useState } from 'react';
import { useTeams } from '../hooks/useTeams';
import { Table, Column } from '../components/common/Table';
import { Badge } from '../components/common/Badge';
import { Modal } from '../components/common/Modal';
import { Loading } from '../components/common/Loading';
import type { Team, Role } from '../types';

const roleColors: Record<Role, 'info' | 'success' | 'warning' | 'default'> = {
  BACKEND: 'info',
  FRONTEND: 'success',
  QA: 'warning',
  DEVOPS: 'default',
};

export function TeamsPage() {
  const { data: teams, loading, error } = useTeams();
  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);

  const columns: Column<Team>[] = [
    {
      key: 'name',
      header: 'Team',
      render: (team) => (
        <div>
          <div className="font-medium text-gray-900">{team.name}</div>
          <div className="text-xs text-gray-500">{team.members.length} members</div>
        </div>
      ),
    },
    {
      key: 'members',
      header: 'Roles',
      render: (team) => (
        <div className="flex flex-wrap gap-1">
          {Array.from(new Set(team.members.map((m) => m.role))).map((role) => (
            <Badge key={role} variant={roleColors[role]} size="sm">
              {role}
            </Badge>
          ))}
        </div>
      ),
    },
    {
      key: 'focusFactor',
      header: 'Focus Factor',
      render: (team) => <span className="font-mono text-sm">{(team.focusFactor * 100).toFixed(0)}%</span>,
    },
    {
      key: 'velocity',
      header: 'Velocity',
      render: (team) => (
        <span className="font-mono text-sm">{team.velocity !== null ? team.velocity : '—'}</span>
      ),
    },
    {
      key: 'expertise',
      header: 'Expertise',
      render: (team) => (
        <div className="flex flex-wrap gap-1">
          {team.expertiseTags.slice(0, 3).map((tag) => (
            <span key={tag} className="px-1.5 py-0.5 bg-gray-100 rounded text-xs">
              {tag}
            </span>
          ))}
          {team.expertiseTags.length > 3 && (
            <span className="text-xs text-gray-400">+{team.expertiseTags.length - 3}</span>
          )}
        </div>
      ),
    },
  ];

  if (loading) return <Loading fullScreen label="Loading teams..." />;
  if (error) return <div className="text-red-600 p-4">{error}</div>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Teams</h1>
        <button className="px-3 py-1.5 bg-brand-600 text-white text-sm font-medium rounded-md hover:bg-brand-700">
          + Add Team
        </button>
      </div>

      <Table
        columns={columns}
        data={teams || []}
        onRowClick={setSelectedTeam}
        emptyMessage="No teams configured"
      />

      <Modal
        isOpen={!!selectedTeam}
        onClose={() => setSelectedTeam(null)}
        title={selectedTeam?.name || 'Team Details'}
        size="lg"
      >
        {selectedTeam && (
          <div className="space-y-4">
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Members</h4>
              <div className="space-y-2">
                {selectedTeam.members.map((member) => (
                  <div key={member.id} className="flex items-center justify-between py-1">
                    <span className="text-sm">{member.name}</span>
                    <div className="flex items-center gap-2">
                      <Badge variant={roleColors[member.role]}>{member.role}</Badge>
                      <span className="text-xs text-gray-500">{(member.availabilityPercent * 100).toFixed(0)}%</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2">Configuration</h4>
              <div className="grid grid-cols-3 gap-4">
                <div className="bg-gray-50 rounded-md p-3">
                  <div className="text-xs text-gray-500">Focus Factor</div>
                  <div className="text-lg font-bold">{(selectedTeam.focusFactor * 100).toFixed(0)}%</div>
                </div>
                <div className="bg-gray-50 rounded-md p-3">
                  <div className="text-xs text-gray-500">Bug Reserve</div>
                  <div className="text-lg font-bold">{(selectedTeam.bugReservePercent * 100).toFixed(0)}%</div>
                </div>
                <div className="bg-gray-50 rounded-md p-3">
                  <div className="text-xs text-gray-500">Tech Debt Reserve</div>
                  <div className="text-lg font-bold">{(selectedTeam.techDebtReservePercent * 100).toFixed(0)}%</div>
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
