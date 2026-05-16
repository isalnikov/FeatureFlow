import { apiClient } from './client';
import type {
  Feature,
  CreateFeatureRequest,
  UpdateFeatureRequest,
  FeatureFilters,
  PaginatedFeatures,
} from '../types';

export const featuresApi = {
  list: async (filters?: FeatureFilters) => {
    const response = await apiClient.get<PaginatedFeatures>('/features', { params: filters });
    return response.data;
  },

  listAll: async () => {
    const response = await apiClient.get<Feature[]>('/features', { params: { size: 1000 } });
    return response.data.content || response.data;
  },

  getById: async (id: string) => {
    const response = await apiClient.get<Feature>(`/features/${id}`);
    return response.data;
  },

  create: async (data: CreateFeatureRequest) => {
    const response = await apiClient.post<Feature>('/features', data);
    return response.data;
  },

  update: async (id: string, data: UpdateFeatureRequest) => {
    const response = await apiClient.put<Feature>(`/features/${id}`, data);
    return response.data;
  },

  delete: async (id: string) => {
    await apiClient.delete(`/features/${id}`);
  },

  bulk: async (features: (CreateFeatureRequest | UpdateFeatureRequest & { id: string })[]) => {
    const response = await apiClient.post<Feature[]>('/features/bulk', { features });
    return response.data;
  },

  getDependencies: async (id: string) => {
    const response = await apiClient.get<string[]>(`/features/${id}/deps`);
    return response.data;
  },

  updateDependencies: async (id: string, dependencies: string[]) => {
    const response = await apiClient.put<string[]>(`/features/${id}/deps`, { dependencies });
    return response.data;
  },
};
