# Sprint 6 — Авторский контроль реализации

> **Reviewer:** Senior Software Architect / Technical Design Authority
> **Date:** 2026-05-16
> **Spec Reference:** `plan/SPEC_SPRINT6.md`, `SPRINT4_5_REVIEW.md` §9 (Sprint 6 recommendations)
> **Status:** ⚠️ PARTIAL — 2 of 8 recommendations implemented, 0 new tests for Sprint 6 code

---

## 1. Executive Summary

Sprint 6 реализован **на 35% от рекомендаций**. Ключевая P1 задача (What-if simulation) выполнена отлично с бонусом (CSV export). Dashboard /timeline endpoint также реализован. Однако **6 из 8 рекомендаций не тронуты**, включая P1 SyncOrchestrator. **Ни одного нового теста** не написано для Sprint 6 кода.

| Метрика | Значение |
|---------|----------|
| Рекомендации выполнены | 2 из 8 (25%) |
| Spec compliance (SPEC_SPRINT6.md) | ~60% |
| Новые тесты | 0 |
| Общий тест-каунт | 355 Java + 25 frontend = 380 (unchanged) |
| BUILD | ✅ SUCCESS |

---

## 2. Sprint 6 Recommendations — Детальная проверка

### ✅ R5: Dashboard /timeline real data — DONE

**Файлы:** `DashboardController.java`, `DashboardService.java`

| Требование | Фактическое | Статус |
|------------|-------------|--------|
| `GET /dashboard/timeline` — real data | SQL JOIN: features → assignments → sprints → teams → products | ✅ |
| `GET /dashboard/conflicts` — real data | SQL query с HAVING COUNT > 3 | ✅ |
| DB views exposed | `v_team_capacity`, `v_feature_summary`, `v_sprint_load` | ✅ |
| DashboardService создан | 8 endpoint'ов total | ✅ |

**Код (verified):**
```java
public List<Map<String, Object>> getTimeline() {
    return jdbcTemplate.queryForList("""
        SELECT f.id as featureId, f.title, f.business_value,
               s.id as sprintId, s.start_date, s.end_date, s.label as sprintLabel,
               t.id as teamId, t.name as teamName,
               p.id as productId, p.name as productName
        FROM features f
        JOIN assignments a ON a.feature_id = f.id
        JOIN sprints s ON a.sprint_id = s.id
        JOIN teams t ON a.team_id = t.id
        JOIN feature_products fp ON fp.feature_id = f.id
        JOIN products p ON fp.product_id = p.id
        ORDER BY s.start_date, f.business_value DESC
        """);
}
```

**Вердикт:** ✅ **Полностью реализовано.** 8 endpoint'ов, реальные SQL запросы, DB views.

---

### ✅ R7: What-if simulation backend — DONE (exceeded scope)

**Файлы:** 14 новых/изменённых файлов

| Компонент | Spec | Implemented | Статус |
|-----------|------|-------------|--------|
| SimulationController | `POST /simulations/run`, `POST /simulations/compare` | Оба endpoint'а + async job | ✅ |
| SimulationRequest | featureIds, teamIds, planningWindowId, changes | Все поля + TypeScript interface | ✅ |
| ComparisonService | compare(baseline, simulation) | Cost delta, conflict delta, timeline diffs, team load diffs | ✅ |
| ComparisonResult | costDelta, conflictDelta, timelineDiffs, teamLoadDiffs | Все поля + TimelineDiff, TeamLoadDiff records | ✅ |
| PlanningResultEntity | JPA entity для persistence | Entity + Repository + Service + Controller | ✅ |
| PlanningJobService.storeResult() | Persist results to data-service | HTTP call to data-service `/planning-results` | ✅ |
| CSV Export | commons-csv dependency | Manual StringBuilder (works, но не spec) | ⚠️ Deviation |
| Frontend SimulationPage | UI for what-if | JSON inputs, run simulation, comparison display | ✅ |
| PlanComparison | Per-feature diffs | Color-coded deltas, timeline diffs | ✅ |

