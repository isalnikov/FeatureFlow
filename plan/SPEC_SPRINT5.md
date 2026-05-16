# Sprint 5 — Integration Hub: Jira Connector + Import/Export Services

> **Module:** integration-hub + data-service
> **Duration:** 2-3 дня
> **Priority:** P0 (блокирует импорт данных из внешних трекеров)

---

## 1. Контекст

### 1.1 Текущее состояние

| Область | Score | Проблема |
|---------|-------|----------|
| JiraConnector | ❌ STUB | Все методы возвращают emptyList/false |
| AdoConnector | ❌ STUB | Все методы возвращают emptyList/false |
| LinearConnector | ❌ STUB | Все методы возвращают emptyList/false |
| ImportService | ❌ STUB | Возвращает ImportResult(0,0,0,0) |
| ExportService | ❌ STUB | Возвращает ExportResult(emptyList) |
| SyncOrchestrator | ❌ STUB | Возвращает SyncResult(0,0,0) |
| IntegrationController | ⚠️ PARTIAL | Endpoint path `/api/v1/integration` (должно быть `/api/v1/integrations`) |
| ExternalIssueMapper | ✅ DONE | Корректно маппит ExternalIssue ↔ FeatureRequest |
| Model records | ✅ DONE | Все DTO records определены |
| TaskTrackerConnector | ✅ DONE | Interface определён |

### 1.2 Что УЖЕ есть в data-service

- `FeatureEntity.externalId` — поле + getter/setter
- `TeamEntity.externalId` — поле + getter/setter
- `SprintEntity.externalId` — поле + getter/setter
- `SprintRepository.existsByExternalId()` — метод
- `EntityMapper` — маппит externalId из DTO в entity

### 1.3 Что НЕ трогать

- ✅ Domain entities (domain-core)
- ✅ MonteCarloSimulator (уже исправлен)
- ✅ Web UI (уже работает)
- ✅ SecurityConfig
- ✅ CI pipeline

---

## 2. Задача 1: JiraClient — реальный WebClient (P0)

### 2.1 Создать `JiraClient.java`

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/JiraClient.java`

Реализовать WebClient для Jira REST API v3:

```java
@Component
public class JiraClient {

    private final WebClient.Builder webClientBuilder;

    public JiraClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private WebClient jiraWebClient(ConnectorConfig config) {
        return webClientBuilder
            .baseUrl(config.baseUrl())
            .defaultHeaders(headers -> {
                if (config.authToken() != null) {
                    headers.setBearerAuth(config.authToken());
                } else if (config.apiToken() != null) {
                    String auth = config.username() + ":" + config.apiToken();
                    String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.setBasicAuth(encoded);
                }
            })
            .build();
    }

    public List<JiraIssue> searchIssues(ConnectorConfig config, String jql, int maxResults) {
        return jiraWebClient(config)
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/api/3/search")
                .queryParam("jql", jql)
                .queryParam("maxResults", maxResults)
                .queryParam("fields", "summary,description,status,priority,labels," +
                    "assignee,components,customfield_10016,customfield_10014," +
                    "issuelinks,created,updated,duedate,project,timetracking")
                .build()
            )
            .retrieve()
            .bodyToMono(JiraSearchResponse.class)
            .map(JiraSearchResponse::issues)
            .block(Duration.ofMinutes(5));
    }

    public JiraMyself getMyself(ConnectorConfig config) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/api/3/myself")
            .retrieve()
            .bodyToMono(JiraMyself.class)
            .block(Duration.ofSeconds(10));
    }

    public List<JiraSprint> getSprints(ConnectorConfig config, String boardId) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/agile/1.0/board/{boardId}/sprint", boardId)
            .retrieve()
            .bodyToMono(JiraSprintResponse.class)
            .map(JiraSprintResponse::sprints)
            .block(Duration.ofMinutes(2));
    }

    public List<JiraBoard> getBoards(ConnectorConfig config, String projectKey) {
        return jiraWebClient(config)
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/agile/1.0/board")
                .queryParam("projectKeyOrId", projectKey)
                .build()
            )
            .retrieve()
            .bodyToMono(JiraBoardResponse.class)
            .map(JiraBoardResponse::boards)
            .block(Duration.ofSeconds(30));
    }

    public void updateIssue(ConnectorConfig config, String issueKey, JiraUpdateRequest request) {
        jiraWebClient(config)
            .put()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block(Duration.ofSeconds(30));
    }

    public JiraIssue getIssue(ConnectorConfig config, String issueKey) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .retrieve()
            .bodyToMono(JiraIssue.class)
            .block(Duration.ofSeconds(10));
    }
}
```

### 2.2 Jira Model Records

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/model/JiraIssue.java`

