# Sprint 2 — Data Service: Tests, Refactoring & DTO Modernization

> **Module:** `data-service/`
> **Agent:** Backend Java/Spring Boot
> **Duration:** 2-3 дня
> **Priority:** P0 (критический риск — 48 файлов production кода без тестов)

---

## 1. Контекст

### 1.1 Текущее состояние

data-service — CRUD API для управления сущностями FeatureFlow. Работает на Spring Boot 3.5.0 + Java 25 + PostgreSQL 16.

| Компонент | Файлы | Статус |
|-----------|-------|--------|
| Controllers | 7 | ✅ Работают |
| Services | 6 | ✅ Работают |
| Repositories | 6 | ✅ Работают |
| DTOs | 18 | ⚠️ 17 mutable POJOs, 1 record |
| Entities (JPA) | 7 | ✅ Работают |
| Mapper | 1 (EntityMapper.java, 170 строк) | ✅ Работает |
| Config | 4 | ✅ Работают |
| **Тесты** | **2** | ❌ Только SprintService + SprintController |

### 1.2 Что уже сделано (НЕ трогать)

- ✅ SprintController уже использует SprintService (рефакторинг выполнен)
- ✅ SecurityConfig настроен с JWT + RBAC (ROLE-based access)
- ✅ Все сервисы имеют `@Transactional` boundaries
- ✅ EntityMapper ручной но корректный
- ✅ GlobalExceptionHandler с ProblemDetail
- ✅ Pagination в FeatureController
- ✅ Bulk operations в FeatureController и AssignmentController
- ✅ SprintServiceTest и SprintControllerTest уже существуют — использовать как образец

### 1.3 Проблемы которые нужно решить

| # | Проблема | Severity |
|---|----------|----------|
| 1 | **0 тестов для ProductService, TeamService, FeatureService, AssignmentService, PlanningWindowService** | CRITICAL |
| 2 | **0 тестов для ProductController, TeamController, FeatureController, AssignmentController, PlanningWindowController, DashboardController** | CRITICAL |
| 3 | **0 тестов для репозиториев** | HIGH |
| 4 | **17 DTOs — mutable POJOs вместо Java 25 records** | MAJOR |
| 5 | **GlobalExceptionHandler не устанавливает title/instance в ProblemDetail** | MINOR |
| 6 | **AssignmentService.findByStatus() принимает String вместо enum** | MINOR |
| 7 | **DashboardController работает напрямую с репозиториями (нет service layer)** | MINOR |
| 8 | **Missing endpoint: GET /dashboard/timeline** | MINOR |
| 9 | **Missing endpoint: PUT /teams/{id}/members** | MINOR |

---

## 2. Задача 1: Тесты для сервисов (P0)

### 2.1 ProductServiceTest

**Файл:** `data-service/src/test/java/com/featureflow/data/service/ProductServiceTest.java`

**Что тестировать:**

```
✅ create() — создаёт продукт, возвращает DTO с заполненным id
✅ getById() — находит существующий продукт
✅ getById() — выбрасывает EntityNotFoundException для несуществующего
✅ listAll() — возвращает все продукты
✅ update() — обновляет name, description, technologyStack
✅ update() — partial update (только name)
✅ update() — EntityNotFoundException
✅ delete() — удаляет продукт
✅ delete() — EntityNotFoundException
✅ getTeams() — возвращает команды продукта
✅ getTeams() — EntityNotFoundException
```

**Pattern:** Использовать `@ExtendWith(MockitoExtension.class)`, mock репозиториев и EntityMapper.

### 2.2 TeamServiceTest

**Файл:** `data-service/src/test/java/com/featureflow/data/service/TeamServiceTest.java`

```
✅ create() — создаёт команду
✅ getById() — находит / не находит
✅ listAll() — возвращает все
✅ update() — обновляет name, focusFactor, bugReservePercent, techDebtReservePercent, velocity, expertiseTags
✅ update() — partial update
✅ delete() — удаляет / EntityNotFoundException
✅ getCapacityForSprint() — возвращает map с aggregated effort
✅ getCapacityForSprint() — EntityNotFoundException
✅ getTeamLoad() — возвращает суммарную нагрузку
✅ getTeamLoad() — EntityNotFoundException
```

