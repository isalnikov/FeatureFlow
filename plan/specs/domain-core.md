# Domain Core — Спецификация

> Чистая Java 25 библиотека. Без Spring, без фреймворков.
> Содержит доменные сущности, value objects, бизнес-правила и интерфейсы алгоритмов.

---

## 1. Структура пакетов

```
com.featureflow.domain/
├── entity/
│   ├── Product.java
│   ├── Team.java
│   ├── TeamMember.java
│   ├── FeatureRequest.java
│   ├── PlanningWindow.java
│   ├── Sprint.java
│   └── Assignment.java
├── valueobject/
│   ├── Role.java
│   ├── EffortEstimate.java
│   ├── ThreePointEstimate.java
│   ├── Capacity.java
│   ├── CapacitySlot.java
│   ├── ClassOfService.java
│   ├── PlanningStatus.java
│   ├── AssignmentStatus.java
│   └── DateRange.java
├── rules/
│   ├── CapacityValidator.java
│   ├── DependencyValidator.java
│   ├── OwnershipValidator.java
│   ├── ParallelismValidator.java
│   └── PlanningConstraintChecker.java
├── planning/
│   ├── PlanningEngine.java          # Interface
│   ├── PlanningResult.java          # Record
│   ├── PlanningRequest.java         # Record
│   ├── Conflict.java                # Record
│   └── CostFunction.java            # Interface
└── exception/
    ├── DomainException.java
    ├── CapacityExceededException.java
    ├── DependencyCycleException.java
    └── OwnershipViolationException.java
```

---

## 2. Value Objects

### 2.1 Role

```java
public enum Role {
    BACKEND,
    FRONTEND,
    QA,
    DEVOPS
}
```

### 2.2 EffortEstimate

```java
public record EffortEstimate(
    double backendHours,
    double frontendHours,
    double qaHours,
    double devopsHours
) {
    public EffortEstimate {
        if (backendHours < 0 || frontendHours < 0 || qaHours < 0 || devopsHours < 0) {
            throw new IllegalArgumentException("Effort cannot be negative");
        }
    }
    
    public double totalHours() {
        return backendHours + frontendHours + qaHours + devopsHours;
    }
    
    public double forRole(Role role) {
        return switch (role) {
            case BACKEND -> backendHours;
            case FRONTEND -> frontendHours;
            case QA -> qaHours;
            case DEVOPS -> devopsHours;
        };
    }
}
```

### 2.3 ThreePointEstimate

```java
public record ThreePointEstimate(
    double optimistic,
    double mostLikely,
    double pessimistic
) {
    public double expected() {
        // PERT formula: (O + 4M + P) / 6
        return (optimistic + 4 * mostLikely + pessimistic) / 6.0;
    }
    
    public double standardDeviation() {
        return (pessimistic - optimistic) / 6.0;
    }
}
```

### 2.4 Capacity

```java
public record Capacity(
    Map<Role, Double> hoursPerRole,
    double focusFactor,
    double bugReservePercent,
    double techDebtReservePercent
) {
    public Capacity {
        Objects.requireNonNull(hoursPerRole);
        if (focusFactor <= 0 || focusFactor > 1) {
            throw new IllegalArgumentException("Focus factor must be in (0, 1]");
        }
    }
    
    public double effectiveHours(Role role) {
        double raw = hoursPerRole.getOrDefault(role, 0.0);
        double reserve = 1.0 - bugReservePercent - techDebtReservePercent;
        return raw * focusFactor * reserve;
    }
    
    public double totalEffectiveHours() {
        return Arrays.stream(Role.values())
            .mapToDouble(this::effectiveHours)
            .sum();
    }
}
```

### 2.5 ClassOfService

```java
public enum ClassOfService {
    EXPEDITE,      // Вытесняет всё, требует подтверждения менеджера
    FIXED_DATE,    // Не может быть сдвинута за дедлайн
    STANDARD,      // Стандартное планирование по приоритету
    FILLER         // Заполняет оставшееся время
}
```

### 2.6 DateRange

```java
public record DateRange(LocalDate start, LocalDate end) {
    public DateRange {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End must be after start");
        }
    }
    
    public long days() {
        return ChronoUnit.DAYS.between(start, end);
    }
    
    public boolean overlaps(DateRange other) {
        return !this.end.isBefore(other.start) && !other.end.isBefore(this.start);
    }
    
    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
```

---

## 3. Entities

### 3.1 Product

