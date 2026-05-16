import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';
import type { TeamLoadReport } from '../../types';

interface TeamLoadChartProps {
  reports: TeamLoadReport[];
  sprintLabels?: string[];
  title?: string;
}

const roleColors: Record<string, string> = {
  BACKEND: '#3b82f6',
  FRONTEND: '#10b981',
  QA: '#f59e0b',
  DEVOPS: '#8b5cf6',
};

const roleLabels: Record<string, string> = {
  BACKEND: 'Backend',
  FRONTEND: 'Frontend',
  QA: 'QA',
  DEVOPS: 'DevOps',
};

function getLoadColor(utilization: number): string {
  if (utilization < 60) return '#10b981';
  if (utilization < 85) return '#f59e0b';
  return '#ef4444';
}

export function TeamLoadChart({ reports, sprintLabels, title = 'Team Load' }: TeamLoadChartProps) {
  if (reports.length === 0) {
    return (
      <div className="flex items-center justify-center py-8 text-gray-400">
        No load data available
      </div>
    );
  }

  const data = reports.map((report) => {
    const loadByRole: Record<string, number> = {};
    const dates = Object.keys(report.loadByDateAndRole);
    const date = dates[0] || '';

    if (date && report.loadByDateAndRole[date]) {
      loadByRole.backend = report.loadByDateAndRole[date]['BACKEND'] || 0;
      loadByRole.frontend = report.loadByDateAndRole[date]['FRONTEND'] || 0;
      loadByRole.qa = report.loadByDateAndRole[date]['QA'] || 0;
      loadByRole.devops = report.loadByDateAndRole[date]['DEVOPS'] || 0;
    }

    return {
      name: report.teamId,
      ...loadByRole,
      utilization: report.utilizationPercent,
      bottlenecks: report.bottleneckRoles.join(', '),
      fillColor: getLoadColor(report.utilizationPercent),
    };
  });

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <h3 className="text-sm font-medium text-gray-900 mb-4">{title}</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <XAxis dataKey="name" tick={{ fontSize: 12 }} />
          <YAxis label={{ value: 'Hours', angle: -90, position: 'insideLeft', fontSize: 12 }} />
          <Tooltip />
          <Legend />
          <Bar dataKey="backend" stackId="a" fill={roleColors.BACKEND} name={roleLabels.BACKEND} />
          <Bar dataKey="frontend" stackId="a" fill={roleColors.FRONTEND} name={roleLabels.FRONTEND} />
          <Bar dataKey="qa" stackId="a" fill={roleColors.QA} name={roleLabels.QA} />
          <Bar dataKey="devops" stackId="a" fill={roleColors.DEVOPS} name={roleLabels.DEVOPS} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
