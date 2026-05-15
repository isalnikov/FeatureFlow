# Planning Engine — Спецификация

> Spring Boot 3.4 микросервис. Обёртка над Domain Core.
> Алгоритмы: Greedy, Simulated Annealing, Monte Carlo.

---

## 1. Структура пакетов

```
com.featureflow.planning/
├── PlanningEngineApplication.java
├── config/
│   ├── PlanningConfig.java
│   └── AsyncConfig.java
├── controller/
│   └── PlanningController.java
├── service/
│   ├── PlanningOrchestrator.java
│   ├── PlanningJobService.java
│   └── PlanCacheService.java
├── greedy/
│   ├── GreedyPlanner.java
│   └── FeatureSorter.java
├── annealing/
│   ├── SimulatedAnnealing.java
│   ├── Solution.java
│   ├── Mutator.java
│   └── AnnealingState.java
├── montecarlo/
│   ├── MonteCarloSimulator.java
│   └── ProbabilityDistribution.java
├── model/
│   ├── PlanningJob.java
│   ├── PlanningJobStatus.java
│   └── PlanningResponse.java
└── exception/
    └── PlanningException.java
```

---

## 2. REST API

### 2.1 PlanningController

```java
@RestController
@RequestMapping("/api/v1/planning")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningOrchestrator orchestrator;
    private final PlanningJobService jobService;

    // POST /api/v1/planning/run — запуск планирования
    @PostMapping("/run")
    public ResponseEntity<PlanningResponse> runPlanning(
        @RequestBody PlanningRunRequest request
    ) {
        var job = orchestrator.startPlanning(request);
        return ResponseEntity.accepted()
            .header("X-Job-Id", job.id().toString())
            .body(PlanningResponse.from(job));
    }

    // GET /api/v1/planning/jobs/{jobId}/status — статус задачи
    @GetMapping("/jobs/{jobId}/status")
    public ResponseEntity<PlanningJobStatus> getJobStatus(
        @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(jobService.getStatus(jobId));
    }

    // GET /api/v1/planning/jobs/{jobId}/result — результат
    @GetMapping("/jobs/{jobId}/result")
    public ResponseEntity<PlanningResult> getResult(
        @PathVariable UUID jobId
    ) {
        var result = jobService.getResult(jobId);
        return result != null
            ? ResponseEntity.ok(result)
            : ResponseEntity.notFound().build();
    }

    // GET /api/v1/planning/jobs/{jobId}/sse — SSE для real-time статуса
    @GetMapping(value = "/jobs/{jobId}/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatus(@PathVariable UUID jobId) {
        return jobService.streamStatus(jobId);
    }

    // POST /api/v1/planning/validate — валидация без запуска
    @PostMapping("/validate")
    public ResponseEntity<List<ConstraintViolation>> validate(
        @RequestBody PlanningRunRequest request
    ) {
        var violations = orchestrator.validate(request);
        return ResponseEntity.ok(violations);
    }
}
```

### 2.2 DTOs

```java
public record PlanningRunRequest(
    List<UUID> featureIds,
    List<UUID> teamIds,
    UUID planningWindowId,
    PlanningParameters parameters,
    Set<UUID> lockedAssignmentIds
) {}

public record PlanningResponse(
    UUID jobId,
    PlanningJobStatus status,
    String message,
    long estimatedCompletionMs
) {}

public record PlanningJobStatus(
    UUID jobId,
    String phase,              // "GREEDY", "ANNEALING", "MONTE_CARLO", "DONE", "FAILED"
    int progressPercent,
    double currentCost,
    double bestCost,
    int iteration,
    String errorMessage
) {}
```

---

## 3. Greedy Planner

### 3.1 Алгоритм

