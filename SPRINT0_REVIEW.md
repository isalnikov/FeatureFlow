# Sprint 0 — Code Review Report

> **Reviewer:** Senior Software Architect / Technical Design Authority
> **Date:** 2026-05-15
> **Spec Reference:** `PLAN.md` + `plan/specs/*.md`
> **Status:** ⚠️ NEEDS IMPROVEMENT — 42/100 compliance

---

## Summary

Проект компилируется (`mvn clean package -DskipTests` → BUILD SUCCESS), доменная модель и DB схема выполнены качественно. Однако критические архитектурные пробелы делают Sprint 0 непригодным для перехода к Sprint 1 без исправлений.

**Главная проблема:** контроллеры работают напрямую с репозиториями, нет service layer и DTO. Это означает, что каждый endpoint Sprint 1 придётся переписывать.

---

## CRITICAL — Fix Before Sprint 1

### C1. Java Version Mismatch

**Проблема:** Spec требует Java 25, но `pom.xml` установлен на Java 21. Dockerfile использует `eclipse-temurin:25-jdk`.

**Файлы:**
- `pom.xml` строки 29-31
- `docker/Dockerfile.java` строка 3

**Решение:** Привести всё к Java 25:

```xml
<!-- pom.xml -->
<properties>
    <java.version>25</java.version>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
</properties>
```

---

### C2. Missing Service Layer

**Проблема:** Все 6 контроллеров работают напрямую с репозиториями. Spec требует `ProductService`, `TeamService`, `FeatureService`, `AssignmentService`, `PlanningWindowService`, `DashboardService` с `@Transactional` границами.

**Файлы:**
- `data-service/src/main/java/com/featureflow/data/controller/*.java`

**Решение:** Создать сервисы:

```java
// data-service/src/main/java/com/featureflow/data/service/ProductService.java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;
    private final EntityMapper mapper;

    public List<ProductDto> listAll() {
        return repository.findAll().stream()
            .map(mapper::toProductDto)
            .toList();
    }

    public ProductDto getById(UUID id) {
        return repository.findById(id)
            .map(mapper::toProductDto)
            .orElseThrow(() -> new EntityNotFoundException("Product", id));
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        var entity = mapper.toProductEntity(request);
        return mapper.toProductDto(repository.save(entity));
    }

    @Transactional
    public ProductDto update(UUID id, UpdateProductRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product", id));
        mapper.updateProductEntity(entity, request);
        return mapper.toProductDto(repository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Product", id);
        }
        repository.deleteById(id);
    }
}
```

Аналогично для `TeamService`, `FeatureService`, `AssignmentService`, `PlanningWindowService`, `DashboardService`.

---

### C3. Missing DTO and Mapper Layer

**Проблема:** Контроллеры возвращают JPA entities напрямую. Это вызывает:
- Lazy-loading serialization exceptions
- StackOverflowError из-за bidirectional связей (Product ↔ Team ↔ Feature)
- Coupling API к persistence model

**Файлы:**
- `data-service/src/main/java/com/featureflow/data/controller/*.java`

**Решение:** Создать пакет `dto/` и `mapper/`:

```java
// data-service/src/main/java/com/featureflow/data/dto/ProductDto.java
public record ProductDto(
    UUID id,
    String name,
    String description,
    Set<String> technologyStack,
    Set<UUID> teamIds,
    int version
) {}

// data-service/src/main/java/com/featureflow/data/dto/CreateProductRequest.java
public record CreateProductRequest(
    String name,
    String description,
    Set<String> technologyStack
) {}

// data-service/src/main/java/com/featureflow/data/dto/UpdateProductRequest.java
public record UpdateProductRequest(
    String name,
    String description,
    Set<String> technologyStack
) {}
```

```java
// data-service/src/main/java/com/featureflow/data/mapper/EntityMapper.java
@Component
public class EntityMapper {

    public ProductDto toProductDto(ProductEntity entity) {
        return new ProductDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            new HashSet<>(entity.getTechnologyStack()),
            entity.getTeams().stream().map(TeamEntity::getId).collect(Collectors.toSet()),
            entity.getVersion()
        );
    }

    public ProductEntity toProductEntity(CreateProductRequest request) {
        var entity = new ProductEntity();
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setTechnologyStack(new HashSet<>(request.technologyStack()));
        return entity;
    }

    public void updateProductEntity(ProductEntity entity, UpdateProductRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setTechnologyStack(new HashSet<>(request.technologyStack()));
    }

    // Аналогично для Team, Feature, Assignment, PlanningWindow, Sprint
}
```

