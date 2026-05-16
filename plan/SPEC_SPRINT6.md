# Sprint 6 — What-If Simulations + Reports + Dashboard Fixes

> **Module:** planning-engine + data-service + web-ui
> **Duration:** 2-3 дня
> **Priority:** P0 (блокирует сравнение планов и аналитику)

---

## 1. Контекст

### 1.1 Текущее состояние

| Область | Score | Проблема |
|---------|-------|----------|
| What-If Simulations | ❌ NONE | Нет baseline persistence, нет branching, нет comparison API |
| PlanningJobService | ⚠️ PARTIAL | storeResult() — dead code, jobs in-memory only |
| Dashboard conflicts | ❌ STUB | Возвращает `{active: 0, list: []}` |
| Dashboard timeline | ❌ STUB | Возвращает `[]` |
| DB Views | ⚠️ UNUSED | 3 views существуют но не exposed via API |
| PlanComparison UI | ⚠️ BASIC | Только cost/conflicts, нет per-feature diff |
| SimulationPage | ⚠️ PARTIAL | UI skeleton есть, но не применяет changes корректно |
| Reports | ❌ NONE | Нет CSV export, нет report generation |
| Permissions | ❌ PARTIAL | SecurityConfig есть JWT, но нет User entity, нет user management |

### 1.2 Что УЖЕ есть

- `PlanningOrchestrator` — полный pipeline: Greedy → SA → Monte Carlo
- `PlanningJobService` — async job tracking с SSE
- `PlanningController` — 6 endpoints (run, execute, status, result, sse, validate)
- `planning_results` table (V3 migration) — для хранения snapshot'ов
- `v_team_capacity`, `v_feature_summary`, `v_sprint_load` — 3 DB views
- `SimulationPage.tsx` — UI skeleton с JSON textareas
- `PlanComparison.tsx` — basic side-by-side comparison
- `SimulationChanges` TypeScript type

### 1.3 Что НЕ трогать

- ✅ Domain entities (domain-core)
- ✅ MonteCarloSimulator (уже исправлен)
- ✅ JiraConnector (Sprint 5)
- ✅ CI pipeline
- ✅ SecurityConfig (permissions — отдельный спринт)

---

## 2. Задача 1: PlanningResult persistence (P0)

### 2.1 Проблема

`PlanningJobService.storeResult()` — dead code. Результаты планирования не сохраняются в БД.
Таблица `planning_results` существует но не используется.

### 2.2 Решение

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/service/PlanningJobService.java`

Добавить сохранение результата в БД через REST call к data-service:

```java
// В PlanningJobService, после завершения job:
private void storeResult(UUID jobId, PlanningResult result) {
    try {
        var payload = Map.of(
            "jobId", jobId.toString(),
            "algorithm", result.algorithm().name(),
            "totalCost", result.totalCost(),
            "computationTimeMs", result.computationTimeMs(),
            "resultData", result
        );
        webClient.post()
            .uri("http://localhost:8080/api/v1/planning-results")
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(Void.class)
            .block(Duration.ofSeconds(10));
    } catch (Exception e) {
        log.warn("Failed to store planning result: {}", e.getMessage());
    }
}
```

**Файл:** `data-service/src/main/java/com/featureflow/data/controller/PlanningResultController.java` (NEW)

```java
@RestController
@RequestMapping("/api/v1/planning-results")
public class PlanningResultController {

    private final PlanningResultService planningResultService;

