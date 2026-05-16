import { apiClient } from './client';
import type { Team, CreateTeamRequest, UpdateTeamRequest, TeamCapacity, TeamLoad } from '../types';

export const teamsApi = {
  list: async () => {
    const response = await apiClient.get<Team[]>('/teams');
    return response.data;
  },

  getById: async (id: string) => {
    const response = await apiClient.get<Team>(`/teams/${id}`);
    return response.data;
  },

  create: async (data: CreateTeamRequest) => {
    const response = await apiClient.post<Team>('/teams', data);
    return response.data;
  },

  update: async (id: string, data: UpdateTeamRequest) => {
    const response = await apiClient.put<Team>(`/teams/${id}`, data);
    return response.data;
  },

  delete: async (id: string) => {
    await apiClient.delete(`/teams/${id}`);
  },

  getCapacity: async (id: string, sprintId: string) => {
    const response = await apiClient.get<TeamCapacity>(`/teams/${id}/capacity`, {
      params: { sprintId },
    });
    return response.data;
  },

  getLoad: async (id: string, sprintId: string) => {
    const response = await apiClient.get<TeamLoad>(`/teams/${id}/load`, {
      params: { sprintId },
    });
    return response.data;
  },

  updateMembers: async (id: string, members: Team['members']) => {
    const response = await apiClient.put<Team>(`/teams/${id}/members`, { members });
    return response.data;
  },
};