**Bonus (не в spec но полезно):**
- PlanningResult CRUD endpoints (POST, GET, GET/{id}, DELETE)
- ReportService с CSV export для features

**Minor deviations from SPEC_SPRINT6.md:**
1. `SavePlanningResultRequest` использует `planningWindowId` вместо `jobId` — functionally acceptable
2. CSV export через manual `StringBuilder` вместо `commons-csv` — работает, но не соответствует spec pattern

**Вердикт:** ✅ **Превзошёл spec.** Полная реализация what-if simulation с persistence, comparison, и frontend UI.

---

### ❌ R1: Fix GanttTimeline sprint labels — NOT DONE

**Файл:** `web-ui/src/components/gantt/GanttTimeline.tsx:20`

**Текущее (unchanged):**
```typescript
case 'sprint':
    return `S${Math.ceil((date.getDate()) / 14)}`;
```

**Ожидаемое:** Использовать actual sprint dates из planning result.

**Impact:** Sprint labels некорректны — показывают `S1`, `S2` на основе дня месяца, а не реальных спринтов.

**Вердикт:** ❌ **Не тронуто.** 30 минут работы, но не сделано.

---

### ❌ R2: SyncOrchestrator full sync — NOT DONE

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/service/SyncOrchestrator.java`

**Текущее (unchanged, 13 lines):**
```java
@Service
public class SyncOrchestrator {
    public SyncResult fullSync() {
        return new SyncResult(0, 0, 0L);
    }
    public record SyncResult(int synced, int errors, long duration) {}
}
```

**Ожидаемое:** Реальная синхронизация: import → map → export → status update.

**Impact:** Endpoint `POST /api/v1/integrations/sync` возвращает `{"synced": 0, "errors": 0, "duration": 0}`.

**Вердикт:** ❌ **Не тронуто.** P1 задача, 4-6 часов работы.

---

### ❌ R3: ADO connector — NOT DONE

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/ado/AdoConnector.java`

**Текущее:** Полный stub — все методы возвращают `false`, `Collections.emptyList()`, или `new ExportResult(Collections.emptyList())`.

**LinearConnector:** Идентичный stub.

**Impact:** 2 из 3 коннекторов не работают.

**Вердикт:** ❌ **Не тронуто.** P2 задача, 6-8 часов работы.

---

### ❌ R4: Repository tests — NOT DONE

**Проверка:**
- `*RepositoryTest.java` files: **0**
- `@DataJpaTest` usage: **0 matches**
- `@JdbcTest` usage: **0 matches**
- Testcontainers usage: **0 matches**

**Ожидаемое:** 6 тестовых файлов для репозиториев с @DataJpaTest + Testcontainers.

**Impact:** Репозитории не тестируются на уровне интеграции.

**Вердикт:** ❌ **Не тронуто.** P2 задача, 2-3 часа работы.

---

### ❌ R6: E2E tests — NOT DONE

**Проверка:**
- `playwright*.*` files: **0**
- `cypress*.*` files: **0**
- `*.e2e.*` files: **0**
- `*.spec.*` files (non-unit): **0**

**Ожидаемое:** Playwright или Cypress с тестами для critical paths.

**Вердикт:** ❌ **Не тронуто.** P2 задача, 4-6 часов работы.

---

### ❌ R8: Web UI dark theme — NOT DONE

**Проверка:**
- `setTheme` calls in `.tsx` files: **0**
- `toggleTheme` calls: **0**
- `darkMode` in tailwind config: **0**
- `dark:` Tailwind classes: **0**

**Текущее:** `uiSlice.ts` имеет `theme: 'light' | 'dark'` и `setTheme` action, но ни один компонент не читает `state.ui.theme` и не рендерит theme toggle.

**Вердикт:** ❌ **Не тронуто.** P3 задача, 2-3 часа работы.

---

## 3. Spec Compliance (SPEC_SPRINT6.md)