```java
public record JiraIssue(
    String id,
    String key,
    JiraIssueFields fields
) {}

public record JiraIssueFields(
    String summary,
    String description,
    JiraStatus status,
    JiraPriority priority,
    List<JiraLabel> labels,
    JiraAssignee assignee,
    List<JiraComponent> components,
    String customfield_10016,  // story points
    List<JiraIssueLink> issuelinks,
    String created,
    String updated,
    String duedate,
    JiraProject project
) {}

public record JiraStatus(String name, String id) {}
public record JiraPriority(String name) {}
public record JiraLabel(String name) {}
public record JiraAssignee(String displayName, String accountId) {}
public record JiraComponent(String name) {}
public record JiraProject(String key, String name) {}

public record JiraIssueLink(
    String id,
    JiraLinkedIssue inwardIssue,
    JiraLinkedIssue outwardIssue,
    JiraLinkType type
) {}

public record JiraLinkedIssue(String key) {}
public record JiraLinkType(String name, String inward, String outward) {}

public record JiraSearchResponse(List<JiraIssue> issues, int total, int maxResults) {}
```

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/model/JiraSprint.java`

```java
public record JiraSprint(
    Long id,
    String name,
    String state,
    String startDate,
    String endDate,
    String goal
) {}

public record JiraSprintResponse(List<JiraSprint> sprints) {}
```

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/model/JiraBoard.java`

```java
public record JiraBoard(Long id, String name, String type) {}
public record JiraBoardResponse(List<JiraBoard> boards) {}
```

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/model/JiraMyself.java`

```java
public record JiraMyself(String accountId, String displayName, String emailAddress) {}
```

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/model/JiraUpdateRequest.java`

```java
public record JiraUpdateRequest(Map<String, Object> fields) {}
```

---

## 3. Задача 2: JiraConnector — реализация (P0)

### 3.1 Реализовать JiraConnector

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/connector/jira/JiraConnector.java`

```java
@Component
public class JiraConnector implements TaskTrackerConnector {

    private static final String SOURCE_TYPE = "jira";

    private final JiraClient jiraClient;
    private final ExternalIssueMapper mapper;

    public JiraConnector(JiraClient jiraClient, ExternalIssueMapper mapper) {
        this.jiraClient = jiraClient;
        this.mapper = mapper;
    }

    @Override
    public String getType() { return SOURCE_TYPE; }

