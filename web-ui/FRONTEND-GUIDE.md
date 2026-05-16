# FeatureFlow — Frontend Developer Guide

> Полное руководство по фронтенд-части платформы FeatureFlow

---

## 1. Быстрый старт

```bash
cd web-ui
cp .env.example .env
npm install
npm run dev
```

Приложение запустится на `http://localhost:3000`. API проксируется на `http://localhost:8080`.

---

## 2. Архитектура проекта

```
web-ui/
├── src/
│   ├── api/                    # API клиенты (axios)
│   │   ├── client.ts           # Базовый axios instance с interceptors
│   │   ├── products.ts         # CRUD продуктов
│   │   ├── teams.ts            # CRUD команд + capacity/load
│   │   ├── features.ts         # CRUD фич + dependencies
│   │   ├── assignments.ts      # CRUD назначений + lock/unlock
│   │   ├── planning.ts         # Запуск планирования, SSE
│   │   ├── dashboard.ts        # Метрики, timeline, conflicts
│   │   └── integrations.ts     # Jira/ADO/Linear интеграции
│   │
│   ├── types/                  # TypeScript интерфейсы
│   │   ├── product.ts          # Product, CreateProductRequest
│   │   ├── team.ts             # Team, TeamMember, Role, TeamCapacity
│   │   ├── feature.ts          # Feature, EffortEstimate, ClassOfService
│   │   ├── assignment.ts       # Assignment, AssignmentStatus
│   │   ├── planning.ts         # PlanningResult, Conflict, FeatureTimeline
│   │   └── index.ts            # Re-export всех типов
│   │
│   ├── components/
│   │   ├── common/             # Переиспользуемые UI компоненты
│   │   │   ├── Button.tsx      # Кнопка (primary/secondary/danger/ghost)
│   │   │   ├── Modal.tsx       # Модальное окно с backdrop
│   │   │   ├── Table.tsx       # Generic таблица с columns
│   │   │   ├── Badge.tsx       # Статусный бейдж
│   │   │   ├── Tooltip.tsx     # Тултип при hover
│   │   │   └── Loading.tsx     # Спиннер загрузки
│   │   │
│   │   ├── layout/
│   │   │   └── AppLayout.tsx   # Sidebar + header + main outlet
│   │   │
│   │   ├── gantt/              # Диаграмма Ганта
│   │   │   ├── GanttChart.tsx  # Главный компонент Ганта
│   │   │   ├── GanttBar.tsx    # Полоска фичи (draggable)
│   │   │   ├── GanttTimeline.tsx # Заголовок временной шкалы
│   │   │   ├── GanttDependencies.tsx # SVG стрелки зависимостей
│   │   │   └── GanttToolbar.tsx # Zoom controls + Run Planning
│   │   │
│   │   ├── planning/           # Компоненты планирования
│   │   │   ├── SprintTable.tsx     # Таблица спринтов
│   │   │   ├── TeamLoadChart.tsx   # Stacked bar chart загрузки
│   │   │   ├── ConflictList.tsx    # Список конфликтов с severity
│   │   │   ├── PlanningControls.tsx # Кнопка запуска + SSE прогресс
│   │   │   └── PlanComparison.tsx  # Сравнение baseline vs simulated
│   │   │
│   │   └── features/           # Компоненты фич
│   │       ├── FeatureForm.tsx     # Форма создания/редактирования
│   │       ├── FeatureList.tsx     # Таблица фич
│   │       ├── FeatureDetail.tsx   # Детальная карточка фичи
│   │       └── DependencyGraph.tsx # Визуализация зависимостей
│   │
│   ├── pages/                  # Страницы приложения
│   │   ├── DashboardPage.tsx   # KPI метрики + графики
│   │   ├── PortfolioPage.tsx   # Timeline всех фич
│   │   ├── PlanPage.tsx        # Гант + планирование
│   │   ├── TeamsPage.tsx       # Управление командами
│   │   ├── FeaturesPage.tsx    # Бэклог фич
│   │   ├── SimulationPage.tsx  # What-if симуляции
│   │   └── SettingsPage.tsx    # Параметры алгоритма + интеграции
│   │
│   ├── hooks/                  # Custom React hooks
│   │   ├── useFeatures.ts      # fetch features (paginated + by id)
│   │   ├── useTeams.ts         # fetch teams (list + by id)
│   │   ├── usePlanning.ts      # fetch assignments + planning result
│   │   └── useSSE.ts           # Server-Sent Events подключение
│   │
│   ├── store/                  # Redux Toolkit
│   │   ├── index.ts            # Store configuration
│   │   ├── planningSlice.ts    # Planning state + async thunk
│   │   └── uiSlice.ts          # UI state (sidebar, modals, zoom)
│   │
│   ├── utils/                  # Утилиты
│   │   ├── date.ts             # Форматирование дат
│   │   ├── colors.ts           # Цветовые маппинги
│   │   └── validation.ts       # Валидация форм
│   │
│   ├── router.tsx              # React Router v7 конфигурация
│   ├── App.tsx                 # Root component
│   ├── main.tsx                # Entry point
│   └── index.css               # Tailwind + custom styles
```