| Stage | Spec Item | Implemented | Status |
|-------|-----------|-------------|--------|
| **Stage 1** | SimulationController | ✅ 2 endpoints | ✅ |
| **Stage 1** | SimulationRequest model | ✅ | ✅ |
| **Stage 1** | ComparisonResult model | ✅ | ✅ |
| **Stage 2** | ComparisonService | ✅ compare() | ✅ |
| **Stage 2** | PlanningResult persistence | ✅ Entity + Repository + Service + Controller | ✅ |
| **Stage 3** | PlanningJobService.storeResult() | ✅ HTTP call to data-service | ✅ |
| **Stage 4** | DashboardService | ✅ 8 endpoints | ✅ |
| **Stage 4** | Dashboard /timeline | ✅ SQL JOIN | ✅ |
| **Stage 4** | Dashboard /conflicts | ✅ SQL query | ✅ |
| **Stage 4** | DB views exposed | ✅ 3 views | ✅ |
| **Stage 5** | Frontend SimulationPage | ✅ Enhanced with comparison | ✅ |
| **Stage 5** | PlanComparison | ✅ Per-feature diffs | ✅ |
| **Stage 5** | planning.ts API | ✅ runSimulation + compareResults | ✅ |
| **Stage 6** | SimulationController tests | ❌ 0 tests | ❌ |
| **Stage 6** | ComparisonService tests | ❌ 0 tests | ❌ |
| **Stage 6** | PlanningResultService tests | ❌ 0 tests | ❌ |
| **Stage 6** | ReportService tests | ❌ 0 tests | ❌ |
| **Stage 6** | DashboardService tests | ❌ 0 tests | ❌ |
| **Bonus** | CSV export | ✅ Manual StringBuilder | ⚠️ Deviation |
| **Bonus** | ReportController | ✅ GET /reports/features/csv | ✅ |

**Spec compliance: 13 of 19 items (68%)** — все функциональные items выполнены, все тестовые items пропущены.

---

## 4. Тестовое покрытие

| Модуль | До Sprint 6 | После Sprint 6 | Изменение |
|--------|------------|---------------|-----------|
| domain-core | 124 | 124 | — |
| data-service | 154 | 154 | — |
| planning-engine | 57 | 57 | — |
| integration-hub | 20 | 20 | — |
| **TOTAL** | **355** | **355** | **0** |

**Критическая проблема:** Sprint 6 добавил ~14 новых файлов backend кода (SimulationController, ComparisonService, PlanningResultService, ReportService, DashboardService) — **ни один не покрыт тестами**.

---

## 5. Качество кода

### 5.1 Positive

| Area | Assessment |
|------|-----------|
| **SimulationController** | Clean async pattern: submit job → poll → return jobId |
| **ComparisonService** | Well-structured diff computation with TimelineDiff/TeamLoadDiff records |
| **DashboardService** | Real SQL JOINs, proper use of JdbcTemplate |
| **PlanningResultEntity** | Proper JPA entity with @Version, @PrePersist, @PreUpdate |
| **PlanComparison (frontend)** | Color-coded deltas, per-feature comparison |
| **CSV export** | Proper escaping for commas/quotes/newlines |

### 5.2 Concerns

| Area | Issue | Severity |
|------|-------|----------|
| **No tests for Sprint 6 code** | 14 new files, 0 tests | 🔴 CRITICAL |
| **SyncOrchestrator still stub** | P1 task not done | 🟡 MAJOR |
| **GanttTimeline labels still wrong** | Cosmetic but was recommended | 🟢 MINOR |
| **ADO/Linear connectors still stubs** | 2 of 3 connectors non-functional | 🟡 MAJOR |
| **Dark theme not wired** | Redux state exists but unused | 🟢 MINOR |
| **Sprint 5 + 6 squashed** | Single commit for both sprints | 🟡 MAJOR |
| **CSV uses manual StringBuilder** | Spec called for commons-csv | 🟢 MINOR |

---

## 6. Отклонения от рекомендаций

