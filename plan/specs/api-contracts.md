# API Contracts — Спецификация

> OpenAPI 3.0 спецификации для всех сервисов.

---

## 1. Data Service API (port 8080)

### 1.1 Products

```
GET     /api/v1/products              — List all products
POST    /api/v1/products              — Create product
GET     /api/v1/products/{id}         — Get product by ID
PUT     /api/v1/products/{id}         — Update product
DELETE  /api/v1/products/{id}         — Delete product
GET     /api/v1/products/{id}/teams   — Get teams owning product
```

**POST /api/v1/products — Request:**
```json
{
  "name": "Backend API",
  "description": "Core backend services",
  "technologyStack": ["Java", "Spring Boot", "PostgreSQL"]
}
```

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Backend API",
  "description": "Core backend services",
  "technologyStack": ["Java", "Spring Boot", "PostgreSQL"],
  "teamIds": [],
  "version": 0
}
```

### 1.2 Features

```
GET     /api/v1/features              — List features (paginated, filterable)
POST    /api/v1/features              — Create feature
GET     /api/v1/features/{id}         — Get feature by ID
PUT     /api/v1/features/{id}         — Update feature
DELETE  /api/v1/features/{id}         — Delete feature
POST    /api/v1/features/bulk         — Bulk create/update
GET     /api/v1/features/{id}/deps    — Get feature dependencies
PUT     /api/v1/features/{id}/deps    — Update dependencies
```

**GET /api/v1/features — Query params:**
| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 20 | Page size |
| classOfService | string | — | EXPEDITE, FIXED_DATE, STANDARD, FILLER |
| productId | UUID | — | Filter by product |
| deadlineBefore | date | — | Features with deadline before date |
| sortBy | string | businessValue | Sort field |
| sortDir | string | desc | asc/desc |

**POST /api/v1/features — Request:**
```json
{
  "title": "Add OAuth2 login",
  "description": "Implement OAuth2 authentication with Google and GitHub",
  "businessValue": 85,
  "classOfService": "STANDARD",
  "productIds": ["550e8400-e29b-41d4-a716-446655440000"],
  "effortEstimate": {
    "backendHours": 40,
    "frontendHours": 16,
    "qaHours": 12,
    "devopsHours": 4
  },
  "stochasticEstimate": {
    "optimistic": 30,
    "mostLikely": 48,
    "pessimistic": 72
  },
  "dependencies": [],
  "requiredExpertise": ["oauth2", "security"],
  "canSplit": false
}
```

### 1.3 Teams

```
GET     /api/v1/teams                 — List all teams
POST    /api/v1/teams                 — Create team
GET     /api/v1/teams/{id}            — Get team by ID
PUT     /api/v1/teams/{id}            — Update team
DELETE  /api/v1/teams/{id}            — Delete team
GET     /api/v1/teams/{id}/capacity   — Get team capacity for sprint
PUT     /api/v1/teams/{id}/members    — Update team members
GET     /api/v1/teams/{id}/load       — Get current team load
```

**POST /api/v1/teams — Request:**
```json
{
  "name": "Team Alpha",
  "members": [
    { "personId": "...", "name": "Alice", "role": "BACKEND", "availabilityPercent": 1.0 },
    { "personId": "...", "name": "Bob", "role": "FRONTEND", "availabilityPercent": 1.0 },
    { "personId": "...", "name": "Charlie", "role": "QA", "availabilityPercent": 0.5 }
  ],
  "focusFactor": 0.7,
  "bugReservePercent": 0.20,
  "techDebtReservePercent": 0.10,
  "expertiseTags": ["legacy-billing", "k8s"]
}
```

### 1.4 Assignments

```
GET     /api/v1/assignments           — List assignments (filterable)
POST    /api/v1/assignments           — Create assignment
PUT     /api/v1/assignments/{id}      — Update assignment
DELETE  /api/v1/assignments/{id}      — Delete assignment
PUT     /api/v1/assignments/{id}/lock   — Lock assignment
PUT     /api/v1/assignments/{id}/unlock — Unlock assignment
POST    /api/v1/assignments/bulk      — Bulk update
```

**Query params for GET:** teamId, sprintId, featureId, status

### 1.5 Planning Windows & Sprints

```
GET     /api/v1/planning-windows                    — List planning windows
POST    /api/v1/planning-windows                    — Create planning window
GET     /api/v1/planning-windows/{id}               — Get window
PUT     /api/v1/planning-windows/{id}               — Update window
GET     /api/v1/planning-windows/{id}/sprints       — Get sprints in window
POST    /api/v1/planning-windows/{id}/sprints       — Add sprint
PUT     /api/v1/sprints/{id}                        — Update sprint
```

### 1.6 Dashboard

```
GET     /api/v1/dashboard/portfolio     — Portfolio overview
GET     /api/v1/dashboard/timeline      — Roadmap timeline data
GET     /api/v1/dashboard/team-load     — Team load heatmap
GET     /api/v1/dashboard/conflicts     — Active conflicts
GET     /api/v1/dashboard/metrics       — KPI metrics
```

**GET /api/v1/dashboard/portfolio — Response:**
```json
{
  "totalFeatures": 150,
  "plannedFeatures": 89,
  "avgTTM": 3.2,
  "avgUtilization": 78.5,
  "activeConflicts": 5,
  "deadlineRisk": 12.3,
  "teamLoadReports": [...],
  "conflicts": [...]
}
```

---

## 2. Planning Engine API (port 8081)

```
POST    /api/v1/planning/run                    — Run planning algorithm (202 Accepted)
GET     /api/v1/planning/jobs/{jobId}/status    — Get job status
GET     /api/v1/planning/jobs/{jobId}/result    — Get planning result
GET     /api/v1/planning/jobs/{jobId}/sse       — SSE stream for real-time status
POST    /api/v1/planning/validate               — Validate without running
```

**POST /api/v1/planning/run — Request:**
```json
{
  "featureIds": ["...", "..."],
  "teamIds": ["...", "..."],
  "planningWindowId": "...",
  "parameters": {
    "w1Ttm": 1.0,
    "w2Underutilization": 0.5,
    "w3DeadlinePenalty": 2.0,
    "maxParallelFeatures": 3,
    "annealing": {
      "initialTemperature": 1000.0,
      "coolingRate": 0.95,
      "minTemperature": 0.1,
      "maxIterations": 10000
    },
    "monteCarlo": {
      "iterations": 1000,
      "confidenceLevel": 0.95
    }
  },
  "lockedAssignmentIds": []
}
```

**Response 202:**
```json
{
  "jobId": "550e8400-e29b-41d4-a716-446655440001",
  "status": "GREEDY",
  "progressPercent": 0,
  "message": "Planning job started",
  "estimatedCompletionMs": 30000
}
```

**SSE Events:**
```
event: status
data: {"jobId":"...","phase":"GREEDY","progressPercent":10,"currentCost":5000,"bestCost":5000,"iteration":0}

