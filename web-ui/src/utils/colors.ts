export const roleColors: Record<string, string> = {
  BACKEND: '#3b82f6',
  FRONTEND: '#10b981',
  QA: '#f59e0b',
  DEVOPS: '#8b5cf6',
};

export const classOfServiceColors: Record<string, string> = {
  EXPEDITE: '#ef4444',
  FIXED_DATE: '#f59e0b',
  STANDARD: '#3b82f6',
  FILLER: '#9ca3af',
};

export const statusColors: Record<string, string> = {
  onTrack: '#10b981',
  atRisk: '#f59e0b',
  blocked: '#ef4444',
  completed: '#6366f1',
};

export const conflictSeverityColors: Record<string, string> = {
  CAPACITY_EXCEEDED: '#ef4444',
  DEPENDENCY_VIOLATION: '#ef4444',
  OWNERSHIP_VIOLATION: '#ef4444',
  DEADLINE_MISSED: '#f59e0b',
  PARALLELISM_EXCEEDED: '#f59e0b',
  DEPENDENCY_CYCLE: '#ef4444',
};

export function getLoadColor(utilization: number): string {
  if (utilization < 60) return '#10b981';
  if (utilization < 85) return '#f59e0b';
  return '#ef4444';
}

export function getDeadlineProbabilityColor(probability: number): string {
  if (probability >= 0.8) return '#10b981';
  if (probability >= 0.5) return '#f59e0b';
  return '#ef4444';
}
