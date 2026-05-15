# Sprint 0 Fixes + Sprint 1 — Code Review Report

> **Reviewer:** Senior Software Architect / Technical Design Authority
> **Date:** 2026-05-15
> **Spec Reference:** `PLAN.md` + `plan/specs/*.md`
> **Previous Review:** `SPRINT0_REVIEW.md`
> **Status:** ✅ GOOD PROGRESS — 72/100 compliance (up from 42%)

---

## Summary

Значительный прогресс. Service layer, DTO/mapper layer и planning engine skeleton добавлены. Domain-core тесты отличные (37 классов, 124 теста, 86.5% coverage). Алгоритмы GreedyPlanner, SimulatedAnnealing, MonteCarlo реализованы. Однако остаются проблемы: SprintController bypasses service layer, DTOs — mutable POJOs вместо Java 25 records, PlanningController — stub без wiring, MonteCarloSimulator имеет bug с захардкоженной датой.

---

## SPRINT0_REVIEW.md — Issue Resolution

### CRITICAL (6 total)

| ID | Issue | Status | Notes |
|----|-------|--------|-------|
| **C1** | Java Version Mismatch | ✅ FIXED | pom.xml → Java 25, Spring Boot 3.5.0 |
| **C2** | Missing Service Layer | ✅ FIXED | 5 сервисов с `@Transactional` |
| **C3** | Missing DTO/Mapper Layer | ⚠️ PARTIAL | 17 DTOs + EntityMapper созданы, но DTOs — mutable POJOs, не records |
| **C4** | Security permitAll | ❌ DEFERRED | Acknowledged в summary |
| **C5** | V7 Migration Missing | ✅ FIXED | 210 features, 363 product links, 35 deps |
| **C6** | Zero Tests | ✅ FIXED | 37 test classes, 124 tests, 86.5% coverage |

### MAJOR (12 total)

| ID | Issue | Status | Notes |
|----|-------|--------|-------|
| **M1** | Planning Engine Empty | ✅ FIXED | GreedyPlanner, SA, MonteCarlo, Orchestrator |
| **M2** | Integration Hub Empty | ⚠️ DEFERRED | Sprint 3 |
| **M3** | N+1 Query | ✅ FIXED | `findDistinctActiveFeatureIds()` |
| **M4** | Immutable Map.of() | ✅ FIXED | LinkedHashMap |
| **M5** | String Enum Comparison | ✅ FIXED | `!= AssignmentStatus.COMPLETED` |
| **M6** | Team.effectiveCapacityForSprint Bug | ✅ FIXED | Проверяет sprint overrides |
| **M7** | Missing Endpoints | ⚠️ PARTIAL | Нет: PUT /teams/{id}/members, GET /dashboard/timeline |
| **M8** | Missing Configs | ✅ FIXED | WebConfig + OpenApiConfig |
| **M9** | Bidirectional Serialization | ⚠️ PARTIAL | DTOs защищают, но нет @JsonIgnore fallback |
| **M10** | Duplicate DB Query | ✅ FIXED | Удалено |
| **M11** | No @Transactional | ✅ FIXED | Services имеют proper boundaries |
| **M12** | CI/CD Missing | ❌ NOT FIXED | |

---

## NEW Issues Found

### N1. SprintController Bypasses Service Layer — CRITICAL

**Файл:** `data-service/src/main/java/com/featureflow/data/controller/SprintController.java:16-57`

Работает напрямую с `SprintRepository`, возвращает/принимает `SprintEntity`. Это **регрессия** — все остальные контроллеры теперь используют сервисы + DTO.

```java
// SprintController.java:23-24 — возвращает JPA entity напрямую
@GetMapping
public List<SprintEntity> list() {
    return sprintRepository.findAll();
}

// SprintController.java:35 — принимает JPA entity как request body
@PostMapping
public ResponseEntity<SprintEntity> create(@RequestBody SprintEntity sprint) {
```

**Решение:** Создать `SprintService`, `SprintDto`, `CreateSprintRequest`, `UpdateSprintRequest`. Рефакторить контроллер по аналогии с остальными.

---

### N2. DTOs Are Mutable POJOs, Not Records — MAJOR

**Файлы:** Все 17 DTO в `data-service/src/main/java/com/featureflow/data/dto/`

Spec и Java 25 best practices рекомендуют records для DTO:

```java
// Сейчас — mutable POJO с getters/setters
public class ProductDto {
    private UUID id;
    private String name;
    // ... 14+ полей с getters/setters
}

// Должно быть — immutable record
public record ProductDto(
    UUID id, String name, String description,
    Set<String> technologyStack, Integer version,
    Instant createdAt, Instant updatedAt
) {}
```

