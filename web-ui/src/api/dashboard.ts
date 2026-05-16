import { apiClient } from './client';
import type { Conflict, TeamLoadReport } from '../types';

export interface DashboardMetrics {
  totalFeatures: number;
  plannedFeatures: number;
  avgTTM: number;
  avgUtilization: number;
  activeConflicts: number;
  deadlineRisk: number;
  teamLoadReports: TeamLoadReport[];
  conflicts: Conflict[];
}

export interface TimelineEntry {
  featureId: string;
  title: string;
  startDate: string;
  endDate: string;
  productId: string;
  teamId: string;
  status: 'onTrack' | 'atRisk' | 'blocked' | 'completed';
}

export const dashboardApi = {
  getPortfolio: async () => {
    const response = await apiClient.get<DashboardMetrics>('/dashboard/portfolio');
    return response.data;
  },

  getTimeline: async () => {
    const response = await apiClient.get<TimelineEntry[]>('/dashboard/timeline');
    return response.data;
  },

  getTeamLoad: async () => {
    const response = await apiClient.get<TeamLoadReport[]>('/dashboard/team-load');
    return response.data;
  },

  getConflicts: async () => {
    const response = await apiClient.get<Conflict[]>('/dashboard/conflicts');
    return response.data;
  },

  getMetrics: async () => {
    const response = await apiClient.get<DashboardMetrics>('/dashboard/metrics');
    return response.data;
  },
};