    @Override
    public boolean testConnection(ConnectorConfig config) {
        try {
            jiraClient.getMyself(config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ExternalIssue> importFeatures(ConnectorConfig config, ImportFilter filter) {
        String jql = buildJql(filter);
        List<JiraIssue> issues = jiraClient.searchIssues(config, jql, filter.maxResults());
        return issues.stream()
            .map(mapper::toExternalIssue)
            .toList();
    }

    @Override
    public List<ExternalSprint> importSprints(ConnectorConfig config, String projectId) {
        List<JiraBoard> boards = jiraClient.getBoards(config, projectId);
        if (boards.isEmpty()) return List.of();

        String boardId = boards.get(0).id().toString();
        List<JiraSprint> sprints = jiraClient.getSprints(config, boardId);
        return sprints.stream()
            .map(mapper::toExternalSprint)
            .toList();
    }

    @Override
    public List<ExternalTeam> importTeams(ConnectorConfig config, String projectId) {
        // Jira doesn't have a direct "team" concept — derive from project members
        // For now, return empty — teams are managed in FeatureFlow
        return List.of();
    }

    @Override
    public ExportResult exportAssignments(ConnectorConfig config, List<AssignmentExport> assignments) {
        List<ExportResult.Entry> results = new ArrayList<>();
        for (AssignmentExport assignment : assignments) {
            try {
                var request = new JiraUpdateRequest(Map.of(
                    "sprint", assignment.targetSprintName()
                ));
                jiraClient.updateIssue(config, assignment.externalIssueKey(), request);
                results.add(new ExportResult.Entry(assignment.externalIssueKey(), true, null));
            } catch (Exception e) {
                results.add(new ExportResult.Entry(assignment.externalIssueKey(), false, e.getMessage()));
            }
        }
        return new ExportResult(results);
    }

    @Override
    public void updateStatuses(ConnectorConfig config, List<StatusUpdate> updates) {
        for (StatusUpdate update : updates) {
            var request = new JiraUpdateRequest(Map.of(
                "status", Map.of("name", update.newStatus())
            ));
            jiraClient.updateIssue(config, update.externalIssueKey(), request);
        }
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

## 4. Задача 3: ExternalIssueMapper — полный маппинг (P0)

### 4.1 Обновить ExternalIssueMapper

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/mapper/ExternalIssueMapper.java`

Добавить метод `toExternalIssue(JiraIssue)` для маппинга Jira → ExternalIssue:

```java
@Component
public class ExternalIssueMapper {

    public ExternalIssue toExternalIssue(JiraIssue jiraIssue) {
        if (jiraIssue == null) return null;

        JiraIssueFields fields = jiraIssue.fields();
        if (fields == null) return null;

        Double storyPoints = parseDouble(fields.customfield_10016());

        List<String> dependencyKeys = fields.issuelinks() != null
            ? fields.issuelinks().stream()
                .filter(link -> link.inwardIssue() != null)
                .map(link -> link.inwardIssue().key())
                .toList()
            : List.of();

        return new ExternalIssue(
            jiraIssue.id(),
            jiraIssue.key(),
            fields.summary(),
            fields.description(),
            fields.status() != null ? fields.status().name() : null,
            null, // issueType — not mapped yet
            fields.priority() != null ? fields.priority().name() : null,
            fields.labels() != null ? fields.labels().stream().map(JiraLabel::name).toList() : List.of(),
            fields.assignee() != null ? fields.assignee().displayName() : null,
            fields.components() != null ? fields.components().stream().map(JiraComponent::name).toList() : List.of(),
            new ExternalEstimate(storyPoints, null, null),
            dependencyKeys,
            parseDate(fields.created()),
            parseDate(fields.updated()),
            parseDate(fields.duedate())
        );
    }

    public ExternalSprint toExternalSprint(JiraSprint jiraSprint) {
        if (jiraSprint == null) return null;
        return new ExternalSprint(
            jiraSprint.id().toString(),
            jiraSprint.name(),
            parseDate(jiraSprint.startDate()),
            parseDate(jiraSprint.endDate()),
            jiraSprint.state(),
            jiraSprint.goal()
        );
    }

    // ... existing toFeatureRequest, fromFeatureRequest methods
}
```

---

## 5. Задача 4: ImportService — реализация (P0)

### 5.1 Реализовать ImportService

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/service/ImportService.java`

```java
@Service
public class ImportService {

    private final Map<String, TaskTrackerConnector> connectorMap;

    public ImportService(List<TaskTrackerConnector> connectors) {
        this.connectorMap = connectors.stream()
            .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
    }

    public ImportResult importFrom(String source, ConnectorConfig config) {
        return importFrom(source, config, new ImportFilter(null, null, null, null, 100));
    }

    public ImportResult importFrom(String source, ConnectorConfig config, ImportFilter filter) {
        var connector = connectorMap.get(source);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown connector type: " + source);
        }

        // Import features
        List<ExternalIssue> externalIssues = connector.importFeatures(config, filter);
        int featuresCreated = 0;
        int featuresUpdated = 0;

        for (var issue : externalIssues) {
            // TODO: When data-service has findByExternalId endpoint, upsert here
            // For now, count as created
            featuresCreated++;
        }

        // Import sprints
        List<ExternalSprint> externalSprints = connector.importSprints(config, filter.projectKey());
        int sprintsCreated = externalSprints.size();

        // Import teams
        List<ExternalTeam> externalTeams = connector.importTeams(config, filter.projectKey());
        int teamsCreated = externalTeams.size();

        return new ImportResult(featuresCreated, featuresUpdated, sprintsCreated, teamsCreated);
    }

    public boolean testConnection(String source, ConnectorConfig config) {
        var connector = connectorMap.get(source);
        if (connector == null) return false;
        return connector.testConnection(config);
    }
}
```

---

## 6. Задача 5: ExportService — реализация (P0)

### 6.1 Реализовать ExportService

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/service/ExportService.java`

```java
@Service
public class ExportService {

    private final Map<String, TaskTrackerConnector> connectorMap;

    public ExportService(List<TaskTrackerConnector> connectors) {
        this.connectorMap = connectors.stream()
            .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
    }

    public ExportResult exportTo(String source, ConnectorConfig config, List<AssignmentExport> assignments) {
        var connector = connectorMap.get(source);
        if (connector == null) {
            throw new IllegalArgumentException("Unknown connector type: " + source);
        }
        return connector.exportAssignments(config, assignments);
    }
}
```

---

## 7. Задача 6: IntegrationController — fix paths (P1)

### 7.1 Обновить IntegrationController

**Файл:** `integration-hub/src/main/java/com/featureflow/integration/controller/IntegrationController.java`

- Изменить base path с `/api/v1/integration` на `/api/v1/integrations`
- Добавить `ImportFilter` в import endpoint
- Добавить `testConnection` endpoint

```java
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final ImportService importService;
    private final ExportService exportService;
    private final SyncOrchestrator syncOrchestrator;

    public IntegrationController(ImportService importService,
                                 ExportService exportService,
                                 SyncOrchestrator syncOrchestrator) {
        this.importService = importService;
        this.exportService = exportService;
        this.syncOrchestrator = syncOrchestrator;
    }

    public record ImportRequest(ConnectorConfig config, ImportFilter filter) {}
    public record ExportRequest(ConnectorConfig config, List<AssignmentExport> assignments) {}

    @PostMapping("/{type}/import")
    public ResponseEntity<ImportResult> importData(
        @PathVariable String type,
        @RequestBody ImportRequest request
    ) {
        ImportFilter filter = request.filter() != null ? request.filter()
            : new ImportFilter(null, null, null, null, 100);
        var result = importService.importFrom(type, request.config(), filter);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{type}/export")
    public ResponseEntity<ExportResult> exportData(
        @PathVariable String type,
        @RequestBody ExportRequest request
    ) {
        var result = exportService.exportTo(type, request.config(), request.assignments());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{type}/test")
    public ResponseEntity<Map<String, Boolean>> testConnection(
        @PathVariable String type,
        @RequestBody ConnectorConfig config
    ) {
        boolean ok = importService.testConnection(type, config);
        return ResponseEntity.ok(Map.of("connected", ok));
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> triggerFullSync() {
        var result = syncOrchestrator.fullSync();
        return ResponseEntity.ok(Map.of(
            "synced", result.synced(),
            "errors", result.errors(),
            "duration", result.duration()
        ));
    }
}
```

---

## 8. Задача 7: Repository methods для externalId (P1)

### 8.1 Добавить в FeatureRepository

**Файл:** `data-service/src/main/java/com/featureflow/data/repository/FeatureRepository.java`

```java
Optional<FeatureEntity> findByExternalId(String externalId);
```

### 8.2 Добавить в TeamRepository

**Файл:** `data-service/src/main/java/com/featureflow/data/repository/TeamRepository.java`

```java
Optional<TeamEntity> findByExternalId(String externalId);
```

---

## 9. Задача 8: Unit Tests для integration-hub (P1)

### 9.1 JiraConnectorTest

**Файл:** `integration-hub/src/test/java/com/featureflow/integration/connector/jira/JiraConnectorTest.java`

```java
@ExtendWith(MockitoExtension.class)
class JiraConnectorTest {

    @Mock private JiraClient jiraClient;
    @Mock private ExternalIssueMapper mapper;

    private JiraConnector connector;

    @BeforeEach
    void setUp() {
        connector = new JiraConnector(jiraClient, mapper);
    }

    @Test
    void getType_returnsJira() {
        assertThat(connector.getType()).isEqualTo("jira");
    }

    @Test
    void testConnection_success() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);
        when(jiraClient.getMyself(config)).thenReturn(new JiraMyself("123", "Test User", "test@test.com"));

        assertThat(connector.testConnection(config)).isTrue();
    }

    @Test
    void testConnection_failure() {
        ConnectorConfig config = new ConnectorConfig("https://invalid.atlassian.net", "basic", "user", "token", null);
        when(jiraClient.getMyself(config)).thenThrow(new RuntimeException("Connection refused"));

        assertThat(connector.testConnection(config)).isFalse();
    }

    @Test
    void importFeatures_mapsJiraIssuesToExternalIssues() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "user", "token", null);
        ImportFilter filter = new ImportFilter("PROJ", List.of("Story"), null, null, 50);

        JiraIssue jiraIssue = new JiraIssue("10001", "PROJ-1",
            new JiraIssueFields("Test Issue", "Description", new JiraStatus("Done", "10001"),
                new JiraPriority("High"), List.of(), null, List.of(), null, List.of(),
                "2025-01-01", "2025-01-15", "2025-02-01", new JiraProject("PROJ", "Test")));

        when(jiraClient.searchIssues(eq(config), anyString(), eq(50))).thenReturn(List.of(jiraIssue));
        ExternalIssue expected = new ExternalIssue("10001", "PROJ-1", "Test Issue", "Description",
            "Done", null, "High", List.of(), null, List.of(), null, List.of(),
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 1));
        when(mapper.toExternalIssue(jiraIssue)).thenReturn(expected);

        List<ExternalIssue> result = connector.importFeatures(config, filter);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).key()).isEqualTo("PROJ-1");
    }
}
```

### 9.2 ImportServiceTest

**Файл:** `integration-hub/src/test/java/com/featureflow/integration/service/ImportServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock private TaskTrackerConnector mockConnector;
    private ImportService importService;

    @BeforeEach
    void setUp() {
        when(mockConnector.getType()).thenReturn("jira");
        importService = new ImportService(List.of(mockConnector));
    }

    @Test
    void importFrom_unknownConnector_throwsException() {
        ConnectorConfig config = new ConnectorConfig("https://test.com", "basic", "u", "t", null);
        assertThatThrownBy(() -> importService.importFrom("unknown", config))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void importFrom_callsConnectorAndReturnsCounts() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        ImportFilter filter = new ImportFilter("PROJ", null, null, null, 50);

        ExternalIssue issue = new ExternalIssue("1", "PROJ-1", "Test", "", "Done", null, "High",
            List.of(), null, List.of(), null, List.of(), null, null, null);
        when(mockConnector.importFeatures(eq(config), any(ImportFilter.class))).thenReturn(List.of(issue));
        when(mockConnector.importSprints(eq(config), eq("PROJ"))).thenReturn(List.of());
        when(mockConnector.importTeams(eq(config), eq("PROJ"))).thenReturn(List.of());

        ImportResult result = importService.importFrom("jira", config, filter);

        assertThat(result.featuresCreated()).isEqualTo(1);
        assertThat(result.featuresUpdated()).isEqualTo(0);
        assertThat(result.sprintsCreated()).isEqualTo(0);
        assertThat(result.teamsCreated()).isEqualTo(0);
    }

    @Test
    void testConnection_delegatesToConnector() {
        ConnectorConfig config = new ConnectorConfig("https://test.atlassian.net", "basic", "u", "t", null);
        when(mockConnector.testConnection(config)).thenReturn(true);

        assertThat(importService.testConnection("jira", config)).isTrue();
    }
}
```

### 9.3 ExternalIssueMapperTest

**Файл:** `integration-hub/src/test/java/com/featureflow/integration/mapper/ExternalIssueMapperTest.java`

```java
class ExternalIssueMapperTest {