    @PostMapping
    public ResponseEntity<Void> saveResult(@RequestBody SavePlanningResultRequest request) {
        planningResultService.save(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<PlanningResultSummary>> listResults(
        @RequestParam(required = false) String algorithm,
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(planningResultService.listRecent(algorithm, limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanningResultDetail> getResult(@PathVariable UUID id) {
        return planningResultService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResult(@PathVariable UUID id) {
        planningResultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Файл:** `data-service/src/main/java/com/featureflow/data/service/PlanningResultService.java` (NEW)

```java
@Service
public class PlanningResultService {

    private final PlanningResultRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(SavePlanningResultRequest request) {
        PlanningResultEntity entity = new PlanningResultEntity();
        entity.setJobId(UUID.fromString(request.jobId()));
        entity.setAlgorithm(request.algorithm());
        entity.setTotalCost(request.totalCost());
        entity.setComputationTimeMs(request.computationTimeMs());
        entity.setResultData(objectMapper.valueToTree(request.resultData()));
        entity.setCreatedAt(LocalDateTime.now());
        repository.save(entity);
    }

    public List<PlanningResultSummary> listRecent(String algorithm, int limit) {
        return repository.findRecent(algorithm, PageRequest.of(0, limit))
            .stream()
            .map(this::toSummary)
            .toList();
    }

    public Optional<PlanningResultDetail> findById(UUID id) { ... }
    public void delete(UUID id) { ... }
}
```

**Файл:** `data-service/src/main/java/com/featureflow/data/entity/PlanningResultEntity.java` (NEW)

```java
@Entity
@Table(name = "planning_results")
public class PlanningResultEntity {
    @Id
    private UUID id;

    @Column(name = "job_id", nullable = false, unique = true)
    private UUID jobId;

    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    @Column(name = "total_cost")
    private double totalCost;

    @Column(name = "computation_time_ms")
    private long computationTimeMs;

    @Column(name = "result_data", columnDefinition = "jsonb")
    private JsonNode resultData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // getters/setters
}
```

**Файл:** `data-service/src/main/java/com/featureflow/data/repository/PlanningResultRepository.java` (NEW)

```java
public interface PlanningResultRepository extends JpaRepository<PlanningResultEntity, UUID> {

    @Query("SELECT p FROM PlanningResultEntity p WHERE (:algorithm IS NULL OR p.algorithm = :algorithm) ORDER BY p.createdAt DESC")
    Page<PlanningResultEntity> findRecent(@Param("algorithm") String algorithm, Pageable pageable);

    boolean existsByJobId(UUID jobId);
}
```

---

## 3. Задача 2: What-If Simulation API (P0)

### 3.1 SimulationController

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/controller/SimulationController.java` (NEW)

```java
@RestController
@RequestMapping("/api/v1/simulations")
public class SimulationController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    @PostMapping("/run")
    public ResponseEntity<SimulationJob> runSimulation(@RequestBody SimulationRequest request) {
        UUID jobId = jobService.submitJob(() -> {
            PlanningRequest baseRequest = request.baseRequest();
            PlanningRequest modified = applySimulationChanges(baseRequest, request.changes());
            return orchestrator.runFullPipeline(modified);
        });
        return ResponseEntity.accepted()
            .header("X-Simulation-Id", jobId.toString())
            .body(new SimulationJob(jobId, "PENDING"));
    }

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResult> compareResults(
        @RequestParam UUID baselineId,
        @RequestParam UUID simulationId
    ) {
        PlanningResult baseline = jobService.getResult(baselineId);
        PlanningResult simulation = jobService.getResult(simulationId);
        return ResponseEntity.ok(ComparisonService.compare(baseline, simulation));
    }

    private PlanningRequest applySimulationChanges(
        PlanningRequest base, SimulationChanges changes
    ) {
        // Apply priority changes (modify businessValue)
        // Apply capacity changes (modify team capacity)
        // Apply added/removed features
        return modifiedRequest;
    }
}
```

### 3.2 ComparisonService

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/service/ComparisonService.java` (NEW)

```java
public class ComparisonService {

    public static ComparisonResult compare(PlanningResult baseline, PlanningResult simulation) {
        return new ComparisonResult(
            baseline.totalCost(),
            simulation.totalCost(),
            simulation.totalCost() - baseline.totalCost(),
            baseline.conflicts().size(),
            simulation.conflicts().size(),
            compareTimelines(baseline.featureTimelines(), simulation.featureTimelines()),
            compareTeamLoads(baseline.teamLoadReports(), simulation.teamLoadReports())
        );
    }

    private static List<TimelineDiff> compareTimelines(
        Map<UUID, FeatureTimeline> baseline,
        Map<UUID, FeatureTimeline> simulation
    ) { ... }

    private static List<TeamLoadDiff> compareTeamLoads(
        Map<UUID, TeamLoadReport> baseline,
        Map<UUID, TeamLoadReport> simulation
    ) { ... }
}
```

### 3.3 Domain Records

**Файл:** `planning-engine/src/main/java/com/featureflow/planning/model/SimulationRequest.java` (NEW)

```java
public record SimulationRequest(
    PlanningRequest baseRequest,
    SimulationChanges changes
) {}

public record SimulationChanges(
    Map<UUID, Double> priorityChanges,      // featureId -> new businessValue
    Map<UUID, Double> capacityChanges,      // teamId -> capacityMultiplier
    List<UUID> addedFeatures,
    List<UUID> removedFeatures
) {}

public record SimulationJob(UUID jobId, String status) {}

public record ComparisonResult(
    double baselineCost,
    double simulationCost,
    double costDelta,
    int baselineConflicts,
    int simulationConflicts,
    List<TimelineDiff> timelineDiffs,
    List<TeamLoadDiff> teamLoadDiffs
) {}

public record TimelineDiff(
    UUID featureId,
    String featureTitle,
    LocalDate baselineStart,
    LocalDate baselineEnd,
    LocalDate simulationStart,
    LocalDate simulationEnd,
    long dayShift
) {}

public record TeamLoadDiff(
    UUID teamId,
    String teamName,
    double baselineUtilization,
    double simulationUtilization,
    double utilizationDelta,
    List<String> newBottleneckRoles
) {}
```

---

## 4. Задача 3: Dashboard fixes (P1)

### 4.1 DashboardController — реальные данные

**Файл:** `data-service/src/main/java/com/featureflow/data/controller/DashboardController.java`

Заменить stubs на реальные данные:

```java
@GetMapping("/conflicts")
public ResponseEntity<DashboardConflicts> getConflicts() {
    // Query assignments for over-capacity teams
    // Check dependency violations
    List<Conflict> conflicts = dashboardService.getActiveConflicts();
    return ResponseEntity.ok(new DashboardConflicts(conflicts.size(), conflicts));
}

@GetMapping("/timeline")
public ResponseEntity<List<TimelineEntry>> getTimeline() {
    // Query features with assignments + sprints
    return ResponseEntity.ok(dashboardService.getTimeline());
}
```

### 4.2 DashboardService

**Файл:** `data-service/src/main/java/com/featureflow/data/service/DashboardService.java` (NEW)

```java
@Service
public class DashboardService {

    private final FeatureRepository featureRepo;
    private final AssignmentRepository assignmentRepo;
    private final SprintRepository sprintRepo;
    private final TeamRepository teamRepo;
    private final JdbcTemplate jdbcTemplate;

    public List<Conflict> getActiveConflicts() {
        // Check team capacity vs assigned effort per sprint
        // Check dependency violations (feature assigned before dependency)
        return conflicts;
    }

    public List<TimelineEntry> getTimeline() {
        // Join features + assignments + sprints
        // Return ordered by sprint start date
        return jdbcTemplate.query("""
            SELECT f.id, f.title, f.business_value,
                   s.id as sprint_id, s.start_date, s.end_date, s.label,
                   t.id as team_id, t.name as team_name,
                   p.id as product_id, p.name as product_name
            FROM features f
            JOIN assignments a ON a.feature_id = f.id
            JOIN sprints s ON a.sprint_id = s.id
            JOIN teams t ON a.team_id = t.id
            JOIN feature_products fp ON fp.feature_id = f.id
            JOIN products p ON fp.product_id = p.id
            ORDER BY s.start_date, f.business_value DESC
            """, (rs, rowNum) -> new TimelineEntry(...));
    }

    public Map<String, Object> getTeamCapacityView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_team_capacity");
    }

    public Map<String, Object> getFeatureSummaryView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_feature_summary");
    }

    public Map<String, Object> getSprintLoadView() {
        return jdbcTemplate.queryForList("SELECT * FROM v_sprint_load");
    }
}
```

### 4.3 Dashboard API — expose DB views

```java
@GetMapping("/views/team-capacity")
public ResponseEntity<List<Map<String, Object>>> getTeamCapacityView() {
    return ResponseEntity.ok(dashboardService.getTeamCapacityView());
}

@GetMapping("/views/feature-summary")
public ResponseEntity<List<Map<String, Object>>> getFeatureSummaryView() {
    return ResponseEntity.ok(dashboardService.getFeatureSummaryView());
}

@GetMapping("/views/sprint-load")
public ResponseEntity<List<Map<String, Object>>> getSprintLoadView() {
    return ResponseEntity.ok(dashboardService.getSprintLoadView());
}
```

---

## 5. Задача 4: CSV Export (P1)

### 5.1 ReportController

**Файл:** `data-service/src/main/java/com/featureflow/data/controller/ReportController.java` (NEW)

```java
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/features/csv")
    public ResponseEntity<byte[]> exportFeaturesCsv(
        @RequestParam(required = false) String productId
    ) {
        byte[] csv = reportService.exportFeaturesCsv(productId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=features.csv")
            .body(csv);
    }

    @GetMapping("/planning/{jobId}/csv")
    public ResponseEntity<byte[]> exportPlanningCsv(@PathVariable UUID jobId) {
        byte[] csv = reportService.exportPlanningCsv(jobId);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "text/csv")
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=planning.csv")
            .body(csv);
    }
}
```

### 5.2 ReportService

**Файл:** `data-service/src/main/java/com/featureflow/data/service/ReportService.java` (NEW)

```java
@Service
public class ReportService {

    private final FeatureRepository featureRepo;
    private final AssignmentRepository assignmentRepo;
    private final PlanningResultRepository planningResultRepo;

    public byte[] exportFeaturesCsv(String productId) {
        List<FeatureEntity> features = productId != null
            ? featureRepo.findByProductId(UUID.fromString(productId))
            : featureRepo.findAll();

        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("ID", "Title", "Business Value", "Class of Service",
                       "Backend Hours", "Frontend Hours", "QA Hours", "Deadline"));

        for (FeatureEntity f : features) {
            printer.printRecord(
                f.getId(), f.getTitle(), f.getBusinessValue(), f.getClassOfService(),
                f.getEffortBackendHours(), f.getEffortFrontendHours(),
                f.getEffortQaHours(), f.getDeadline()
            );
        }

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
```

**Dependency:** Добавить `org.apache.commons:commons-csv` в `data-service/pom.xml`

---

## 6. Задача 5: Web UI — Simulation improvements (P1)

### 6.1 SimulationPage — feature selection

**Файл:** `web-ui/src/pages/SimulationPage.tsx`

- Добавить dropdown для выбора baseline plan из списка сохранённых результатов
- Добавить multi-select для выбора фич для priority changes
- Добавить slider для capacity changes по командам
- После запуска — показать comparison с baseline

### 6.2 PlanComparison — enhanced diff

**Файл:** `web-ui/src/components/planning/PlanComparison.tsx`

- Добавить per-feature timeline comparison table
- Добавить color-coded diffs (green = improved, red = worse)
- Добавить team load comparison chart

### 6.3 planningApi — new methods

**Файл:** `web-ui/src/api/planning.ts`

```typescript
export const planningApi = {
  // ... existing
  listResults: async (algorithm?: string, limit = 20) => {
    const params = new URLSearchParams({ limit: String(limit) });
    if (algorithm) params.set('algorithm', algorithm);
    const r = await apiClient.get(`/planning-results?${params}`);
    return r.data;
  },
  getResult: async (id: string) => {
    const r = await apiClient.get(`/planning-results/${id}`);
    return r.data;
  },
  runSimulation: async (request: SimulationRequest) => {
    const r = await planningClient.post('/simulations/run', request);
    return { simulationId: r.headers['x-simulation-id'], ...r.data };
  },
  compareResults: async (baselineId: string, simulationId: string) => {
    const r = await planningClient.post(
      `/simulations/compare?baselineId=${baselineId}&simulationId=${simulationId}`
    );
    return r.data;
  },
};
```

---

## 7. Порядок выполнения

```
Этап 1: PlanningResult persistence (1.5 часа)
  ├── PlanningResultEntity, PlanningResultRepository
  ├── PlanningResultService, PlanningResultController
  └── Integration with PlanningJobService.storeResult()

Этап 2: What-If Simulation API (2 часа)
  ├── SimulationController, SimulationRequest, SimulationChanges
  ├── ComparisonService, ComparisonResult, TimelineDiff, TeamLoadDiff
  └── applySimulationChanges logic

Этап 3: Dashboard fixes (1.5 часа)
  ├── DashboardService — getActiveConflicts, getTimeline
  ├── DashboardController — replace stubs
  └── Expose DB views via API

Этап 4: CSV Export (1 час)
  ├── ReportController, ReportService
  └── Add commons-csv dependency

Этап 5: Web UI improvements (2 часа)
  ├── SimulationPage — baseline selection, feature/team controls
  ├── PlanComparison — enhanced diff visualization
  └── planningApi — listResults, runSimulation, compareResults

Этап 6: Tests (2 часа)
  ├── ComparisonServiceTest
  ├── DashboardServiceTest
  ├── PlanningResultServiceTest
  └── ReportServiceTest
```

---

## 8. Acceptance Criteria

### Must Have
- [ ] PlanningResult persistence — results saved to DB via data-service endpoint
- [ ] PlanningResultController — CRUD endpoints: POST, GET, GET/{id}, DELETE
- [ ] SimulationController — POST /simulations/run, POST /simulations/compare
- [ ] ComparisonService — compare baseline vs simulation with timeline/team diffs
- [ ] DashboardController — conflicts и timeline возвращают реальные данные (не stubs)
- [ ] DashboardService — getActiveConflicts, getTimeline реализованы
- [ ] DB views exposed — GET /api/v1/dashboard/views/*
- [ ] CSV export — GET /api/v1/reports/features/csv
- [ ] `mvn clean verify` — BUILD SUCCESS
- [ ] Web UI — SimulationPage wired to simulation API, PlanComparison enhanced

### Nice to Have
- [ ] PlanningJobService.storeResult() integrated into job completion flow
- [ ] Simulation page — baseline selection from saved results
- [ ] Color-coded diff visualization in PlanComparison

### NOT in Scope
- User management / permissions (отложено)
- PDF export
- Scheduled reports
- Custom report builder
- Planning-engine security

---

## 9. Команды для верификации

```bash
# Backend
mvn clean verify --batch-mode

# Frontend
cd web-ui
npm run typecheck
npm run build
```

---

## 10. Known Patterns & Conventions

### 10.1 CSV export pattern

```java
CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "Title", ...));
for (Entity e : entities) {
    printer.printRecord(e.getId(), e.getTitle(), ...);
}
return writer.toString().getBytes(StandardCharsets.UTF_8);
```

### 10.2 Comparison pattern

```java
public static ComparisonResult compare(PlanningResult baseline, PlanningResult simulation) {
    return new ComparisonResult(
        baseline.totalCost(), simulation.totalCost(),
        simulation.totalCost() - baseline.totalCost(),
        ...
    );
}
```

### 10.3 DB view query pattern

```java
jdbcTemplate.queryForList("SELECT * FROM v_team_capacity")
```
