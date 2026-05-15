# Integration Hub — Спецификация

> Spring Boot 3.4 микросервис. Коннекторы к внешним трекерам задач.
> Модульная система коннекторов с единым интерфейсом.

---

## 1. Структура пакетов

```
com.featureflow.integration/
├── IntegrationHubApplication.java
├── config/
│   └── IntegrationConfig.java
├── controller/
│   └── IntegrationController.java
├── connector/
│   ├── TaskTrackerConnector.java       # Interface
│   ├── jira/
│   │   ├── JiraConnector.java
│   │   ├── JiraClient.java
│   │   ├── JiraConfig.java
│   │   └── model/
│   │       ├── JiraIssue.java
│   │       ├── JiraProject.java
│   │       └── JiraSprint.java
│   ├── ado/
│   │   ├── AdoConnector.java
│   │   ├── AdoClient.java
│   │   └── model/
│   │       ├── AdoWorkItem.java
│   │       └── AdoSprint.java
│   └── linear/
│       ├── LinearConnector.java
│       ├── LinearClient.java
│       └── model/
│           ├── LinearIssue.java
│           └── LinearTeam.java
├── service/
│   ├── ImportService.java
│   ├── ExportService.java
│   └── SyncOrchestrator.java
├── model/
│   ├── ExternalIssue.java              # Unified model
│   ├── ImportResult.java
│   ├── ExportResult.java
│   └── SyncStatus.java
├── mapper/
│   └── ExternalIssueMapper.java
└── exception/
    ├── IntegrationException.java
    └── ConnectorNotFoundException.java
```

---

## 2. Connector Interface

```java
public interface TaskTrackerConnector {
    
    /**
     * Тип трекера (jira, ado, linear).
     */
    String getType();
    
    /**
     * Проверка подключения.
     */
    boolean testConnection(ConnectorConfig config);
    
    /**
     * Импорт фич из трекера.
     */
    List<ExternalIssue> importFeatures(
        ConnectorConfig config,
        ImportFilter filter
    );
    
    /**
     * Импорт спринтов.
     */
    List<ExternalSprint> importSprints(
        ConnectorConfig config,
        String projectId
    );
    
    /**
     * Импорт команд.
     */
    List<ExternalTeam> importTeams(
        ConnectorConfig config,
        String projectId
    );
    
    /**
     * Экспорт назначений обратно в трекер (создание задач).
     */
    ExportResult exportAssignments(
        ConnectorConfig config,
        List<AssignmentExport> assignments
    );
    
    /**
     * Обновление статусов задач.
     */
    void updateStatuses(
        ConnectorConfig config,
        List<StatusUpdate> updates
    );
}
```

### Unified Models

```java
public record ExternalIssue(
    String externalId,
    String key,              // e.g., "PROJ-123"
    String title,
    String description,
    String status,
    String issueType,        // epic, story, task, bug
    String priority,
    List<String> labels,
    String assignee,
    List<String> componentNames,
    ExternalEstimate estimate,
    List<String> dependencyKeys,
    LocalDate created,
    LocalDate updated,
    LocalDate dueDate
) {}

public record ExternalEstimate(
    Double storyPoints,
    Double originalEstimateHours,
    Double remainingEstimateHours
) {}

public record ExternalSprint(
    String externalId,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    String state,            // active, future, closed
    String goal
) {}

public record ExternalTeam(
    String externalId,
    String name,
    List<ExternalTeamMember> members
) {}

public record ExternalTeamMember(
    String accountId,
    String displayName,
    String role              // derived from groups/roles in tracker
) {}

public record AssignmentExport(
    String externalIssueKey,
    String targetSprintName,
    String assignee,
    String status
) {}

public record StatusUpdate(
    String externalIssueKey,
    String newStatus,
    String comment
) {}
```

---

## 3. Jira Connector

### 3.1 JiraClient

