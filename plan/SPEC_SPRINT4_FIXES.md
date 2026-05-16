# Sprint 4 — Critical Fixes: MonteCarlo, Web UI Wiring, Infrastructure

> **Module:** planning-engine + web-ui
> **Agent:** Backend Java (MonteCarlo) + Frontend TypeScript (web-ui)
> **Duration:** 1-2 дня
> **Priority:** P0 (блокирует корректность системы)

---

## 1. Контекст

### 1.1 Текущее состояние

| Область | Score | Проблема |
|---------|-------|----------|
| MonteCarloSimulator | ❌ BROKEN | Формула perturbation масштабирует расстояние от reference date вместо duration |
| Web UI Forms | ❌ STUB | console.log вместо API calls |
| Planning API baseURL | ❌ WRONG | planningBaseUrl declared but never used |
| Gantt drag-and-drop | ❌ BROKEN | Hardcoded sprint ID |
| SSE hook | ❌ WRONG URL | Port 8080 вместо 8081 |
| Web UI tests | ❌ 0 files | No test coverage |
| ESLint | ❌ Missing | No config file |
| frappe-gantt | ⚠️ Unused | Dependency but not used |

### 1.2 Что НЕ трогать

- ✅ Domain entities — работают корректно
- ✅ JPA repositories — работают корректно
- ✅ EntityMapper — работает корректно
- ✅ SecurityConfig — настроен
- ✅ CI pipeline — тестирует все модули
- ✅ data-service tests — 78 тестов проходят
- ✅ DTOs → records — уже конвертированы (Sprint 4 предыдущий агент)

---

## 2. Задача 1: Fix MonteCarloSimulator Date Bug (P0 BLOCKER)

### 2.1 Проблема

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/montecarlo/MonteCarloSimulator.java`

**Текущая формула (НЕКОРРЕКТНАЯ):**
```java
long baseDays = baseDate.toEpochDay();
long referenceDays = LocalDate.of(baseDate.getYear() - 1, 1, 1).toEpochDay();
long durationFromRef = baseDays - referenceDays;
long perturbedDays = referenceDays + Math.round(durationFromRef * ratio);
return LocalDate.ofEpochDay(perturbedDays);
```

**Проблема:** Perturbation масштабирует **абсолютное расстояние от 1 января прошлого года** вместо того чтобы perturboвать **duration работы фичи**.

**Пример бага:**
- baseDate = 2026-06-01 (day 20604), reference = 2025-01-01 (day 20089)
- durationFromRef = 515 дней
- ratio = 2.0 → perturbedDays = 20089 + 1030 = 21119 → **2027-11-01** (515 дней позже!)
- ratio = 0.5 → perturbedDays = 20089 + 257 = 20346 → **2025-09-01** (257 дней раньше!)

### 2.2 Правильная формула

Perturboвать **duration от start date фичи до base completion date**:

```java
private LocalDate perturbCompletionDate(FeatureRequest feature, LocalDate baseDate, Random random) {
    ThreePointEstimate estimate = feature.getStochasticEstimate();
    if (estimate == null) {
        return baseDate;
    }

    double sampled = sampleTriangular(
        estimate.optimistic(),
        estimate.mostLikely(),
        estimate.pessimistic(),
        random
    );

    double expected = estimate.expected();
    double ratio = sampled / expected;

    // Perturb the DURATION, not the absolute date
    long startDays = feature.getStartDate().toEpochDay();
    long baseDays = baseDate.toEpochDay();
    long duration = baseDays - startDays;
    long perturbedDuration = Math.round(duration * ratio);

    return LocalDate.ofEpochDay(startDays + perturbedDuration);
}
```

**Если у FeatureRequest нет startDate** — использовать baseDate как reference и perturboвать относительно него:

```java
private LocalDate perturbCompletionDate(FeatureRequest feature, LocalDate baseDate, Random random) {
    ThreePointEstimate estimate = feature.getStochasticEstimate();
    if (estimate == null) {
        return baseDate;
    }

    double sampled = sampleTriangular(
        estimate.optimistic(),
        estimate.mostLikely(),
        estimate.pessimistic(),
        random
    );

    double expected = estimate.expected();
    double ratio = sampled / expected;

    // Perturb relative to baseDate: shift by (ratio - 1) * baseDays
    long baseDays = baseDate.toEpochDay();
    long perturbationDays = Math.round(baseDays * (ratio - 1));
    return LocalDate.ofEpochDay(baseDays + perturbationDays);
}
```

### 2.3 Тесты

**Файл:** `planning-engine/src/test/java/com/featureflow/planning/montecarlo/MonteCarloSimulatorTest.java`

Добавить тесты:

```java
@Test
void perturbCompletionDate_earlyDeadline_lowProbability() {
    // Feature with deadline BEFORE base completion date
    UUID f1 = uuid("f1");
    FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
    feature.setDeadline(LocalDate.of(2025, 1, 5)); // Early deadline
    feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
    feature.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

    MonteCarloParameters params = new MonteCarloParameters(500, 0.95);

    MonteCarloSimulator simulator = new MonteCarloSimulator();
    Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
        List.of(feature),
        Map.of(f1, LocalDate.of(2025, 1, 15)), // Base completion after deadline
        params,
        new Random(42)
    );

    FeatureDeadlineProbability prob = results.get(f1);
    assertThat(prob.probability()).isLessThan(0.5); // Should be low probability
}

