import planningReducer, { clearPlanning, setResult, updateJobStatus } from '../store/planningSlice';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { PlanningJob, PlanningResult } from '../types';

vi.mock('../api/planning', () => ({
  planningApi: {
    run: vi.fn(),
    getJobStatus: vi.fn(),
    getResult: vi.fn(),
  },
}));

describe('planningSlice', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('has initial state', () => {
    const state = planningReducer(undefined, { type: 'unknown' });
    expect(state.currentJob).toBeNull();
    expect(state.currentResult).toBeNull();
    expect(state.isPlanning).toBe(false);
    expect(state.error).toBeNull();
  });

  it('clearPlanning resets state', () => {
    const state = planningReducer(
      { currentJob: { jobId: '1' } as unknown as PlanningJob, currentResult: {} as unknown as PlanningResult, isPlanning: true, error: 'err' },
      clearPlanning()
    );
    expect(state.currentJob).toBeNull();
    expect(state.currentResult).toBeNull();
    expect(state.isPlanning).toBe(false);
    expect(state.error).toBeNull();
  });

  it('updateJobStatus updates the job', () => {
    const job = { jobId: 'job-1', status: 'RUNNING', progressPercent: 50, currentCost: 100, bestCost: 90, iteration: 10, errorMessage: null } as PlanningJob;
    const state = planningReducer(undefined, updateJobStatus(job));
    expect(state.currentJob).toEqual(job);
  });

  it('setResult updates the result', () => {
    const result = {
      featureTimelines: {},
      assignments: [],
      conflicts: [],
      teamLoadReports: {},
      totalCost: 0,
      computationTimeMs: 0,
      algorithm: 'GREEDY',
    } as PlanningResult;
    const state = planningReducer(undefined, setResult(result));
    expect(state.currentResult).toEqual(result);
  });
});
