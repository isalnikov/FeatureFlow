# Sprint 0 — Implementation Summary

> **Date:** 2026-05-15
> **Status:** ✅ BUILD SUCCESS
> **Branch:** main

---

## Что сделано

### 1. Maven Multi-Module Project
- Parent `pom.xml` с 4 модулями: `domain-core`, `data-service`, `planning-engine`, `integration-hub`
- Java 21 LTS, Spring Boot 3.4.1
- Dependency management для SpringDoc, Resilience4j

### 2. Domain Core (чистая Java, без Spring)
**Value Objects (8):**
- `Role` — enum: BACKEND, FRONTEND, QA, DEVOPS
- `EffortEstimate` — record с оценками по ролям, валидацией, арифметикой
- `ThreePointEstimate` — PERT формула, стандартное отклонение
- `Capacity` — эффективные часы с учётом focus factor и резервов
- `ClassOfService` — EXPEDITE, FIXED_DATE, STANDARD, FILLER
- `DateRange` — пересечения, содержит, длительность
- `CapacitySlot` — слот капасити с проверкой вместимости
- `AnnealingParameters`, `MonteCarloParameters` — параметры алгоритмов

**Entities (7):**
- `Product` — с tech stack и командами-владельцами
- `Team` — с members, capacity, expertise, calendar, velocity
- `TeamMember` — person, role, availability
- `FeatureRequest` — оценки, зависимости, экспертиза, deadline, class of service
- `Sprint` — даты, capacity overrides
- `PlanningWindow` — контейнер спринтов
- `Assignment` — feature + team + sprint + effort + status (lock/unlock)

