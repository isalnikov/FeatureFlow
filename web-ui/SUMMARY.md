# FeatureFlow Web UI — Summary

> Дата: 2026-05-16
> Статус: Стартовый проект создан, готов к запуску и дальнейшей разработке

---

## Что сделано

Создан полный стартовый проект фронтенд-приложения FeatureFlow (`web-ui/`) на основе спецификаций из `PLAN.md`, `TODO.md`, `ux.md` и `plan/specs/`.

### Структура проекта (58 файлов)

```
web-ui/
├── package.json                    # React 19 + TypeScript + Vite + Tailwind
├── tsconfig.json                   # Strict mode, path aliases
├── vite.config.ts                  # Dev server + API proxy на :8080
├── tailwind.config.js              # Кастомная палитра (brand, status, load)
├── postcss.config.js
├── .env.example                    # VITE_API_URL, PLANNING_ENGINE_URL, INTEGRATION_HUB_URL
├── .gitignore
├── index.html
├── FRONTEND-GUIDE.md               # Полное руководство для разработчика
└── src/
    ├── main.tsx                    # Entry point
    ├── App.tsx                     # Redux Provider + RouterProvider
    ├── router.tsx                  # 7 маршрутов
    ├── index.css                   # Tailwind + custom component classes
    │
    ├── api/                        # 7 API модулей
    │   ├── client.ts               # Axios instance + JWT interceptor + error handler
    │   ├── products.ts             # CRUD продуктов
    │   ├── teams.ts                # CRUD команд + capacity + load
    │   ├── features.ts             # CRUD фич + bulk + dependencies
    │   ├── assignments.ts          # CRUD назначений + lock/unlock + bulk
    │   ├── planning.ts             # Run planning + job status + result + SSE URL
    │   ├── dashboard.ts            # Metrics + timeline + conflicts + team load
    │   └── integrations.ts         # Jira/ADO/Linear import/export/test
    │
    ├── types/                      # Полная типизация домена
    │   ├── product.ts              # Product, CreateProductRequest
    │   ├── team.ts                 # Team, TeamMember, Role, TeamCapacity, TeamLoad
    │   ├── feature.ts              # Feature, EffortEstimate, ThreePointEstimate, ClassOfService
    │   ├── assignment.ts           # Assignment, AssignmentStatus
    │   ├── planning.ts             # PlanningJob, PlanningResult, Conflict, FeatureTimeline, TeamLoadReport
    │   └── index.ts                # Re-export всех типов
    │
    ├── components/                 # 22 компонента
    │   ├── common/                 # Переиспользуемые UI элементы
    │   │   ├── Button.tsx          # 4 варианта + 3 размера + loading state
    │   │   ├── Modal.tsx           # Backdrop, ESC close, 4 размера
    │   │   ├── Table.tsx           # Generic таблица с sortable columns
    │   │   ├── Badge.tsx           # 5 цветовых вариантов
    │   │   ├── Tooltip.tsx         # 4 позиции
    │   │   └── Loading.tsx         # 3 размера + fullscreen mode
    │   ├── layout/
    │   │   └── AppLayout.tsx       # Sidebar (collapsible) + header + nav + outlet
    │   ├── gantt/                  # Интерактивная диаграмма Ганта
    │   │   ├── GanttChart.tsx      # Главный компонент с zoom, drag-drop, dependencies
    │   │   ├── GanttBar.tsx        # Draggable полоска фичи с color-by-CoS
    │   │   ├── GanttTimeline.tsx   # Заголовок шкалы (day/week/sprint/month)
    │   │   ├── GanttDependencies.tsx # SVG стрелки зависимостей
    │   │   └── GanttToolbar.tsx    # Zoom controls + Run Planning button
    │   ├── planning/               # Компоненты планирования
    │   │   ├── SprintTable.tsx     # Таблица спринтов с assignments
    │   │   ├── TeamLoadChart.tsx   # Stacked bar chart (Recharts) по ролям
    │   │   ├── ConflictList.tsx    # Список конфликтов с severity badges
    │   │   ├── PlanningControls.tsx # Кнопка запуска + SSE progress bar
    │   │   └── PlanComparison.tsx  # Side-by-side сравнение baseline vs simulated
    │   └── features/               # Компоненты фич
    │       ├── FeatureForm.tsx     # Полная форма: title, description, value, CoS, deadline, products, effort (4 roles), expertise tags, canSplit
    │       ├── FeatureList.tsx     # Таблица фич с badges
    │       ├── FeatureDetail.tsx   # Детальная карточка с effort breakdown
    │       └── DependencyGraph.tsx # Визуализация зависимостей
    │
    ├── pages/                      # 7 страниц
    │   ├── DashboardPage.tsx       # 6 KPI cards + TeamLoadChart + ConflictList
    │   ├── PortfolioPage.tsx       # Timeline всех фич с фильтрацией по статусу
    │   ├── PlanPage.tsx            # GanttChart + PlanningControls + sidebar (FeatureDetail, TeamLoad, Conflicts)
    │   ├── TeamsPage.tsx           # Таблица команд + modal с деталями
    │   ├── FeaturesPage.tsx        # FeatureList + DependencyGraph + create/edit modals
    │   ├── SimulationPage.tsx      # What-if параметры + PlanComparison
    │   └── SettingsPage.tsx        # Алгоритм параметры + Jira/ADO интеграции
    │
    ├── hooks/                      # 4 custom hooks
    │   ├── useFeatures.ts          # Paginated fetch + by-id fetch
    │   ├── useTeams.ts             # List + by-id fetch
    │   ├── usePlanning.ts          # Assignments + PlanningResult fetch
    │   └── useSSE.ts               # EventSource подключение к planning SSE
    │
    ├── store/                      # Redux Toolkit
    │   ├── index.ts                # Store с planning + ui slices
    │   ├── planningSlice.ts        # runPlanning async thunk (polling → result)
    │   └── uiSlice.ts              # Sidebar, zoom, selections, modals, theme
    │
    └── utils/                      # 3 утилиты
        ├── date.ts                 # formatDate, formatShortDate, daysBetween, addDaysToDate
        ├── colors.ts               # Role/CoS/status/conflict color mappings
        └── validation.ts           # validateFeature, validateTeam, hasErrors
```