---

## 3. Стек технологий

| Технология | Версия | Назначение |
|------------|--------|------------|
| React | 19 | UI framework |
| TypeScript | 5.6 | Типизация |
| Vite | 6 | Bundler + dev server |
| React Router | 7 | Routing |
| Redux Toolkit | 2 | State management |
| Axios | 1.7 | HTTP client |
| Recharts | 2.12 | Графики (team load) |
| date-fns | 4 | Работа с датами |
| Tailwind CSS | 3.4 | Styling |
| clsx | 2.0 | Conditional classNames |

---

## 4. API Endpoints

### Data Service (port 8080)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/v1/products` | Список продуктов |
| POST | `/api/v1/products` | Создать продукт |
| GET | `/api/v1/features` | Список фич (paginated) |
| POST | `/api/v1/features` | Создать фичу |
| PUT | `/api/v1/features/{id}` | Обновить фичу |
| GET | `/api/v1/teams` | Список команд |
| POST | `/api/v1/teams` | Создать команду |
| GET | `/api/v1/assignments` | Список назначений |
| PUT | `/api/v1/assignments/{id}/lock` | Заблокировать назначение |
| GET | `/api/v1/dashboard/portfolio` | Метрики портфеля |

### Planning Engine (port 8081)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/v1/planning/run` | Запустить планирование (202) |
| GET | `/api/v1/planning/jobs/{id}/status` | Статус задачи |
| GET | `/api/v1/planning/jobs/{id}/result` | Результат планирования |
| GET | `/api/v1/planning/jobs/{id}/sse` | SSE stream прогресса |

---

## 5. State Management

### Redux Store

```typescript
// RootState
interface RootState {
  planning: {
    currentJob: PlanningJob | null;    // Текущая задача планирования
    currentResult: PlanningResult | null; // Результат
    isPlanning: boolean;               // Флаг выполнения
    error: string | null;
  };
  ui: {
    sidebarOpen: boolean;
    sidebarSection: 'features' | 'teams' | 'conflicts' | 'load';
    ganttZoom: 'day' | 'week' | 'sprint' | 'month';
    selectedFeatureId: string | null;
    selectedTeamId: string | null;
    showCreateFeatureModal: boolean;
    // ...
  };
}
```

### Ключевые actions

```typescript
// Запуск планирования (async thunk)
dispatch(runPlanning(request));

// UI actions
dispatch(setSelectedFeature(featureId));
dispatch(setGanttZoom('week'));
dispatch(toggleSidebar());
dispatch(setShowCreateFeatureModal(true));
```

---

## 6. Ключевые пользовательские сценарии

### 6.1 Создание фичи и планирование

1. Перейти на `/features`
2. Нажать "+ Add Feature"
3. Заполнить форму (title, description, business value, effort estimates, products)
4. Сохранить
5. Перейти на `/plan`
6. Нажать "Run Planning"
7. Наблюдать прогресс через SSE (GREEDY → ANNEALING → MONTE_CARLO → DONE)
8. Просмотреть результат на диаграмме Ганта

### 6.2 What-if симуляция

1. Перейти на `/simulation`
2. Изменить приоритеты фич или capacity команд
3. Нажать "Run Simulation"
4. Сравнить baseline и simulated планы в PlanComparison

### 6.3 Ручная корректировка плана

