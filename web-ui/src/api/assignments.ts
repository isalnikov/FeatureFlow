import { apiClient } from './client';
import type { Assignment, CreateAssignmentRequest, UpdateAssignmentRequest } from '../types';

export const assignmentsApi = {
  list: async (filters?: { teamId?: string; sprintId?: string; featureId?: string; status?: string }) => {
    const response = await apiClient.get<Assignment[]>('/assignments', { params: filters });
    return response.data;
  },

  create: async (data: CreateAssignmentRequest) => {
    const response = await apiClient.post<Assignment>('/assignments', data);
    return response.data;
  },

  update: async (id: string, data: UpdateAssignmentRequest) => {
    const response = await apiClient.put<Assignment>(`/assignments/${id}`, data);
    return response.data;
  },

  delete: async (id: string) => {
    await apiClient.delete(`/assignments/${id}`);
  },

  lock: async (id: string) => {
    const response = await apiClient.put<Assignment>(`/assignments/${id}/lock`);
    return response.data;
  },

  unlock: async (id: string) => {
    const response = await apiClient.put<Assignment>(`/assignments/${id}/unlock`);
    return response.data;
  },

  bulk: async (updates: { id: string; data: UpdateAssignmentRequest }[]) => {
    const response = await apiClient.post<Assignment[]>('/assignments/bulk', { updates });
    return response.data;
  },
};
