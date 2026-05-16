import { apiClient } from './client';
import type { PlanningJob, PlanningResult, PlanningRunRequest } from '../types';

const planningBaseUrl = import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081/api/v1';

export const planningApi = {
  run: async (request: PlanningRunRequest) => {
    const response = await apiClient.post('/planning/run', request);
    return {
      jobId: response.headers['x-job-id'] || response.data.jobId,
      ...response.data,
    } as PlanningJob;
  },

  getJobStatus: async (jobId: string) => {
    const response = await apiClient.get<PlanningJob>(`/planning/jobs/${jobId}/status`);
    return response.data;
  },

  getResult: async (jobId: string) => {
    const response = await apiClient.get<PlanningResult>(`/planning/jobs/${jobId}/result`);
    return response.data;
  },

  validate: async (request: PlanningRunRequest) => {
    const response = await apiClient.post('/planning/validate', request);
    return response.data;
  },
};