```java
@Component
@RequiredArgsConstructor
public class JiraClient {

    private final WebClient.Builder webClientBuilder;
    private final JiraConfig config;

    private WebClient jiraWebClient(ConnectorConfig connectorConfig) {
        return webClientBuilder
            .baseUrl(connectorConfig.getBaseUrl())
            .defaultHeaders(headers -> {
                if (connectorConfig.getAuthToken() != null) {
                    headers.setBearerAuth(connectorConfig.getAuthToken());
                } else {
                    String auth = connectorConfig.getUsername() + ":" + connectorConfig.getApiToken();
                    headers.setBasicAuth(
                        Base64.getEncoder().encodeToString(auth.getBytes())
                    );
                }
            })
            .filter(ExchangeFilterFunctions.retry(3))
            .build();
    }

    public Flux<JiraIssue> searchIssues(String jql, int maxResults) {
        return jiraWebClient(config)
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/api/3/search")
                .queryParam("jql", jql)
                .queryParam("maxResults", maxResults)
                .queryParam("fields", "summary,description,status,priority,labels," +
                    "assignee,components,customfield_10016,customfield_10014," +
                    "issuelinks,created,updated,duedate,project")
                .build()
            )
            .retrieve()
            .bodyToMono(JiraSearchResponse.class)
            .flux()
            .flatMapMany(resp -> Flux.fromIterable(resp.issues()));
    }

    public Flux<JiraSprint> getSprints(String boardId) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/agile/1.0/board/{boardId}/sprint", boardId)
            .retrieve()
            .bodyToMono(JiraSprintResponse.class)
            .flux()
            .flatMapMany(resp -> Flux.fromIterable(resp.sprints()));
    }

    public Mono<JiraIssue> getIssue(String issueKey) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .retrieve()
            .bodyToMono(JiraIssue.class);
    }

    public Mono<Void> updateIssue(String issueKey, UpdateIssueRequest request) {
        return jiraWebClient(config)
            .put()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class);
    }

    public Mono<JiraIssue> createIssue(CreateIssueRequest request) {
        return jiraWebClient(config)
            .post()
            .uri("/rest/api/3/issue")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(JiraIssue.class);
    }
}
```

### 3.2 JiraConnector

```java
@Component
@RequiredArgsConstructor
public class JiraConnector implements TaskTrackerConnector {

    private final JiraClient jiraClient;
    private final ExternalIssueMapper mapper;

    @Override
    public String getType() {
        return "jira";
    }

    @Override
    public boolean testConnection(ConnectorConfig config) {
        try {
            jiraClient.getMyself(config).block(Duration.ofSeconds(10));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ExternalIssue> importFeatures(ConnectorConfig config, ImportFilter filter) {
        String jql = buildJql(filter);
        return jiraClient.searchIssues(jql, filter.maxResults())
            .map(mapper::toExternalIssue)
            .collectList()
            .block(Duration.ofMinutes(5));
    }

    @Override
    public List<ExternalSprint> importSprints(ConnectorConfig config, String projectId) {
        // Get board for project, then get sprints
        return jiraClient.getSprints(projectId)
            .map(mapper::toExternalSprint)
            .collectList()
            .block(Duration.ofMinutes(2));
    }

    @Override
    public ExportResult exportAssignments(
        ConnectorConfig config,
        List<AssignmentExport> assignments
    ) {
        List<ExportResult.Entry> results = new ArrayList<>();
        
        for (AssignmentExport assignment : assignments) {
            try {
                // Update issue's sprint and assignee
                var updateRequest = new UpdateIssueRequest(
                    Map.of("sprint", assignment.targetSprintName()),
                    Map.of("assignee", Map.of("name", assignment.assignee()))
                );
                jiraClient.updateIssue(assignment.externalIssueKey(), updateRequest)
                    .block(Duration.ofSeconds(30));
                results.add(new ExportResult.Entry(
                    assignment.externalIssueKey(), true, null
                ));
            } catch (Exception e) {
                results.add(new ExportResult.Entry(
                    assignment.externalIssueKey(), false, e.getMessage()
                ));
            }
        }
        
        return new ExportResult(results);
    }

    private String buildJql(ImportFilter filter) {
        StringBuilder jql = new StringBuilder();
        jql.append("project = ").append(filter.projectKey());
        
        if (filter.issueTypes() != null && !filter.issueTypes().isEmpty()) {
            jql.append(" AND issuetype IN (").append(String.join(",", filter.issueTypes())).append(")");
        }
        
        if (filter.statuses() != null && !filter.statuses().isEmpty()) {
            jql.append(" AND status IN (").append(String.join(",", filter.statuses())).append(")");
        }
        
        if (filter.sinceDate() != null) {
            jql.append(" AND updated >= '").append(filter.sinceDate()).append("'");
        }
        
        return jql.toString();
    }
}
```

---

## 4. Import/Export Service

### 4.1 ImportService

