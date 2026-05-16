# FeatureFlow — Ретроспектива проекта

> **Дата:** 2026-05-15
> **Автор:** Technical Design Authority
> **Основание:** PLAN.md, SPRINT0_REVIEW.md, SPRINT1_REVIEW.md, текущее состояние репозитория

---

## 1. Executive Summary

Проект FeatureFlow находится на **~40% реализации** от полного плана (8 спринтов).
Ядро системы (domain-core) реализовано отлично. Планировщик (planning-engine) имеет работающие алгоритмы но требует доработки. CRUD-слой (data-service) функционален но не протестирован. Интеграции и фронтенд не начаты.

**Общий compliance score: ~45/100** (было 28/100 на момент SPRINT1_REVIEW, после фиксации алгоритмов поднялся).

---

## 2. Что сделано хорошо ✅

### 2.1 Domain Core — ОТЛИЧНО (95/100)

| Метрика | Значение |
|---------|----------|
| Файлы | 34 main / 29 test |
| Coverage | 86.5% JaCoCo |
| Тесты | ~124 test cases |

- Все доменные сущности реализованы: Product, Team, TeamMember, FeatureRequest, Sprint, Assignment, PlanningWindow
- Value objects: Role, Capacity, EffortEstimate, ThreePointEstimate, DateRange, ClassOfService, AnnealingParameters, MonteCarloParameters
- Бизнес-правила: DependencyValidator, OwnershipValidator, CapacityValidator, ParallelismValidator
- Sealed class hierarchy для DomainException
- Java 25 patterns: records, pattern matching, switch expressions — используются корректно
- Тесты покрывают happy paths, edge cases, validation failures

### 2.2 Planning Engine — ХОРОШО (60/100, было 55%)

| Метрика | Значение |
|---------|----------|
| Файлы | 14 main / 11 test |
| Алгоритмы | 3 реализованы |

- **GreedyPlanner** — полная реализация (437 строк): topological sort, capacity allocation, team assignment, dependency resolution
- **SimulatedAnnealing** — корректная реализация с temperature cooling, acceptance probability formula
- **MonteCarloSimulator** — triangular distribution sampling, P50/P90 percentiles
- **PlanningOrchestrator** — полный pipeline: Greedy → SA → Monte Carlo
- **PlanningJobService** — async job tracking с SSE streaming
- **PlanningController** — 6 эндпоинтов: `/run`, `/execute`, `/jobs/{id}/status`, `/jobs/{id}/result`, `/jobs/{id}/sse`, `/validate`
- 33 unit/integration теста проходят успешно

### 2.3 Data Service — НЕПЛОХО (70/100)

| Метрика | Значение |
|---------|----------|
| Файлы | 48 main / 0 test |
| Контроллеры | 7 |
| Сервисы | 5 |
| Репозитории | 6 |
| DTO | 17 |

- Все 5 сервисов с `@Transactional` boundaries
- 6 из 7 контроллеров используют service layer + DTO (SprintController — исключение)
- EntityMapper ручной но корректный (170 строк)
- Config: CORS, OpenAPI, GlobalExceptionHandler
- Pagination в FeatureController
- N+1 query fixed через `findDistinctActiveFeatureIds()`

### 2.4 Инфраструктура — ХОРОШО

- Docker Compose: PostgreSQL 16, Redis 7, 3 Java сервиса
- CI/CD pipeline: test → build → docker validate
- Maven multi-module: 4 модуля с общим parent POM
- Java 25, Spring Boot 3.5.0 — версии актуальны

### 2.5 Документация — ОТЛИЧНО

- PLAN.md: C4 diagrams, архитектура, roadmap, risk analysis
- 7 спецификаций в `plan/specs/`
- 2 детальных code review отчёта (SPRINT0: 776 строк, SPRINT1: 435 строк)
- README.md с описанием проекта

---

## 3. Отклонения от плана ⚠️

### 3.1 Sprint-план vs реальность

| Спринт | План | Факт | Статус |
|--------|------|------|--------|
| **Sprint 0** | Модель данных, API, заглушки, CI/CD | ✅ Выполнено (с регрессиями) | DONE |
| **Sprint 1** | CRUD: продукты, команды, фичи, календари. Базовый UI | CRUD ✅, UI ❌ | PARTIAL |
| **Sprint 2** | Жадный алгоритм, Ганта | Алгоритм ✅, Ганта ❌ | PARTIAL |
| **Sprint 3** | Имитация отжига, сравнение планов | Алгоритм ✅, сравнение ❌ | PARTIAL |
| **Sprint 4** | Монте-Карло, вероятностные прогнозы | Алгоритм ✅, интеграция ❌ | PARTIAL |
| **Sprint 5** | Jira интеграция, классы обслуживания | ❌ Не начато | NOT STARTED |
| **Sprint 6** | What-if симуляции, разрешения, отчёты | ❌ Не начато | NOT STARTED |
| **Sprint 7** | Тесты, оптимизация, документация | ❌ Не начато | NOT STARTED |

