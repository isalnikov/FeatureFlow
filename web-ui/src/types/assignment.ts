import type { EffortEstimate } from './feature';

export type AssignmentStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'LOCKED';

export interface Assignment {
  id: string;
  featureId: string;
  teamId: string;
  sprintId: string;
  allocatedEffort: EffortEstimate;
  status: AssignmentStatus;
}

export interface CreateAssignmentRequest {
  featureId: string;
  teamId: string;
  sprintId: string;
  allocatedEffort: EffortEstimate;
}

export interface UpdateAssignmentRequest {
  sprintId?: string;
  allocatedEffort?: EffortEstimate;
  status?: AssignmentStatus;
}
