# Web UI — Спецификация

> React 19 + TypeScript + Vite. SPA с интерактивной диаграммой Ганта.

---

## 1. Структура проекта

```
web-ui/
├── package.json
├── tsconfig.json
├── vite.config.ts
├── index.html
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── router.tsx
│   ├── api/
│   │   ├── client.ts              # Axios instance
│   │   ├── products.ts
│   │   ├── teams.ts
│   │   ├── features.ts
│   │   ├── planning.ts
│   │   ├── assignments.ts
│   │   └── integrations.ts
│   ├── types/
│   │   ├── index.ts
│   │   ├── product.ts
│   │   ├── team.ts
│   │   ├── feature.ts
│   │   ├── planning.ts
│   │   └── assignment.ts
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Tooltip.tsx
│   │   │   └── Loading.tsx
│   │   ├── gantt/
│   │   │   ├── GanttChart.tsx
│   │   │   ├── GanttBar.tsx
│   │   │   ├── GanttTimeline.tsx
│   │   │   ├── GanttDependencies.tsx
│   │   │   └── GanttToolbar.tsx
│   │   ├── planning/
│   │   │   ├── SprintTable.tsx
│   │   │   ├── TeamLoadChart.tsx
│   │   │   ├── ConflictList.tsx
│   │   │   ├── PlanningControls.tsx
│   │   │   └── PlanComparison.tsx
│   │   └── features/
│   │       ├── FeatureForm.tsx
│   │       ├── FeatureList.tsx
│   │       ├── FeatureDetail.tsx
│   │       └── DependencyGraph.tsx
│   ├── pages/
│   │   ├── DashboardPage.tsx
│   │   ├── PortfolioPage.tsx
│   │   ├── PlanPage.tsx
│   │   ├── TeamsPage.tsx
│   │   ├── FeaturesPage.tsx
│   │   ├── SettingsPage.tsx
│   │   └── SimulationPage.tsx
│   ├── hooks/
│   │   ├── useFeatures.ts
│   │   ├── useTeams.ts
│   │   ├── usePlanning.ts
│   │   └── useSSE.ts
│   ├── store/
│   │   ├── index.ts
│   │   ├── planningSlice.ts
│   │   └── uiSlice.ts
│   └── utils/
│       ├── date.ts
│       ├── colors.ts
│       └── validation.ts
```

---

## 2. TypeScript Types

```typescript
// types/feature.ts
export interface Feature {
  id: string;
  title: string;
  description: string;
  businessValue: number;
  requestorId: string;
  deadline: string | null;
  classOfService: 'EXPEDITE' | 'FIXED_DATE' | 'STANDARD' | 'FILLER';
  productIds: string[];
  effortEstimate: EffortEstimate;
  stochasticEstimate: ThreePointEstimate | null;
  dependencies: string[];
  requiredExpertise: string[];
  canSplit: boolean;
  version: number;
}

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

// types/team.ts
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

export interface TeamMember {
  id: string;
  personId: string;
  name: string;
  role: 'BACKEND' | 'FRONTEND' | 'QA' | 'DEVOPS';
  availabilityPercent: number;
}

// types/planning.ts
export interface PlanningJob {
  jobId: string;
  status: 'GREEDY' | 'ANNEALING' | 'MONTE_CARLO' | 'DONE' | 'FAILED';
  progressPercent: number;
  currentCost: number;
  bestCost: number;
  iteration: number;
  errorMessage: string | null;
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

export interface Conflict {
  type: 'CAPACITY_EXCEEDED' | 'DEPENDENCY_VIOLATION' | 'OWNERSHIP_VIOLATION' |
        'PARALLELISM_EXCEEDED' | 'DEADLINE_MISSED' | 'DEPENDENCY_CYCLE';
  featureId: string;
  teamId: string | null;
  message: string;
}

export interface Assignment {
  id: string;
  featureId: string;
  teamId: string;
  sprintId: string;
  allocatedEffort: EffortEstimate;
  status: 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'LOCKED';
}
```