### 2.3 FeatureServiceTest

**Файл:** `data-service/src/test/java/com/featureflow/data/service/FeatureServiceTest.java`

```
✅ create() — создаёт фичу с productIds
✅ create() — создаёт фичу без продуктов
✅ getById() — находит / не находит
✅ listAll() — без pagination
✅ listAll(Pageable) — с pagination
✅ update() — обновляет все поля
✅ update() — partial update
✅ delete() — удаляет / EntityNotFoundException
✅ bulkCreate() — создаёт несколько фич
✅ getDependencies() — возвращает set UUIDs
✅ getDependencies() — EntityNotFoundException
✅ updateDependencies() — обновляет зависимости
✅ updateDependencies() — EntityNotFoundException
```

### 2.4 AssignmentServiceTest

**Файл:** `data-service/src/test/java/com/featureflow/data/service/AssignmentServiceTest.java`

```
✅ create() — создаёт assignment с feature/team/sprint
✅ create() — EntityNotFoundException для feature
✅ create() — EntityNotFoundException для team
✅ create() — EntityNotFoundException для sprint
✅ getById() — находит / не находит
✅ listAll() — возвращает все
✅ findByTeamId() — фильтрует по команде
✅ findBySprintId() — фильтрует по спринту
✅ findByStatus() — фильтрует по статусу
✅ update() — обновляет effort, status
✅ delete() — удаляет / EntityNotFoundException
✅ lock() — блокирует assignment
✅ unlock() — разблокирует
✅ bulkCreate() — создаёт несколько
```

### 2.5 PlanningWindowServiceTest

**Файл:** `data-service/src/test/java/com/featureflow/data/service/PlanningWindowServiceTest.java`

```
✅ create() — создаёт planning window
✅ getById() — находит / не находит
✅ listAll() — возвращает все
✅ delete() — удаляет / EntityNotFoundException
✅ addSprint() — добавляет спринт к окну
✅ addSprint() — EntityNotFoundException для окна
✅ removeSprint() — удаляет спринт
✅ getSprints() — возвращает спринты окна
✅ getSprints() — EntityNotFoundException
```

### 2.6 SprintServiceTest — УЖЕ СУЩЕСТВУЕТ

Файл `SprintServiceTest.java` уже есть. Проверить что покрывает:
- listByPlanningWindow()
- getById()
- create()
- update()
- delete()

Если недостаточно — дополнить.

---

## 3. Задача 2: Тесты для контроллеров (P0)

### 3.1 Pattern для всех @WebMvcTest

```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    // tests...
}
```

**Важно:** Каждый контроллер-тест мокает ТОЛЬКО свой сервис. Не использовать `@SpringBootTest`.

### 3.2 ProductControllerTest

```
✅ GET /api/v1/products — 200, list of products
✅ GET /api/v1/products/{id} — 200, product found
✅ GET /api/v1/products/{id} — 404, not found
✅ POST /api/v1/products — 201, created
✅ PUT /api/v1/products/{id} — 200, updated
✅ PUT /api/v1/products/{id} — 404, not found
✅ DELETE /api/v1/products/{id} — 204, deleted
✅ DELETE /api/v1/products/{id} — 404, not found
✅ GET /api/v1/products/{id}/teams — 200, list of teams
```

### 3.3 TeamControllerTest

```
✅ GET /api/v1/teams — 200
✅ GET /api/v1/teams/{id} — 200 / 404
✅ POST /api/v1/teams — 201
✅ PUT /api/v1/teams/{id} — 200 / 404
✅ DELETE /api/v1/teams/{id} — 204 / 404
✅ GET /api/v1/teams/{id}/capacity?sprintId= — 200
✅ GET /api/v1/teams/{id}/load?sprintId= — 200
```

### 3.4 FeatureControllerTest

```
✅ GET /api/v1/features — 200, page of features
✅ GET /api/v1/features?classOfService=STANDARD — filtered
✅ GET /api/v1/features/{id} — 200 / 404
✅ POST /api/v1/features — 201
✅ PUT /api/v1/features/{id} — 200 / 404
✅ DELETE /api/v1/features/{id} — 204 / 404
✅ POST /api/v1/features/bulk — 201
✅ GET /api/v1/features/{id}/deps — 200
✅ PUT /api/v1/features/{id}/deps — 204 / 404
```