```java
@Component
@RequiredArgsConstructor
public class GreedyPlanner {

    private final ConstraintChecker constraintChecker;

    public PlanningResult plan(PlanningRequest request) {
        long start = System.currentTimeMillis();
        
        // 1. Sort features by priority with dependency resolution
        List<FeatureRequest> sortedFeatures = FeatureSorter.sortByPriority(
            request.features(),
            request.products()
        );

        // 2. Initialize capacity tracking
        Map<UUID, Map<UUID, Capacity>> remainingCapacity = initCapacity(request);

        // 3. Greedy assignment
        List<Assignment> assignments = new ArrayList<>();
        Map<UUID, FeatureTimeline> timelines = new HashMap<>();
        List<Conflict> conflicts = new ArrayList<>();

        for (FeatureRequest feature : sortedFeatures) {
            var result = assignFeature(
                feature,
                request,
                remainingCapacity,
                assignments
            );
            
            if (result.isSuccess()) {
                assignments.addAll(result.assignments());
                timelines.put(feature.getId(), result.timeline());
            } else {
                conflicts.addAll(result.conflicts());
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        
        return new PlanningResult(
            assignments,
            conflicts,
            timelines,
            computeTeamLoad(assignments, request),
            computeCost(assignments, timelines, request.parameters()),
            elapsed,
            PlanningResult.PlanningAlgorithm.GREEDY
        );
    }

    private FeatureAssignmentResult assignFeature(
        FeatureRequest feature,
        PlanningRequest request,
        Map<UUID, Map<UUID, Capacity>> remainingCapacity,
        List<Assignment> existingAssignments
    ) {
        // 1. Find candidate teams
        List<Team> candidates = OwnershipValidator.findCandidateTeams(
            feature, request.teams(), request.products()
        );

        // 2. Determine earliest start (after dependencies)
        LocalDate earliestStart = computeEarliestStart(feature, existingAssignments);

        // 3. For each candidate team, find the earliest slot
        for (Team team : candidates) {
            var slot = findEarliestSlot(
                feature, team, earliestStart, remainingCapacity, request
            );
            
            if (slot != null) {
                // Create assignments for each sprint the feature spans
                var assignments = createAssignments(feature, team, slot);
                var timeline = computeTimeline(feature, slot);
                
                // Update remaining capacity
                deductCapacity(remainingCapacity, team, slot, feature.getEffortEstimate());
                
                return FeatureAssignmentResult.success(assignments, timeline);
            }
        }

        // No valid assignment found
        return FeatureAssignmentResult.conflict(
            new Conflict(Conflict.Type.CAPACITY_EXCEEDED, feature.getId(), null,
                "No team has sufficient capacity for feature: " + feature.getTitle())
        );
    }

    private PlanningSlot findEarliestSlot(
        FeatureRequest feature,
        Team team,
        LocalDate earliestStart,
        Map<UUID, Map<UUID, Capacity>> remainingCapacity,
        PlanningRequest request
    ) {
        // Iterate through sprints starting from earliestStart
        // For each sprint, check if capacity is sufficient for each role
        // Determine how many sprints the feature needs (max across roles)
        // Check parallelism constraint
        // Return slot if valid, null otherwise
    }
}
```

### 3.2 FeatureSorter

```java
public final class FeatureSorter {

    /**
     * Топологическая сортировка с учётом приоритета.
     * Фичи без зависимостей сортируются по businessValue (desc).
     */
    public static List<FeatureRequest> sortByPriority(
        List<FeatureRequest> features,
        List<Product> products
    ) {
        // Build dependency graph
        Map<UUID, FeatureRequest> featureMap = features.stream()
            .collect(Collectors.toMap(FeatureRequest::getId, Function.identity()));
        
        // Kahn's algorithm with priority queue
        Map<UUID, Integer> inDegree = computeInDegrees(features);
        PriorityQueue<FeatureRequest> pq = new PriorityQueue<>(
            Comparator.comparingDouble(FeatureRequest::getBusinessValue).reversed()
        );
        
        // Add all features with no dependencies
        features.stream()
            .filter(f -> inDegree.get(f.getId()) == 0)
            .forEach(pq::add);
        
        List<FeatureRequest> sorted = new ArrayList<>();
        while (!pq.isEmpty()) {
            FeatureRequest current = pq.poll();
            sorted.add(current);
            
            // Reduce in-degree for dependent features
            for (FeatureRequest f : features) {
                if (f.dependsOn(current.getId())) {
                    int newDegree = inDegree.merge(f.getId(), -1, Integer::sum);
                    if (newDegree == 0) {
                        pq.add(f);
                    }
                }
            }
        }
        
        if (sorted.size() < features.size()) {
            throw new DependencyCycleException("Cyclic dependency detected in features");
        }
        
        return sorted;
    }
}
```

---

## 4. Simulated Annealing

### 4.1 Solution

```java
public record Solution(
    Map<UUID, Assignment> assignments,      // featureId -> assignment
    Map<UUID, Integer> featureSprintIndex,   // featureId -> sprint index
    Map<UUID, UUID> featureTeamMapping,      // featureId -> teamId
    double cost
) {
    public Solution mutate(Mutator mutator, Random random) {
        return mutator.apply(this, random);
    }
}
```

### 4.2 Mutator