---

## 3. API Client

```typescript
// api/client.ts
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for auth token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

```typescript
// api/planning.ts
import { apiClient } from './client';
import type { PlanningJob, PlanningResult } from '../types';

export const planningApi = {
  runPlanning: async (request: PlanningRunRequest) => {
    const response = await apiClient.post('/planning/run', request);
    return {
      jobId: response.headers['x-job-id'],
      ...response.data,
    } as PlanningJob;
  },

  getJobStatus: async (jobId: string) => {
    const response = await apiClient.get(`/planning/jobs/${jobId}/status`);
    return response.data as PlanningJob;
  },

  getResult: async (jobId: string) => {
    const response = await apiClient.get(`/planning/jobs/${jobId}/result`);
    return response.data as PlanningResult;
  },

  validate: async (request: PlanningRunRequest) => {
    const response = await apiClient.post('/planning/validate', request);
    return response.data;
  },
};
```

---

## 4. Key Components

### 4.1 GanttChart

```tsx
// components/gantt/GanttChart.tsx
import { useRef, useEffect, useState } from 'react';
import type { Feature, Assignment, PlanningResult } from '../../types';
import { GanttBar } from './GanttBar';
import { GanttTimeline } from './GanttTimeline';
import { GanttDependencies } from './GanttDependencies';

interface GanttChartProps {
  features: Feature[];
  assignments: Assignment[];
  planningResult: PlanningResult | null;
  onDragEnd: (featureId: string, newSprintId: string) => void;
  onFeatureClick: (featureId: string) => void;
}

export function GanttChart({
  features,
  assignments,
  planningResult,
  onDragEnd,
  onFeatureClick,
}: GanttChartProps) {
  const [draggedFeature, setDraggedFeature] = useState<string | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // Group assignments by feature
  const featureAssignments = assignments.reduce((acc, assignment) => {
    if (!acc[assignment.featureId]) {
      acc[assignment.featureId] = [];
    }
    acc[assignment.featureId].push(assignment);
    return acc;
  }, {} as Record<string, Assignment[]>);

  return (
    <div ref={containerRef} className="gantt-container">
      <GanttTimeline />
      
      <div className="gantt-body">
        {features.map((feature) => {
          const timeline = planningResult?.featureTimelines[feature.id];
          const assigns = featureAssignments[feature.id] || [];
          
          return (
            <GanttBar
              key={feature.id}
              feature={feature}
              timeline={timeline}
              assignments={assigns}
              isDragged={draggedFeature === feature.id}
              onDragStart={() => setDraggedFeature(feature.id)}
              onDragEnd={(sprintId) => onDragEnd(feature.id, sprintId)}
              onClick={() => onFeatureClick(feature.id)}
            />
          );
        })}
        
        {planningResult && (
          <GanttDependencies
            features={features}
            timelines={planningResult.featureTimelines}
          />
        )}
      </div>
    </div>
  );
}
```

### 4.2 TeamLoadChart

```tsx
// components/planning/TeamLoadChart.tsx
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { TeamLoadReport } from '../../types';

interface TeamLoadChartProps {
  reports: TeamLoadReport[];
  sprintLabels: string[];
}

