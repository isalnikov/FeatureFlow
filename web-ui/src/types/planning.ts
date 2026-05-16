import type { EffortEstimate } from './feature';
import type { Assignment } from './assignment';

export type PlanningPhase = 'GREEDY' | 'ANNEALING' | 'MONTE_CARLO' | 'DONE' | 'FAILED';

export type ConflictType =
  | 'CAPACITY_EXCEEDED'
  | 'DEPENDENCY_VIOLATION'
  | 'OWNERSHIP_VIOLATION'
  | 'PARALLELISM_EXCEEDED'
  | 'DEADLINE_MISSED'
  | 'DEPENDENCY_CYCLE';

export interface PlanningJob {
  jobId: string;
  status: PlanningPhase;
  progressPercent: number;
  currentCost: number;
  bestCost: number;
  iteration: number;
  errorMessage: string | null;
}

export interface PlanningParameters {
  w1Ttm: number;
  w2Underutilization: number;
  w3DeadlinePenalty: number;
  maxParallelFeatures: number;
  annealing: {
    initialTemperature: number;
    coolingRate: number;
    minTemperature: number;
    maxIterations: number;
  };
  monteCarlo: {
    iterations: number;
    confidenceLevel: number;
  };
}

export interface PlanningRunRequest {
  featureIds: string[];
  teamIds?: string[];
  planningWindowId: string;
  parameters: PlanningParameters;
  lockedAssignmentIds: string[];
}

export interface Conflict {
  type: ConflictType;
  featureId: string;
  teamId: string | null;
  message: string;
}

export interface FeatureTimeline {
  featureId: string;
  startDate: string;
  endDate: string;
  probabilityOfMeetingDeadline: number;
}

export interface TeamLoadReport {
  teamId: string;
  loadByDateAndRole: Record<string, Record<string, number>>;
  utilizationPercent: number;
  bottleneckRoles: string[];
}

export interface PlanningResult {
  assignments: Assignment[];
  conflicts: Conflict[];
  featureTimelines: Record<string, FeatureTimeline>;
  teamLoadReports: Record<string, TeamLoadReport>;
  totalCost: number;
  computationTimeMs: number;
  algorithm: 'GREEDY' | 'SIMULATED_ANNEALING' | 'MONTE_CARLO';
}

export interface Sprint {
  id: string;
  planningWindowId: string;
  startDate: string;
  endDate: string;
  label: string;
  capacityOverrides?: Record<string, number>;
}

export interface PlanningWindow {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  sprints: Sprint[];
}

export interface SimulationChanges {
  priorityChanges?: Record<string, number>;
  capacityChanges?: Record<string, number>;
  addedFeatures?: string[];
  removedFeatures?: string[];
}
