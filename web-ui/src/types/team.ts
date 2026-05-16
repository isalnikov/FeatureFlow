export type Role = 'BACKEND' | 'FRONTEND' | 'QA' | 'DEVOPS';

export interface TeamMember {
  id: string;
  personId: string;
  name: string;
  role: Role;
  availabilityPercent: number;
}

export interface Team {
  id: string;
  name: string;
  members: TeamMember[];
  focusFactor: number;
  bugReservePercent: number;
  techDebtReservePercent: number;
  velocity: number | null;
  expertiseTags: string[];
  version: number;
}

export interface CreateTeamRequest {
  name: string;
  members: Omit<TeamMember, 'id'>[];
  focusFactor: number;
  bugReservePercent: number;
  techDebtReservePercent: number;
  velocity?: number | null;
  expertiseTags: string[];
}

export interface UpdateTeamRequest extends Partial<CreateTeamRequest> {}

export interface TeamCapacity {
  teamId: string;
  sprintId: string;
  capacityByRole: Record<Role, number>;
  effectiveCapacityByRole: Record<Role, number>;
}

export interface TeamLoad {
  teamId: string;
  sprintId: string;
  loadByRole: Record<Role, number>;
  utilizationPercent: number;
  bottleneckRoles: Role[];
}
