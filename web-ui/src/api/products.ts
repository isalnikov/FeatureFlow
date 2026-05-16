import { apiClient } from './client';
import type { Product, CreateProductRequest, UpdateProductRequest } from '../types';

export const productsApi = {
  list: async () => {
    const response = await apiClient.get<Product[]>('/products');
    return response.data;
  },

  getById: async (id: string) => {
    const response = await apiClient.get<Product>(`/products/${id}`);
    return response.data;
  },

  create: async (data: CreateProductRequest) => {
    const response = await apiClient.post<Product>('/products', data);
    return response.data;
  },

  update: async (id: string, data: UpdateProductRequest) => {
    const response = await apiClient.put<Product>(`/products/${id}`, data);
    return response.data;
  },

  delete: async (id: string) => {
    await apiClient.delete(`/products/${id}`);
  },

  getTeams: async (id: string) => {
    const response = await apiClient.get(`/products/${id}/teams`);
    return response.data;
  },
};