@Test
void perturbCompletionDate_lateDeadline_highProbability() {
    // Feature with deadline FAR AFTER base completion date
    UUID f1 = uuid("f1");
    FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
    feature.setDeadline(LocalDate.of(2025, 6, 1)); // Far future deadline
    feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
    feature.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

    MonteCarloParameters params = new MonteCarloParameters(500, 0.95);

    MonteCarloSimulator simulator = new MonteCarloSimulator();
    Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
        List.of(feature),
        Map.of(f1, LocalDate.of(2025, 1, 15)), // Base completion well before deadline
        params,
        new Random(42)
    );

    FeatureDeadlineProbability prob = results.get(f1);
    assertThat(prob.probability()).isGreaterThan(0.7); // Should be high probability
}

@Test
void perturbCompletionDate_perturbationReasonable() {
    // Verify perturbation magnitude is reasonable (not 500+ days)
    UUID f1 = uuid("f1");
    FeatureRequest feature = new FeatureRequest(f1, "f1", "", 50.0);
    feature.setDeadline(LocalDate.of(2025, 2, 1));
    feature.setEffortEstimate(new EffortEstimate(40, 0, 0, 0));
    feature.setStochasticEstimate(new ThreePointEstimate(30, 40, 60));

    MonteCarloParameters params = new MonteCarloParameters(100, 0.95);
    LocalDate baseDate = LocalDate.of(2025, 1, 15);

    MonteCarloSimulator simulator = new MonteCarloSimulator();

    // Run multiple simulations and check perturbation range
    for (int i = 0; i < 20; i++) {
        Map<UUID, FeatureDeadlineProbability> results = simulator.simulate(
            List.of(feature),
            Map.of(f1, baseDate),
            params,
            new Random(i)
        );

        FeatureDeadlineProbability prob = results.get(f1);
        LocalDate p50 = prob.p50Date();
        LocalDate p90 = prob.p90Date();

        // Perturbation should be within reasonable range (±30 days from base)
        long p50Diff = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(baseDate, p50));
        long p90Diff = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(baseDate, p90));

        assertThat(p50Diff).isLessThan(30);
        assertThat(p90Diff).isLessThan(60);
    }
}
```

### 2.4 Verification

```bash
mvn test -pl planning-engine -Dtest=MonteCarloSimulatorTest --batch-mode
```

---

## 3. Задача 2: Wire Web UI Forms to API (P0)

### 3.1 FeaturesPage — Create/Edit

**Файл:** `web-ui/src/pages/FeaturesPage.tsx`

**Текущее (строка ~62):**
```typescript
const handleCreate = (data: CreateFeatureRequest) => {
  console.log('Create feature:', data);
  setShowCreateModal(false);
};
```

**Стало:**
```typescript
const handleCreate = async (data: CreateFeatureRequest) => {
  try {
    await featuresApi.create(data);
    setShowCreateModal(false);
    // Refresh feature list
    setFeatures(prev => [...prev, /* new feature from response */]);
  } catch (error) {
    console.error('Failed to create feature:', error);
  }
};
```

**Аналогично для handleEdit:**
```typescript
const handleEdit = async (id: string, data: UpdateFeatureRequest) => {
  try {
    await featuresApi.update(id, data);
    setShowEditModal(false);
    // Update feature in list
    setFeatures(prev => prev.map(f => f.id === id ? { ...f, ...data } : f));
  } catch (error) {
    console.error('Failed to update feature:', error);
  }
};
```

**Для handleDelete:**
```typescript
const handleDelete = async (id: string) => {
  if (!confirm('Delete this feature?')) return;
  try {
    await featuresApi.delete(id);
    setFeatures(prev => prev.filter(f => f.id !== id));
  } catch (error) {
    console.error('Failed to delete feature:', error);
  }
};
```

### 3.2 SettingsPage — Planning Parameters

**Файл:** `web-ui/src/pages/SettingsPage.tsx`

**Текущее:**
```typescript
const handleSavePlanningParams = (params: PlanningParameters) => {
  console.log('Save planning params:', params);
};
```

**Стало:**
```typescript
const handleSavePlanningParams = async (params: PlanningParameters) => {
  try {
    // Store in localStorage for now (no backend endpoint yet)
    localStorage.setItem('planningParams', JSON.stringify(params));
    setSaveSuccess('Planning parameters saved');
    setTimeout(() => setSaveSuccess(null), 3000);
  } catch (error) {
    console.error('Failed to save parameters:', error);
    setSaveError('Failed to save parameters');
  }
};
```

### 3.3 SettingsPage — Integration Configs

**Текущее:**
```typescript
const handleTestJira = () => {
  console.log('Test Jira connection:', jiraConfig);
};
```

**Стало:**
```typescript
const handleTestJira = async () => {
  try {
    setTesting('jira');
    const result = await integrationsApi.testConnection('jira', {
      baseUrl: jiraConfig.baseUrl,
      authType: 'token',
      apiToken: jiraConfig.apiToken,
    });
    setTestResult('jira', result.success ? 'success' : 'error');
  } catch (error) {
    setTestResult('jira', 'error');
  } finally {
    setTesting(null);
  }
};
```

### 3.4 SettingsPage — Import/Export

```typescript
const handleImport = async (type: 'jira' | 'ado' | 'linear') => {
  try {
    setImporting(true);
    const result = await integrationsApi.import(type, integrationConfig, importFilter);
    setImportResult(result);
  } catch (error) {
    console.error('Import failed:', error);
  } finally {
    setImporting(false);
  }
};
```

### 3.5 SimulationPage — Wire to API

**Файл:** `web-ui/src/pages/SimulationPage.tsx`

**Текущее:**
```typescript
const runSimulation = async () => {
  setIsSimulating(true);
  setTimeout(() => setIsSimulating(false), 2000);
};
```

**Стало:**
```typescript
const runSimulation = async () => {
  try {
    setIsSimulating(true);
    setSimulationResult(null);

    // Parse JSON inputs
    const priorityChanges = JSON.parse(priorityChangesJson);
    const capacityChanges = JSON.parse(capacityChangesJson);

    // Build simulation request
    const request: PlanningRunRequest = {
      featureIds: selectedFeatures,
      teamIds: selectedTeams,
      planningWindowId: selectedWindow,
      parameters: {
        ...defaultParams,
        w1Ttm: priorityChanges.w1Ttm ?? defaultParams.w1Ttm,
        w2Underutilization: priorityChanges.w2Underutilization ?? defaultParams.w2Underutilization,
        w3DeadlinePenalty: priorityChanges.w3DeadlinePenalty ?? defaultParams.w3DeadlinePenalty,
      },
      lockedAssignmentIds: lockedAssignments,
    };

    // Run planning with modified parameters
    const job = await planningApi.run(request);

    // Poll for completion
    while (true) {
      await new Promise(resolve => setTimeout(resolve, 1000));
      const status = await planningApi.getJobStatus(job.jobId);
      if (status.status === 'DONE' || status.status === 'FAILED') break;
    }

    const result = await planningApi.getResult(job.jobId);
    setSimulationResult(result);
  } catch (error) {
    console.error('Simulation failed:', error);
  } finally {
    setIsSimulating(false);
  }
};
```

### 3.6 PortfolioPage — Wire Timeline

**Файл:** `web-ui/src/pages/PortfolioPage.tsx`

**Текущее:**
```typescript
const timeline = []; // TODO: fetch from dashboardApi.getTimeline()
```

**Стало:**
```typescript
const [timeline, setTimeline] = useState<TimelineEntry[]>([]);
const [loading, setLoading] = useState(true);