Обновить контроллеры для использования сервисов и DTO:

```java
// ProductController.java — после рефакторинга
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    public ResponseEntity<List<ProductDto>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@PathVariable UUID id, @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### C4. Security Completely Open

**Проблема:** `.requestMatchers("/api/v1/**").permitAll()` — весь API без аутентификации.

**Файл:** `data-service/src/main/java/com/featureflow/data/config/SecurityConfig.java`

**Решение:** Добавить JWT filter chain и RBAC структуру:

```java
// data-service/src/main/java/com/featureflow/data/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

Добавить `@PreAuthorize` на контроллеры:

```java
// Пример на FeatureController
@PostMapping
@PreAuthorize("hasAnyRole('DELIVERY_MANAGER', 'PRODUCT_OWNER')")
public ResponseEntity<FeatureDto> create(@RequestBody CreateFeatureRequest request) {
    ...
}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('DELIVERY_MANAGER')")
public ResponseEntity<Void> delete(@PathVariable UUID id) {
    ...
}
```

Роли: `ADMIN`, `DELIVERY_MANAGER`, `PRODUCT_OWNER`, `TEAM_LEAD`.

---

### C5. V7 Migration Missing

**Проблема:** `SPRINT0_SUMMARY.md` заявляет `V7__seed_features.sql` с 200+ фичами. Файл отсутствует.

**Решение:** Либо создать миграцию, либо удалить упоминание из summary.

Если создавать — сгенерировать 200+ фичей с:
- Разными classOfService
- Разными effortEstimate
- Зависимостями между фичами (30+ связей)
- Привязкой к продуктам (feature_products)

---

### C6. Zero Tests

**Проблема:** Ни одного тестового файла. Sprint 0 acceptance criteria: «Компилируется, тесты green».

**Решение:** Минимум — написать unit-тесты для domain-core (8 сценариев из спека):

```java
// domain-core/src/test/java/com/featureflow/domain/valueobject/EffortEstimateTest.java
class EffortEstimateTest {

    @Test
    void totalHours_shouldSumAllRoles() {
        var estimate = new EffortEstimate(40, 24, 16, 8);
        assertThat(estimate.totalHours()).isEqualTo(88);
    }

    @Test
    void forRole_shouldReturnCorrectValue() {
        var estimate = new EffortEstimate(40, 24, 16, 8);
        assertThat(estimate.forRole(Role.BACKEND)).isEqualTo(40);
        assertThat(estimate.forRole(Role.FRONTEND)).isEqualTo(24);
        assertThat(estimate.forRole(Role.QA)).isEqualTo(16);
        assertThat(estimate.forRole(Role.DEVOPS)).isEqualTo(8);
    }

    @Test
    void negativeHours_shouldThrow() {
        assertThatThrownBy(() -> new EffortEstimate(-1, 0, 0, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/valueobject/ThreePointEstimateTest.java
class ThreePointEstimateTest {

    @Test
    void expected_shouldUsePERTFormula() {
        var estimate = new ThreePointEstimate(30, 48, 72);
        // (30 + 4*48 + 72) / 6 = 294 / 6 = 49
        assertThat(estimate.expected()).isEqualTo(49.0);
    }

    @Test
    void standardDeviation_shouldBeCorrect() {
        var estimate = new ThreePointEstimate(30, 48, 72);
        // (72 - 30) / 6 = 7
        assertThat(estimate.standardDeviation()).isEqualTo(7.0);
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/valueobject/CapacityTest.java
class CapacityTest {

    @Test
    void effectiveHours_shouldApplyFocusFactorAndReserves() {
        var capacity = new Capacity(
            Map.of(Role.BACKEND, 160.0, Role.FRONTEND, 120.0, Role.QA, 80.0, Role.DEVOPS, 40.0),
            0.7,    // focus factor
            0.20,   // bug reserve
            0.10    // tech debt reserve
        );
        // 160 * 0.7 * (1 - 0.20 - 0.10) = 160 * 0.7 * 0.7 = 78.4
        assertThat(capacity.effectiveHours(Role.BACKEND)).isEqualTo(78.4);
    }

    @Test
    void invalidFocusFactor_shouldThrow() {
        assertThatThrownBy(() -> new Capacity(Map.of(), 0, 0, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Capacity(Map.of(), 1.5, 0, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/valueobject/DateRangeTest.java
class DateRangeTest {

    @Test
    void overlaps_shouldDetectIntersection() {
        var r1 = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        var r2 = new DateRange(LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 15));
        var r3 = new DateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));

        assertThat(r1.overlaps(r2)).isTrue();
        assertThat(r1.overlaps(r3)).isFalse();
    }

    @Test
    void contains_shouldCheckDate() {
        var range = new DateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        assertThat(range.contains(LocalDate.of(2025, 1, 15))).isTrue();
        assertThat(range.contains(LocalDate.of(2025, 2, 1))).isFalse();
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/rules/DependencyValidatorTest.java
class DependencyValidatorTest {

    @Test
    void checkNoCycles_shouldPassForAcyclicGraph() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b"), uuid("c")),
            uuid("b"), Set.of(uuid("c")),
            uuid("c"), Set.of()
        );
        assertThatCode(() -> DependencyValidator.checkNoCycles(deps))
            .doesNotThrowAnyException();
    }

    @Test
    void checkNoCycles_shouldDetectCycle() {
        var deps = Map.of(
            uuid("a"), Set.of(uuid("b")),
            uuid("b"), Set.of(uuid("c")),
            uuid("c"), Set.of(uuid("a"))
        );
        assertThatThrownBy(() -> DependencyValidator.checkNoCycles(deps))
            .isInstanceOf(DependencyCycleException.class);
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/rules/OwnershipValidatorTest.java
class OwnershipValidatorTest {

    @Test
    void findCandidateTeams_shouldFilterByProductOwnership() {
        var product = new Product(uuid("p1"));
        product.addTeam(uuid("t1"));
        product.addTeam(uuid("t2"));

        var feature = new FeatureRequest(uuid("f1"));
        feature.addProduct(uuid("p1"));

        var team1 = new Team(uuid("t1"));
        var team2 = new Team(uuid("t2"));
        var team3 = new Team(uuid("t3"));

        var candidates = OwnershipValidator.findCandidateTeams(
            feature, List.of(team1, team2, team3), Map.of(uuid("p1"), product)
        );

        assertThat(candidates).hasSize(2);
        assertThat(candidates).extracting(Team::getId).containsExactlyInAnyOrder(uuid("t1"), uuid("t2"));
    }
}
```

