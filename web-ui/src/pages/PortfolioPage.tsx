import { useState, useEffect } from 'react';
import { dashboardApi, type TimelineEntry } from '../../api/dashboard';
import { Badge } from '../components/common/Badge';
import { Loading } from '../components/common/Loading';

const statusColors: Record<string, 'success' | 'warning' | 'error' | 'default'> = {
  onTrack: 'success',
  atRisk: 'warning',
  blocked: 'error',
  completed: 'default',
};

export function PortfolioPage() {
  const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>('all');

  useEffect(() => {
    dashboardApi
      .getTimeline()
      .then(setTimeline)
      .catch((err) => {
        const message = err instanceof Error ? err.message : 'Failed to load portfolio';
        setError(message);
      })
      .finally(() => setLoading(false));
  }, []);

  if (error) {
    return (
      <div>
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Portfolio Overview</h1>
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
          <p className="font-medium">Failed to load portfolio data</p>
          <p className="text-sm mt-1">{error}</p>
          <button
            onClick={() => {
              setError(null);
              setLoading(true);
              dashboardApi.getTimeline().then(setTimeline).catch((err) => setError(err.message)).finally(() => setLoading(false));
            }}
            className="mt-3 text-sm font-medium text-red-600 hover:text-red-800 underline"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const filtered = filter === 'all' ? timeline : timeline.filter((t) => t.status === filter);

  if (loading) return <Loading fullScreen label="Loading portfolio..." />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Portfolio Overview</h1>
        <div className="flex gap-2">
          {['all', 'onTrack', 'atRisk', 'blocked', 'completed'].map((status) => (
            <button
              key={status}
              onClick={() => setFilter(status)}
              className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors ${
                filter === status
                  ? 'bg-brand-600 text-white'
                  : 'bg-white text-gray-600 border border-gray-300 hover:bg-gray-50'
              }`}
            >
              {status === 'all' ? 'All' : status}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200">
        <div className="px-4 py-3 border-b border-gray-200 grid grid-cols-5 text-xs font-medium text-gray-500 uppercase">
          <div>Feature</div>
          <div>Product</div>
          <div>Team</div>
          <div>Timeline</div>
          <div>Status</div>
        </div>
        {filtered.map((entry) => (
          <div key={entry.featureId} className="px-4 py-3 border-b border-gray-100 grid grid-cols-5 text-sm">
            <div className="font-medium text-gray-900">{entry.title}</div>
            <div className="text-gray-600">{entry.productId}</div>
            <div className="text-gray-600">{entry.teamId}</div>
            <div className="text-gray-600">
              {entry.startDate} → {entry.endDate}
            </div>
            <div>
              <Badge variant={statusColors[entry.status] || 'default'}>{entry.status}</Badge>
            </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="px-4 py-8 text-center text-gray-400">No features in portfolio</div>
        )}
      </div>
    </div>
  );
}
