export interface Product {
  id: string;
  name: string;
  description: string;
  technologyStack: string[];
  teamIds: string[];
  version: number;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  technologyStack: string[];
}

export interface UpdateProductRequest extends Partial<CreateProductRequest> {}
