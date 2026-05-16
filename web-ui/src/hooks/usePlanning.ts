import { useState, useEffect, useCallback } from 'react';
import { assignmentsApi } from '../api/assignments';
import { planningApi } from '../api/planning';
import type { Assignment, PlanningResult } from '../types';

export function useAssignments(filters?: { teamId?: string; sprintId?: string; featureId?: string; status?: string }) {
  const [data, setData] = useState<Assignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchAssignments = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await assignmentsApi.list(filters);
      setData(result);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to fetch assignments');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchAssignments();
  }, [fetchAssignments]);

  return { data, loading, error, refetch: fetchAssignments };
}

export function usePlanningResult(jobId: string | null) {
  const [data, setData] = useState<PlanningResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchResult = useCallback(async () => {
    if (!jobId) return;
    setLoading(true);
    setError(null);
    try {
      const result = await planningApi.getResult(jobId);
      setData(result);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to fetch planning result');
    } finally {
      setLoading(false);
    }
  }, [jobId]);

  useEffect(() => {
    fetchResult();
  }, [fetchResult]);

  return { data, loading, error, refetch: fetchResult };
}