export function TeamLoadChart({ reports, sprintLabels }: TeamLoadChartProps) {
  const data = reports.map((report) => ({
    name: report.teamId,
    backend: report.loadByDateAndRole?.['BACKEND'] || 0,
    frontend: report.loadByDateAndRole?.['FRONTEND'] || 0,
    qa: report.loadByDateAndRole?.['QA'] || 0,
    devops: report.loadByDateAndRole?.['DEVOPS'] || 0,
    utilization: report.utilizationPercent,
    bottlenecks: report.bottleneckRoles.join(', '),
  }));

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data}>
        <XAxis dataKey="name" />
        <YAxis label={{ value: 'Hours', angle: -90, position: 'insideLeft' }} />
        <Tooltip />
        <Legend />
        <Bar dataKey="backend" stackId="a" fill="#3b82f6" name="Backend" />
        <Bar dataKey="frontend" stackId="a" fill="#10b981" name="Frontend" />
        <Bar dataKey="qa" stackId="a" fill="#f59e0b" name="QA" />
        <Bar dataKey="devops" stackId="a" fill="#8b5cf6" name="DevOps" />
      </BarChart>
    </ResponsiveContainer>
  );
}
```

### 4.3 PlanningControls

```tsx
// components/planning/PlanningControls.tsx
import { useState } from 'react';
import { planningApi } from '../../api/planning';
import { useSSE } from '../../hooks/useSSE';
import type { PlanningParameters } from '../../types';

interface PlanningControlsProps {
  featureIds: string[];
  planningWindowId: string;
  onPlanningComplete: (result: any) => void;
}

export function PlanningControls({
  featureIds,
  planningWindowId,
  onPlanningComplete,
}: PlanningControlsProps) {
  const [isPlanning, setIsPlanning] = useState(false);
  const [jobId, setJobId] = useState<string | null>(null);
  const [progress, setProgress] = useState(0);
  const [phase, setPhase] = useState<string | null>(null);

  const { status } = useSSE(jobId ? `/planning/jobs/${jobId}/sse` : null);

  useEffect(() => {
    if (status) {
      setProgress(status.progressPercent);
      setPhase(status.phase);
      
      if (status.phase === 'DONE') {
        setIsPlanning(false);
        onPlanningComplete(status);
      }
    }
  }, [status]);

  const handleRunPlanning = async () => {
    setIsPlanning(true);
    setProgress(0);
    
    const job = await planningApi.runPlanning({
      featureIds,
      planningWindowId,
      parameters: getDefaultParameters(),
      lockedAssignmentIds: new Set(),
    });
    
    setJobId(job.jobId);
  };

  return (
    <div className="planning-controls">
      <button
        onClick={handleRunPlanning}
        disabled={isPlanning || featureIds.length === 0}
        className="btn btn-primary"
      >
        {isPlanning ? 'Планирование...' : 'Запустить планирование'}
      </button>

      {isPlanning && (
        <div className="planning-progress">
          <div className="progress-bar" style={{ width: `${progress}%` }} />
          <span className="progress-label">
            {phase}: {progress}%
          </span>
        </div>
      )}
    </div>
  );
}
```

### 4.4 ConflictList

```tsx
// components/planning/ConflictList.tsx
import type { Conflict } from '../../types';

interface ConflictListProps {
  conflicts: Conflict[];
  onResolve?: (conflict: Conflict) => void;
}

const conflictSeverity: Record<Conflict['type'], 'error' | 'warning' | 'info'> = {
  CAPACITY_EXCEEDED: 'error',
  DEPENDENCY_VIOLATION: 'error',
  OWNERSHIP_VIOLATION: 'error',
  DEADLINE_MISSED: 'warning',
  PARALLELISM_EXCEEDED: 'warning',
  DEPENDENCY_CYCLE: 'error',
};

