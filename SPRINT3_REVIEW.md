# FeatureFlow — Ретроспектива Sprint 0–3 + Review Sprint 3

> **Дата:** 2026-05-16
> **Автор:** Technical Design Authority
> **Основание:** PLAN.md, SPRINT0_REVIEW.md, SPRINT1_REVIEW.md, SPRINT3_SUMMARY.md, RETROSPECTIVE.md, текущее состояние репозитория

---

## 1. Executive Summary

Проект FeatureFlow прошёл 3 спринта из 8 запланированных.
**Общий compliance score: ~75/100** (было 42% после Sprint 0, 28% после Sprint 1/2 регрессий, 72% после Sprint 2 fixes, теперь 75% после Sprint 3).

**Ключевые достижения Sprint 3:**
- CI pipeline теперь тестирует все 3 модуля (domain-core, planning-engine, data-service)
- 257 тестов проходят, 0 failures
- PlanningController /run реально вызывает orchestrator
- 64 новых unit теста для data-service сервисов
- Code quality: FeatureSorter O(n), Mutator deduplication, GlobalExceptionHandler RFC 7807

**Ключевая проблема Sprint 3:**
- **MonteCarloSimulator date bug НЕ исправлен** — commit `3c8946e` заявляет fix, но формула `referenceDays + durationFromRef * ratio` математически некорректна. Perturbation масштабирует расстояние от 1 января прошлого года вместо того чтобы perturboвать duration работы.

---

## 2. Ретроспектива по спринтам

### Sprint 0 — Foundation

| Метрика | Значение |
|---------|----------|
| **Цель** | Модель данных, API контракты, заглушки, CI/CD |
| **Факт** | Domain-core реализован, CI только domain-core |
| **Score** | 42/100 |

**Что сделано:**
- ✅ 34 domain entity/valueobject/rules/planning файла
- ✅ 29 test файлов, 124 тестов, 86.5% coverage
- ✅ Sealed classes, records, pattern matching — Java 25 patterns
- ✅ Maven multi-module structure
- ✅ Docker Compose (PostgreSQL, Redis)

**Что НЕ сделано:**
- ❌ CI тестирует только domain-core
- ❌ Security permitAll
- ❌ Planning engine — заглушки
- ❌ Data-service — заглушки
- ❌ Integration hub — заглушки
- ❌ Web UI — отсутствует

**Issues из SPRINT0_REVIEW.md:** 18 issues (6 CRITICAL, 12 MAJOR)

---

### Sprint 1/2 — CRUD + Algorithms (с регрессиями)

| Метрика | Значение |
|---------|----------|
| **Цель** | CRUD: продукты, команды, фичи. Жадный алгоритм, имитация отжига, Монте-Карло |
| **Факт** | CRUD работает, алгоритмы реализованы но с багами |
| **Score** | 28/100 (после регрессий) → 72/100 (после Sprint 2 fixes) |

**Что сделано:**
- ✅ 7 controllers, 6 services, 6 repositories в data-service
- ✅ 17 DTOs + EntityMapper
- ✅ GreedyPlanner, SimulatedAnnealing, MonteCarloSimulator — реальные реализации
- ✅ PlanningOrchestrator pipeline: Greedy → SA → Monte Carlo
- ✅ 7 JPA entities с Flyway миграциями
- ✅ SecurityConfig с JWT + RBAC (но не enforced)

**Катастрофические регрессии (обнаружены в SPRINT1_REVIEW.md):**
- ❌ Все алгоритмы заменены на stubs (возвращают `List.of()`)
- ❌ PlanningOrchestrator превращён в 20-строчный passthrough
- ❌ Все planning-engine тесты удалены
- ❌ Integration hub полностью stub
- ❌ SprintController bypasses service layer

**Sprint 2 fixes:**
- ✅ Алгоритмы восстановлены
- ✅ computeTeamLoadReports реализован (был stub)
- ✅ computeCost использует weights (был без весов)
- ✅ MonteCarloSimulator — попытка fix date bug (НЕ успешная)
- ✅ PlanningController endpoints добавлены

**Issues из SPRINT1_REVIEW.md:** 14 новых issues (N1–N13)

---

### Sprint 3 — Testing + CI + Quality

