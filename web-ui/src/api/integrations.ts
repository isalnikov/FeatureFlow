import { apiClient } from './client';

export interface IntegrationConfig {
  baseUrl: string;
  authType: 'basic' | 'token' | 'oauth';
  username?: string;
  apiToken?: string;
}

export interface ImportFilter {
  projectKey?: string;
  issueTypes?: string[];
  statuses?: string[];
  sinceDate?: string;
  maxResults?: number;
}

export interface ImportResult {
  featuresCreated: number;
  featuresUpdated: number;
  sprintsCreated: number;
  teamsCreated: number;
}

export const integrationsApi = {
  import: async (type: 'jira' | 'ado' | 'linear', config: IntegrationConfig, filter: ImportFilter) => {
    const response = await apiClient.post<ImportResult>(`/integrations/${type}/import`, { config, filter });
    return response.data;
  },

  export: async (type: 'jira' | 'ado' | 'linear', config: IntegrationConfig, assignmentIds: string[]) => {
    const response = await apiClient.post(`/integrations/${type}/export`, { config, assignmentIds });
    return response.data;
  },

  testConnection: async (type: 'jira' | 'ado' | 'linear', config: IntegrationConfig) => {
    const response = await apiClient.post(`/integrations/${type}/test`, { config });
    return response.data;
  },
};
