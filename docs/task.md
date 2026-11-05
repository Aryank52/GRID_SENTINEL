# Task Tracker — GRID SENTINEL

**Updated:** 2026-10-31  
**Current Phase:** Phase 1 — Foundation & Toolchain  
**Sprint:** Sprint 1 — Repository Initialization & Build Verification

---

## Phase 1: Repo Reconnaissance & Build/Toolchain (Current Sprint)

### Sprint 1 Goals
- [ ] Initialize Maven multi-module backend structure
- [ ] Initialize Vite + React 19 + TypeScript + Tailwind 4 frontend
- [ ] Pin Spring Boot 4.1.x exact patch version
- [ ] Configure Flyway + PostGIS baseline migration
- [ ] Scaffold 15 bounded module packages
- [ ] Verify backend compiles, frontend type-checks
- [ ] Create GitHub Actions CI skeleton

---

### Sprint 1 Tasks

#### Backend — Project Structure & Build

| ID | Task | Owner | Status | Notes |
|----|------|-------|--------|-------|
| 1.1 | Create parent `pom.xml` with Spring Boot 4.1.x dependency management | | ⏳ Pending | Pin exact patch after checking latest |
| 1.2 | Create `grid-sentinel-common` module (shared kernel) | | ⏳ Pending | Value objects, exceptions, utilities |
| 1.3 | Create `grid-sentinel-domain` module (15 bounded contexts) | | ⏳ Pending | Package structure only, no logic yet |
| 1.4 | Create `grid-sentinel-infrastructure` module (JPA, Flyway, adapters) | | ⏳ Pending | Repositories, migrations, external ports |
| 1.5 | Create `grid-sentinel-application` module (use cases, orchestration) | | ⏳ Pending | Application services, transactions |
| 1.6 | Create `grid-sentinel-api` module (controllers, DTOs, OpenAPI) | | ⏳ Pending | REST endpoints, mapping |
| 1.7 | Create `grid-sentinel-starter` module (Spring Boot main class) | | ⏳ Pending | Entry point, auto-configuration |
| 1.8 | Configure Flyway in `infrastructure` module | | ⏳ Pending | `src/main/resources/db/migration` |
| 1.9 | Write baseline migration `V1__init_schema.sql` | | ⏳ Pending | PostGIS extension, core tables |
| 1.10 | Add Testcontainers + ArchUnit test dependencies | | ⏳ Pending | Test scope only |
| 1.11 | Verify `mvn compile` passes on all modules | | ⏳ Pending | No warnings/errors |

#### Frontend — Project Structure & Build

| ID | Task | Owner | Status | Notes |
|----|------|-------|--------|-------|
| 1.12 | Initialize Vite + React 19 + TypeScript project in `frontend/` | | ⏳ Pending | `npm create vite@latest` |
| 1.13 | Install Tailwind CSS 4 + PostCSS + Vite plugin | | ⏳ Pending | `@tailwindcss/vite` |
| 1.14 | Install MapLibre GL JS 6.x + TypeScript types | | ⏳ Pending | `maplibre-gl`, `@types/maplibre-gl` |
| 1.15 | Install React Query (TanStack Query) + Zustand | | ⏳ Pending | Server/client state |
| 1.16 | Install shadcn/ui components (or Headless UI + custom) | | ⏳ Pending | Accessible primitives |
| 1.17 | Configure TypeScript strict mode + path aliases | | ⏳ Pending | `@/` alias for `src/` |
| 1.18 | Create minimal `App.tsx` with MapLibre placeholder | | ⏳ Pending | Renders map container |
| 1.19 | Verify `npm run build` and `npm run typecheck` pass | | ⏳ Pending | No errors |

#### CI / DevOps

| ID | Task | Owner | Status | Notes |
|----|------|-------|--------|-------|
| 1.20 | Create `.github/workflows/ci.yml` | | ⏳ Pending | Build, test, lint, typecheck |
| 1.21 | Configure Maven cache in Actions | | ⏳ Pending | Speed up builds |
| 1.22 | Configure npm cache in Actions | | ⏳ Pending | Speed up frontend builds |
| 1.23 | Add Testcontainers PostgreSQL/PostGIS service to workflow | | ⏳ Pending | Integration test DB |

---

## Phase 2: Database & Core Domain Model (Next Sprint)

*Not started — will plan after Phase 1 complete*

| Area | Key Tasks |
|------|-----------|
| Migrations | Core tables: asset, grid_node, grid_edge, observation, risk_score, threat_event, incident, simulation, response_plan, recovery_state, protection_plan, audit_log |
| Domain | Rich aggregates with invariants, value objects (Geometry, Criticality, HealthIndex), domain events |
| Repositories | Spring Data JPA + custom spatial queries |
| ArchUnit | Enforce layer rules: api→app→domain←infra, no controller→repository |

---

## Phase 3: Asset & Grid APIs

*Not started*

---

## Phase 4: Risk Engine

*Not started*

---

## Phase 5: Observation & Detection Pipeline

*Not started*

---

## Phase 6: Incident & Simulation

*Not started*

---

## Phase 7: Response & Recovery

*Not started*

---

## Phase 8: Command Center & Map

*Not started*

---

## Phase 9: Remaining Screens

*Not started*

---

## Phase 10: Audit & Security Hardening

*Not started*

---

## Phase 11: AI Copilot

*Not started*

---

## Phase 12: Performance, Polish & E2E

*Not started*

---

## Definition of Done (Per Master Prompt)

A task is **done** only when:
- [ ] Code exists and compiles
- [ ] Design matches domain model (OOAD principles)
- [ ] Tests exist and pass (unit + integration where applicable)
- [ ] Errors/loading handled (frontend) or exceptions mapped (backend)
- [ ] Logs/metrics sufficient (structured logging, correlation IDs)
- [ ] API docs updated (OpenAPI annotations)
- [ ] Relevant diagrams/docs updated
- [ ] No unrelated regressions introduced
- [ ] Flagship demo scenario still works (once defined)

---

## Blockers / Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Spring Boot 4.1.x not yet GA | High | Check release status; fall back to 3.4.x if needed |
| PostGIS 3.6 + PostgreSQL 18 Docker image | Medium | Use `postgis/postgis:18-3.6` or build custom |
| MapLibre GL JS 6.x peer deps | Low | Pin compatible React/types versions |
| ONNX Runtime Java availability | Medium | Verify Maven Central; bundle model artifacts as test resources |

---

## Notes

- **Do not jump ahead.** Complete Sprint 1 fully before starting Phase 2.
- **Commit often.** Each task should be a logical commit.
- **Update this file** after each task completion with status and any discoveries.