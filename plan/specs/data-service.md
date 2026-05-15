# Data Service — Спецификация

> Spring Boot 3.4 микросервис. CRUD API, BFF, управление сущностями.
> PostgreSQL через Spring Data JPA.

---

## 1. Структура пакетов

```
com.featureflow.data/
├── DataServiceApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── OpenApiConfig.java
├── controller/
│   ├── ProductController.java
│   ├── TeamController.java
│   ├── FeatureController.java
│   ├── PlanningWindowController.java
│   ├── AssignmentController.java
│   ├── SprintController.java
│   └── DashboardController.java
├── service/
│   ├── ProductService.java
│   ├── TeamService.java
│   ├── FeatureService.java
│   ├── PlanningWindowService.java
│   ├── AssignmentService.java
│   └── DashboardService.java
├── repository/
│   ├── ProductRepository.java
│   ├── TeamRepository.java
│   ├── FeatureRepository.java
│   ├── PlanningWindowRepository.java
│   ├── AssignmentRepository.java
│   └── SprintRepository.java
├── entity/
│   ├── ProductEntity.java
│   ├── TeamEntity.java
│   ├── TeamMemberEntity.java
│   ├── FeatureEntity.java
│   ├── PlanningWindowEntity.java
│   ├── SprintEntity.java
│   └── AssignmentEntity.java
├── mapper/
│   └── EntityMapper.java
└── dto/
    ├── ProductDto.java
    ├── TeamDto.java
    ├── FeatureDto.java
    └── ...
```

---

## 2. REST API Endpoints

### 2.1 Products

```
GET     /api/v1/products              — List all products
GET     /api/v1/products/{id}         — Get product by ID
POST    /api/v1/products              — Create product
PUT     /api/v1/products/{id}         — Update product
DELETE  /api/v1/products/{id}         — Delete product
GET     /api/v1/products/{id}/teams   — Get teams owning product
```

### 2.2 Teams

```
GET     /api/v1/teams                 — List all teams
GET     /api/v1/teams/{id}            — Get team by ID
POST    /api/v1/teams                 — Create team
PUT     /api/v1/teams/{id}            — Update team
DELETE  /api/v1/teams/{id}            — Delete team
GET     /api/v1/teams/{id}/capacity   — Get team capacity for sprint
PUT     /api/v1/teams/{id}/members    — Update team members
GET     /api/v1/teams/{id}/load       — Get current team load
```

### 2.3 Features

```
GET     /api/v1/features              — List features (with pagination, filtering)
GET     /api/v1/features/{id}         — Get feature by ID
POST    /api/v1/features              — Create feature
PUT     /api/v1/features/{id}         — Update feature
DELETE  /api/v1/features/{id}         — Delete feature
POST    /api/v1/features/bulk         — Bulk create/update
GET     /api/v1/features/{id}/deps    — Get feature dependencies
PUT     /api/v1/features/{id}/deps    — Update dependencies
GET     /api/v1/features/import/jira  — Import from Jira
```

### 2.4 Planning Windows & Sprints

```
GET     /api/v1/planning-windows                    — List planning windows
POST    /api/v1/planning-windows                    — Create planning window
GET     /api/v1/planning-windows/{id}               — Get window
PUT     /api/v1/planning-windows/{id}               — Update window
GET     /api/v1/planning-windows/{id}/sprints       — Get sprints in window
POST    /api/v1/planning-windows/{id}/sprints       — Add sprint
PUT     /api/v1/sprints/{id}                        — Update sprint
```

### 2.5 Assignments

```
GET     /api/v1/assignments                         — List assignments (filterable)
POST    /api/v1/assignments                         — Create assignment
PUT     /api/v1/assignments/{id}                    — Update assignment
DELETE  /api/v1/assignments/{id}                    — Delete assignment
PUT     /api/v1/assignments/{id}/lock               — Lock assignment
PUT     /api/v1/assignments/{id}/unlock             — Unlock assignment
POST    /api/v1/assignments/bulk                    — Bulk update
```

### 2.6 Dashboard

```
GET     /api/v1/dashboard/portfolio                 — Portfolio overview
GET     /api/v1/dashboard/timeline                  — Roadmap timeline
GET     /api/v1/dashboard/team-load                 — Team load heatmap
GET     /api/v1/dashboard/conflicts                 — Active conflicts
GET     /api/v1/dashboard/metrics                   — KPI metrics
```

---

## 3. Entity Examples

### 3.1 ProductEntity

```java
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_tech_stack", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "technology")
    private Set<String> technologyStack = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "product_teams",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<TeamEntity> teams = new HashSet<>();

    @Version
    private Integer version;
}
```

### 3.2 TeamEntity

```java
@Entity
@Table(name = "teams")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMemberEntity> members = new ArrayList<>();

    @Column
    private Double focusFactor = 0.7;

    @Column
    private Double bugReservePercent = 0.20;

    @Column
    private Double techDebtReservePercent = 0.10;

    @Column
    private Double velocity;  // story points per sprint

    @ElementCollection
    @CollectionTable(name = "team_expertise", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "expertise_tag")
    private Set<String> expertiseTags = new HashSet<>();

    @Version
    private Integer version;
}
```

### 3.3 TeamMemberEntity