```java
@Component
public class Mutator {

    private final ConstraintChecker constraintChecker;

    public Solution apply(Solution current, Random random) {
        int mutationType = random.nextInt(4);
        
        return switch (mutationType) {
            case 0 -> shiftFeatureSprint(current, random);
            case 1 -> reassignTeam(current, random);
            case 2 -> swapFeatures(current, random);
            case 3 -> toggleParallelism(current, random);
            default -> current;
        };
    }

    private Solution shiftFeatureSprint(Solution current, Random random) {
        // Pick a random feature
        UUID featureId = pickRandomFeature(current, random);
        int currentSprint = current.featureSprintIndex().get(featureId);
        
        // Shift forward or backward by 1
        int direction = random.nextBoolean() ? 1 : -1;
        int newSprint = currentSprint + direction;
        
        if (newSprint < 0) return current;
        
        // Create new solution with shifted sprint
        var newSprintIndex = new HashMap<>(current.featureSprintIndex());
        newSprintIndex.put(featureId, newSprint);
        
        return new Solution(
            current.assignments(),
            newSprintIndex,
            current.featureTeamMapping(),
            Double.MAX_VALUE  // Cost will be computed
        );
    }

    private Solution reassignTeam(Solution current, Random random) {
        // Pick a random feature and reassign to a different candidate team
        // ...
    }

    private Solution swapFeatures(Solution current, Random random) {
        // Swap sprint assignments of two features with similar priority
        // ...
    }

    private Solution toggleParallelism(Solution current, Random random) {
        // Split or merge a feature's epics for parallelism
        // ...
    }
}
```

### 4.3 SimulatedAnnealing

```java
@Component
@RequiredArgsConstructor
public class SimulatedAnnealing {

    private final Mutator mutator;
    private final CostFunction costFunction;

    public Solution optimize(
        Solution initialSolution,
        AnnealingParameters params,
        PlanningRequest request
    ) {
        Solution current = initialSolution;
        Solution best = current;
        double temperature = params.initialTemperature();
        Random random = new Random(42); // Seeded for reproducibility
        
        int iteration = 0;
        while (temperature > params.minTemperature() && iteration < params.maxIterations()) {
            // Generate neighbor
            Solution neighbor = current.mutate(mutator, random);
            
            // Check hard constraints
            if (violatesHardConstraints(neighbor, request)) {
                temperature *= params.coolingRate();
                iteration++;
                continue;
            }
            
            // Compute cost
            double neighborCost = costFunction.compute(neighbor);
            neighbor = new Solution(
                neighbor.assignments(),
                neighbor.featureSprintIndex(),
                neighbor.featureTeamMapping(),
                neighborCost
            );
            
            double deltaCost = neighborCost - current.cost();
            
            // Accept or reject
            if (deltaCost < 0 || random.nextDouble() < Math.exp(-deltaCost / temperature)) {
                current = neighbor;
                
                if (current.cost() < best.cost()) {
                    best = current;
                }
            }
            
            // Cool down
            temperature *= params.coolingRate();
            iteration++;
        }
        
        return best;
    }

    private boolean violatesHardConstraints(Solution solution, PlanningRequest request) {
        // Check:
        // 1. Capacity not exceeded for any team/sprint/role
        // 2. Dependencies satisfied
        // 3. Product ownership respected
        // 4. Parallelism limit not exceeded
        // 5. Locked assignments unchanged
        return false; // Placeholder
    }
}
```

---

## 5. Monte Carlo Simulator

### 5.1 Алгоритм

```java
@Component
@RequiredArgsConstructor
public class MonteCarloSimulator {

    private final GreedyPlanner greedyPlanner;

    /**
     * Запускает N симуляций с вариацией оценок.
     * Для каждой фичи вычисляет вероятность уложиться в дедлайн.
     */
    public Map<UUID, FeatureDeadlineProbability> simulate(
        PlanningRequest request,
        MonteCarloParameters params
    ) {
        Map<UUID, List<LocalDate>> completionDates = new HashMap<>();
        
        for (UUID featureId : request.features().stream()
            .map(FeatureRequest::getId).toList()) {
            completionDates.put(featureId, new ArrayList<>());
        }
        
        for (int i = 0; i < params.iterations(); i++) {
            // Perturb estimates using triangular distribution
            PlanningRequest perturbedRequest = perturbEstimates(request);
            
            // Run greedy planning
            PlanningResult result = greedyPlanner.plan(perturbedRequest);
            
            // Collect completion dates
            for (var entry : result.featureTimelines().entrySet()) {
                completionDates.get(entry.getKey()).add(entry.getValue().endDate());
            }
        }
        
        // Compute probabilities
        return completionDates.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> computeProbability(entry.getKey(), entry.getValue(), request)
            ));
    }

    private PlanningRequest perturbEstimates(PlanningRequest request) {
        // For each feature with ThreePointEstimate, sample from triangular distribution
        // Replace EffortEstimate with sampled values
        return request; // Placeholder
    }

    private FeatureDeadlineProbability computeProbability(
        UUID featureId,
        List<LocalDate> completionDates,
        PlanningRequest request
    ) {
        FeatureRequest feature = request.features().stream()
            .filter(f -> f.getId().equals(featureId))
            .findFirst()
            .orElseThrow();
        
        if (!feature.hasDeadline()) {
            return new FeatureDeadlineProbability(featureId, null, 0, 0, 0);
        }
        
        long metDeadline = completionDates.stream()
            .filter(d -> !d.isAfter(feature.getDeadline()))
            .count();
        
        double probability = (double) metDeadline / completionDates.size();
        
        // Percentiles
        List<LocalDate> sorted = completionDates.stream().sorted().toList();
        LocalDate p50 = sorted.get((int) (sorted.size() * 0.5));
        LocalDate p90 = sorted.get((int) (sorted.size() * 0.9));
        
        return new FeatureDeadlineProbability(
            featureId, feature.getDeadline(), probability, p50, p90
        );
    }
}

public record FeatureDeadlineProbability(
    UUID featureId,
    LocalDate deadline,
    double probability,
    LocalDate percentile50,
    LocalDate percentile90
) {}
```

