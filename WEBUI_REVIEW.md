# Web UI — Code Review (Designer Delivery)

> **Reviewer:** Senior Software Architect / Technical Design Authority
> **Date:** 2026-05-16
> **Spec Reference:** `plan/specs/web-ui.md`, `front.md`
> **Module:** `web-ui/`
> **Status:** ⚠️ GOOD FOUNDATION — 58 files, 7 pages, complete component library, but needs wiring and testing

---

## 1. Executive Summary

Дизайнер создал полноценный frontend-каркас FeatureFlow: **58 файлов**, **7 страниц**, **20+ компонентов**, **Redux store**, **API layer**, **TypeScript types**, **Tailwind CSS** с кастомной дизайн-системой.

**Overall assessment: 65/100** — отличная визуальная и архитектурная основа, но формы не подключены к API, нет тестов, есть несколько багов.

---

## 2. Что сделано хорошо ✅

### 2.1 Архитектура — ОТЛИЧНО (90/100)

| Компонент | Файлы | Assessment |
|-----------|-------|------------|
| **API Layer** | 7 файлов | Axios client с interceptors, auth token, typed endpoints |
| **Types** | 6 файлов | Полные TypeScript interfaces для всех domain entities |
| **Store** | 3 файла | Redux Toolkit: planningSlice (async thunk), uiSlice (UI state) |
| **Hooks** | 4 файла | useFeatures, useTeams, usePlanning, useSSE — cancellation support |
| **Components** | 20 файлов | Common (6), Gantt (5), Planning (5), Features (4) |
| **Pages** | 7 файлов | Dashboard, Portfolio, Plan, Teams, Features, Simulation, Settings |
| **Utils** | 3 файла | Colors, dates, validation |

**Структура соответствует Clean Architecture:**
```
src/
├── api/          → Infrastructure layer
├── types/        → Domain types
├── store/        → State management
├── hooks/        → Data fetching
├── components/   → UI components (common/features/gantt/planning/layout)
├── pages/        → Route-level components
└── utils/        → Pure functions
```

### 2.2 Дизайн-система — ОТЛИЧНО

