# FeatureFlow

> Платформа интеллектуального планирования портфеля разработки ПО

Автоматизированное планирование новых фич для организаций с множеством кросс-функциональных команд и бизнес-заказчиков. Минимизирует Time-to-Market, балансирует загрузку команд, учитывает зависимости и ограничения.

---

## Возможности

- **Автоматическое планирование** — жадный алгоритм + имитация отжига для оптимизации расписания
- **Вероятностное прогнозирование** — Монте-Карло симуляция для оценки вероятности дедлайнов
- **Управление капасити** — учёт ролей (Backend, Frontend, QA, DevOps), focus factor, резервы на баги и техдолг
- **Зависимости и приоритеты** — топологическая сортировка, классы обслуживания (Expedite, Fixed Date, Standard, Filler)
- **Симуляции «что-если»** — ветвление плана, сравнение с baseline
- **Интеграции** — импорт/экспорт из Jira, Azure DevOps, Linear
- **Интерактивный UI** — диаграмма Ганта, drag-and-drop, дашборды загрузки

---

## Стек

| Компонент | Технология |
|-----------|------------|
| Backend | Java 25, Spring Boot 3.4 |
| Frontend | React 19, TypeScript, Vite |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| Build | Maven |
| Tests | JUnit 5, Testcontainers |
| Infra | Docker, Docker Compose |

---

## Архитектура

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────────┐
│   Web UI     │────▶│  Data Service    │────▶│  Planning Engine │
│ React 19/TS  │     │  Spring Boot     │     │  Spring Boot     │
│  (port 3000) │     │  (port 8080)     │     │  (port 8081)     │
└──────────────┘     └────────┬─────────┘     └────────┬─────────┘
                              │                        │
                     ┌────────▼────────┐      ┌────────▼────────┐
                     │   PostgreSQL    │      │   Domain Core   │
                     │   (port 5432)   │      │   (JAR lib)     │
                     └─────────────────┘      └─────────────────┘

┌──────────────────┐
│ Integration Hub  │
│ Spring Boot      │────▶ Jira / ADO / Linear
│ (port 8082)      │
└──────────────────┘
```

### Модули

| Модуль | Описание |
|--------|----------|
| `domain-core` | Чистая Java библиотека: сущности, value objects, бизнес-правила, интерфейсы алгоритмов |
| `data-service` | CRUD API, управление продуктами, командами, фичами, назначениями |
| `planning-engine` | Алгоритмы: Greedy, Simulated Annealing, Monte Carlo |
| `integration-hub` | Коннекторы к внешним трекерам задач |
| `web-ui` | SPA с диаграммой Ганта, дашбордами, формами |

---

## Быстрый старт

### Требования

- Java 25+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### Запуск через Docker Compose

```bash
docker compose up -d
```

Сервисы будут доступны:
- Web UI: http://localhost:3000
- Data Service API: http://localhost:8080
- Planning Engine API: http://localhost:8081
- Integration Hub API: http://localhost:8082
- Swagger UI: http://localhost:8080/swagger-ui.html

### Ручной запуск

```bash
# Backend
mvn clean install -DskipTests

# Запуск сервисов
java -jar data-service/target/*.jar
java -jar planning-engine/target/*.jar
java -jar integration-hub/target/*.jar

# Frontend
cd web-ui
npm install
npm run dev
```

---

## Алгоритмы планирования

### Фаза 1 — Жадный алгоритм

1. Топологическая сортировка фич по приоритету с учётом зависимостей
2. Для каждой фичи — поиск ближайшего доступного слота у команд-кандидатов
3. Резервирование капасити на всё время выполнения

### Фаза 2 — Имитация отжига

Целевая функция:
```
Cost = w1 × Σ(Value_f × CompletionDate_f)
     + w2 × Σ(Underutilization²)
     + w3 × Σ(DeadlinePenalty)
```

Мутации: сдвиг спринта, переназначение команды, обмен фич, изменение параллелизма.

### Фаза 3 — Монте-Карло

Стохастическое моделирование с трёхточечными оценками (PERT) для получения вероятностных прогнозов дедлайнов.

---

## Документация

Полная архитектурная спецификация:

| Файл | Содержание |
|------|------------|
| [PLAN.md](PLAN.md) | Мастер-план: C4 диаграммы, стек, итерации, инфраструктура |
| [plan/specs/domain-core.md](plan/specs/domain-core.md) | Доменные сущности, value objects, бизнес-правила |
| [plan/specs/planning-engine.md](plan/specs/planning-engine.md) | Алгоритмы планирования |
| [plan/specs/data-service.md](plan/specs/data-service.md) | REST API, Spring Data JPA |
| [plan/specs/integration-hub.md](plan/specs/integration-hub.md) | Коннекторы Jira/ADO/Linear |
| [plan/specs/web-ui.md](plan/specs/web-ui.md) | React компоненты, страницы |
| [plan/specs/api-contracts.md](plan/specs/api-contracts.md) | OpenAPI спецификации |
| [plan/specs/data-model.md](plan/specs/data-model.md) | PostgreSQL схема, миграции |

---

## Roadmap

| Спринт | Задачи |
|--------|--------|
| Sprint 0 | Модель данных, API контракты, CI/CD |
| Sprint 1 | CRUD: продукты, команды, фичи, календари. Базовый UI |
| Sprint 2 | Жадный алгоритм, отображение Ганта |
| Sprint 3 | Имитация отжига, сравнение планов |
| Sprint 4 | Монте-Карло, вероятностные прогнозы |
| Sprint 5 | Jira интеграция, классы обслуживания |
| Sprint 6 | What-if симуляции, отчёты |
| Sprint 7 | Тесты, оптимизация, документация |

---

## Конфигурация

Ключевые параметры в `application.yml`:

```yaml
featureflow:
  planning:
    annealing:
      initial-temperature: 1000.0
      cooling-rate: 0.95
      max-iterations: 10000
    weights:
      w1-ttm: 1.0
      w2-underutilization: 0.5
      w3-deadline-penalty: 2.0
  capacity:
    default-focus-factor: 0.7
    default-bug-reserve: 0.20
    default-techdebt-reserve: 0.10
  parallelism:
    max-features-per-team-per-sprint: 3
```

---

## Лицензия

MIT