1. На `/plan` перетащить фичу на другой спринт (drag-and-drop)
2. Кликнуть на фичу для просмотра деталей
3. Заблокировать назначение (lock) чтобы алгоритм его не трогал

---

## 7. Дизайн-система

### Цвета

```
Brand:     blue-500 (#3b82f6) — основные действия
Success:   green-500 (#10b981) — on track, completed
Warning:   amber-500 (#f59e0b) — at risk, deadline warning
Error:     red-500 (#ef4444)   — blocked, conflicts
Info:      blue-500 (#3b82f6)  — planned, filler
```

### Class of Service Colors

| Class | Color | Usage |
|-------|-------|-------|
| EXPEDITE | Red (#ef4444) | Критические задачи |
| FIXED_DATE | Amber (#f59e0b) | Фиксированный дедлайн |
| STANDARD | Blue (#3b82f6) | Обычные задачи |
| FILLER | Gray (#9ca3af) | Заполнители, техдолг |

### Role Colors (для графиков загрузки)

| Role | Color |
|------|-------|
| BACKEND | Blue (#3b82f6) |
| FRONTEND | Green (#10b981) |
| QA | Amber (#f59e0b) |
| DEVOPS | Purple (#8b5cf6) |

### Компоненты

| Компонент | Props | States |
|-----------|-------|--------|
| Button | variant, size, loading, disabled | default, hover, active, disabled, loading |
| Modal | isOpen, onClose, title, size | open, closed |
| Badge | variant, size | default, success, warning, error, info |
| Table | columns, data, onRowClick | empty, loading, populated |
| Tooltip | content, position, children | visible, hidden |

---

## 8. Что нужно доработать

### 8.1 Подключить реальные данные

Сейчас формы создания/обновления фич и команд логируют данные в консоль. Нужно:

```typescript
// В FeatureForm onSubmit:
import { featuresApi } from '../../api/features';

const handleCreate = async (data: CreateFeatureRequest) => {
  await featuresApi.create(data);
  refetch();
  setShowCreateModal(false);
};
```

### 8.2 Drag-and-drop Ганта

Текущая реализация использует HTML5 drag-and-drop API. Для production рекомендуется:

- **@dnd-kit/core** — современная библиотека для DnD в React
- Или интегрировать **frappe-gantt** как fallback

### 8.3 Аутентификация

В `api/client.ts` уже есть interceptor для JWT токена. Нужно добавить:

- Страницу логина
- Refresh token logic
- RBAC на уровне UI (скрыть кнопки для ролей без прав)

### 8.4 Обработка ошибок

Добавить:
- Error boundary для всего приложения
- Toast-уведомления для ошибок API
- Retry logic для failed requests

### 8.5 Тесты

```bash
# Рекомендуемые тесты:
npm install -D vitest @testing-library/react @testing-library/jest-dom

# Тестировать:
# - Компоненты форм (валидация)
# - API client (mock axios)
# - Redux slices (reducers, thunks)
# - Хуки (useSSE, useFeatures)
```

---

## 9. Производительность

- **Virtual scrolling** для списков > 100 элементов (рекомендуется `@tanstack/react-virtual`)
- **React.memo** для тяжёлых компонентов (GanttBar, TeamLoadChart)
- **useMemo** для вычисляемых данных (featureAssignments, timelines)
- **Debounced search** для фильтрации фич
- **Lazy loading** страниц через `React.lazy()`

---

## 10. Responsive Design

Основные пользователи работают на десктопах (1920×1080+). Sidebar скрывается на мобильных через `lg:translate-x-0`. Для планшетов во время стендапов — минимальная адаптация таблиц.

---

## 11. Команды для разработки

```bash
npm run dev          # Dev server с hot reload
npm run build        # Production build
npm run preview      # Preview production build
npm run lint         # ESLint check
npm run typecheck    # TypeScript type check
```

---

## 12. Связь с бэкендом

| Frontend | Backend Service | Port |
|----------|----------------|------|
| `api/client.ts` | data-service | 8080 |
| `api/planning.ts` | planning-engine | 8081 |
| `api/integrations.ts` | integration-hub | 8082 |

Vite dev server проксирует `/api` на `localhost:8080`. Для planning engine и integration hub нужно использовать полные URL из `.env`.
