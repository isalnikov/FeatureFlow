import { useState, useEffect, useCallback } from 'react';
import { featuresApi } from '../api/features';
import type { Feature, FeatureFilters, PaginatedFeatures } from '../types';

export function useFeatures(filters?: FeatureFilters) {
  const [data, setData] = useState<PaginatedFeatures | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchFeatures = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await featuresApi.list(filters);
      setData(result);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : 'Failed to fetch features');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    fetchFeatures();
  }, [fetchFeatures]);

  return { data, loading, error, refetch: fetchFeatures };
}

export function useFeature(id: string | null) {
  const [data, setData] = useState<Feature | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setData(null);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);

    featuresApi
      .getById(id)
      .then((result) => {
        if (!cancelled) setData(result);
      })
      .catch((e: unknown) => {
        if (!cancelled) setError(e instanceof Error ? e.message : 'Failed to fetch feature');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  return { data, loading, error };
}
