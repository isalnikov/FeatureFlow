# FeatureFlow Kanban — Context Summary

> Generated: 2026-05-15 | Sprint 1 Complete

---

## Project Overview

**FeatureFlow** — multi-module Spring Boot 3.5.0 + Java 25 portfolio planning system.

### Modules
| Module | Purpose | Tests | Coverage |
|--------|---------|-------|----------|
| `domain-core` | Domain entities, value objects, rules, exceptions | 124 | 86.5% |
| `planning-engine` | Greedy, SA, Monte Carlo algorithms + REST API | 32 | 80%+ |
| `data-service` | CRUD API, JPA entities, BFF | 0 | — |
| `integration-hub` | Jira/ADO/Linear connectors (skeleton) | 0 | — |

### Key Files
- `PLAN.md` — project plan with sprint breakdown
- `TODO.md` — task tracking
- `SPRINT0_REVIEW.md` — previous code review (42% → 72% compliance)
- `SPRINT0_SUMMARY.md` — Sprint 0 fixes summary
- `SPRINT1_SUMMARY.md` — Sprint 1 review (60% overall compliance)
- `plan/specs/` — detailed specs for each module

---

## Sprint 1 — Completed Work

### Algorithms (TDD, all passing)
1. **FeatureSorter** — Kahn's topological sort + priority queue (6 tests)
2. **GreedyPlanner** — capacity-aware feature assignment (5 tests)
3. **SimulatedAnnealing** — metaheuristic optimization via Mutator (9 tests)
4. **MonteCarloSimulator** — triangular distribution deadline forecasting (3 tests)
5. **PlanningOrchestrator** — full pipeline: Greedy → SA → MonteCarlo (1 test)
6. **Integration tests** — 5 tests with real algorithms, no mocks

### Infrastructure
- **PlanningController** — POST /api/v1/planning/run
- **AsyncConfig** — virtual threads via `Executors.newVirtualThreadPerTaskExecutor()`
- **SecurityConfig** — JWT OAuth2 RS + RBAC (ADMIN, DELIVERY_MANAGER, PRODUCT_OWNER, TEAM_LEAD)
- **CI/CD** — `.github/workflows/ci.yml` (test → build → docker)
- **JaCoCo** — 80% coverage threshold enforcement

### Commits (12 total)
```
ec121ed FeatureSorter TDD
381a41d GreedyPlanner TDD
73edcca SimulatedAnnealing TDD (Solution + Mutator + Annealing)
00ae711 MonteCarloSimulator TDD
c1103ef PlanningOrchestrator TDD
7f6cac8 PlanningController + DTOs + WebMvcTest
5147a75 AsyncConfig + PlanningConfig
538c97a JaCoCo coverage enforcement
520116d JWT + RBAC security + H2 test dep
db6c5c6 CI/CD pipeline
b0a95c1 Integration tests + @JsonIgnore fixes
1623b2e Sprint 1 review summary
```

---

## Architecture — Key Classes

### planning-engine/src/main/java/com/featureflow/planning/
```
greedy/
  FeatureSorter.java      — static, Kahn's algo + PQ by businessValue
  GreedyPlanner.java      — plan() → assignments respecting capacity/deps
annealing/
  Solution.java           — record: assignments, sprintIndex, teamMapping, cost
  Mutator.java            — shiftFeatureSprint, swapFeatures, reassignTeam
  SimulatedAnnealing.java — optimize() with cooling schedule
montecarlo/
  FeatureDeadlineProbability.java — record: featureId, deadline, probability, p50, p90
  MonteCarloSimulator.java  — simulate() with triangular distribution
service/
  PlanningOrchestrator.java — runFullPipeline(): Greedy → SA → MonteCarlo → merge
controller/
  PlanningController.java   — POST /run → PlanningResult
config/
  AsyncConfig.java          — @EnableAsync + virtual threads
model/
  PlanningRunRequest.java   — DTO with nested params
  PlanningResponse.java     — DTO for job submission
```

### domain-core key classes
```
entity/       — FeatureRequest, Team, Product, Sprint, Assignment, PlanningWindow
valueobject/  — EffortEstimate, Capacity, ThreePointEstimate, DateRange, Role, ClassOfService
planning/     — PlanningRequest, PlanningResult, PlanningParameters, FeatureTimeline, Conflict
rules/        — OwnershipValidator, DependencyValidator, CapacityValidator, ParallelismValidator
exception/    — DomainException (sealed) → CapacityExceededException, DependencyCycleException, etc.
```

---

## Known Issues (from SPRINT1_SUMMARY.md)

### CRITICAL
1. PlanningController accepts domain `PlanningRequest` directly — needs DTO layer
2. GreedyPlanner doesn't validate multi-product ownership

### MAJOR
3. SimulatedAnnealing cost function = Double.MAX_VALUE (stub) — algorithm accepts all mutations
4. MonteCarlo perturbs completion date instead of effort estimates
5. No async execution + SSE for long-running planning jobs
6. CI/CD only checks domain-core coverage, not planning-engine

### MINOR
7. FeatureSorter O(n²) secondary sort (acceptable for <500 features)
8. No validation on PlanningRequest constructor
9. Default JWT secret in SecurityConfig
10. No @PreAuthorize on controller methods (defense-in-depth)

---

## Build Status
```
mvn clean verify → BUILD SUCCESS
156 tests, 0 failures
domain-core: 124 tests, 86.5% coverage
planning-engine: 32 tests, 80%+ coverage (JaCoCo enforced)
```

---

## TDD Workflow Used
1. Write failing test (RED) → verify it fails
2. Write minimal code to pass (GREEN) → verify it passes
3. Refactor → keep tests green
4. Commit after each cycle
5. No production code without failing test first

---

## Next Steps (Sprint 2 recommendations)
1. Implement WeightedCostFunction for SimulatedAnnealing
2. Add DTO layer for Planning API (PlanningRequestDto → mapper → PlanningRequest)
3. Async execution + SSE for planning jobs
4. Multi-product feature support in GreedyPlanner
5. Data-service tests (@DataJpaTest + Testcontainers)
6. Add planning-engine coverage check to CI/CD
7. Remove default JWT secret