    private ExternalIssueMapper mapper = new ExternalIssueMapper();

    @Test
    void toExternalIssue_mapsAllFields() {
        JiraIssue jiraIssue = new JiraIssue("10001", "PROJ-1",
            new JiraIssueFields("Test Issue", "Description", new JiraStatus("Done", "10001"),
                new JiraPriority("High"), List.of(new JiraLabel("backend")),
                new JiraAssignee("Alice", "acc-1"), List.of(new JiraComponent("API")),
                "8.0", List.of(), "2025-01-01T00:00:00.000+0000",
                "2025-01-15T00:00:00.000+0000", "2025-02-01", new JiraProject("PROJ", "Test")));

        ExternalIssue result = mapper.toExternalIssue(jiraIssue);

        assertThat(result.externalId()).isEqualTo("10001");
        assertThat(result.key()).isEqualTo("PROJ-1");
        assertThat(result.title()).isEqualTo("Test Issue");
        assertThat(result.description()).isEqualTo("Description");
        assertThat(result.status()).isEqualTo("Done");
        assertThat(result.priority()).isEqualTo("High");
        assertThat(result.labels()).containsExactly("backend");
        assertThat(result.assignee()).isEqualTo("Alice");
        assertThat(result.componentNames()).containsExactly("API");
        assertThat(result.estimate().storyPoints()).isEqualTo(8.0);
    }