event: status
data: {"jobId":"...","phase":"ANNEALING","progressPercent":40,"currentCost":4200,"bestCost":4100,"iteration":2500}

event: status
data: {"jobId":"...","phase":"MONTE_CARLO","progressPercent":70,"currentCost":4100,"bestCost":4050,"iteration":500}

event: status
data: {"jobId":"...","phase":"DONE","progressPercent":100,"currentCost":4050,"bestCost":4050,"iteration":10000}
```

**GET /api/v1/planning/jobs/{jobId}/result — Response:**
```json
{
  "assignments": [
    {
      "id": "...",
      "featureId": "...",
      "teamId": "...",
      "sprintId": "...",
      "allocatedEffort": {"backendHours":40,"frontendHours":16,"qaHours":12,"devopsHours":4},
      "status": "PLANNED"
    }
  ],
  "conflicts": [
    {
      "type": "DEADLINE_MISSED",
      "featureId": "...",
      "teamId": null,
      "message": "Feature 'Add OAuth2 login' cannot meet deadline 2025-03-01"
    }
  ],
  "featureTimelines": {
    "feature-id-1": {
      "featureId": "feature-id-1",
      "startDate": "2025-01-15",
      "endDate": "2025-02-15",
      "probabilityOfMeetingDeadline": 0.85
    }
  },
  "teamLoadReports": {
    "team-id-1": {
      "teamId": "team-id-1",
      "loadByDateAndRole": {
        "2025-01-15": {"BACKEND": 32, "FRONTEND": 16, "QA": 8, "DEVOPS": 4}
      },
      "utilizationPercent": 82.5,
      "bottleneckRoles": ["QA"]
    }
  },
  "totalCost": 4050.0,
  "computationTimeMs": 15234,
  "algorithm": "SIMULATED_ANNEALING"
}
```

---

## 3. Integration Hub API (port 8082)

```
POST    /api/v1/integrations/{type}/import   — Import from tracker (jira, ado, linear)
POST    /api/v1/integrations/{type}/export   — Export assignments to tracker
POST    /api/v1/integrations/{type}/test     — Test connection
POST    /api/v1/integrations/webhook/{type}  — Handle webhook from tracker
```

**POST /api/v1/integrations/jira/import — Request:**
```json
{
  "config": {
    "baseUrl": "https://mycompany.atlassian.net",
    "authType": "basic",
    "username": "user@company.com",
    "apiToken": "..."
  },
  "filter": {
    "projectKey": "PROJ",
    "issueTypes": ["Epic", "Story"],
    "statuses": ["To Do", "In Progress"],
    "sinceDate": "2025-01-01",
    "maxResults": 1000
  }
}
```

**Response:**
```json
{
  "featuresCreated": 45,
  "featuresUpdated": 12,
  "sprintsCreated": 8,
  "teamsCreated": 3
}
```

---

## 4. Error Response Format

All errors follow RFC 7807 Problem Detail:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Feature with id 123e4567-e89b-12d3-a456-42661417e001 not found",
  "instance": "/api/v1/features/123e4567-e89b-12d3-a456-42661417e001"
}
```

**HTTP Status Codes:**

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 202 | Accepted (async operation started) |
| 204 | No Content (delete) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (concurrent modification) |
| 422 | Unprocessable Entity (domain constraint violation) |
| 500 | Internal Server Error |

---

## 5. Authentication

Bearer token via `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

JWT claims:
```json
{
  "sub": "user-id",
  "roles": ["DELIVERY_MANAGER"],
  "exp": 1700000000,
  "iat": 1699996400
}
```