```java
// domain-core/src/test/java/com/featureflow/domain/rules/ParallelismValidatorTest.java
class ParallelismValidatorTest {

    @Test
    void withinParallelismLimit_shouldAllowUpTo3Features() {
        var team = new Team(uuid("t1"));
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments)).isTrue();
    }

    @Test
    void withinParallelismLimit_shouldReject4thFeature() {
        var team = new Team(uuid("t1"));
        var sprint = new Sprint(uuid("s1"), LocalDate.now(), LocalDate.now().plusWeeks(2));

        var assignments = List.of(
            createAssignment(uuid("t1"), uuid("s1"), uuid("f1")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f2")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f3")),
            createAssignment(uuid("t1"), uuid("s1"), uuid("f4"))
        );

        assertThat(ParallelismValidator.withinParallelismLimit(team, sprint, assignments)).isFalse();
    }
}
```

---

## MAJOR — Fix During Sprint 1

### M1. Planning Engine — Empty Skeleton

**Проблема:** Только `PlanningEngineApplication.java`. Spec требует полный набор классов.

**Что создать:**
```
planning-engine/src/main/java/com/featureflow/planning/
├── controller/PlanningController.java
├── service/PlanningOrchestrator.java
├── service/PlanningJobService.java
├── service/PlanCacheService.java
├── config/AsyncConfig.java
├── greedy/GreedyPlanner.java
├── greedy/FeatureSorter.java
├── annealing/SimulatedAnnealing.java
├── annealing/Solution.java
├── annealing/Mutator.java
├── montecarlo/MonteCarloSimulator.java
└── model/PlanningJob.java
```

См. `plan/specs/planning-engine.md` для полного описания.

### M2. Integration Hub — Empty Skeleton

**Проблема:** Только interface + model records.

**Что создать:**
```
integration-hub/src/main/java/com/featureflow/integration/
├── controller/IntegrationController.java
├── connector/jira/JiraConnector.java
├── connector/jira/JiraClient.java
├── connector/ado/AdoConnector.java
├── connector/linear/LinearConnector.java
├── service/ImportService.java
├── service/ExportService.java
├── service/SyncOrchestrator.java
├── mapper/ExternalIssueMapper.java
└── config/IntegrationConfig.java
```