useEffect(() => {
  const fetchTimeline = async () => {
    try {
      const data = await dashboardApi.getTimeline();
      setTimeline(data);
    } catch (error) {
      console.error('Failed to fetch timeline:', error);
    } finally {
      setLoading(false);
    }
  };
  fetchTimeline();
}, []);
```

---

## 4. Задача 3: Fix Planning API BaseURL (P0)

### 4.1 Проблема

**Файл:** `web-ui/src/api/planning.ts`

```typescript
const planningBaseUrl = import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081/api/v1';
// ... но все вызовы используют apiClient (port 8080)
```

### 4.2 Fix

Создать отдельный axios instance для planning engine:

```typescript
import axios from 'axios';

const planningClient = axios.create({
  baseURL: import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081/api/v1',
  timeout: 60000, // Planning can take longer
  headers: { 'Content-Type': 'application/json' },
});

// Reuse auth token interceptor
planningClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) { config.headers.Authorization = `Bearer ${token}`; }
  return config;
});

export const planningApi = {
  run: async (request: PlanningRunRequest) => {
    const r = await planningClient.post('/planning/run', request);
    return { jobId: r.headers['x-job-id'] || r.data.jobId, ...r.data } as PlanningJob;
  },
  getJobStatus: async (jobId: string) => {
    const r = await planningClient.get(`/planning/jobs/${jobId}/status`);
    return r.data;
  },
  getResult: async (jobId: string) => {
    const r = await planningClient.get(`/planning/jobs/${jobId}/result`);
    return r.data;
  },
  validate: async (request: PlanningRunRequest) => {
    const r = await planningClient.post('/planning/validate', request);
    return r.data;
  },
};
```

---

## 5. Задача 4: Fix Gantt Drag-and-Drop (P1)

### 5.1 Проблема

**Файл:** `web-ui/src/components/gantt/GanttChart.tsx`

```typescript
const droppedSprintId = 'dropped-sprint'; // TODO: detect actual sprint
```

### 5.2 Fix

Реализовать hit-testing для определения спринта по X-координате:

```typescript
const handleDrop = (featureId: string, clientX: number) => {
  const container = ganttContainerRef.current;
  if (!container || !planningResult) return;

  const rect = container.getBoundingClientRect();
  const x = clientX - rect.left + container.scrollLeft;

  // Calculate date from X position
  const totalDays = daysBetween(startDate, endDate);
  const pixelsPerDay = rect.width / totalDays;
  const droppedDayOffset = Math.floor(x / pixelsPerDay);
  const droppedDate = addDaysToDate(startDate, droppedDayOffset);

  // Find which sprint contains this date
  const sprints = planningResult.sprints || [];
  const targetSprint = sprints.find(s =>
    droppedDate >= s.startDate && droppedDate <= s.endDate
  );

  if (!targetSprint) {
    console.warn('No sprint found for dropped date:', droppedDate);
    return;
  }

  // Dispatch assignment update
  dispatch(updateAssignment({ featureId, sprintId: targetSprint.id }));
};
```

### 5.3 GanttBar — Pass feature data

```typescript
<GanttBar
  feature={feature}
  timeline={timeline}
  onDrop={(featureId, clientX) => handleDrop(featureId, clientX)}
