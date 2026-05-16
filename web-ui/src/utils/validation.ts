import type { CreateFeatureRequest, CreateTeamRequest } from '../types';

export interface ValidationErrors {
  [key: string]: string;
}

export function validateFeature(data: Partial<CreateFeatureRequest>): ValidationErrors {
  const errors: ValidationErrors = {};

  if (!data.title?.trim()) {
    errors.title = 'Title is required';
  }

  if (data.businessValue !== undefined && (data.businessValue < 0 || data.businessValue > 100)) {
    errors.businessValue = 'Business value must be between 0 and 100';
  }

  if (!data.productIds || data.productIds.length === 0) {
    errors.productIds = 'At least one product is required';
  }

  if (data.effortEstimate) {
    const total =
      data.effortEstimate.backendHours +
      data.effortEstimate.frontendHours +
      data.effortEstimate.qaHours +
      data.effortEstimate.devopsHours;

    if (total <= 0) {
      errors.effortEstimate = 'Total effort must be greater than 0';
    }

    if (Object.values(data.effortEstimate).some((v) => v < 0)) {
      errors.effortEstimate = 'Effort estimates cannot be negative';
    }
  }

  return errors;
}

export function validateTeam(data: Partial<CreateTeamRequest>): ValidationErrors {
  const errors: ValidationErrors = {};

  if (!data.name?.trim()) {
    errors.name = 'Team name is required';
  }

  if (!data.members || data.members.length === 0) {
    errors.members = 'At least one member is required';
  }

  if (data.focusFactor !== undefined && (data.focusFactor <= 0 || data.focusFactor > 1)) {
    errors.focusFactor = 'Focus factor must be between 0 and 1';
  }

  if (data.bugReservePercent !== undefined && (data.bugReservePercent < 0 || data.bugReservePercent > 1)) {
    errors.bugReservePercent = 'Bug reserve must be between 0 and 1';
  }

  return errors;
}

export function hasErrors(errors: ValidationErrors): boolean {
  return Object.keys(errors).length > 0;
}