### 3.2 Критические отклонения

| # | Отклонение | Влияние | Причина |
|---|-----------|---------|---------|
| 1 | **Web UI полностью отсутствует** | 0 файлов, React app не создан | Фронтенд не был передан агенту |
| 2 | **Integration Hub — все stubs** | 3 коннектора возвращают пустые данные | Deferred to Sprint 3, не реализован |
| 3 | **data-service: 0 тестов** | 48 файлов без единого теста | Не было включено в scope агентов |
| 4 | **CI тестирует только domain-core** | data-service, planning-engine, integration-hub не проверяются | CI pipeline неполный |
| 5 | **Security permitAll** | Нет JWT, RBAC, авторизации | Deferred to Sprint 4 |
| 6 | **SprintController bypasses service layer** | Архитектурная регрессия | Ошибка при реализации |
| 7 | **DTOs — mutable POJOs вместо records** | 16/17 DTO нарушают Java 25 best practice | Не было требованием для агентов |

### 3.3 Алгоритмические отклонения

| Компонент | План | Факт | Проблема |
|-----------|------|------|----------|
| GreedyPlanner.computeTeamLoadReports | Агрегация по team/sprint/role | ✅ Исправлено | Было stub (Map.of()) |
| GreedyPlanner.computeCost | WeightedCostFunction (w1, w2, w3) | ✅ Исправлено | Игнорировал веса |
| MonteCarloSimulator perturbDate | Perturbation относительно baseDate | ⚠️ Частично | Формула `baseDays * ratio` математически некорректна для epoch days |
| PlanningController /run | Async job submission | ⚠️ Stub | Возвращает fake jobId, не вызывает orchestrator |
| SimulatedAnnealing Random seed | Configurable seed | ⚠️ Hardcoded(42) | Acceptable для тестов, не для prod |
| Mutator | 3 mutation types без дублирования | ⚠️ Duplicate methods | Public/private overloads с одинаковой логикой |
| FeatureSorter secondary sort | O(1) via pre-computed index | ⚠️ O(n²) | `indexOf()` в comparator |

---

## 4. Что нужно улучшить 🔧

### 4.1 Критические (P0)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 1 | **Написать тесты для data-service** — 5 сервисов, 7 контроллеров. Минимум: @DataJpaTest для репозиториев, @WebMvcTest для контроллеров, unit tests для сервисов | data-service | 8-12 часов |
| 2 | **Исправить SprintController** — создать SprintService, SprintDto, убрать прямой доступ к SprintRepository | data-service | 30 мин |
| 3 | **Исправить CI pipeline** — `mvn test` без `-pl domain-core` чтобы тестировать все модули | .github/ci.yml | 15 мин |
| 4 | **Исправить MonteCarloSimulator perturbDate** — формула `baseDays * ratio` некорректна. Нужно: `baseDate.plusDays((long)((sampled - expected) * scaleFactor))` | planning-engine | 30 мин |
| 5 | **Реализовать PlanningController /run** — подключить jobService.submitJob() с реальной конверсией PlanningRunRequest → PlanningRequest | planning-engine | 2 часа |

### 4.2 Важные (P1)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 6 | **Конвертировать DTOs в records** — 16 mutable POJOs → immutable Java 25 records. TeamMemberDto уже record | data-service | 2-3 часа |
| 7 | **Добавить недостающие эндпоинты** — PUT /teams/{id}/members, GET /dashboard/timeline | data-service | 1 час |
| 8 | **Убрать дублирование в Mutator** — удалить public overloads, оставить методы с Random | planning-engine | 15 мин |
| 9 | **Исправить FeatureSorter O(n²)** — pre-compute index map | planning-engine | 10 мин |
| 10 | **Сделать Random seed configurable** — inject через @Value или constructor | planning-engine | 15 мин |
| 11 | **Добавить @JsonIgnore fallback** — на bidirectional relationships в entity | data-service | 30 мин |
| 12 | **Fix GlobalExceptionHandler** — добавить title/instance в ProblemDetail (RFC 7807) | data-service | 10 мин |

### 4.3 Средние (P2)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 13 | **Реализовать Integration Hub коннекторы** — Jira REST API, ADO API, Linear GraphQL. Начать с одного (Jira) | integration-hub | 16-24 часа |
| 14 | **Усилить assertions в тестах** — SimulatedAnnealingTest: проверить improvement; MonteCarloSimulatorTest: проверить distribution shape | planning-engine | 1 час |
| 15 | **Добавить integration tests** — PlanningPipelineIntegrationTest сейчас минимальный | planning-engine | 2 часа |
| 16 | **Security: JWT + RBAC** — Spring Security, 4 роли: ADMIN, DELIVERY_MANAGER, PRODUCT_OWNER, TEAM_LEAD | data-service | 4-6 часов |