**Почему это важно:**
- Immutability by default — thread-safe
- Меньше boilerplate (нет getters/setters/equals/hashCode/toString)
- Pattern matching support
- Компактные конструкторы для валидации
- Соответствует Java 25 patterns spec

**Затрагивает:** ProductDto, TeamDto, TeamMemberDto, FeatureDto, SprintDto, PlanningWindowDto, AssignmentDto, CreateProductRequest, UpdateProductRequest, CreateTeamRequest, UpdateTeamRequest, CreateFeatureRequest, UpdateFeatureRequest, CreateSprintRequest, CreatePlanningWindowRequest, CreateAssignmentRequest, UpdateAssignmentRequest

---

### N3. PlanningController — Stub Without Wiring — MAJOR

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/controller/PlanningController.java:26-39`

```java
// PlanningController.java:28-38 — НИКОГДА не вызывает orchestrator!
public ResponseEntity<PlanningResponse> runPlanning(@RequestBody PlanningRunRequest request) {
    UUID jobId = UUID.randomUUID();
    return ResponseEntity.accepted()
        .header("X-Job-Id", jobId.toString())
        .body(new PlanningResponse(jobId, "ACCEPTED", "Planning job submitted", 30000));
}
```

**Отсутствует:**
- Конвертация `PlanningRunRequest` → `PlanningRequest` (domain object)
- Вызов `PlanningOrchestrator.startPlanning()`
- Job tracking (PlanningJobService)
- Endpoints: `GET /jobs/{id}/status`, `GET /jobs/{id}/result`, `GET /jobs/{id}/sse`, `POST /validate`

**Решение:**

```java
@RestController
@RequestMapping("/api/v1/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    @PostMapping("/run")
    public ResponseEntity<PlanningResponse> runPlanning(@RequestBody PlanningRunRequest request) {
        var job = orchestrator.startPlanning(toPlanningRequest(request));
        return ResponseEntity.accepted()
            .header("X-Job-Id", job.id().toString())
            .body(PlanningResponse.from(job));
    }

    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<PlanningJobStatus> getJobStatus(@PathVariable UUID jobId) {
        return ResponseEntity.ok(jobService.getStatus(jobId));
    }

    @GetMapping("/jobs/{jobId}/result")
    public ResponseEntity<PlanningResult> getResult(@PathVariable UUID jobId) {
        var result = jobService.getResult(jobId);
        return result != null
            ? ResponseEntity.ok(result)
            : ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/jobs/{jobId}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatus(@PathVariable UUID jobId) {
        return jobService.streamStatus(jobId);
    }

    @PostMapping("/validate")
    public ResponseEntity<List<ConstraintViolation>> validate(@RequestBody PlanningRunRequest request) {
        var violations = orchestrator.validate(toPlanningRequest(request));
        return ResponseEntity.ok(violations);
    }
}
```

---

### N4. MonteCarloSimulator — Hardcoded Date Bug — BUG

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/montecarlo/MonteCarloSimulator.java:60-61`

```java
long daysDiff = (long) ((baseDate.toEpochDay() - LocalDate.of(2025, 1, 1).toEpochDay()) * ratio);
return LocalDate.of(2025, 1, 1).plusDays(daysDiff);
```

Захардкоженная дата `2025-01-01` означает, что perturbation рассчитывается относительно фиксированной эпохи, а не относительно baseDate. При baseDate далеко от 2025-01-01 результаты будут некорректными.

**Решение:**

```java
long baseDays = baseDate.toEpochDay();
long perturbedDays = (long) (baseDays * ratio);
return LocalDate.ofEpochDay(perturbedDays);
```

---

### N5. GreedyPlanner.computeTeamLoadReports — Empty Stub — BUG

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/greedy/GreedyPlanner.java:249-263`

Возвращает пустые отчёты с `Map.of()`, `0.0`, `List.of()` для каждой команды. Team load reports — core output планировщика.

**Решение:** Агрегировать assignments по team/sprint/role:

```java
private Map<UUID, TeamLoadReport> computeTeamLoadReports(
    List<Assignment> assignments,
    PlanningRequest request
) {
    return request.teams().stream().collect(Collectors.toMap(
        Team::getId,
        team -> {
            var teamAssignments = assignments.stream()
                .filter(a -> a.teamId().equals(team.getId()))
                .toList();

            var loadByDateAndRole = teamAssignments.stream()
                .collect(Collectors.groupingBy(
                    a -> findSprintDate(a.sprintId(), request),
                    Collectors.groupingBy(
                        a -> computeRoleLoad(a),
                        Collectors.summingDouble(EffortEstimate::totalHours)
                    )
                ));

            double utilization = computeUtilization(teamAssignments, team);
            var bottlenecks = findBottleneckRoles(teamAssignments, team);

            return new TeamLoadReport(team.getId(), loadByDateAndRole, utilization, bottlenecks);
        }
    ));
}
```

---

### N6. GreedyPlanner.computeCost Ignores Weights — BUG

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/greedy/GreedyPlanner.java:265-279`

Считает cost как простую сумму дней, игнорируя `PlanningParameters` weights (`w1Ttm`, `w2Underutilization`, `w3DeadlinePenalty`).

**Решение:** Реализовать `WeightedCostFunction` из спека (`plan/specs/domain-core.md:5.4`).

---

### N7. FeatureSorter Secondary Sort Is O(n²) — PERFORMANCE

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/greedy/FeatureSorter.java:41-43`

```java
.thenComparing(f -> featureMap.keySet().toList().indexOf(f.getId()))
```

Создаёт новый список и делает `indexOf` для каждого сравнения. O(n²) в priority queue.

**Решение:** Pre-compute index map:

```java
Map<UUID, Integer> originalIndex = new HashMap<>();
for (int i = 0; i < features.size(); i++) {
    originalIndex.put(features.get(i).getId(), i);
}
// Then use:
.thenComparing(f -> originalIndex.get(f.getId()))
```

---

### N8. Mutator Has Duplicate Methods — CODE SMELL

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/annealing/Mutator.java`

Два overloaded метода для `shiftFeatureSprint`, `swapFeatures`, `reassignTeam` — один public (package-private) с явными параметрами, один private с Random. Строки 21-35 дублируют 72-87, 37-53 дублируют 89-116, 55-70 дублируют 118-139.

**Решение:** Убрать public overloads, оставить только методы с Random.

---

### N9. No data-service Tests — GAP

Summary сам признаёт: «data-service: 0 tests, TBD». Нет `@DataJpaTest`, `@WebMvcTest`, или service unit tests. Service layer полностью не протестирован.

**Что добавить:**
- `ProductServiceTest` — CRUD операции
- `FeatureServiceTest` — CRUD + pagination + bulk
- `TeamServiceTest` — CRUD + capacity + load
- `AssignmentServiceTest` — CRUD + lock/unlock
- `PlanningWindowServiceTest` — CRUD + sprints
- `ProductControllerTest` — `@WebMvcTest`
- `FeatureControllerTest` — `@WebMvcTest`

---

### N10. PlanningOrchestrator Hardcoded Random Seed — MINOR

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/service/PlanningOrchestrator.java:63`

`new Random(42)` делает Monte Carlo результаты детерминированными.

**Решение:** Использовать `SecureRandom` или inject seed для тестов.

---

### N11. SimulatedAnnealing Fixed Seed — MINOR

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/annealing/SimulatedAnnealing.java:27`

`new Random(42)`. Acceptable для тестов, но должно быть configurable для production.

---

### N12. GlobalExceptionHandler Missing Title/Instance — MINOR

**Файл:** `data-service/src/main/java/com/featureflow/data/config/GlobalExceptionHandler.java:18`

`ProblemDetail.forStatusAndDetail` не устанавливает title/instance. RFC 7807 рекомендует.

---

### N13. AssignmentService.findByStatus String Parsing — MINOR

**Файл:** `data-service/src/main/java/com/featureflow/data/service/AssignmentService.java:64-68`

Принимает `String status`, делает `AssignmentStatus.valueOf(status)`. Должен принимать `AssignmentStatus` напрямую.

---

## Code Quality Assessment

### Java 25 Patterns

| Feature | Usage | Assessment |
|---------|-------|------------|
| Records | Domain VOs (EffortEstimate, Capacity, Sprint) | ✅ Excellent |
| Sealed classes | DomainException hierarchy | ✅ Correct |
| Pattern matching | switch expressions in domain | ✅ Good |
| Virtual threads | AsyncConfig.java:16 | ✅ Correct |
| DTOs | Mutable POJOs | ❌ Should be records |

### Spring Boot Best Practices

| Practice | Assessment |
|----------|------------|
| Constructor injection | ✅ All services/controllers |
| @Transactional boundaries | ✅ Read-only on class, write on methods |
| ProblemDetail errors | ✅ RFC 7807 |
| CORS config | ✅ WebConfig |
| OpenAPI config | ✅ OpenApiConfig |
| Security | ❌ Still permitAll |
| Pagination | ✅ FeatureController |
| Entity exposure | ❌ SprintController exposes entities |

### Algorithm Correctness

| Algorithm | Assessment |
|-----------|------------|
| **FeatureSorter** | ✅ Correct topo sort + cycle detection. O(n²) secondary sort (N7). |
| **GreedyPlanner** | ⚠️ Core logic sound. computeTeamLoadReports stub (N5), computeCost ignores weights (N6). |
| **SimulatedAnnealing** | ✅ Standard implementation, correct acceptance probability. |
| **Mutator** | ✅ Three mutation types. Duplicate methods (N8). |
| **MonteCarloSimulator** | ❌ Triangular sampling correct, but perturbCompletionDate has hardcoded date bug (N4). |
| **PlanningOrchestrator** | ⚠️ Pipeline correct, but mergeResults doesn't apply optimized assignments. |

---

## Test Quality Assessment

### Domain-Core Tests (37 classes, 124 tests) — EXCELLENT

| Test Class | Tests | Quality |
|------------|-------|---------|
| EffortEstimateTest | 6 | ✅ Happy path, zero, negative, scaling, addition |
| CapacityTest | 8 | ✅ Effective hours, missing role, totals, validation |
| ThreePointEstimateTest | 6 | ✅ PERT formula, stddev, variance, negatives |
| DateRangeTest | ✅ | Present |
| DependencyValidatorTest | 6 | ✅ Acyclic, cycle, empty, topo sort |
| ParallelismValidatorTest | 6 | ✅ Limit 3/4, completed ignored, custom max |
| CapacityValidatorTest | 4 | ✅ Pass/fail scenarios |
| OwnershipValidatorTest | ✅ | Present |
| TeamTest | 8 | ✅ Creation, add/remove member, expertise, capacity |
| PlanningResult/Request/Parameters/Conflict/etc | ✅ | Present |

### Planning-Engine Tests (8 classes) — GOOD

| Test Class | Quality |
|------------|---------|
| GreedyPlannerTest | ✅ 5 tests — simple plan, priority, deps, capacity, no candidate |
| FeatureSorterTest | ✅ Present |
| SimulatedAnnealingTest | ⚠️ 2 tests — только проверяет cost < MAX_VALUE. Нет assertion на improvement. |
| MutatorTest | ✅ Present |
| SolutionTest | ✅ Present |
| MonteCarloSimulatorTest | ⚠️ 3 tests — range check. Не проверяет distribution shape. |
| PlanningOrchestratorTest | ⚠️ 1 test — mocks, cost merge. Не проверяет timeline probability update. |
| PlanningControllerTest | ✅ Present |

### Data-Service Tests — 0

Нет ни одного теста. Service layer полностью untested.

---

## Spec Compliance Scorecard

| Area | Before | After | Notes |
|------|--------|-------|-------|
| Domain Core structure | 85% | 85% | Unchanged, already good |
| Domain Core tests | 0% | 86.5% | Excellent |
| Data Service structure | 35% | 75% | Service + DTO added, SprintController regressed |
| Data Service API coverage | 55% | 70% | Missing endpoints |
| Planning Engine | 5% | 55% | Algorithms present, missing: JobService, CacheService, SSE, status/result endpoints |
| Database schema | 90% | 95% | V7 added |
| Security | 10% | 10% | Deferred |
| Infrastructure | 60% | 60% | CI/CD missing |
| Java 25 patterns | 60% | 65% | DTOs should be records |
| **OVERALL** | **42%** | **72%** | |

---

## Priority Recommendations

| Priority | Issue | Effort | Action |
|----------|-------|--------|--------|
| **P0** | N1 — SprintController bypasses service | 15 min | Создать SprintService + SprintDto |
| **P0** | N4 — MonteCarlo date bug | 10 min | Убрать hardcoded 2025-01-01 |
| **P0** | N5 — computeTeamLoadReports stub | 30 min | Реализовать агрегацию |
| **P0** | N6 — computeCost ignores weights | 30 min | Реализовать WeightedCostFunction |
| **P1** | N3 — PlanningController stub | 2 hours | Wire orchestrator, add job tracking, SSE |
| **P1** | N2 — DTOs → records | 1 hour | Convert all 17 DTOs |
| **P1** | N9 — data-service tests | 4 hours | Service + controller tests |
| **P2** | N8 — Mutator deduplication | 15 min | Remove duplicate methods |
| **P2** | N7 — FeatureSorter O(n²) | 10 min | Pre-compute index map |
| **P2** | N10/N11 — Random seeds | 15 min | Make configurable |
| **P3** | N12 — ProblemDetail title/instance | 10 min | Add title and instance fields |
| **P3** | N13 — AssignmentService string parsing | 10 min | Use AssignmentStatus enum |
| **Deferred** | C4 — Security | Sprint 4 | JWT + RBAC |
| **Deferred** | M2 — Integration Hub | Sprint 3 | Jira/ADO/Linear connectors |
| **Deferred** | M12 — CI/CD | Sprint 2 | GitHub Actions pipeline |