```java
public final class Product {
    private final UUID id;
    private String name;
    private String description;
    private Set<String> technologyStack;
    private Set<UUID> teamIds;  // Teams that own this product
    
    // Constructor, getters, setters
    // equals/hashCode by id
}
```

### 3.2 TeamMember

```java
public record TeamMember(
    UUID personId,
    String name,
    Role role,
    double availabilityPercent  // 1.0 = full time, 0.5 = half time
) {}
```

### 3.3 Team

```java
public final class Team {
    private final UUID id;
    private String name;
    private List<TeamMember> members;
    private Capacity defaultCapacity;
    private Map<UUID, DateRange> calendar;  // teamId -> availability overrides
    private Set<String> expertiseTags;      // e.g., "legacy-billing", "k8s"
    private Double velocity;                // story points per sprint (optional)
    
    public Capacity effectiveCapacityForSprint(Sprint sprint) {
        // Apply calendar overrides, holidays, vacations
        // Return adjusted capacity
    }
    
    public Map<Role, Long> memberCountByRole() {
        return members.stream()
            .collect(Collectors.groupingBy(TeamMember::role, Collectors.counting()));
    }
}
```

### 3.4 FeatureRequest

```java
public final class FeatureRequest {
    private final UUID id;
    private String title;
    private String description;
    private double businessValue;           // WSJF score or manual priority
    private UUID requestorId;
    private LocalDate deadline;             // Optional
    private ClassOfService classOfService;
    private Set<UUID> productIds;           // Products this feature touches
    private EffortEstimate effortEstimate;
    private ThreePointEstimate stochasticEstimate;  // Optional
    private Set<UUID> dependencies;         // Feature IDs that must complete first
    private Set<String> requiredExpertise;  // Required expertise tags
    private boolean canSplit;               // Can be split into parallel epics
    private int version;                    // Optimistic locking
    
    public boolean hasDeadline() {
        return deadline != null;
    }
    
    public boolean dependsOn(UUID featureId) {
        return dependencies.contains(featureId);
    }
}
```

### 3.5 Sprint

```java
public record Sprint(
    UUID id,
    LocalDate startDate,
    LocalDate endDate,
    Map<UUID, Capacity> capacityOverrides  // teamId -> override capacity
) {
    public DateRange dateRange() {
        return new DateRange(startDate, endDate);
    }
}
```

### 3.6 PlanningWindow

```java
public final class PlanningWindow {
    private final UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Sprint> sprints;
    
    public int sprintIndexForDate(LocalDate date) {
        for (int i = 0; i < sprints.size(); i++) {
            if (sprints.get(i).dateRange().contains(date)) {
                return i;
            }
        }
        return -1;
    }
}
```

### 3.7 Assignment

```java
public final class Assignment {
    private final UUID id;
    private UUID featureId;
    private UUID teamId;
    private UUID sprintId;
    private EffortEstimate allocatedEffort;
    private AssignmentStatus status;
    
    public enum AssignmentStatus {
        PLANNED, IN_PROGRESS, COMPLETED, LOCKED
    }
}
```

---

## 4. Бизнес-правила (Validators)

### 4.1 CapacityValidator

```java
public final class CapacityValidator {
    
    /**
     * Проверяет, что суммарная нагрузка по каждой роли не превышает капасити.
     */
    public static ValidationResult checkCapacity(
        Team team,
        Sprint sprint,
        List<Assignment> existingAssignments,
        EffortEstimate newEffort
    ) {
        var capacity = team.effectiveCapacityForSprint(sprint);
        
        for (Role role : Role.values()) {
            double used = existingAssignments.stream()
                .mapToDouble(a -> a.allocatedEffort().forRole(role))
                .sum();
            double available = capacity.effectiveHours(role);
            double needed = newEffort.forRole(role);
            
            if (used + needed > available) {
                return ValidationResult.failure(
                    "Team %s exceeds %s capacity in sprint %s: %.1f + %.1f > %.1f"
                        .formatted(team.getId(), role, sprint.getId(), used, needed, available)
                );
            }
        }
        return ValidationResult.success();
    }
}
```

### 4.2 DependencyValidator