### 4.4 Долгосрочные (P3)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 17 | **Создать Web UI** — React 19 + TypeScript + Vite. Gantt chart, dashboards, forms, drag-and-drop | web-ui | 40-60 часов |
| 18 | **What-if симуляции** — branching plans, comparison с baseline | planning-engine | 8-12 часов |
| 19 | **Performance optimization** — планирование 500 фич / 50 команд < 30 сек | planning-engine | TBD |
| 20 | **Docker images build & push** — CI docker job только validates config, не build/push | .github/ci.yml | 1 час |

---

## 5. Метрики проекта

### 5.1 Код

| Метрика | Значение |
|---------|----------|
| Всего main файлов | 117 |
| Всего test файлов | 40 |
| Соотношение test/main | 0.34 |
| Строки кода (оценка) | ~8,000+ |
| Modules | 4 (domain-core, data-service, planning-engine, integration-hub) |

### 5.2 Тестовое покрытие

| Модуль | Test Files | Coverage | Статус |
|--------|-----------|----------|--------|
| domain-core | 29 | 86.5% | ✅ Excellent |
| planning-engine | 11 | ~60% (оценка) | ⚠️ Good |
| data-service | 0 | 0% | ❌ Critical |
| integration-hub | 0 | 0% | ❌ Critical |
| **Итого** | **40** | **~35%** (оценка) | ⚠️ Needs work |

### 5.3 Spec Compliance

| Область | Score | Notes |
|---------|-------|-------|
| Domain Core structure | 95% | Почти полностью по спеке |
| Domain Core tests | 86.5% | Excellent |
| Data Service structure | 75% | SprintController regression, missing endpoints |
| Data Service API coverage | 70% | 2 endpoint missing |
| Planning Engine algorithms | 60% | Algorithms real, /run stub, MonteCarlo bug |
| Planning Engine tests | 55% | Weak assertions в SA/MonteCarlo tests |
| Integration Hub | 20% | Skeleton only, all stubs |
| Web UI | 0% | Не создан |
| Database schema | 95% | V7 migration present |
| Security | 10% | permitAll |
| CI/CD | 40% | Only tests domain-core |
| **OVERALL** | **~45%** | |

---

## 6. Рекомендации для следующего спринта

### Приоритетный порядок:

1. **Тесты data-service** — самый большой риск. 48 файлов production кода без единого теста.
2. **Исправить CI** — тривиальная фиксация, но блокирует confidence в качестве.
3. **SprintController refactor** — архитектурная регрессия, ломает pattern.
4. **MonteCarloSimulator fix** — математический баг делает вероятности некорректными.
5. **DTO → records** — технический долг, влияет на maintainability.
6. **PlanningController /run** — endpoint не работает, блокирует async flow.
7. **Integration Hub (Jira connector)** — первый реальный коннектор, открывает Sprint 5.

### Что НЕ делать сейчас:
- Web UI — требует отдельного агента с frontend expertise
- Security — deferred by design, не блокирует core functionality
- What-if симуляции — зависит от стабильности planning pipeline

---

## 7. Lessons Learned

### Что сработало хорошо:
- **Domain-first подход** — domain-core как foundation оказался правильным решением
- **Code review процесс** — SPRINT0_REVIEW.md и SPRINT1_REVIEW.md выявили критические проблемы до того как они стали дороже
- **PLAN.md + specs** — детальная документация позволила разным агентам работать консистентно
- **Java 25 patterns** — records, sealed classes, pattern matching в domain-core — excellent fit

### Что пошло не так:
- **Регрессии при fixes** — SprintController bypass и удаление planning-engine tests при Sprint 1/2 показывают отсутствие regression testing
- **Stub creep** — integration-hub остался stubs через несколько спринтов. Stub-код имеет тенденцию "жить" дольше planned
- **CI gap** — тестирование только одного модуля создало false sense of security
- **No frontend agent** — web-ui полностью пропущен, нужно было явно назначить ответственного

### Что изменить в процессе:
1. **CI должен тестировать ВСЕ модули** — без исключений
2. **Каждый PR должен включать тесты** — правило без исключений
3. **Stub code должен иметь expiry** — если stub не заменён за 1 спринт, это blocker
4. **Regression tests перед merge** — automated check что существующие тесты не сломаны
5. **Frontend требует отдельного трека** — не может быть "остаточным" task для backend-агента

---

## 8. Состояние на момент ретроспективы

```
FeatureFlow v0.1.0-SNAPSHOT
├── domain-core        ████████████████████████████████████████ 95%  ✅
├── data-service       ████████████████████████████░░░░░░░░░░░░ 70%  ⚠️
├── planning-engine    ████████████████████████░░░░░░░░░░░░░░░░ 60%  ⚠️
├── integration-hub    ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 20%  ❌
├── web-ui             ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  0%  ❌
├── tests              ██████████████░░░░░░░░░░░░░░░░░░░░░░░░░░ 35%  ⚠️
├── CI/CD              ████████████████░░░░░░░░░░░░░░░░░░░░░░░░ 40%  ⚠️
├── security           ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 10%  ❌
└── documentation      ████████████████████████████████████████ 95%  ✅
```

**Overall: ~45/100**
