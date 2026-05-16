import axios from 'axios';
import type { PlanningJob, PlanningResult, PlanningRunRequest } from '../types';

const planningClient = axios.create({
  baseURL: import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081/api/v1',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' },
});

planningClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) { config.headers.Authorization = `Bearer ${token}`; }
  return config;
});

export const planningApi = {
  run: async (request: PlanningRunRequest) => {
    const response = await planningClient.post('/planning/run', request);
    return {
      jobId: response.headers['x-job-id'] || response.data.jobId,
      ...response.data,
    } as PlanningJob;
  },

  getJobStatus: async (jobId: string) => {
    const response = await planningClient.get<PlanningJob>(`/planning/jobs/${jobId}/status`);
    return response.data;
  },

  getResult: async (jobId: string) => {
    const response = await planningClient.get<PlanningResult>(`/planning/jobs/${jobId}/result`);
    return response.data;
  },

  validate: async (request: PlanningRunRequest) => {
    const response = await planningClient.post('/planning/validate', request);
    return response.data;
  },
};
