# Repository Baseline — GRID SENTINEL

**Date:** 2026-10-31  
**Inspection:** Fresh repository — no existing code, configuration, or artifacts.

---

## 1. Repository Tree

```
GRID_SENTINEL/
└── docs/
    ├── repo-baseline.md        (this file)
    ├── architecture-gap.md     (to be created)
    └── task.md                 (to be created)
```

---

## 2. Build System

- **None detected.** No `pom.xml`, `build.gradle`, `package.json`, or similar.
- **Decision:** Use **Maven** for backend (standard for Spring Boot enterprise projects) and **Vite** for frontend (React 19 + TypeScript + Tailwind 4).

---

## 3. Existing Code / Working Features

- **Zero.** This is a greenfield repository.

---

## 4. Dependencies

- **None.** No `node_modules`, no Maven cache, no lockfiles.

---

## 5. Tests

- **None.** No test directories, no test configuration.

---

## 6. Environment / Configuration

- **None.** No `.env`, `application.yml`, `docker-compose.yml`, or CI/CD configs.

---

## 7. Dead / Duplicate / Generated / Abandoned Code

- **N/A** — repository is empty.

---

## 8. Architecture Baseline (Current State)

| Layer          | Status       | Notes                              |
|----------------|--------------|------------------------------------|
| Backend API    | ❌ Missing   | Spring Boot 4.1.x + Java 21        |
| Domain Model   | ❌ Missing   | OOAD modules per spec              |
| Database       | ❌ Missing   | PostgreSQL 18 + PostGIS 3.6 + Flyway |
| Frontend       | ❌ Missing   | React 19.3 + TS + Tailwind 4 + MapLibre GL JS 6 |
| Auth/Security  | ❌ Missing   | Spring Security + RBAC             |
| Observability  | ❌ Missing   | Actuator, OpenAPI, structured logs |
| Testing        | ❌ Missing   | JUnit 5, Testcontainers, ArchUnit, Playwright |
| CI/CD          | ❌ Missing   | GitHub Actions (planned)           |

---

## 9. Immediate Next Steps (Phase 1)

1. Initialize Maven multi-module backend structure.
2. Initialize Vite + React + TypeScript frontend.
3. Add Spring Boot 4.1.x parent POM with pinned patch version.
4. Configure Flyway + PostGIS migration baseline.
5. Scaffold domain module packages per bounded contexts.
6. Write `architecture-gap.md` and `task.md` with Sprint 1 tasks.
7. Verify build passes (backend compile, frontend type-check).