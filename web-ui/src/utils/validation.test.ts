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

  it('returns error for business value over 100', () => {
    const errors = validateFeature({
      title: 'Test',
      description: 'A test',
      businessValue: 150,
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
      members: [{ personId: 'm1', name: 'Alice', role: 'BACKEND', availabilityPercent: 100 }],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
      techDebtReservePercent: 0.1,
      expertiseTags: [],
    });
    expect(hasErrors(errors)).toBe(false);
  });

  it('returns error for empty name', () => {
    const errors = validateTeam({
      name: '',
      members: [{ personId: 'm1', name: 'Alice', role: 'BACKEND', availabilityPercent: 100 }],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
      techDebtReservePercent: 0.1,
      expertiseTags: [],
    });
    expect(errors.name).toBeDefined();
  });

  it('returns error for empty members', () => {
    const errors = validateTeam({
      name: 'Team Alpha',
      members: [],
      focusFactor: 0.7,
      bugReservePercent: 0.2,
      techDebtReservePercent: 0.1,
      expertiseTags: [],
    });
    expect(errors.members).toBeDefined();
  });
});