/>
```

---

## 6. Задача 5: Fix SSE Hook BaseURL (P1)

### 6.1 Проблема

**Файл:** `web-ui/src/hooks/useSSE.ts`

```typescript
const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

SSE для planning jobs на port 8081.

### 6.2 Fix

Добавить параметр для выбора baseURL:

```typescript
export function useSSE(url: string | null, service: 'data' | 'planning' = 'data') {
  const [status, setStatus] = useState<Record<string, unknown> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!url) return;

    const baseUrl = service === 'planning'
      ? import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081'
      : import.meta.env.VITE_API_URL || 'http://localhost:8080';

    const eventSource = new EventSource(`${baseUrl}${url}`);

    eventSource.onmessage = (event) => {
      try {
        setStatus(JSON.parse(event.data));
      } catch {
        setError('Failed to parse SSE data');
      }
    };

    eventSource.onerror = () => {
      setError('SSE connection lost');
      eventSource.close();
    };

    return () => eventSource.close();
  }, [url, service]);

  return { status, error };
}
```

**Usage:**
```typescript
// For planning jobs
const { status } = useSSE(`/planning/jobs/${jobId}/sse`, 'planning');

// For data-service events
const { status } = useSSE('/events/notifications', 'data');
```

---

## 7. Задача 6: Add ESLint Config (P1)

### 7.1 Создать `web-ui/eslint.config.js`

```javascript
import js from '@eslint/js';
import ts from 'typescript-eslint';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';

export default [
  js.configs.recommended,
  ...ts.configs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    plugins: {
      react,
      'react-hooks': reactHooks,
    },
    rules: {
      'react/react-in-jsx-scope': 'off', // React 19 doesn't need import
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      '@typescript-eslint/no-explicit-any': 'warn',
      'prefer-const': 'warn',
    },
  },
  {
    ignores: ['dist/', 'node_modules/', '*.config.js'],
  },
];
```