---

## 6. PlanningOrchestrator

```java
@Service
@RequiredArgsConstructor
public class PlanningOrchestrator {

    private final GreedyPlanner greedyPlanner;
    private final SimulatedAnnealing annealing;
    private final MonteCarloSimulator monteCarlo;
    private final PlanningJobService jobService;
    private final PlanCacheService cacheService;
    private final Executor taskExecutor;  // Virtual thread executor

    @Async
    public PlanningJob startPlanning(PlanningRunRequest request) {
        var job = jobService.createJob(request);
        
        CompletableFuture.runAsync(() -> {
            try {
                // Phase 1: Greedy
                jobService.updatePhase(job.id(), "GREEDY", 0);
                var greedyResult = greedyPlanner.plan(buildRequest(request));
                jobService.updateProgress(job.id(), 33, greedyResult.totalCost());
                
                // Phase 2: Simulated Annealing
                jobService.updatePhase(job.id(), "ANNEALING", 33);
                var initialSolution = Solution.from(greedyResult);
                var optimizedSolution = annealing.optimize(
                    initialSolution,
                    request.parameters().annealing(),
                    buildRequest(request)
                );
                var annealedResult = PlanningResult.from(optimizedSolution);
                jobService.updateProgress(job.id(), 66, annealedResult.totalCost());
                
                // Phase 3: Monte Carlo
                jobService.updatePhase(job.id(), "MONTE_CARLO", 66);
                var probabilities = monteCarlo.simulate(
                    buildRequest(request),
                    request.parameters().monteCarlo()
                );
                jobService.updateProgress(job.id(), 90);
                
                // Finalize
                var finalResult = mergeResults(annealedResult, probabilities);
                jobService.completeJob(job.id(), finalResult);
                cacheService.cacheResult(request, finalResult);
                
            } catch (Exception e) {
                jobService.failJob(job.id(), e.getMessage());
            }
        }, taskExecutor);
        
        return job;
    }

    public List<ConstraintViolation> validate(PlanningRunRequest request) {
        var planningRequest = buildRequest(request);
        return constraintChecker.checkAll(
            planningRequest,
            Collections.emptyList()
        );
    }
}
```

---

## 7. AsyncConfig (Virtual Threads)

```java
@Configuration
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

---

## 8. Конфигурация

```yaml
# application.yml
server:
  port: 8081
  tomcat:
    threads:
      max: 200

spring:
  threads:
    virtual:
      enabled: true

featureflow:
  planning:
    annealing:
      initial-temperature: 1000.0
      cooling-rate: 0.95
      min-temperature: 0.1
      max-iterations: 10000
    weights:
      w1-ttm: 1.0
      w2-underutilization: 0.5
      w3-deadline-penalty: 2.0
    monte-carlo:
      iterations: 1000
      confidence-level: 0.95
    parallelism:
      max-features-per-team-per-sprint: 3
```

---

## 9. Тестовые сценарии

| Тест | Описание |
|------|----------|
| GreedyPlanner.simplePlan | 3 фичи, 1 команда, без зависимостей |
| GreedyPlanner.withDependencies | Фичи с зависимостями A→B→C |
| GreedyPlanner.capacityConstraint | Перегрузка — фича сдвигается |
| GreedyPlanner.multiTeam | Фичи на разных командах |
| SimulatedAnnealing.improvesGreedy | Cost уменьшается на 10%+ |
| SimulatedAnnealing.respectsConstraints | Нет нарушений после оптимизации |
| MonteCarlo.probabilityDistribution | Вероятности в диапазоне [0, 1] |
| MonteCarlo.convergence | Стабильные результаты при N=1000 |
| PlanningOrchestrator.fullPipeline | Greedy → Annealing → Monte Carlo |