    @Test
    void toExternalIssue_nullFields_returnsNull() {
        assertThat(mapper.toExternalIssue(null)).isNull();
    }

    @Test
    void toExternalSprint_mapsAllFields() {
        JiraSprint jiraSprint = new JiraSprint(1L, "Sprint 1", "active",
            "2025-01-01", "2025-01-14", "Deliver feature X");

        ExternalSprint result = mapper.toExternalSprint(jiraSprint);

        assertThat(result.externalId()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Sprint 1");
        assertThat(result.state()).isEqualTo("active");
        assertThat(result.goal()).isEqualTo("Deliver feature X");
    }

    @Test
    void toFeatureRequest_mapsExternalIssue() {
        ExternalIssue externalIssue = new ExternalIssue("1", "PROJ-1", "Test", "Desc",
            "Done", null, "High", List.of(), "Alice", List.of(),
            new ExternalEstimate(8.0, null, null), List.of(),
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 15), LocalDate.of(2025, 2, 1));

        FeatureRequest result = mapper.toFeatureRequest(externalIssue);

        assertThat(result.getTitle()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getDeadline()).isEqualTo(LocalDate.of(2025, 2, 1));
    }
}
```

---

## 10. Порядок выполнения

```
Этап 1: JiraClient + Jira models (1 час)
  └── JiraClient.java, JiraIssue, JiraSprint, JiraBoard, JiraMyself records