### 7.2 Update package.json devDependencies

```json
"devDependencies": {
  "@eslint/js": "^9.0.0",
  "typescript-eslint": "^8.0.0",
  "eslint-plugin-react": "^7.37.0",
  "eslint-plugin-react-hooks": "^5.0.0",
  // ... existing
}
```

---

## 8. Задача 7: Add Web UI Unit Tests (P1)

### 8.1 Setup

**Install:**
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom
```

**Add to vite.config.ts:**
```typescript
/// <reference types="vitest" />
export default defineConfig({
  // ... existing
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
  },
});
```

**Create `web-ui/src/test/setup.ts`:**
```typescript
import '@testing-library/jest-dom';
```

**Update package.json scripts:**
```json
"scripts": {
  "test": "vitest",
  "test:coverage": "vitest --coverage"
}
```

### 8.2 Button.test.tsx

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '../components/common/Button';
import { describe, it, expect } from 'vitest';

describe('Button', () => {
  it('renders with default props', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button')).toHaveTextContent('Click me');
  });

  it('applies variant classes', () => {
    render(<Button variant="danger">Delete</Button>);
    const button = screen.getByRole('button');
    expect(button).toHaveClass('bg-red-600');
  });

  it('shows loading spinner', () => {
    render(<Button loading>Save</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText('Save')).toBeInTheDocument();
  });

  it('calls onClick', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click</Button>);
    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('disabled state prevents click', () => {
    const handleClick = vi.fn();
    render(<Button disabled onClick={handleClick}>Disabled</Button>);
    fireEvent.click(screen.getByRole('button'));
    expect(handleClick).not.toHaveBeenCalled();
  });
});
```

### 8.3 Badge.test.tsx

```typescript
import { render, screen } from '@testing-library/react';
import { Badge } from '../components/common/Badge';
import { describe, it, expect } from 'vitest';

describe('Badge', () => {
  it('renders with default variant', () => {
    render(<Badge>Default</Badge>);
    expect(screen.getByText('Default')).toBeInTheDocument();
  });

  it('applies success variant', () => {
    render(<Badge variant="success">OK</Badge>);
    expect(screen.getByText('OK')).toHaveClass('bg-green-100');
  });

  it('applies error variant', () => {
    render(<Badge variant="error">Error</Badge>);
    expect(screen.getByText('Error')).toHaveClass('bg-red-100');
  });

  it('applies size classes', () => {
    render(<Badge size="sm">Small</Badge>);
    expect(screen.getByText('Small')).toHaveClass('text-xs');
  });
});
```

### 8.4 validation.test.ts

```typescript
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
});
```

### 8.5 planningSlice.test.ts

```typescript
import planningReducer, { runPlanning, clearPlanning } from '../store/planningSlice';
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('../api/planning', () => ({
  planningApi: {
    run: vi.fn(),
    getJobStatus: vi.fn(),
    getResult: vi.fn(),
  },
}));

describe('planningSlice', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('has initial state', () => {
    const state = planningReducer(undefined, { type: 'unknown' });
    expect(state.currentJob).toBeNull();
    expect(state.currentResult).toBeNull();
    expect(state.isPlanning).toBe(false);
    expect(state.error).toBeNull();
  });

  it('clearPlanning resets state', () => {
    const state = planningReducer(
      { currentJob: { jobId: '1' } as any, currentResult: {} as any, isPlanning: true, error: 'err' },
      clearPlanning()
    );
    expect(state.currentJob).toBeNull();
    expect(state.isPlanning).toBe(false);
  });
});
```

---

## 9. Задача 8: Remove Unused frappe-gantt (P2)

**Файл:** `web-ui/package.json`

Удалить:
```json
"frappe-gantt": "^0.6.0",
```

---

## 10. Задача 9: Add web-ui to docker-compose.yml (P2)

**Файл:** `docker-compose.yml`

Добавить service:
```yaml
web-ui:
  build:
    context: ./web-ui
    dockerfile: ../docker/Dockerfile.web-ui
  ports: ["3000:80"]
  depends_on: [data-service, planning-engine]
  environment:
    VITE_API_URL: http://localhost:8080/api/v1
    VITE_PLANNING_ENGINE_URL: http://localhost:8081/api/v1
```

