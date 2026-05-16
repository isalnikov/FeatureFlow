import { useState, useEffect } from 'react';
import { dashboardApi, type DashboardMetrics } from '../../api/dashboard';
import { TeamLoadChart } from '../../components/planning/TeamLoadChart';
import { ConflictList } from '../../components/planning/ConflictList';
import { Loading } from '../../components/common/Loading';

interface MetricCardProps {
  title: string;
  value: string | number;
  unit?: string;
  severity?: 'default' | 'error' | 'warning';
}

function MetricCard({ title, value, unit, severity = 'default' }: MetricCardProps) {
  const severityColors = {
    default: 'text-gray-900',
    error: 'text-red-600',
    warning: 'text-amber-600',
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-4">
      <div className="text-sm text-gray-500">{title}</div>
      <div className={`text-2xl font-bold mt-1 ${severityColors[severity]}`}>
        {value}
        {unit && <span className="text-sm font-normal text-gray-500 ml-1">{unit}</span>}
      </div>
    </div>
  );
}

export function DashboardPage() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    dashboardApi
      .getMetrics()
      .then(setMetrics)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed to load metrics'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading fullScreen label="Loading dashboard..." />;
  if (error) return <div className="text-red-600 p-4">{error}</div>;
  if (!metrics) return <div className="text-gray-500 p-4">No data available</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Portfolio Dashboard</h1>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <MetricCard title="Total Features" value={metrics.totalFeatures} />
        <MetricCard title="Planned" value={metrics.plannedFeatures} />
        <MetricCard title="Avg TTM" value={metrics.avgTTM} unit="sprints" />
        <MetricCard title="Utilization" value={metrics.avgUtilization} unit="%" />
        <MetricCard title="Conflicts" value={metrics.activeConflicts} severity={metrics.activeConflicts > 0 ? 'error' : 'default'} />
        <MetricCard title="Deadline Risk" value={metrics.deadlineRisk} unit="%" severity={metrics.deadlineRisk > 20 ? 'warning' : 'default'} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <TeamLoadChart
          reports={metrics.teamLoadReports}
          title="Team Load Overview"
        />
        <ConflictList conflicts={metrics.conflicts} title="Active Conflicts" />
      </div>
    </div>
  );
}