### 3.5 AssignmentControllerTest

```
✅ GET /api/v1/assignments — 200, all
✅ GET /api/v1/assignments?teamId= — filtered by team
✅ GET /api/v1/assignments?sprintId= — filtered by sprint
✅ GET /api/v1/assignments?status=PLANNED — filtered by status
✅ POST /api/v1/assignments — 201
✅ PUT /api/v1/assignments/{id} — 200 / 404
✅ DELETE /api/v1/assignments/{id} — 204 / 404
✅ PUT /api/v1/assignments/{id}/lock — 200 / 404
✅ PUT /api/v1/assignments/{id}/unlock — 200 / 404
✅ POST /api/v1/assignments/bulk — 201
```

### 3.6 PlanningWindowControllerTest

```
✅ GET /api/v1/planning-windows — 200
✅ GET /api/v1/planning-windows/{id} — 200 / 404
✅ POST /api/v1/planning-windows — 201
✅ DELETE /api/v1/planning-windows/{id} — 204 / 404
✅ GET /api/v1/planning-windows/{id}/sprints — 200
✅ POST /api/v1/planning-windows/{id}/sprints — 201 / 404
```

### 3.7 DashboardControllerTest

```
✅ GET /api/v1/dashboard/portfolio — 200, returns counts
✅ GET /api/v1/dashboard/team-load — 200
✅ GET /api/v1/dashboard/conflicts — 200
✅ GET /api/v1/dashboard/metrics — 200
```

### 3.8 SprintControllerTest — УЖЕ СУЩЕСТВУЕТ

Проверить покрытие. Если недостаточно — дополнить:
```
✅ GET /api/v1/sprints?planningWindowId= — 200
✅ GET /api/v1/sprints?planningWindowId=null — 200, empty list
✅ GET /api/v1/sprints/{id} — 200
✅ POST /api/v1/sprints?planningWindowId= — 201
✅ PUT /api/v1/sprints/{id} — 200
✅ DELETE /api/v1/sprints/{id} — 204
```

---

## 4. Задача 3: Repository Tests (P1)