| # | Recommendation | Priority | Est. Time | Status | Reason |
|---|---------------|----------|-----------|--------|--------|
| 1 | Fix GanttTimeline labels | P2 | 30 min | ❌ NOT DONE | Ignored |
| 2 | SyncOrchestrator | P1 | 4-6 hrs | ❌ NOT DONE | Ignored |
| 3 | ADO connector | P2 | 6-8 hrs | ❌ NOT DONE | Ignored |
| 4 | Repository tests | P2 | 2-3 hrs | ❌ NOT DONE | Ignored |
| 5 | Dashboard /timeline | P2 | 1-2 hrs | ✅ DONE | |
| 6 | E2E tests | P2 | 4-6 hrs | ❌ NOT DONE | Ignored |
| 7 | What-if simulation | P1 | 8-12 hrs | ✅ DONE | Exceeded scope |
| 8 | Dark theme | P3 | 2-3 hrs | ❌ NOT DONE | Ignored |

**Completion: 2 of 8 (25%)**

---

## 7. Final Verdict

### Sprint 6: ⚠️ PARTIALLY ACCEPTED — 35% recommendation compliance

| Metric | Value |
|--------|-------|
| Recommendations completed | 2 of 8 (25%) |
| SPEC_SPRINT6.md compliance | 68% (functional done, tests skipped) |
| New tests added | 0 |
| New code files | ~14 |
| BUILD | ✅ SUCCESS |

### Что принято:
- ✅ **What-if simulation** — полная реализация с backend API, persistence, comparison service, frontend UI. Превзошёл spec (добавлен CSV export).
- ✅ **Dashboard /timeline** — реальные SQL JOIN запросы, 8 endpoint'ов, DB views.

### Что НЕ принято:
- ❌ **0 тестов для Sprint 6 кода** — критическое нарушение. SimulationController, ComparisonService, PlanningResultService, ReportService, DashboardService — все без тестов.
- ❌ **SyncOrchestrator** — P1 задача, всё ещё stub.
- ❌ **GanttTimeline labels** — 30 минут работы, не сделано.
- ❌ **ADO connector** — 2 из 3 коннекторов не работают.
- ❌ **Repository tests** — @DataJpaTest так и не добавлены.
- ❌ **E2E tests** — Playwright/Cypress не добавлены.
- ❌ **Dark theme** — инфраструктура есть, UI не подключён.

---

## 8. Рекомендации для Sprint 7

### P0 (Must fix)
| # | Задача | Оценка |
|---|--------|--------|
| 1 | **Тесты для Sprint 6 кода** — SimulationController, ComparisonService, PlanningResultService, ReportService, DashboardService | 4-6 часов |
| 2 | **SyncOrchestrator** — full sync: import → map → export → status update | 4-6 часов |

### P1 (Should fix)
| # | Задача | Оценка |
|---|--------|--------|
| 3 | **Fix GanttTimeline sprint labels** — использовать actual sprint dates | 30 мин |
| 4 | **Repository tests** — @DataJpaTest + Testcontainers для 6 репозиториев | 2-3 часа |
| 5 | **ADO connector** — Azure DevOps REST API | 6-8 часов |
| 6 | **Dark theme** — wire theme toggle to UI | 2-3 часа |

### P2 (Nice to have)
| # | Задача | Оценка |
|---|--------|--------|
| 7 | **E2E tests** — Playwright для critical paths (login, create feature, run planning) | 4-6 часов |
| 8 | **Linear connector** — GraphQL API | 6-8 часов |
| 9 | **CSV export** — migrate to commons-csv per spec | 30 мин |

---

## 9. Project Health Trend

| Sprint | Tests | Spec Compliance | New Features |
|--------|-------|----------------|--------------|
| Sprint 0 | 124 | 42% | Domain model |
| Sprint 1/2 | 124 | 28%→72% | CRUD + algorithms |
| Sprint 3 | 257 | 75% | CI + tests + quality |
| Sprint 4/5 | 380 | 92% | Web UI wiring + Jira |
| **Sprint 6** | **380** | **35%** | What-if simulation + dashboard |

**Тренд:** Количество тестов стагнирует (380 → 380). Spec compliance резко упала (92% → 35%) из-за того что Sprint 6 выполнил только 2 из 8 рекомендаций. Функциональность растёт, но качество кода (test coverage для нового кода) снижается.