```java
public final class DependencyValidator {
    
    /**
     * Проверяет отсутствие циклических зависимостей.
     */
    public static void checkNoCycles(Map<UUID, Set<UUID>> dependencies) {
        // Topological sort with cycle detection (Kahn's algorithm)
        Map<UUID, Integer> inDegree = new HashMap<>();
        for (var entry : dependencies.entrySet()) {
            inDegree.putIfAbsent(entry.getKey(), 0);
            for (UUID dep : entry.getValue()) {
                inDegree.merge(dep, 1, Integer::sum);
            }
        }
        
        Queue<UUID> queue = inDegree.entrySet().stream()
            .filter(e -> e.getValue() == 0)
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(ArrayDeque::new));
        
        int processed = 0;
        while (!queue.isEmpty()) {
            UUID node = queue.poll();
            processed++;
            for (UUID dep : dependencies.getOrDefault(node, Set.of())) {
                int newDegree = inDegree.merge(dep, -1, Integer::sum);
                if (newDegree == 0) queue.add(dep);
            }
        }
        
        if (processed < dependencies.size()) {
            throw new DependencyCycleException("Cyclic dependency detected");
        }
    }
    
    /**
     * Проверяет, что все зависимости фичи завершены до её старта.
     */
    public static boolean dependenciesSatisfied(
        FeatureRequest feature,
        Map<UUID, Assignment> assignments,
        PlanningWindow window
    ) {
        // Check that all dependency features complete before this feature starts
        // ...
    }
}
```

### 4.3 OwnershipValidator

```java
public final class OwnershipValidator {
    
    /**
     * Проверяет, что команда владеет всеми продуктами фичи.
     */
    public static boolean teamOwnsProducts(
        Team team,
        FeatureRequest feature,
        Map<UUID, Product> products
    ) {
        return feature.getProductIds().stream()
            .map(products::get)
            .allMatch(p -> p.getTeamIds().contains(team.getId()));
    }
    
    /**
     * Возвращает команды-кандидаты для фичи.
     */
    public static List<Team> findCandidateTeams(
        FeatureRequest feature,
        List<Team> teams,
        Map<UUID, Product> products
    ) {
        return teams.stream()
            .filter(t -> teamOwnsProducts(t, feature, products))
            .toList();
    }
}
```

### 4.4 ParallelismValidator

```java
public final class ParallelismValidator {
    
    private static final int MAX_PARALLEL_FEATURES = 3;
    
    /**
     * Проверяет, что команда не ведёт более N фич параллельно.
     */
    public static boolean withinParallelismLimit(
        Team team,
        Sprint sprint,
        List<Assignment> assignments
    ) {
        long activeFeatures = assignments.stream()
            .filter(a -> a.teamId().equals(team.getId()))
            .filter(a -> a.sprintId().equals(sprint.getId()))
            .filter(a -> a.status() != AssignmentStatus.COMPLETED)
            .map(Assignment::featureId)
            .distinct()
            .count();
        
        return activeFeatures < MAX_PARALLEL_FEATURES;
    }
}
```

### 4.5 PlanningConstraintChecker (Aggregate)

```java
public record PlanningConstraintChecker(
    CapacityValidator capacityValidator,
    DependencyValidator dependencyValidator,
    OwnershipValidator ownershipValidator,
    ParallelismValidator parallelismValidator
) {
    
    public List<ConstraintViolation> checkAll(
        PlanningRequest request,
        List<Assignment> proposedAssignments
    ) {
        List<ConstraintViolation> violations = new ArrayList<>();
        
        for (Assignment assignment : proposedAssignments) {
            // Check ownership
            // Check capacity
            // Check parallelism
            // Check dependencies
        }
        
        // Check for dependency cycles
        // Check deadline violations
        
        return violations;
    }
}

public record ConstraintViolation(
    ViolationType type,
    UUID featureId,
    UUID teamId,
    String message
) {
    public enum ViolationType {
        CAPACITY_EXCEEDED,
        DEPENDENCY_VIOLATION,
        OWNERSHIP_VIOLATION,
        PARALLELISM_EXCEEDED,
        DEADLINE_MISSED,
        DEPENDENCY_CYCLE
    }
}
```

---

## 5. Алгоритмические интерфейсы

### 5.1 PlanningEngine Interface

```java
public interface PlanningEngine {
    
    /**
     * Запускает планирование и возвращает результат.
     */
    PlanningResult plan(PlanningRequest request);
    
    /**
     * Асинхронная версия для длительных расчётов.
     */
    CompletableFuture<PlanningResult> planAsync(PlanningRequest request);
}
```

### 5.2 PlanningRequest