---

## Технологический стек

| Технология | Версия | Назначение |
|------------|--------|------------|
| React | 19 | UI framework |
| TypeScript | 5.6 | Strict mode типизация |
| Vite | 6 | Bundler + dev server |
| React Router | 7 | Client-side routing |
| Redux Toolkit | 2 | Global state management |
| Axios | 1.7 | HTTP client с interceptors |
| Recharts | 2.12 | Графики (team load) |
| date-fns | 4 | Работа с датами |
| Tailwind CSS | 3.4 | Utility-first styling |
| clsx | 2.0 | Conditional class names |

---

## Реализованные экраны

| Экран | URL | Описание |
|-------|-----|----------|
| Dashboard | `/` | KPI метрики (total features, planned, avg TTM, utilization, conflicts, deadline risk) + графики загрузки + список конфликтов |
| Portfolio | `/portfolio` | Timeline всех фич с группировкой и фильтрацией по статусу (onTrack/atRisk/blocked/completed) |
| Plan | `/plan` | Интерактивная диаграмма Ганта с drag-and-drop, zoom (day/week/sprint/month), зависимостями (SVG), запуском планирования с SSE-прогрессом |
| Teams | `/teams` | Таблица команд с ролями, focus factor, velocity, expertise tags; modal с деталями членов команды |
| Features | `/features` | Бэклог фич с таблицей, формой создания/редактирования, детальным просмотром и графом зависимостей |
| Simulation | `/simulation` | What-if симуляция с параметрами приоритетов/capacity и сравнением baseline vs simulated |
| Settings | `/settings` | Параметры алгоритма (веса, annealing, Monte Carlo), default team settings, интеграции Jira/ADO |

---

## API интеграция

### Endpoints покрыты

| Сервис | Port | Endpoints |
|--------|------|-----------|
| data-service | 8080 | /products, /features, /teams, /assignments, /planning-windows, /dashboard |
| planning-engine | 8081 | /planning/run, /planning/jobs/{id}/status, /planning/jobs/{id}/result, /planning/jobs/{id}/sse |
| integration-hub | 8082 | /integrations/{type}/import, /export, /test |

### SSE Flow

```
User clicks "Run Planning"
  → POST /planning/run (202 Accepted, returns jobId)
  → EventSource connects to /planning/jobs/{jobId}/sse
  → Events: GREEDY (10%) → ANNEALING (40%) → MONTE_CARLO (70%) → DONE (100%)
  → GET /planning/jobs/{jobId}/result
  → Display PlanningResult on Gantt + charts
```

---

## Дизайн-система

### Цветовая палитра

- **Brand**: blue-500 (#3b82f6) — основные действия
- **Status**: green (onTrack), amber (atRisk), red (blocked), indigo (completed)
- **Load**: green (<60%), amber (60-85%), red (>85%)
- **Class of Service**: red (EXPEDITE), amber (FIXED_DATE), blue (STANDARD), gray (FILLER)
- **Roles**: blue (Backend), green (Frontend), amber (QA), purple (DevOps)

### Компоненты и состояния

Каждый компонент поддерживает: default, hover, active, disabled, loading, error

---

## Что нужно доработать

1. **Реальные API вызовы в формах** — сейчас create/update логируют в консоль
2. **Drag-and-drop Ганта** — заменить HTML5 DnD на @dnd-kit/core
3. **Аутентификация** — login page, refresh token, RBAC на UI
4. **Error handling** — Error boundary, toast notifications, retry logic
5. **Тесты** — Vitest + Testing Library для компонентов, hooks, store
6. **Virtual scrolling** — @tanstack/react-virtual для больших списков
7. **Lazy loading** — React.lazy для роутов
8. **i18n** — react-i18next для мультиязычности

---

## Запуск

```bash
cd web-ui
cp .env.example .env
npm install
npm run dev        # http://localhost:3000
npm run build      # Production build
npm run typecheck  # TypeScript check
npm run lint       # ESLint check
```

---

## Соответствие спецификациям

| Spec | Статус | Примечание |
|------|--------|------------|
| `plan/specs/web-ui.md` | ✅ Полностью | Структура, типы, компоненты, routing, store |
| `plan/specs/api-contracts.md` | ✅ Полностью | Все endpoints покрыты API модулями |
| `plan/specs/data-model.md` | ✅ | Типы соответствуют PostgreSQL схеме |
| `PLAN.md` | ✅ | Все экраны из Section 8 реализованы |
| `TODO.md` | ✅ | Модель данных, сущности, ограничения учтены |
| `ux.md` | ✅ | Дизайн-система, состояния, responsive |