| Метрика | Значение |
|---------|----------|
| **Цель** | CI fix, PlanningController wiring, data-service tests, code quality |
| **Факт** | 257 тестов, CI все модули, PlanningController wired |
| **Score** | 75/100 |

**Что сделано (verified):**

| # | Claim | Verified | Status |
|---|-------|----------|--------|
| 1 | CI tests all 3 modules | `-pl domain-core,planning-engine,data-service` | ✅ PASS |
| 2 | PlanningController /run wired | `jobService.submitJob(request)` | ✅ PASS |
| 3 | 64 new data-service tests | 5 service tests (78 total data-service tests) | ✅ PASS |
| 4 | 257 tests pass | BUILD SUCCESS, 0 failures | ✅ PASS |
| 5 | Mutator deduplication | Convenience wrappers delegate to private | ✅ PASS |
| 6 | FeatureSorter O(n) | Pre-computed index map | ✅ PASS |
| 7 | GlobalExceptionHandler RFC 7807 | title/instance on all handlers | ✅ PASS |
| 8 | @JsonIgnore bidirectional | On TeamEntity.members + .products | ✅ PASS |
| 9 | SprintController.list() all sprints | `listAll()` when no param | ✅ PASS |
| 10 | DTOs → records deferred | 18/19 classes, only TeamMemberDto record | ✅ PASS |
| 11 | MonteCarlo date bug fixed | **BUG STILL PRESENT** | ❌ **FAIL** |

**Commits Sprint 3:**
| Commit | Description | Files | Lines |
|--------|-------------|-------|-------|
| `2958cdf` | CI + PlanningController wiring | 3 | +294/-18 |
| `d26db7c` | data-service unit tests (5 services) | 5 | +1096 |
| `163424a` | Code quality improvements | 6 | +68/-21 |
| `9a3a81d` | Sprint 3 review/docs | — | — |

**Deferred items:**
- ⏳ DTOs → Java 25 records (17 файлов, большой scope)
- ⏳ Configurable Random seed (требует изменения constructors + тестов)

---

## 3. Отклонения от плана (PLAN.md)

### 3.1 Sprint-план vs реальность

| Спринт | План | Факт | Статус |
|--------|------|------|--------|
| **Sprint 0** | Модель данных, API, заглушки, CI/CD | ✅ Выполнено | DONE |
| **Sprint 1** | CRUD: продукты, команды, фичи, календари. Базовый UI | CRUD ✅, UI ❌ | PARTIAL |
| **Sprint 2** | Жадный алгоритм, Ганта | Алгоритм ✅, Ганта ❌ | PARTIAL |
| **Sprint 3** | Имитация отжига, сравнение планов | Алгоритм ✅, сравнение ❌ | PARTIAL |
| **Sprint 4** | Монте-Карло, вероятностные прогнозы | Алгоритм ✅, integration ❌, bug ❌ | PARTIAL |
| **Sprint 5** | Jira интеграция, классы обслуживания | ❌ Не начато | NOT STARTED |
| **Sprint 6** | What-if симуляции, разрешения, отчёты | ❌ Не начато | NOT STARTED |
| **Sprint 7** | Тесты, оптимизация, документация | ⚠️ Частично (257 тестов) | IN PROGRESS |

### 3.2 Критические отклонения

| # | Отклонение | Влияние | Статус |
|---|-----------|---------|--------|
| 1 | **Web UI полностью отсутствует** | 0 файлов, React app не создан | ❌ NOT STARTED |
| 2 | **Integration Hub — все stubs** | 3 коннектора возвращают пустые данные | ❌ NOT STARTED |
| 3 | **MonteCarloSimulator date bug** | Вероятности дедлайнов некорректны | ❌ BUG NOT FIXED |
| 4 | **DTOs — mutable POJOs** | 18/19 DTO нарушают Java 25 best practice | ⏳ DEFERRED |
| 5 | **data-service controller tests** | 6 из 7 контроллеров без тестов | ⏳ SPRINT 4 |
| 6 | **data-service coverage ~40%** | JaCoCo minimum не установлен | ⏳ SPRINT 4 |
| 7 | **Security permitAll** | JWT decoder есть но не enforced | ⏳ DEFERRED |
| 8 | **CI docker job** | Только validates config, не build/push | ⏳ LOW |