export function ConflictList({ conflicts, onResolve }: ConflictListProps) {
  if (conflicts.length === 0) {
    return (
      <div className="conflict-list conflict-list--empty">
        <span className="icon-check">Нет конфликтов</span>
      </div>
    );
  }

  return (
    <div className="conflict-list">
      <h3>Конфликты ({conflicts.length})</h3>
      <ul>
        {conflicts.map((conflict, index) => (
          <li key={index} className={`conflict-item severity-${conflictSeverity[conflict.type]}`}>
            <span className="conflict-type">{conflict.type}</span>
            <span className="conflict-message">{conflict.message}</span>
            {onResolve && (
              <button onClick={() => onResolve(conflict)} className="btn btn-sm">
                Resolve
              </button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
```

---

## 5. Pages

### 5.1 DashboardPage

```tsx
// pages/DashboardPage.tsx
export function DashboardPage() {
  const { data: metrics } = useDashboardMetrics();
  
  return (
    <div className="dashboard">
      <div className="dashboard-grid">
        <MetricCard title="Total Features" value={metrics.totalFeatures} />
        <MetricCard title="Planned Features" value={metrics.plannedFeatures} />
        <MetricCard title="Avg TTM" value={metrics.avgTTM} unit="sprints" />
        <MetricCard title="Team Utilization" value={metrics.avgUtilization} unit="%" />
        <MetricCard title="Active Conflicts" value={metrics.activeConflicts} severity="error" />
        <MetricCard title="Deadline Risk" value={metrics.deadlineRisk} unit="%" severity="warning" />
      </div>
      
      <div className="dashboard-charts">
        <TeamLoadChart reports={metrics.teamLoadReports} sprintLabels={metrics.sprintLabels} />
        <ConflictList conflicts={metrics.conflicts} />
      </div>
    </div>
  );
}
```

### 5.2 PlanPage

```tsx
// pages/PlanPage.tsx
export function PlanPage() {
  const { data: features } = useFeatures();
  const { data: assignments } = useAssignments();
  const { data: planningResult } = usePlanningResult();
  const [selectedFeature, setSelectedFeature] = useState<string | null>(null);
  
  return (
    <div className="plan-page">
      <PlanningControls
        featureIds={features.map(f => f.id)}
        planningWindowId="current"
        onPlanningComplete={(result) => console.log('Planning complete', result)}
      />
      
      <div className="plan-content">
        <GanttChart
          features={features}
          assignments={assignments}
          planningResult={planningResult}
          onDragEnd={handleDragEnd}
          onFeatureClick={setSelectedFeature}
        />
        
        <div className="plan-sidebar">
          {selectedFeature && <FeatureDetail featureId={selectedFeature} />}
          <TeamLoadChart reports={planningResult?.teamLoadReports || []} />
          <ConflictList conflicts={planningResult?.conflicts || []} />
        </div>
      </div>
    </div>
  );
}
```

### 5.3 SimulationPage

```tsx
// pages/SimulationPage.tsx
export function SimulationPage() {
  const [baselinePlan, setBaselinePlan] = useState<PlanningResult | null>(null);
  const [simulatedPlan, setSimulatedPlan] = useState<PlanningResult | null>(null);
  const [changes, setChanges] = useState<SimulationChanges>({});
  
  const handleRunSimulation = async () => {
    // Create a branch of the current plan with modifications
    // Run planning with modified parameters
    // Compare results
  };
  
  return (
    <div className="simulation-page">
      <SimulationControls
        changes={changes}
        onChange={setChanges}
        onRun={handleRunSimulation}
      />
      
      <div className="simulation-comparison">
        <div className="simulation-panel">
          <h3>Baseline</h3>
          {baselinePlan && <PlanSummary result={baselinePlan} />}
        </div>
        
        <div className="simulation-panel">
          <h3>Simulated</h3>
          {simulatedPlan && <PlanSummary result={simulatedPlan} />}
        </div>
        
        {baselinePlan && simulatedPlan && (
          <PlanComparison baseline={baselinePlan} simulated={simulatedPlan} />
        )}
      </div>
    </div>
  );
}
```

---

## 6. Routing

```tsx
// router.tsx
import { createBrowserRouter } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './pages/DashboardPage';
import { PortfolioPage } from './pages/PortfolioPage';
import { PlanPage } from './pages/PlanPage';
import { TeamsPage } from './pages/TeamsPage';
import { FeaturesPage } from './pages/FeaturesPage';
import { SimulationPage } from './pages/SimulationPage';
import { SettingsPage } from './pages/SettingsPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'portfolio', element: <PortfolioPage /> },
      { path: 'plan', element: <PlanPage /> },
      { path: 'teams', element: <TeamsPage /> },
      { path: 'features', element: <FeaturesPage /> },
      { path: 'simulation', element: <SimulationPage /> },
      { path: 'settings', element: <SettingsPage /> },
    ],
  },
]);
```

---

## 7. State Management (Redux Toolkit)

```typescript
// store/planningSlice.ts
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { planningApi } from '../api/planning';
import type { PlanningResult, PlanningJob } from '../types';

interface PlanningState {
  currentJob: PlanningJob | null;
  currentResult: PlanningResult | null;
  isPlanning: boolean;
  error: string | null;
}

const initialState: PlanningState = {
  currentJob: null,
  currentResult: null,
  isPlanning: false,
  error: null,
};

export const runPlanning = createAsyncThunk(
  'planning/run',
  async (request: PlanningRunRequest, { dispatch }) => {
    const job = await planningApi.runPlanning(request);
    
    // Poll for status
    while (true) {
      await new Promise(resolve => setTimeout(resolve, 1000));
      const status = await planningApi.getJobStatus(job.jobId);
      dispatch(updateJobStatus(status));
      
      if (status.phase === 'DONE' || status.phase === 'FAILED') {
        break;
      }
    }
    
    return await planningApi.getResult(job.jobId);
  }
);

const planningSlice = createSlice({
  name: 'planning',
  initialState,
  reducers: {
    updateJobStatus: (state, action) => {
      state.currentJob = action.payload;
    },
    clearPlanning: (state) => {
      state.currentResult = null;
      state.currentJob = null;
      state.isPlanning = false;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(runPlanning.pending, (state) => {
        state.isPlanning = true;
        state.error = null;
      })
      .addCase(runPlanning.fulfilled, (state, action) => {
        state.currentResult = action.payload;
        state.isPlanning = false;
      })
      .addCase(runPlanning.rejected, (state, action) => {
        state.isPlanning = false;
        state.error = action.error.message || 'Planning failed';
      });
  },
});
```

---

## 8. SSE Hook

```typescript
// hooks/useSSE.ts
import { useEffect, useState, useCallback } from 'react';

export function useSSE(url: string | null) {
  const [status, setStatus] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!url) return;

    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
    const eventSource = new EventSource(`${baseUrl}${url}`);

    eventSource.onmessage = (event) => {
      setStatus(JSON.parse(event.data));
    };

    eventSource.onerror = () => {
      setError('SSE connection lost');
      eventSource.close();
    };

    return () => eventSource.close();
  }, [url]);

  return { status, error };
}
```

---

## 9. Dependencies

```json
{
  "dependencies": {
    "react": "^19.0.0",
    "react-dom": "^19.0.0",
    "react-router-dom": "^7.0.0",
    "@reduxjs/toolkit": "^2.0.0",
    "react-redux": "^9.0.0",
    "axios": "^1.7.0",
    "recharts": "^2.12.0",
    "frappe-gantt": "^0.6.0",
    "date-fns": "^4.0.0",
    "clsx": "^2.0.0"
  },
  "devDependencies": {
    "typescript": "^5.6.0",
    "vite": "^6.0.0",
    "@vitejs/plugin-react": "^4.3.0",
    "@types/react": "^19.0.0",
    "@types/react-dom": "^19.0.0",
    "tailwindcss": "^3.4.0",
    "eslint": "^9.0.0"
  }
}
```

---

## 10. Тестовые сценарии

| Тест | Описание |
|------|----------|
| GanttChart.render | Отображение фич на таймлайне |
| GanttChart.dragDrop | Перетаскивание фичи на другой спринт |
| TeamLoadChart.render | Столбчатая диаграмма загрузки по ролям |
| PlanningControls.runPlanning | Запуск планирования и отображение прогресса |
| ConflictList.render | Отображение конфликтов с severity |
| FeatureForm.validation | Валидация формы создания фичи |
| useSSE.connect | SSE подключение к планировщику |