### 4.1 Pattern

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:tc:postgresql:16:///testdb"
})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    // tests...
}
```

Использовать **Testcontainers** для PostgreSQL (уже в pom.xml).

### 4.2 ProductRepositoryTest

```
✅ findAll() — returns all products
✅ findById() — finds by UUID
✅ existsById() — true/false
✅ deleteById() — removes entity
✅ save() — persists with generated UUID
✅ save() — @Version optimistic locking
```

### 4.3 TeamRepositoryTest

```
✅ findAll() — returns all
✅ findById() — finds by UUID
✅ findByProductId() — returns teams for a product
✅ save() — persists with members
```

### 4.4 FeatureRepositoryTest

```
✅ findAll() — returns all
✅ findById() — finds by UUID
✅ findAll(Pageable) — pagination works
✅ findByClassOfService() — filters by enum
✅ findByIdWithDependencies() — fetches dependencies eagerly
✅ countByClassOfService() — returns count
```

### 4.5 AssignmentRepositoryTest

```
✅ findByTeamId() — filters by team
✅ findBySprintId() — filters by sprint
✅ findByStatus() — filters by enum
✅ findByTeamAndSprint() — compound filter
✅ findDistinctActiveFeatureIds() — returns unique feature IDs
✅ findByFeatureId() — finds by feature
```

### 4.6 PlanningWindowRepositoryTest

```
✅ findAll() — returns all
✅ findById() — finds by UUID
✅ findByStartDateBetween() — date range query
```

### 4.7 SprintRepositoryTest

```
✅ findByPlanningWindowId() — filters by window
✅ findByPlanningWindowIdOrderByStartDate() — ordered
✅ findById() — finds by UUID
```

---

## 5. Задача 4: Конвертировать DTOs в Java 25 records (P1)

### 5.1 Правила конвертации

1. **Response DTOs** (те что возвращаются клиенту) → records
2. **Request DTOs** (те что приходят от клиента) → records
3. **Все nullable поля** → обернуть в `Optional` или оставить как-is (Jackson handles null)
4. **Jackson** автоматически сериализует/desериализует records
5. **EntityMapper** нужно обновить — вместо setters использовать constructor

### 5.2 Список DTOs для конвертации (17 штук)

**Response DTOs (6):**

| Текущий | Строк | Полей |
|---------|-------|-------|
| `ProductDto` | 36 | id, name, description, technologyStack, version, createdAt, updatedAt, createdBy, updatedBy |
| `TeamDto` | ~40 | id, name, focusFactor, bugReservePercent, techDebtReservePercent, velocity, expertiseTags, externalId, version, createdAt, updatedAt, createdBy, updatedBy |
| `FeatureDto` | ~50 | id, title, description, businessValue, requestorId, deadline, classOfService, effortEstimate, stochasticEstimate, requiredExpertise, canSplit, externalId, externalSource, version, createdAt, updatedAt, createdBy, updatedBy |
| `AssignmentDto` | ~30 | id, featureId, teamId, sprintId, allocatedEffort, status, version, createdAt, updatedAt |
| `PlanningWindowDto` | ~20 | id, startDate, endDate, version, createdAt |
| `SprintDto` | ~25 | id, planningWindowId, startDate, endDate, capacityOverrides, externalId |

**Request DTOs (8):**

| Текущий | Строк | Полей |
|---------|-------|-------|
| `CreateProductRequest` | 16 | name, description, technologyStack |
| `UpdateProductRequest` | ~20 | name, description, technologyStack |
| `CreateTeamRequest` | ~25 | name, focusFactor, bugReservePercent, techDebtReservePercent, expertiseTags, externalId |
| `UpdateTeamRequest` | ~30 | name, focusFactor, bugReservePercent, techDebtReservePercent, velocity, expertiseTags |
| `CreateFeatureRequest` | ~35 | title, description, businessValue, requestorId, deadline, classOfService, effortEstimate, stochasticEstimate, requiredExpertise, canSplit, externalId, externalSource, productIds |
| `UpdateFeatureRequest` | ~45 | Все поля CreateFeatureRequest (nullable) |
| `CreateSprintRequest` | ~20 | startDate, endDate, capacityOverrides, externalId |
| `UpdateSprintRequest` | ~20 | startDate, endDate, capacityOverrides, externalId |
| `CreatePlanningWindowRequest` | ~12 | startDate, endDate |
| `CreateAssignmentRequest` | ~15 | featureId, teamId, sprintId, allocatedEffort |
| `UpdateAssignmentRequest` | ~12 | allocatedEffort, status |

**Уже record (1):**
- `TeamMemberDto` — НЕ трогать, это образец

### 5.3 Пример конвертации

**Было (mutable POJO):**
```java
public class ProductDto {
    private UUID id;
    private String name;
    // ... 14+ полей с getters/setters
}
```

**Стало (immutable record):**
```java
public record ProductDto(
    UUID id,
    String name,
    String description,
    Set<String> technologyStack,
    Integer version,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}
