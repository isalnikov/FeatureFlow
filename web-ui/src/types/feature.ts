export type ClassOfService = 'EXPEDITE' | 'FIXED_DATE' | 'STANDARD' | 'FILLER';

export interface EffortEstimate {
  backendHours: number;
  frontendHours: number;
  qaHours: number;
  devopsHours: number;
}

export interface ThreePointEstimate {
  optimistic: number;
  mostLikely: number;
  pessimistic: number;
}

export interface Feature {
  id: string;
  title: string;
  description: string;
  businessValue: number;
  requestorId: string;
  deadline: string | null;
  classOfService: ClassOfService;
  productIds: string[];
  effortEstimate: EffortEstimate;
  stochasticEstimate: ThreePointEstimate | null;
  dependencies: string[];
  requiredExpertise: string[];
  canSplit: boolean;
  version: number;
}

export interface CreateFeatureRequest {
  title: string;
  description: string;
  businessValue: number;
  classOfService: ClassOfService;
  productIds: string[];
  effortEstimate: EffortEstimate;
  stochasticEstimate?: ThreePointEstimate | null;
  dependencies: string[];
  requiredExpertise: string[];
  canSplit: boolean;
}

export interface UpdateFeatureRequest extends Partial<CreateFeatureRequest> {}

export interface FeatureFilters {
  page?: number;
  size?: number;
  classOfService?: ClassOfService;
  productId?: string;
  deadlineBefore?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export interface PaginatedFeatures {
  content: Feature[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
