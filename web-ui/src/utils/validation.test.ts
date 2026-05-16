import { validateFeature, validateTeam, hasErrors } from '../utils/validation';
import { describe, it, expect } from 'vitest';

describe('validateFeature', () => {
  it('returns empty errors for valid feature', () => {
    const errors = validateFeature({
      title: 'Test Feature',
      description: 'A test',
      businessValue: 50,
      classOfService: 'STANDARD',
      productIds: ['p1'],
      effortEstimate: { backendHours: 10, frontendHours: 10, qaHours: 5, devopsHours: 2 },
      dependencies: [],
      requiredExpertise: [],
      canSplit: false,
    });
    expect(hasErrors(errors)).toBe(false);
  });

  it('returns error for empty title', () => {
    const errors = validateFeature({
      title: '',
      description: 'A test',
      businessValue: 50,
      classOfService: 'STANDARD',
      productIds: ['p1'],
      effortEstimate: { backendHours: 10, frontendHours: 10, qaHours: 5, devopsHours: 2 },
      dependencies: [],
      requiredExpertise: [],
      canSplit: false,
    });
    expect(errors.title).toBeDefined();
    expect(hasErrors(errors)).toBe(true);
  });

  it('returns error for zero business value', () => {
    const errors = validateFeature({
      title: 'Test',
      description: 'A test',
      businessValue: 0,
      classOfService: 'STANDARD',
      productIds: ['p1'],
      effortEstimate: { backendHours: 10, frontendHours: 10, qaHours: 5, devopsHours: 2 },
      dependencies: [],
      requiredExpertise: [],
      canSplit: false,
    });
    expect(errors.businessValue).toBeDefined();
  });

  it('returns error for empty productIds', () => {
    const errors = validateFeature({
      title: 'Test',
      description: 'A test',
      businessValue: 50,
      classOfService: 'STANDARD',
      productIds: [],
      effortEstimate: { backendHours: 10, frontendHours: 10, qaHours: 5, devopsHours: 2 },
      dependencies: [],
      requiredExpertise: [],
      canSplit: false,
    });
    expect(errors.productIds).toBeDefined();
  });

  it('returns error for negative effort', () => {
    const errors = validateFeature({
      title: 'Test',
      description: 'A test',
      businessValue: 50,
      classOfService: 'STANDARD',
      productIds: ['p1'],
      effortEstimate: { backendHours: -1, frontendHours: 10, qaHours: 5, devopsHours: 2 },
      dependencies: [],
      requiredExpertise: [],
      canSplit: false,
    });
    expect(errors.effortEstimate).toBeDefined();
  });
});

describe('validateTeam', () => {
  it('returns empty errors for valid team', () => {
    const errors = validateTeam({
      name: 'Team Alpha',
      members: [{ id: 'm1', name: 'Alice', role: 'BACKEND', allocation: 1.0 }],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
    });
    expect(hasErrors(errors)).toBe(false);
  });

  it('returns error for empty name', () => {
    const errors = validateTeam({
      name: '',
      members: [{ id: 'm1', name: 'Alice', role: 'BACKEND', allocation: 1.0 }],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
    });
    expect(errors.name).toBeDefined();
  });

  it('returns error for empty members', () => {
    const errors = validateTeam({
      name: 'Team Alpha',
      members: [],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
    });
    expect(errors.members).toBeDefined();
  });
});