```java
@Service
@RequiredArgsConstructor
public class ImportService {

    private final Map<String, TaskTrackerConnector> connectors;
    private final FeatureRepository featureRepository;
    private final TeamRepository teamRepository;
    private final SprintRepository sprintRepository;

    @Transactional
    public ImportResult importFromTracker(
        String connectorType,
        ConnectorConfig config,
        ImportFilter filter
    ) {
        var connector = connectors.get(connectorType);
        if (connector == null) {
            throw new ConnectorNotFoundException(connectorType);
        }

        // Import features
        var externalIssues = connector.importFeatures(config, filter);
        int featuresCreated = 0;
        int featuresUpdated = 0;
        
        for (var issue : externalIssues) {
            var existing = featureRepository.findByExternalId(issue.externalId());
            if (existing.isPresent()) {
                updateFeature(existing.get(), issue);
                featuresUpdated++;
            } else {
                createFeature(issue);
                featuresCreated++;
            }
        }

        // Import sprints
        var externalSprints = connector.importSprints(config, filter.projectKey());
        int sprintsCreated = 0;
        for (var sprint : externalSprints) {
            if (!sprintRepository.existsByExternalId(sprint.externalId())) {
                createSprint(sprint);
                sprintsCreated++;
            }
        }

        // Import teams
        var externalTeams = connector.importTeams(config, filter.projectKey());
        int teamsCreated = 0;
        for (var team : externalTeams) {
            if (!teamRepository.existsByExternalId(team.externalId())) {
                createTeam(team);
                teamsCreated++;
            }
        }

        return new ImportResult(
            featuresCreated, featuresUpdated, sprintsCreated, teamsCreated
        );
    }
}
```

### 4.2 SyncOrchestrator

```java
@Service
@RequiredArgsConstructor
public class SyncOrchestrator {

    private final ImportService importService;
    private final ExportService exportService;
    private final ScheduledTaskRegistrar taskRegistrar;

    /**
     * Настраивает периодическую синхронизацию.
     */
    @Scheduled(fixedRateString = "${featureflow.integration.sync-interval:300000}")
    public void scheduledSync() {
        // For each configured connector, sync data
        // ...
    }

    /**
     * Webhook handler for real-time updates.
     */
    public void handleWebhook(String connectorType, WebhookPayload payload) {
        // Process real-time update from tracker
        // ...
    }
}
```

---

## 5. Integration Controller

```java
@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final ImportService importService;
    private final ExportService exportService;
    private final SyncOrchestrator syncOrchestrator;

    // POST /api/v1/integrations/{type}/import
    @PostMapping("/{type}/import")
    public ResponseEntity<ImportResult> importData(
        @PathVariable String type,
        @RequestBody ImportRequest request
    ) {
        var result = importService.importFromTracker(
            type, request.config(), request.filter()
        );
        return ResponseEntity.ok(result);
    }

    // POST /api/v1/integrations/{type}/export
    @PostMapping("/{type}/export")
    public ResponseEntity<ExportResult> exportData(
        @PathVariable String type,
        @RequestBody ExportRequest request
    ) {
        var result = exportService.exportToTracker(
            type, request.config(), request.assignments()
        );
        return ResponseEntity.ok(result);
    }

    // POST /api/v1/integrations/{type}/test
    @PostMapping("/{type}/test")
    public ResponseEntity<Map<String, Boolean>> testConnection(
        @PathVariable String type,
        @RequestBody ConnectorConfig config
    ) {
        boolean ok = importService.testConnection(type, config);
        return ResponseEntity.ok(Map.of("connected", ok));
    }

    // POST /api/v1/integrations/webhook/{type}
    @PostMapping("/webhook/{type}")
    public ResponseEntity<Void> handleWebhook(
        @PathVariable String type,
        @RequestBody WebhookPayload payload
    ) {
        syncOrchestrator.handleWebhook(type, payload);
        return ResponseEntity.accepted().build();
    }
}
```

---

## 6. Circuit Breaker & Retry

```java
@Configuration
public class IntegrationConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .slidingWindowSize(10)
                .build())
            .timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(30))
                .build())
            .build());
    }
}
```

---

## 7. Конфигурация

```yaml
# application.yml
server:
  port: 8082

spring:
  threads:
    virtual:
      enabled: true

featureflow:
  integration:
    sync-interval: 300000  # 5 minutes
    connectors:
      jira:
        enabled: true
        base-url: ${JIRA_BASE_URL:}
        auth-type: ${JIRA_AUTH_TYPE:basic}  # basic or bearer
    retry:
      max-attempts: 3
      backoff-delay: 1000
    circuit-breaker:
      failure-threshold: 50
      wait-duration: 60000
```

---

## 8. Тестовые сценарии

| Тест | Описание |
|------|----------|
| JiraConnector.importFeatures | Импорт фич из Jira с JQL |
| JiraConnector.testConnection | Проверка подключения |
| ImportService.createOrUpdate | Создание или обновление фич |
| ExportService.exportAssignments | Экспорт назначений в Jira |
| CircuitBreaker.openOnFailure | Circuit breaker открывается при ошибках |
| WebhookHandler.processUpdate | Обработка webhook от Jira |
| ExternalIssueMapper.mapping | Корректный маппинг Jira → ExternalIssue |