См. `plan/specs/integration-hub.md`.

### M3. DashboardController N+1 Query

**Проблема:** `assignmentRepo.findAll()` загружает ВСЕ назначения в память, затем `.map(a -> a.getFeature().getId())` вызывает lazy-load для каждой фичи.

**Файл:** `data-service/src/main/java/com/featureflow/data/controller/DashboardController.java:26-28`

**Решение:** Заменить на projection query:

```java
// В AssignmentRepository
@Query("SELECT DISTINCT a.featureId FROM AssignmentEntity a WHERE a.status <> 'COMPLETED'")
List<UUID> findDistinctActiveFeatureIds();

// В DashboardController
@GetMapping("/portfolio")
public ResponseEntity<PortfolioDashboard> getPortfolio() {
    var activeFeatureIds = assignmentRepo.findDistinctActiveFeatureIds();
    // ...
}
```

### M4. Immutable Map in JPA Entities

**Проблема:** `Map.of()` создаёт immutable map. Hibernate не сможет populate поля из БД.

**Файлы:**
- `FeatureEntity.java:48`
- `AssignmentEntity.java:33`

**Решение:**

```java
// Было:
@Column(name = "effort_estimate", columnDefinition = "jsonb")
private Map<String, Double> effortEstimate = Map.of(
    "backendHours", 0.0, "frontendHours", 0.0, "qaHours", 0.0, "devopsHours", 0.0
);

// Стало:
@Column(name = "effort_estimate", columnDefinition = "jsonb")
private Map<String, Double> effortEstimate = new LinkedHashMap<>(Map.of(
    "backendHours", 0.0, "frontendHours", 0.0, "qaHours", 0.0, "devopsHours", 0.0
));
```

### M5. String Comparison for Enum

**Проблема:** `.name().equals("COMPLETED")` — fragile, defeats enum purpose.

**Файл:** `ParallelismValidator.java:24,41,57`

**Решение:**

```java
// Было:
!a.getStatus().name().equals("COMPLETED")

// Стало:
a.getStatus() != AssignmentStatus.COMPLETED
```

### M6. Team.effectiveCapacityForSprint() Bug

**Проблема:** `calendar.get(id)` где `id` — ID самой команды. Calendar map `Map<UUID, DateRange>` должен ключиться по sprint ID или date, не по team ID. Метод всегда возвращает `defaultCapacity`.

**Файл:** `domain-core/src/main/java/com/featureflow/domain/entity/Team.java:48-57`

**Решение:**

```java
public Capacity effectiveCapacityForSprint(Sprint sprint) {
    // Check sprint-level overrides first
    if (sprint != null && sprint.capacityOverrides() != null) {
        var override = sprint.capacityOverrides().get(this.id);
        if (override != null) {
            return override;
        }
    }
    return defaultCapacity;
}
```

Или изменить структуру calendar на `Map<LocalDate, DateRange>` для хранения дат отсутствия.

### M7. Missing Controllers and Endpoints

**Что добавить:**

| Endpoint | Controller | Описание |
|----------|------------|----------|
| `GET /api/v1/teams/{id}/capacity` | TeamController | Капасити команды для спринта |
| `PUT /api/v1/teams/{id}/members` | TeamController | Обновить состав команды |
| `GET /api/v1/teams/{id}/load` | TeamController | Текущая загрузка команды |
| `GET /api/v1/features/{id}/deps` | FeatureController | Зависимости фичи |
| `PUT /api/v1/features/{id}/deps` | FeatureController | Обновить зависимости |
| `PUT /api/v1/assignments/{id}/unlock` | AssignmentController | Unlock назначения |
| `GET /api/v1/dashboard/timeline` | DashboardController | Roadmap timeline |
| `GET/POST /api/v1/sprints` | SprintController (new) | CRUD спринтов |

### M8. Missing Config Classes

**Что создать:**

```java
// data-service/src/main/java/com/featureflow/data/config/WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
    }
}
```

```java
// data-service/src/main/java/com/featureflow/data/config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI featureFlowOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("FeatureFlow Data Service API")
                .version("1.0.0")
                .description("REST API for FeatureFlow portfolio management"))
            .servers(List.of(new Server().url("http://localhost:8080")));
    }
}
```

### M9. Bidirectional Relationship Serialization

**Проблема:** Product ↔ Team ↔ Feature — bidirectional many-to-many. Без `@JsonIgnore` сериализация вызовет StackOverflowError.