```

**Request DTOs с @JsonInclude для nullable:**
```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpdateProductRequest(
    String name,
    String description,
    Set<String> technologyStack
) {}
```

### 5.4 Обновление EntityMapper

После конвертации DTOs в records, EntityMapper нужно обновить:

**Было:**
```java
public ProductDto toProductDto(ProductEntity entity) {
    ProductDto dto = new ProductDto();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    // ... setters
    return dto;
}
```

**Стало:**
```java
public ProductDto toProductDto(ProductEntity entity) {
    return new ProductDto(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getTechnologyStack(),
        entity.getVersion(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getCreatedBy(),
        entity.getUpdatedBy()
    );
}
```

### 5.5 Порядок конвертации

1. Сначала response DTOs (ProductDto, SprintDto, PlanningWindowDto — самые простые)
2. Потом request DTOs (CreateProductRequest, UpdateProductRequest — простые)
3. Потом сложные DTOs (FeatureDto, CreateFeatureRequest — много полей)
4. В конце AssignmentDto, CreateAssignmentRequest (зависят от entity relationships)

**После КАЖДОГО DTO запустить `mvn test -pl data-service` чтобы убедиться что ничего не сломалось.**

---

## 6. Задача 5: Minor fixes (P2)

### 6.1 GlobalExceptionHandler — добавить title/instance

**Файл:** `config/GlobalExceptionHandler.java`

```java
@ExceptionHandler(EntityNotFoundException.class)
public ProblemDetail handleNotFound(EntityNotFoundException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("Resource Not Found");
    problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
    return problem;
}
```

Применить ко всем 4 handler методам.

### 6.2 AssignmentService.findByStatus — использовать enum

**Файл:** `service/AssignmentService.java:64-68`

**Было:**
```java
public List<AssignmentDto> findByStatus(String status) {
    AssignmentStatus assignmentStatus = AssignmentStatus.valueOf(status);
    // ...
}
```

**Стало:**
```java
public List<AssignmentDto> findByStatus(AssignmentStatus status) {
    // ...
}
```

Обновить AssignmentController:
```java
// Было: service.findByStatus(status.name())
// Стало: service.findByStatus(status)
```

### 6.3 DashboardController — добавить /timeline endpoint

```java
@GetMapping("/timeline")
public ResponseEntity<List<Map<String, Object>>> timeline() {
    // Вернуть упрощённый timeline: спринты с датами и количеством assignments
    // Заглушка до реализации Sprint 3
    return ResponseEntity.ok(List.of());
}
```

---

## 7. Существующие файлы для образца

### 7.1 SprintServiceTest.java (уже есть)

Путь: `data-service/src/test/java/com/featureflow/data/service/SprintServiceTest.java`

Использовать как template для всех service тестов:
- `@ExtendWith(MockitoExtension.class)`
- Mock репозиториев и EntityMapper
- `@Test` методы с assertions через AssertJ

### 7.2 SprintControllerTest.java (уже есть)

Путь: `data-service/src/test/java/com/featureflow/data/controller/SprintControllerTest.java`

Использовать как template для всех controller тестов:
- `@WebMvcTest(SprintController.class)`
- `@MockitoBean private SprintService service`
- `MockMvc` для HTTP assertions
- `ObjectMapper` для сериализации request body

---

## 8. Зависимости и тестовые фреймворки

### 8.1 Уже в pom.xml (НЕ добавлять)

```xml
<!-- Test -->
spring-boot-starter-test          (JUnit 5, AssertJ, Mockito)
spring-security-test              (Security testing)
testcontainers:junit-jupiter      (Docker-based integration tests)
testcontainers:postgresql         (PostgreSQL container)
jacoco-maven-plugin               (Code coverage)
```

### 8.2 Annotations

| Annotation | Use |
|------------|-----|
| `@ExtendWith(MockitoExtension.class)` | Unit tests (service) |
| `@WebMvcTest(Controller.class)` | Controller tests |
| `@MockitoBean` | Mock beans in @WebMvcTest |
| `@DataJpaTest` | Repository tests |
| `@TestPropertySource` | Override properties for tests |

---

## 9. Acceptance Criteria

### 9.1 Minimum (must have)

- [ ] Все 5 сервисов покрыты unit тестами (минимум 10 тестов на сервис)
- [ ] Все 7 контроллеров покрыты @WebMvcTest (минимум 5 тестов на контроллер)
- [ ] Все 6 репозиториев покрыты @DataJpaTest (минимум 3 теста на репозиторий)
- [ ] `mvn test -pl data-service` проходит без ошибок
- [ ] JaCoCo coverage >= 70% для data-service
- [ ] CI pipeline (`mvn test`) проходит для data-service

### 9.2 Nice to have

- [ ] Все 17 DTOs конвертированы в records
- [ ] EntityMapper обновлён для работы с records
- [ ] GlobalExceptionHandler имеет title/instance
- [ ] AssignmentService.findByStatus использует enum
- [ ] GET /dashboard/timeline endpoint добавлен

### 9.3 NOT in scope

- [ ] Web UI (React)
- [ ] Integration Hub коннекторы
- [ ] Security JWT implementation (уже есть SecurityConfig)
- [ ] Docker image build/push
- [ ] Performance testing

---

## 10. Порядок выполнения

```
Этап 1: Service tests    (5 файлов, ~4-6 часов)
  ├── ProductServiceTest
  ├── TeamServiceTest
  ├── FeatureServiceTest
  ├── AssignmentServiceTest
  └── PlanningWindowServiceTest

Этап 2: Controller tests (7 файлов, ~4-6 часов)
  ├── ProductControllerTest
  ├── TeamControllerTest
  ├── FeatureControllerTest
  ├── AssignmentControllerTest
  ├── PlanningWindowControllerTest
  ├── DashboardControllerTest
  └── SprintControllerTest (дополнить)

Этап 3: Repository tests (6 файлов, ~2-3 часа)
  ├── ProductRepositoryTest
  ├── TeamRepositoryTest
  ├── FeatureRepositoryTest
  ├── AssignmentRepositoryTest
  ├── PlanningWindowRepositoryTest
  └── SprintRepositoryTest

Этап 4: DTO → records    (17 файлов + EntityMapper, ~2-3 часа)
  ├── Response DTOs (6 файлов)
  ├── Request DTOs (11 файлов)
  └── EntityMapper.java (обновить)

Этап 5: Minor fixes      (3 файла, ~30 мин)
  ├── GlobalExceptionHandler
  ├── AssignmentService
  └── DashboardController
```

---

## 11. Команды для верификации

```bash
# Запустить все тесты data-service
mvn test -pl data-service -am

# Запустить только service тесты
mvn test -pl data-service -Dtest="*ServiceTest"

# Запустить только controller тесты
mvn test -pl data-service -Dtest="*ControllerTest"

# Запустить только repository тесты
mvn test -pl data-service -Dtest="*RepositoryTest"

# Проверить coverage
mvn test -pl data-service -am && open data-service/target/site/jacoco/index.html

# Скомпилировать (проверить что records не ломают compilation)
mvn compile -pl data-service -am
```

---

## 12. Known Patterns & Conventions

### 12.1 Service layer pattern

```java
@Service
@Transactional(readOnly = true)
public class XxxService {
    private final XxxRepository repository;
    private final EntityMapper mapper;

    public XxxService(XxxRepository repository, EntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // read methods — readOnly (по умолчанию)
    // write methods — @Transactional (override)
}
```

### 12.2 Controller pattern

```java
@RestController
@RequestMapping("/api/v1/xxx")
public class XxxController {
    private final XxxService service;

    public XxxController(XxxService service) {
        this.service = service;
    }

    // GET → list/get
    // POST → create (201)
    // PUT → update (200)
    // DELETE → delete (204)
    // EntityNotFoundException → ResponseEntity.notFound()
}
```

### 12.3 Entity pattern

```java
@Entity
@Table(name = "xxx")
public class XxxEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    private Integer version;  // optimistic locking

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
```

### 12.4 Test data helpers

Для service тестов — создавать entity вручную:
```java
private ProductEntity product(String name) {
    ProductEntity entity = new ProductEntity();
    entity.setName(name);
    entity.setDescription("Test product");
    return entity;
}
```

Для controller тестов — создавать DTO вручную:
```java
private CreateProductRequest request(String name) {
    return new CreateProductRequest(name, "Test", Set.of("java"));
}
// Или после конвертации в record:
// new CreateProductRequest(name, "Test", Set.of("java"))
```

---

## 13. Важные замечания

1. **НЕ менять SecurityConfig** — он уже настроен с JWT + RBAC
2. **НЕ менять EntityMapper логику** — только обновить синтаксис для records
3. **НЕ менять JPA entities** — они работают корректно
4. **НЕ менять repositories** — они работают корректно
5. **НЕ менять application.yml** — конфигурация корректна
6. **Следовать существующим patterns** — использовать SprintServiceTest/SprintControllerTest как образец
7. **Каждый DTO → record конвертировать отдельно** — запускать тесты после каждого
8. **MockMvc в controller тестах** — НЕ использовать @SpringBootTest
9. **Testcontainers для repository тестов** — НЕ использовать H2
10. **AssertJ для assertions** — НЕ использовать JUnit assertions