**Tailwind config** с кастомной палитрой:
- `brand` — 10 оттенков синего (#eff6ff → #1e3a8a)
- `status` — onTrack (#10b981), atRisk (#f59e0b), blocked (#ef4444), completed (#6366f1)
- `load` — low/medium/high цветовая индикация
- Шрифты: Inter (sans), JetBrains Mono/Fira Code (mono)

**Common components** reusable:
- `Button` — 4 variants (primary/secondary/danger/ghost), 3 sizes, loading state
- `Modal` — backdrop, ESC close, body overflow lock, 4 sizes, aria-modal
- `Table` — generic `<T>`, sortable, custom render, empty state
- `Badge` — 5 variants, 2 sizes
- `Tooltip` — 4 positions
- `Loading` — 3 sizes, fullscreen mode

### 2.3 Gantt Chart — ХОРОШО

- Custom implementation (не frappe-gantt) — правильный выбор для кастомизации
- SVG dependency arrows с bezier curves
- Zoom levels: day/week/sprint/month
- HTML5 drag-and-drop для feature bars
- Color by ClassOfService
- Probability indicator на барах

### 2.4 API Integration — ХОРОШО

- Axios client с JWT interceptor (Authorization: Bearer token)
- 401 → redirect to /login
- Typed API functions для всех endpoints: products, teams, features, assignments, planning, dashboard, integrations
- Planning API использует отдельный baseURL (port 8081)

### 2.5 Redux Store — ХОРОШО

- `planningSlice`: async thunk `runPlanning` с polling job status каждые 1 секунду
- `uiSlice`: sidebar, gantt zoom, selected items, modals, theme
- Proper TypeScript types (RootState, AppDispatch)

### 2.6 Documentation — ОТЛИЧНО

- `FRONTEND-GUIDE.md` (353 строки) — comprehensive developer guide
- `SUMMARY.md` (211 строк) — project summary
- Оба на русском, с примерами кода и архитектурными диаграммами

---

## 3. Критические проблемы 🔴

### C1. Формы НЕ подключены к API

**Файлы:** `FeaturesPage.tsx`, `SettingsPage.tsx`, `SimulationPage.tsx`

```typescript
// FeaturesPage.tsx:62 — console.log вместо API call
const handleCreate = (data: CreateFeatureRequest) => {
  console.log('Create feature:', data);
  // TODO: wire to API
};

// SettingsPage.tsx:150 — console.log вместо API call
const handleSavePlanningParams = (params: PlanningParameters) => {
  console.log('Save planning params:', params);
};

// SimulationPage.tsx:45 — stub implementation
const runSimulation = async () => {
  setTimeout(() => setIsSimulating(false), 2000);
};
```

**Impact:** Пользователь не может создавать/редактировать фичи, сохранять настройки, запускать симуляции.

**Fix:** Заменить `console.log` на вызовы `featuresApi.create()`, `featuresApi.update()`, `integrationsApi.import()`, etc.

### C2. Planning API использует wrong baseURL

**Файл:** `api/planning.ts:5`

```typescript
const planningBaseUrl = import.meta.env.VITE_PLANNING_ENGINE_URL || 'http://localhost:8081/api/v1';
// ... но все вызовы используют apiClient (port 8080), а не planningBaseUrl!
```

`planningBaseUrl` объявлен но **никогда не используется**. Все вызовы идут через `apiClient` который pointing на port 8080 (data-service), а planning engine на port 8081.

**Fix:** Создать отдельный axios instance для planning engine или использовать `planningBaseUrl` в вызовах.

### C3. Drag-and-drop Gantt не работает

**Файл:** `components/gantt/GanttChart.tsx`, `components/gantt/GanttBar.tsx`

```typescript
// GanttChart.tsx:89 — hardcoded sprint ID
const droppedSprintId = 'dropped-sprint'; // TODO: detect actual sprint
```

Drop handler всегда возвращает `'dropped-sprint'` вместо определения реального спринта по позиции.

**Fix:** Реализовать hit-testing: определить какой sprint column соответствует X-координате drop.

---

## 4. Важные проблемы 🟡

### M1. Нет тестов

**0 test файлов** во всём web-ui. Нет unit tests для компонентов, hooks, utils, store.

**Что добавить:**
- `Button.test.tsx`, `Modal.test.tsx`, `Table.test.tsx` — common components
- `useFeatures.test.ts`, `useSSE.test.ts` — hooks
- `validation.test.ts` — utils
- `planningSlice.test.ts` — store async thunk

### M2. Нет ESLint config

`package.json` имеет `"lint": "eslint ."` но **нет `eslint.config.js`** или `.eslintrc`.

**Fix:** Добавить `eslint.config.js` с TypeScript + React rules.

### M3. GanttTimeline sprint labels — hardcoded formula

**Файл:** `components/gantt/GanttTimeline.tsx:35`

```typescript
const label = `S${Math.ceil(date.getDate() / 14)}`;
```

Использует день месяца / 14 вместо реальных sprint данных из planning result.

**Fix:** Использовать `planningResult.featureTimelines` или actual sprint data.

### M4. frappe-gantt в dependencies но не используется

**Файл:** `package.json:14`

```json
"frappe-gantt": "^0.6.0",
```

Библиотека установлена но Gantt chart — custom implementation. Лишняя зависимость ~50KB.

**Fix:** Удалить из package.json или использовать библиотеку вместо custom implementation.

### M5. SSE hook использует wrong base URL

**Файл:** `hooks/useSSE.ts:10`

```typescript
const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const eventSource = new EventSource(`${baseUrl}${url}`);
```

SSE endpoint для planning jobs находится на planning-engine (port 8081), не data-service (port 8080).

**Fix:** Использовать `VITE_PLANNING_ENGINE_URL` для SSE planning endpoints.

### M6. Нет Dockerfile для web-ui

`docker-compose.yml` не включает web-ui service. Frontend ожидается запускать отдельно через `npm run dev`.

**Fix:** Добавить `web-ui` service в docker-compose.yml с nginx для production build.

### M7. Нет Prettier config

`front.md` упоминает Prettier но **нет `.prettierrc`**.

**Fix:** Добавить `.prettierrc` с consistent formatting rules.

### M8. PortfolioPage использует stub timeline data

**Файл:** `pages/PortfolioPage.tsx:45`

```typescript
const timeline = []; // TODO: fetch from dashboardApi.getTimeline()
```

Timeline table пустой — данные не загружаются.

**Fix:** Подключить `dashboardApi.getTimeline()`.

---

## 5. Minor issues 🟢

| ID | Issue | File | Severity |
|----|-------|------|----------|
| m1 | `noUnusedLocals: true` в tsconfig — может вызвать build errors | tsconfig.json | LOW |
| m2 | Нет favicon — index.html ссылается на `/vite.svg` которого нет | index.html | LOW |
| m3 | Нет loading state для DashboardPage | DashboardPage.tsx | LOW |
| m4 | DependencyGraph — list-based, не visual graph | DependencyGraph.tsx | LOW |
| m5 | SettingsPage integration forms не сохраняют | SettingsPage.tsx | MEDIUM |
| m6 | SimulationPage — полная заглушка | SimulationPage.tsx | MEDIUM |
| m7 | Нет error boundary | App.tsx | LOW |
| m8 | Theme toggle в uiSlice но не используется в UI | uiSlice.ts / AppLayout.tsx | LOW |
| m9 | Нет pagination controls в FeatureList | FeatureList.tsx | LOW |
| m10 | `bulk` API в features.ts не соответствует backend endpoint | api/features.ts | MEDIUM |

---

## 6. Spec Compliance (plan/specs/web-ui.md)

| Requirement | Status | Notes |
|-------------|--------|-------|
| React 19 + TypeScript | ✅ | React 19.0.0, TypeScript 5.6.0 |
| Vite build | ✅ | vite.config.ts configured |
| Tailwind CSS | ✅ | v3.4 with custom palette |
| React Router | ✅ | 7 routes configured |
| Redux Toolkit | ✅ | store with 2 slices |
| Dashboard page | ✅ | KPI cards + TeamLoadChart + ConflictList |
| Gantt chart | ✅ | Custom implementation with drag-and-drop |
| Team load visualization | ✅ | Recharts stacked bar chart |
| Planning controls | ✅ | Run Planning + SSE progress |
| Feature CRUD forms | ⚠️ | Form exists but not wired to API |
| What-if simulation | ⚠️ | Page exists but stub implementation |
| Integration settings | ⚠️ | Forms exist but not wired |
| Drag-and-drop on Gantt | ⚠️ | Implemented but doesn't detect sprints |
| SSE for planning status | ⚠️ | Hook exists but wrong base URL |
| Responsive design | ⚠️ | Sidebar collapses but no mobile testing |
| Tests | ❌ | 0 test files |
| ESLint + Prettier | ❌ | No config files |

**Compliance: ~65/100**

---

## 7. Tech Stack Assessment

| Technology | Version | Assessment |
|------------|---------|------------|
| React | 19.0.0 | ✅ Latest |
| TypeScript | 5.6.0 | ✅ Modern |
| Vite | 6.0.0 | ✅ Latest |
| Redux Toolkit | 2.0.0 | ✅ Good choice |
| React Router | 7.0.0 | ✅ Latest |
| Tailwind CSS | 3.4.0 | ✅ Good |
| Axios | 1.7.0 | ✅ Standard |
| Recharts | 2.12.0 | ✅ Good for charts |
| date-fns | 4.0.0 | ✅ Lightweight |
| frappe-gantt | 0.6.0 | ⚠️ Unused dependency |

---

## 8. Recommendations

### P0 (Must fix before release)
1. **Wire forms to API** — FeaturesPage, SettingsPage, SimulationPage
2. **Fix Planning API baseURL** — separate axios instance for port 8081
3. **Fix Gantt drag-and-drop** — implement sprint hit-testing
4. **Fix SSE hook baseURL** — use VITE_PLANNING_ENGINE_URL

### P1 (Should fix)
5. **Add ESLint config** — eslint.config.js with TypeScript + React rules
6. **Add unit tests** — start with common components + utils
7. **Remove frappe-gantt** — or use it instead of custom Gantt
8. **Wire PortfolioPage timeline** — connect to dashboardApi.getTimeline()
9. **Add web-ui to docker-compose.yml** — nginx service for production
10. **Fix bulk API** — match backend endpoint signature

### P2 (Nice to have)
11. **Add Prettier config** — .prettierrc
12. **Add error boundary** — catch render errors
13. **Implement dark theme** — wire theme toggle to UI
14. **Add pagination controls** — FeatureList pagination
15. **Add favicon** — replace /vite.svg reference

---

## 9. File Count Summary

| Category | Files | Lines (est.) |
|----------|-------|-------------|
| Config | 7 | ~150 |
| API | 7 | ~270 |
| Types | 6 | ~295 |
| Store | 3 | ~178 |
| Hooks | 4 | ~241 |
| Common components | 6 | ~300 |
| Gantt components | 5 | ~494 |
| Planning components | 5 | ~403 |
| Feature components | 4 | ~529 |
| Layout | 1 | ~97 |
| Pages | 7 | ~811 |
| Utils | 3 | ~132 |
| Documentation | 2 | ~564 |
| **TOTAL** | **58** | **~4,464** |

---

## 10. Verdict

**Status: ⚠️ GOOD FOUNDATION — Ready for Sprint 4 wiring**

Дизайнер создал **качественный frontend-каркас** с правильной архитектурой, хорошей дизайн-системой и полным покрытием страниц из spec. Основные проблемы — **формы не подключены к API** и **нет тестов** — это ожидаемо для designer delivery и должно быть addressed в Sprint 4.

**Следующий шаг:** Backend developer должен wire forms to API, fix baseURL issues, и добавить tests.