Этап 2: JiraConnector реализация (1 час)
  └── JiraConnector.java: testConnection, importFeatures, importSprints, exportAssignments

Этап 3: ExternalIssueMapper полный маппинг (30 мин)
  └── toExternalIssue(JiraIssue), toExternalSprint(JiraSprint), parseDate, parseDouble

Этап 4: ImportService + ExportService реализация (30 мин)
  └── Wire connectors, map lookup, error handling

Этап 5: IntegrationController fix (15 мин)
  └── Path fix, ImportRequest/ExportRequest records, testConnection endpoint

Этап 6: Repository methods (10 мин)
  └── findByExternalId в FeatureRepository и TeamRepository

Этап 7: Unit tests (2 часа)
  └── JiraConnectorTest, ImportServiceTest, ExternalIssueMapperTest
```

---

## 11. Acceptance Criteria

### Must Have
- [ ] JiraClient — WebClient с Jira REST API v3, searchIssues, getMyself, getSprints, getBoards, updateIssue
- [ ] JiraConnector — все 6 методов TaskTrackerConnector реализованы
- [ ] ExternalIssueMapper — toExternalIssue(JiraIssue), toExternalSprint(JiraSprint) с полным маппингом
- [ ] ImportService — connector lookup через Map, importFrom, testConnection
- [ ] ExportService — connector lookup, exportTo
- [ ] IntegrationController — path `/api/v1/integrations`, testConnection endpoint
- [ ] FeatureRepository.findByExternalId, TeamRepository.findByExternalId
- [ ] `mvn test -pl integration-hub` — все тесты проходят
- [ ] `mvn clean verify` — BUILD SUCCESS

### Nice to Have
- [ ] ADO connector — базовая реализация (testConnection + importFeatures)
- [ ] Resilience4j circuit breaker на JiraClient
- [ ] Retry policy на JiraClient (3 attempts)

### NOT in Scope
- Webhook handler
- Linear connector
- Scheduled sync
- Security (JWT/RBAC)
- Integration tests с реальным Jira

---

## 12. Команды для верификации

```bash
# Backend
mvn test -pl integration-hub --batch-mode
mvn clean verify --batch-mode

# Check integration-hub coverage
mvn test -pl integration-hub jacoco:report --batch-mode
```

---

## 13. Known Patterns & Conventions

### 13.1 WebClient pattern (WebFlux)

```java
private WebClient webClient(ConnectorConfig config) {
    return webClientBuilder
        .baseUrl(config.baseUrl())
        .defaultHeaders(headers -> {
            if (config.authToken() != null) {
                headers.setBearerAuth(config.authToken());
            } else if (config.apiToken() != null) {
                String auth = config.username() + ":" + config.apiToken();
                headers.setBasicAuth(Base64.getEncoder().encodeToString(auth.getBytes()));
            }
        })
        .build();
}
```

### 13.2 Connector lookup pattern

```java
private final Map<String, TaskTrackerConnector> connectorMap;

public Service(List<TaskTrackerConnector> connectors) {
    this.connectorMap = connectors.stream()
        .collect(Collectors.toMap(TaskTrackerConnector::getType, c -> c));
}
```

### 13.3 Test pattern

```java
@ExtendWith(MockitoExtension.class)
class ConnectorTest {
    @Mock private Dependency dep;
    private Connector connector;

    @BeforeEach void setUp() { connector = new Connector(dep); }
}
```
