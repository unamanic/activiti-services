# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Activiti Cloud microservices platform — a multi-module Gradle project with four Spring Boot services and an Angular frontend. Services communicate asynchronously via RabbitMQ (Spring Cloud Stream) and use PostgreSQL for persistence.

## Build Commands

```bash
# Build all Java modules
./gradlew build

# Build a specific module
./gradlew :runtime-bundle:build
./gradlew :query-service:build
./gradlew :connector-service:build
./gradlew :modeler-backend:build

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :query-service:test

# Run a single test class
./gradlew :query-service:test --tests "com.example.MyTest"

# Clean build
./gradlew clean build
```

## Local Development

Start infrastructure dependencies (PostgreSQL, RabbitMQ, Keycloak, NGINX):
```bash
docker-compose up -d
```

Each service has an `application-local.yaml` for local dev overrides. Run a service locally with:
```bash
./gradlew :runtime-bundle:bootRun --args='--spring.profiles.active=local'
```

## Architecture

**Services and responsibilities:**

- **runtime-bundle** — BPMN process execution engine. Runs process instances and publishes lifecycle events to RabbitMQ.
- **connector-service** — Integration connectors that consume integration requests from RabbitMQ and call external systems. Contains example connectors (`TestActionConnector`, `IncrementActionConnector`).
- **query-service** — GraphQL API for querying process/task data. Consumes events from RabbitMQ to build its read model. Includes `PurgeService` (cascading delete) and `ReplayService`/`ReplayController` (replay endpoint, admin-only).
- **modeler-backend** — BPMN/DMN process definition modeling service.
- **modeler-app** — Angular frontend (currently discontinued).

**Communication patterns:**
- Services publish/consume events via RabbitMQ using Spring Cloud Stream bindings.
- `query-service` exposes a GraphQL endpoint (`/graphql`) and consumes engine events to populate its audit/query database.
- All services are secured with Keycloak OAuth2/JWT. Roles: `ACTIVITI_USER`, `ACTIVITI_ADMIN`, `ACTIVITI_MODELER`.

**Databases:**
- PostgreSQL in production; H2 in-memory for tests.
- `query-service` has a custom `h2.schema.sql` for H2 compatibility.

## Key Technology Versions

- Java 21 (toolchain enforced)
- Spring Boot 3.5.7
- Activiti Cloud 9.0.0 (varies by module, see individual `build.gradle`)
- Gradle 8.8
- Angular 14 / TypeScript 4.7 (modeler-app)

## Module Structure Pattern

Each Java module follows the same pattern:
- `build.gradle` — dependencies and Spring Boot plugin
- `gradle/wrapper/` — local Gradle wrapper
- `src/main/resources/application.yaml` — main config
- `src/main/resources/application-local.yaml` — local dev overrides
- `Dockerfile` — OpenJDK 21 slim image, exposes port 8080

## Frontend (modeler-app)

```bash
npm ci                    # Install dependencies
npm run build             # Development build
npm run lint              # ESLint + Stylelint
npm test                  # Jest tests
```

Uses Nx monorepo tooling, NgRx for state management, Apollo GraphQL client, and bpmn-js/dmn-js for diagram editing.