**Создать `docker/Dockerfile.web-ui`:**
```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY web-ui/package.json web-ui/package-lock.json* ./
RUN npm ci
COPY web-ui/ .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Создать `docker/nginx.conf`:**
```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/v1/ {
        proxy_pass http://data-service:8080/api/v1/;
    }
}
```

---

## 11. Задача 10: Add Prettier Config (P2)

**Создать `web-ui/.prettierrc`:**
```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2,
  "arrowParens": "always"
}
```

**Update package.json:**
```json
"scripts": {
  "format": "prettier --write \"src/**/*.{ts,tsx,css}\""
}
```

---

## 12. Порядок выполнения

```
Этап 1: MonteCarlo fix (30 мин)
  └── Fix perturbCompletionDate formula + add tests

Этап 2: Web UI API wiring (4-6 часов)
  ├── FeaturesPage — create/edit/delete
  ├── SettingsPage — planning params, integrations
  ├── SimulationPage — run simulation
  └── PortfolioPage — timeline fetch

Этап 3: BaseURL fixes (30 мин)
  ├── Planning API — separate axios instance
  └── SSE hook — service parameter

Этап 4: Gantt drag-and-drop (2 часа)
  └── Sprint hit-testing by X coordinate

Этап 5: Infrastructure (1 час)
  ├── ESLint config
  ├── Remove frappe-gantt
  └── Prettier config

Этап 6: Tests (3-4 часа)
  ├── Button, Badge, Modal tests
  ├── validation tests
  └── planningSlice tests
```

---

## 13. Acceptance Criteria

### Must Have
- [ ] MonteCarloSimulator perturbation formula fixed — perturbation within ±30 days
- [ ] MonteCarloSimulatorTest — 3 new tests pass
- [ ] FeaturesPage — create/edit/delete wired to API
- [ ] SettingsPage — planning params saved to localStorage
- [ ] Planning API — uses port 8081 baseURL
- [ ] SSE hook — uses correct baseURL based on service type
- [ ] `mvn test -pl planning-engine` — all tests pass
- [ ] `npm run typecheck` — no TypeScript errors

### Nice to Have
- [ ] Gantt drag-and-drop — sprint hit-testing works
- [ ] SimulationPage — wired to planning API
- [ ] PortfolioPage — timeline loaded from API
- [ ] ESLint — `npm run lint` passes
- [ ] Unit tests — 15+ tests for common components + utils
- [ ] frappe-gantt removed from package.json
- [ ] Prettier config added
- [ ] web-ui in docker-compose.yml

### NOT in Scope
- Web UI dark theme implementation
- Mobile responsive testing
- E2E tests (Playwright/Cypress)
- Performance optimization
- What-if simulation backend

---

## 14. Команды для верификации

```bash
# Backend
mvn test -pl planning-engine -Dtest=MonteCarloSimulatorTest --batch-mode
mvn test -pl planning-engine --batch-mode

# Frontend
cd web-ui
npm install
npm run typecheck
npm run lint
npm test
npm run build
```

---

## 15. Known Patterns & Conventions

### 15.1 API call pattern

```typescript
const handleAction = async (data: RequestType) => {
  try {
    setLoading(true);
    const result = await apiService.action(data);
    // Update local state
    setState(prev => updateState(prev, result));
    // Show success
    setSuccess('Action completed');
  } catch (error) {
    console.error('Action failed:', error);
    setError('Action failed');
  } finally {
    setLoading(false);
  }
};
```

### 15.2 Test pattern

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

describe('ComponentName', () => {
  it('renders correctly', () => {
    render(<ComponentName />);
    expect(screen.getByText('Expected')).toBeInTheDocument();
  });
});
```

### 15.3 Axios interceptor pattern

```typescript
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) { config.headers.Authorization = `Bearer ${token}`; }
  return config;
});
```

---

## 16. Важные замечания

1. **НЕ менять domain entities** — они работают корректно
2. **НЕ менять JPA repositories** — они работают корректно
3. **НЕ менять data-service** — 78 тестов проходят
4. **MonteCarlo fix — минимальное изменение** — только формула perturbCompletionDate
5. **Web UI — следовать существующим patterns** — использовать featuresApi, teamsApi, etc.
6. **Каждый API call — try/catch с loading state** — consistent error handling
7. **Тесты — vitest + testing-library** — НЕ использовать Jest
8. **ESLint — TypeScript + React rules** — НЕ использовать JavaScript-only config
9. **Gantt hit-testing — использовать actual sprint dates** — НЕ hardcoded labels
10. **SSE hook — backward compatible** — default service = 'data'