```java
@Entity
@Table(name = "team_members")
public class TeamMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Column(nullable = false)
    private UUID personId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Double availabilityPercent = 1.0;
}
```

### 3.4 FeatureEntity

```java
@Entity
@Table(name = "features")
public class FeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double businessValue;

    private UUID requestorId;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassOfService classOfService = ClassOfService.STANDARD;

    @ManyToMany
    @JoinTable(
        name = "feature_products",
        joinColumns = @JoinColumn(name = "feature_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<ProductEntity> products = new HashSet<>();

    @Embedded
    private EffortEstimateEmbeddable effortEstimate;

    @Embedded
    private ThreePointEstimateEmbeddable stochasticEstimate;

    @ManyToMany
    @JoinTable(
        name = "feature_dependencies",
        joinColumns = @JoinColumn(name = "feature_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_id")
    )
    private Set<FeatureEntity> dependencies = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "feature_required_expertise", joinColumns = @JoinColumn(name = "feature_id"))
    @Column(name = "expertise_tag")
    private Set<String> requiredExpertise = new HashSet<>();

    @Column(nullable = false)
    private Boolean canSplit = false;

    @Version
    private Integer version;
}
```

---

## 4. Repository Examples

```java
public interface FeatureRepository extends JpaRepository<FeatureEntity, UUID> {
    
    @Query("SELECT f FROM FeatureEntity f LEFT JOIN FETCH f.products WHERE f.classOfService = :cos")
    List<FeatureEntity> findByClassOfService(@Param("cos") ClassOfService cos);
    
    @Query("SELECT f FROM FeatureEntity f WHERE f.deadline <= :date AND f.deadline IS NOT NULL")
    List<FeatureEntity> findFeaturesWithDeadlineBefore(@Param("date") LocalDate date);
    
    @Query(value = """
        SELECT f.* FROM features f
        JOIN feature_products fp ON f.id = fp.feature_id
        WHERE fp.product_id = :productId
        """, nativeQuery = true)
    List<FeatureEntity> findByProductId(@Param("productId") UUID productId);
    
    Page<FeatureEntity> findAll(Pageable pageable);
}

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {
    
    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.teamId = :teamId AND a.sprintId = :sprintId
        AND a.status <> 'COMPLETED'
        """)
    List<AssignmentEntity> findActiveByTeamAndSprint(
        @Param("teamId") UUID teamId,
        @Param("sprintId") UUID sprintId
    );
    
    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.featureId = :featureId
        ORDER BY a.sprint.startDate
        """)
    List<AssignmentEntity> findByFeatureIdOrderBySprint(
        @Param("featureId") UUID featureId
    );
    
    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.status = 'LOCKED'
        """)
    List<AssignmentEntity> findLockedAssignments();
}
```

---

## 5. Service Layer

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureService {

    private final FeatureRepository repository;
    private final EntityMapper mapper;

    public Page<FeatureDto> listFeatures(FeatureFilter filter, Pageable pageable) {
        // Apply filters: classOfService, deadline range, product, etc.
        return repository.findAll(pageable).map(mapper::toFeatureDto);
    }

    public FeatureDto getFeature(UUID id) {
        return repository.findById(id)
            .map(mapper::toFeatureDto)
            .orElseThrow(() -> new EntityNotFoundException("Feature", id));
    }

    @Transactional
    public FeatureDto createFeature(CreateFeatureRequest request) {
        var entity = mapper.toFeatureEntity(request);
        var saved = repository.save(entity);
        return mapper.toFeatureDto(saved);
    }

    @Transactional
    public FeatureDto updateFeature(UUID id, UpdateFeatureRequest request) {
        var entity = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Feature", id));
        
        mapper.updateFeatureEntity(entity, request);
        var saved = repository.save(entity);
        return mapper.toFeatureDto(saved);
    }

    @Transactional
    public void deleteFeature(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Feature", id);
        }
        repository.deleteById(id);
    }
}
```

---

## 6. Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/**").authenticated()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

### RBAC

| Роль | Доступ |
|------|--------|
| ADMIN | Полный доступ ко всему |
| DELIVERY_MANAGER | CRUD фич, команд, запуск планирования |
| PRODUCT_OWNER | CRUD фич, просмотр планов |
| TEAM_LEAD | Обновление капасити, состава команды |

---

## 7. Error Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(OptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Concurrent modification");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }
}
```

---

## 8. Конфигурация

```yaml
# application.yml
server:
  port: 8080
  tomcat:
    threads:
      max: 200

spring:
  threads:
    virtual:
      enabled: true
  datasource:
    url: jdbc:postgresql://localhost:5432/featureflow
    username: ff
    password: ff_secret
    hikari:
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  flyway:
    enabled: true
    locations: classpath:db/migration

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
```

---

## 9. Тестовые сценарии

| Тест | Описание |
|------|----------|
| ProductCrudTest | CRUD операции с продуктами |
| TeamCapacityTest | Расчёт капасити команды |
| FeatureDependencyTest | Создание и валидация зависимостей |
| AssignmentLockTest | Lock/unlock назначений |
| DashboardPortfolioTest | Агрегация данных для дашборда |
| ConcurrentUpdateTest | Optimistic locking при параллельном обновлении |
| PaginationTest | Пагинация и фильтрация фич |