---

## 4. Code Review Sprint 3

### 4.1 Positive

| Area | Assessment |
|------|-----------|
| **Test discipline** | 64 новых тестов за один спринт — отличный темп |
| **CI improvement** | Устранён false sense of security — теперь все модули тестируются |
| **PlanningController** | Реальная async job submission с SSE |
| **Code quality** | FeatureSorter O(n²)→O(n), Mutator deduplication, RFC 7807 |
| **Commit hygiene** | 3 focused commits, каждый с чёткой целью |
| **Self-awareness** | SPRINT3_SUMMARY.md честно отмечает deferred items |

### 4.2 Critical: MonteCarloSimulator Bug

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/montecarlo/MonteCarloSimulator.java:44-65`

**Текущая формула:**
```java
long baseDays = baseDate.toEpochDay();
long referenceDays = LocalDate.of(baseDate.getYear() - 1, 1, 1).toEpochDay();
long durationFromRef = baseDays - referenceDays;
long perturbedDays = referenceDays + Math.round(durationFromRef * ratio);
return LocalDate.ofEpochDay(perturbedDays);
```

**Проблема:** Perturbation масштабирует **абсолютное расстояние от 1 января прошлого года** вместо того чтобы perturboвать **duration работы фичи**.

**Пример:**
- baseDate = 2026-06-01 (day 20604)
- reference = 2025-01-01 (day 20089)
- durationFromRef = 515 дней
- ratio = 2.0 (pessimistic sample) → perturbedDays = 20089 + 1030 = 21119 → **2027-11-01** (515 дней позже!)
- ratio = 0.5 (optimistic sample) → perturbedDays = 20089 + 257 = 20346 → **2025-09-01** (257 дней раньше!)

Это делает вероятности соблюдения дедлайнов **бессмысленными**.

**Правильная формула:**
```java
long baseDays = baseDate.toEpochDay();
long perturbationDays = Math.round(baseDays * (ratio - 1));
return LocalDate.ofEpochDay(baseDays + perturbationDays);
```

Или ещё лучше — perturboвать duration а не absolute date:
```java
long baseDays = baseDate.toEpochDay();
long startDays = featureStartDate.toEpochDay();
long duration = baseDays - startDays;
long perturbedDuration = Math.round(duration * ratio);
return LocalDate.ofEpochDay(startDays + perturbedDuration);
```

### 4.3 Deferred: DTOs → Records

**Оценка:** 17 файлов DTO + EntityMapper refactor + service/controller updates.
**Рекомендация:** Выделить отдельный спринт (Sprint 4). Конвертировать по одному DTO за раз, запуская тесты после каждого.

### 4.4 Deferred: Configurable Random Seed

**Оценка:** Низкий effort (1-2 часа). Можно сделать в Sprint 4 как quick win.

---

## 5. Метрики проекта

### 5.1 Тестовое покрытие

| Модуль | Test Files | Tests | Coverage | Status |
|--------|-----------|-------|----------|--------|
| domain-core | 29 | 124 | 80%+ | ✅ Excellent |
| planning-engine | 12 | 55 | 80%+ | ✅ Good |
| data-service | 7 | 78 | ~40% | ⚠️ Needs work |
| integration-hub | 0 | 0 | 0% | ❌ Empty |
| **TOTAL** | **48** | **257** | **~55%** | ⚠️ |

### 5.2 Spec Compliance

| Область | Score | Trend |
|---------|-------|-------|
| Domain Core | 95% | → Stable |
| Planning Engine | 75% | ↑ +15% |
| Data Service | 70% | ↑ +10% |
| Integration Hub | 20% | → Stable |
| Web UI | 0% | → Stable |
| CI/CD | 90% | ↑ +50% |
| Tests | 65% | ↑ +25% |
| Security | 10% | → Stable |
| **OVERALL** | **~75%** | ↑ +3% |

---

## 6. Рекомендации для Sprint 4

### 6.1 Must Have (P0)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 1 | **Fix MonteCarloSimulator date bug** — правильная формула perturbation | planning-engine | 30 мин |
| 2 | **data-service controller tests** — @WebMvcTest для 6 контроллеров | data-service | 4-6 часов |
| 3 | **EntityMapper tests** — verify mapping correctness | data-service | 1-2 часа |
| 4 | **Set JaCoCo minimum for data-service** — enforce 70%+ | data-service/pom.xml | 10 мин |

### 6.2 Should Have (P1)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 5 | **DTOs → Java 25 records** — 17 файлов, по одному за раз | data-service | 3-4 часа |
| 6 | **Configurable Random seed** — @Value injection | planning-engine | 1 час |
| 7 | **data-service @DataJpaTest** — repository tests с Testcontainers | data-service | 2-3 часа |
| 8 | **GET /dashboard/timeline** endpoint | data-service | 30 мин |
| 9 | **PUT /teams/{id}/members** endpoint | data-service | 30 мин |

### 6.3 Could Have (P2)

| # | Задача | Модуль | Оценка |
|---|--------|--------|--------|
| 10 | **Integration Hub Jira connector** — первый реальный коннектор | integration-hub | 8-12 часов |
| 11 | **CI docker build/push** — не только validate config | .github/ci.yml | 1 час |
| 12 | **Strengthen SA/MonteCarlo test assertions** | planning-engine | 1 час |

### 6.4 NOT in Scope

- Web UI (React) — требует отдельного frontend agent
- Security enforcement — deferred by design
- What-if симуляции — зависит от стабильности planning pipeline
- Performance optimization 500 features/50 teams < 30 sec

---

## 7. Lessons Learned

### Что сработало хорошо:
1. **Domain-first подход** — domain-core как foundation оказался правильным решением
2. **Code review процесс** — SPRINT0/1/3 reviews выявили критические проблемы
3. **PLAN.md + specs** — детальная документация позволяет разным агентам работать консистентно
4. **CI fix first** — устранение false sense of security сэкономило время
5. **Batch test creation** — 64 теста за один спринт, efficient
6. **Small focused commits** — каждый fix isolated, easy to review

### Что пошло не так:
1. **Регрессии при fixes** — Sprint 1/2 удалил алгоритмы и тесты. Regression testing отсутствовал.
2. **Stub creep** — integration-hub остался stubs через 3 спринта
3. **MonteCarlo bug не исправлен** — commit заявлял fix но формула всё ещё некорректна
4. **DTO conversion deferred twice** — нужно было начать раньше
5. **No frontend agent** — web-ui полностью пропущен

### Что изменить в процессе:
1. **Regression tests перед merge** — automated check что существующие тесты не сломаны
2. **Stub code expiry** — если stub не заменён за 1 спринт, это blocker
3. **Coverage minimums early** — data-service 40% slipped through因为没有 minimum
4. **Frontend как отдельный трек** — не может быть "остаточным" task
5. **Verify fixes, don't trust commit messages** — MonteCarlo bug "fixed" but wasn't

---

## 8. Состояние на момент ретроспективы

```
FeatureFlow v0.1.0-SNAPSHOT
├── domain-core        ████████████████████████████████████████ 95%  ✅
├── data-service       █████████████████████████████░░░░░░░░░░░ 70%  ⚠️
├── planning-engine    ████████████████████████████████░░░░░░░░ 75%  ⚠️
├── integration-hub    ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 20%  ❌
├── web-ui             ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  0%  ❌
├── tests              ████████████████████████████░░░░░░░░░░░░ 55%  ⚠️
├── CI/CD              ██████████████████████████████████░░░░░░ 70%  ⚠️
├── security           ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░ 10%  ❌
└── documentation      ████████████████████████████████████████ 95%  ✅
```

**Overall: ~75/100**

---

## 9. Sprint 3 Review Verdict

**Status: ⚠️ GOOD PROGRESS WITH ONE CRITICAL BUG**

Sprint 3 достиг значительного прогресса: CI исправлен, 64 новых теста, PlanningController работает, code quality улучшена. Однако **MonteCarloSimulator date bug остаётся** — это блокирует корректность вероятностных прогнозов дедлайнов, что является core feature системы.

**Recommendation:** Перед началом Sprint 4, **сначала исправить MonteCarloSimulator** (30 мин), затем proceed с планом Sprint 4.