```java
public record PlanningRequest(
    List<FeatureRequest> features,
    List<Team> teams,
    List<Product> products,
    PlanningWindow planningWindow,
    PlanningParameters parameters,
    Set<UUID> lockedAssignments  // Assignments that cannot be changed
) {}

public record PlanningParameters(
    double w1Ttm,
    double w2Underutilization,
    double w3DeadlinePenalty,
    int maxParallelFeatures,
    AnnealingParameters annealing,
    MonteCarloParameters monteCarlo
) {}

public record AnnealingParameters(
    double initialTemperature,
    double coolingRate,
    double minTemperature,
    int maxIterations
) {
    public AnnealingParameters {
        if (initialTemperature <= 0) throw new IllegalArgumentException();
        if (coolingRate <= 0 || coolingRate >= 1) throw new IllegalArgumentException();
        if (minTemperature <= 0) throw new IllegalArgumentException();
        if (maxIterations <= 0) throw new IllegalArgumentException();
    }
}

public record MonteCarloParameters(
    int iterations,
    double confidenceLevel
) {
    public MonteCarloParameters {
        if (iterations <= 0) throw new IllegalArgumentException();
        if (confidenceLevel <= 0 || confidenceLevel <= 1) throw new IllegalArgumentException();
    }
}
```

### 5.3 PlanningResult

```java
public record PlanningResult(
    List<Assignment> assignments,
    List<Conflict> conflicts,
    Map<UUID, FeatureTimeline> featureTimelines,
    Map<UUID, TeamLoadReport> teamLoadReports,
    double totalCost,
    long computationTimeMs,
    PlanningAlgorithm algorithm
) {
    public enum PlanningAlgorithm {
        GREEDY,
        SIMULATED_ANNEALING,
        MONTE_CARLO
    }
}

public record FeatureTimeline(
    UUID featureId,
    LocalDate startDate,
    LocalDate endDate,
    double probabilityOfMeetingDeadline
) {}

public record TeamLoadReport(
    UUID teamId,
    Map<LocalDate, Map<Role, Double>> loadByDateAndRole,
    double utilizationPercent,
    List<String> bottleneckRoles
) {}
```

### 5.4 CostFunction

```java
public interface CostFunction {
    double compute(PlanningResult result);
}

public record WeightedCostFunction(
    double w1Ttm,
    double w2Underutilization,
    double w3DeadlinePenalty
) implements CostFunction {
    
    @Override
    public double compute(PlanningResult result) {
        double ttmCost = computeTtmCost(result);
        double underutilCost = computeUnderutilizationCost(result);
        double deadlineCost = computeDeadlinePenaltyCost(result);
        
        return w1Ttm * ttmCost + w2Underutilization * underutilCost + w3DeadlinePenalty * deadlineCost;
    }
    
    private double computeTtmCost(PlanningResult result) {
        return result.featureTimelines().values().stream()
            .mapToDouble(ft -> {
                var feature = findFeature(result, ft.featureId());
                return feature.businessValue() * ft.endDate().toEpochDay();
            })
            .sum();
    }
    
    private double computeUnderutilizationCost(PlanningResult result) {
        return result.teamLoadReports().values().stream()
            .mapToDouble(report -> {
                double underutil = 1.0 - report.utilizationPercent() / 100.0;
                return underutil * underutil;
            })
            .sum();
    }
    
    private double computeDeadlinePenaltyCost(PlanningResult result) {
        return result.conflicts().stream()
            .filter(c -> c.type() == Conflict.Type.DEADLINE_MISSED)
            .count();
    }
}
```

---

## 6. Исключения

```java
public sealed abstract class DomainException extends RuntimeException
    permits CapacityExceededException, DependencyCycleException, OwnershipViolationException {
    
    protected DomainException(String message) {
        super(message);
    }
}

public final class CapacityExceededException extends DomainException {
    public CapacityExceededException(String message) { super(message); }
}

public final class DependencyCycleException extends DomainException {
    public DependencyCycleException(String message) { super(message); }
}

public final class OwnershipViolationException extends DomainException {
    public OwnershipViolationException(String message) { super(message); }
}
```

---

## 7. Зависимости (pom.xml)

```xml
<dependencies>
    <!-- No Spring! Pure Java library -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 8. Тестовые сценарии

| Тест | Описание |
|------|----------|
| EffortEstimate.totalHours | Сумма часов по ролям |
| ThreePointEstimate.expected | PERT формула |
| Capacity.effectiveHours | С учётом focus factor и резервов |
| DateRange.overlaps | Пересечение периодов |
| DependencyValidator.checkNoCycles | Обнаружение цикла A→B→C→A |
| OwnershipValidator.findCandidateTeams | Фильтрация по владению продуктами |
| ParallelismValidator | Limit 3 фичи/команда/спринт |
| WeightedCostFunction | Вычисление aggregate cost |