**Validators (5):**
- `CapacityValidator` — проверка превышения капасити по ролям
- `DependencyValidator` — обнаружение циклов (Kahn's algorithm), topological sort
- `OwnershipValidator` — проверка владения продуктами, поиск candidate teams
- `ParallelismValidator` — лимит параллельных фич на команду
- `PlanningConstraintChecker` — aggregate constraint checker

**Planning Interfaces:**
- `PlanningEngine` — interface с plan() и planAsync()
- `PlanningRequest`, `PlanningResult`, `PlanningParameters`
- `CostFunction` — interface для целевой функции
- `Conflict`, `ConstraintViolation`, `FeatureTimeline`, `TeamLoadReport`

**Exceptions (sealed hierarchy):**
- `DomainException` → `CapacityExceededException`, `DependencyCycleException`, `OwnershipViolationException`

### 3. Data Service (Spring Boot 3.4)
**JPA Entities (7):**
- `ProductEntity`, `TeamEntity`, `TeamMemberEntity`
- `FeatureEntity` — JSONB для effort_estimate и stochastic_estimate
- `SprintEntity`, `PlanningWindowEntity`, `AssignmentEntity`

**Repositories (6):**
- `ProductRepository`, `TeamRepository`, `FeatureRepository` (с custom queries)
- `AssignmentRepository` (с custom queries для team/sprint/status)
- `PlanningWindowRepository`, `SprintRepository`

**Controllers (6):**
- `ProductController` — CRUD + teams
- `TeamController` — CRUD
- `FeatureController` — CRUD + pagination + bulk
- `AssignmentController` — CRUD + lock/unlock + bulk
- `PlanningWindowController` — CRUD + sprints management
- `DashboardController` — portfolio, team-load, conflicts, metrics

**Config:**
- `SecurityConfig` — permitAll для /api/v1/** (dev mode)
- `GlobalExceptionHandler` — ProblemDetail для EntityNotFoundException, DomainException, OptimisticLockingFailureException, Validation

### 4. Flyway Migrations (7 файлов)
| Миграция | Описание |
|----------|----------|
| `V1__initial_schema.sql` | 12 таблиц, 20+ индексов, triggers для updated_at |
| `V2__add_external_ids.sql` | external_id для интеграций с Jira/ADO/Linear |
| `V3__add_planning_results.sql` | planning_results table для snapshots |
| `V4__add_audit_columns.sql` | created_by, updated_by для audit trail |
| `V5__add_views.sql` | v_team_capacity, v_feature_summary, v_sprint_load |
| `V6__seed_data.sql` | 5 продуктов, 5 команд, 20 members, 12 спринтов |
| `V7__seed_features.sql` | 200+ фичей с зависимостями и product links |

### 5. Planning Engine (Spring Boot skeleton)
- `PlanningEngineApplication.java`
- `application.yml` с параметрами annealing, monte-carlo, weights

### 6. Integration Hub (Spring Boot skeleton)
- `IntegrationHubApplication.java`
- `TaskTrackerConnector` interface
- 12 model records: ExternalIssue, ExternalSprint, ExternalTeam, etc.
- `application.yml` с connector config

### 7. Infrastructure
- `docker-compose.yml` — PostgreSQL 16, Redis 7, data-service (8080), planning-engine (8081), integration-hub (8082)
- `docker/Dockerfile.java` — Eclipse Temurin 25 JDK

---

## Структура проекта

```
featureflow/
├── pom.xml                              # Parent POM
├── docker-compose.yml                   # Docker Compose
├── docker/Dockerfile.java               # Docker image
├── domain-core/                         # Pure Java domain library
│   └── src/main/java/com/featureflow/domain/
│       ├── entity/                      # 7 entities
│       ├── valueobject/                 # 8 value objects
│       ├── rules/                       # 5 validators
│       ├── planning/                    # 8 planning types
│       └── exception/                   # 4 exceptions
├── data-service/                        # Spring Boot CRUD API
│   └── src/main/
│       ├── java/com/featureflow/data/
│       │   ├── DataServiceApplication.java
│       │   ├── controller/              # 6 controllers
│       │   ├── entity/                  # 7 JPA entities
│       │   ├── repository/              # 6 repositories
│       │   └── config/                  # Security, ExceptionHandler
│       └── resources/
│           ├── application.yml
│           └── db/migration/            # V1-V7 Flyway migrations
├── planning-engine/                     # Spring Boot algorithms
│   └── src/main/java/com/featureflow/planning/
│       └── PlanningEngineApplication.java
└── integration-hub/                     # Spring Boot connectors
    └── src/main/java/com/featureflow/integration/
        ├── IntegrationHubApplication.java
        ├── connector/TaskTrackerConnector.java
        └── model/                       # 12 records
```

## Сборка

```bash
mvn clean package -DskipTests
# BUILD SUCCESS — все 4 модуля
```

## Запуск

```bash
docker-compose up -d
# PostgreSQL: localhost:5432
# Redis: localhost:6379
# Data Service API: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
```

## API Endpoints (data-service)

| Метод | Path | Описание |
|-------|------|----------|
| GET | `/api/v1/products` | Список продуктов |
| POST | `/api/v1/products` | Создать продукт |
| GET | `/api/v1/teams` | Список команд |
| POST | `/api/v1/teams` | Создать команду |
| GET | `/api/v1/features` | Список фич (paginated, filterable) |
| POST | `/api/v1/features` | Создать фичу |
| GET | `/api/v1/assignments` | Список назначений (filterable) |
| POST | `/api/v1/assignments` | Создать назначение |
| PUT | `/api/v1/assignments/{id}/lock` | Lock assignment |
| GET | `/api/v1/planning-windows` | Planning windows |
| GET | `/api/v1/dashboard/portfolio` | Portfolio overview |

## DB Schema Summary

| Таблица | Записей (seed) | Описание |
|---------|----------------|----------|
| products | 5 | Продукты с tech stack |
| teams | 5 | Команды с capacity |
| team_members | 20 | Члены команд по ролям |
| team_expertise | 10 | Теги экспертизы |
| product_teams | 10 | Связь продукт-команда |
| features | 200+ | Фичи с оценками и зависимостями |
| feature_products | 200+ | Связь фича-продукт |
| feature_dependencies | 30+ | Зависимости между фичами |
| planning_windows | 1 | Q1-Q2 2025 |
| sprints | 12 | 2-недельные спринты |
| assignments | 0 | Назначения (заполняются алгоритмом) |
| planning_results | 0 | Snapshots результатов |

## Следующие шаги

- **Sprint 1:** GreedyPlanner, SimulatedAnnealing, MonteCarlo алгоритмы
- **Sprint 2:** DTOs, mappers, service layer для data-service
- **Sprint 3:** Jira connector, ImportService/ExportService
- **Sprint 4:** Web UI — React приложение с Gantt chart
