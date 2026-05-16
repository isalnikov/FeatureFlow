import axios from 'axios';
import type { PlanningJob, PlanningResult, PlanningRunRequest } from '../types';

export interface SimulationRequest {
  baseRequest: PlanningRunRequest;
  changes: {
    priorityChanges?: Record<string, number>;
    capacityChanges?: Record<string, number>;
    addedFeatures?: string[];
    removedFeatures?: string[];
  };
}

export interface ComparisonResult {
  baselineCost: number;
  simulationCost: number;
  costDelta: number;
  baselineConflicts: number;
  simulationConflicts: number;
  timelineDiffs: Array<{
    featureId: string;
    featureTitle: string;
    baselineStart: string;
    baselineEnd: string;
    simulationStart: string;
    simulationEnd: string;
    dayShift: number;
  }>;
  teamLoadDiffs: Array<{
    teamId: string;
    teamName: string;
    baselineUtilization: number;
    simulationUtilization: number;
    utilizationDelta: number;
    newBottleneckRoles: string[];
  }>;
}

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

  runSimulation: async (request: SimulationRequest) => {
    const response = await planningClient.post('/simulations/run', request);
    return {
      simulationId: response.headers['x-simulation-id'] || response.data.jobId,
      ...response.data,
    };
  },

  compareResults: async (baselineId: string, simulationId: string) => {
    const response = await planningClient.post<ComparisonResult>(
      `/simulations/compare?baselineId=${baselineId}&simulationId=${simulationId}`
    );
    return response.data;
  },
};