**Файлы:**
- `ProductEntity.java:28-32`
- `TeamEntity.java:38-44`
- `FeatureEntity.java:42-48`

**Решение (два варианта):**

Вариант A — `@JsonIgnore` на обратной стороне:
```java
// ProductEntity.java
@ManyToMany(mappedBy = "products")
@JsonIgnore
private Set<TeamEntity> teams = new HashSet<>();
```

Вариант B — использовать DTO (рекомендуется, см. C3).

### M10. Duplicate DB Query in FeatureController

**Проблема:** `repository.findByIdWithProducts(id)` вызывается дважды.

**Файл:** `FeatureController.java:37-39`

**Решение:** Сохранить результат в переменную.

### M11. No @Transactional on Write Operations

**Проблема:** Create/update/delete в контроллерах без `@Transactional`.

**Решение:** Перенести логику в сервисы (см. C2), где `@Transactional` будет на уровне методов сервиса.

### M12. CI/CD Pipeline Missing

**Что создать:** `.github/workflows/ci.yml`

```yaml
name: CI/CD
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '25', distribution: 'temurin' }
      - run: mvn test
      - run: mvn verify -Pintegration-test
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: docker compose build
```

---

## MINOR — Nice to Have

| # | Проблема | Файл | Рекомендация |
|---|----------|------|--------------|
| m1 | `PlanningStatus` unused | `PlanningStatus.java` | Удалить или использовать |
| m2 | FeatureEntity effort as Map | `FeatureEntity.java:48` | Использовать `@Embeddable` вместо Map |
| m3 | CapacityValidator redundant filtering | `CapacityValidator.java:25-26` | Убрать внутренний filter, если assignments pre-filtered |
| m4 | SprintEntity missing @Version | `SprintEntity.java` | Добавить optimistic locking |
| m5 | PlanningWindowEntity missing updatedAt | `PlanningWindowEntity.java` | Добавить `@PreUpdate` |
| m6 | Dockerfile ARG fragile | `Dockerfile.java:3` | Использовать явный путь к JAR |
| m7 | docker-compose missing web-ui | `docker-compose.yml` | Добавить web-ui service |
| m8 | No updated_at trigger for sprints | `V1__initial_schema.sql` | Добавить trigger |
| m9 | Missing WeightedCostFunction | domain-core/planning | Реализовать из спека |
| m10 | PlanningConstraintChecker incomplete | `PlanningConstraintChecker.java` | Добавить capacity, parallelism, deadline checks |
| m11 | DependencyValidator missing dependenciesSatisfied | `DependencyValidator.java` | Реализовать второй метод из спека |
| m12 | ConstraintViolation ≈ Conflict | planning package | Консолидировать или связать |

---

## Spec Compliance Scorecard

| Area | Score | Notes |
|------|-------|-------|
| Domain Core structure | 85% | Все entities, VOs, validators, planning interfaces present |
| Domain Core patterns | 90% | Records, sealed classes, pattern matching — correctly |
| Data Service structure | 35% | Нет service layer, DTO layer, mappers |
| Data Service API coverage | 55% | Core CRUD есть, много missing endpoints |
| Planning Engine | 5% | Только application class |
| Integration Hub | 20% | Interface + model records, нет коннекторов |
| Database schema | 90% | V1-V6 excellent, V7 missing |
| Security | 10% | Config есть, но permitAll |
| Infrastructure | 60% | docker-compose OK, CI/CD missing |
| Tests | 0% | Zero test files |
| **OVERALL** | **42%** | |

---

## Priority Order for Fixes

1. **C1** — Java version (5 min fix)
2. **C4** — Map.of() → HashMap (5 min fix)
3. **C5** — String enum comparison (5 min fix)
4. **C6** — Team.effectiveCapacityForSprint bug (15 min fix)
5. **C3** — DTO + mapper layer (2-3 hours)
6. **C2** — Service layer (3-4 hours)
7. **C6** — Domain-core unit tests (2 hours)
8. **C5** — V7 migration or remove (1 hour)
9. **M3** — N+1 fix (30 min)
10. **M8** — WebConfig, OpenApiConfig (30 min)
11. **M9** — @JsonIgnore or DTOs (covered by C3)
12. **M7** — Missing endpoints (2 hours)
13. **M1** — Planning Engine skeleton (4 hours)
14. **M2** — Integration Hub skeleton (4 hours)
15. **M12** — CI/CD (1 hour)
16. **C4** — Security foundation (2 hours)
17. Minor items (as time permits)
