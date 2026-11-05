# Architecture Gap Analysis — GRID SENTINEL

**Date:** 2026-10-31  
**Baseline:** Empty repository (greenfield)  
**Target:** Full OOAD-compliant Grid Resilience Decision-Support Platform

---

## Gap Summary

| Capability Area | Current | Target | Gap Size | Priority |
|-----------------|---------|--------|----------|----------|
| **Build/Toolchain** | None | Maven (backend) + Vite (frontend) | 🔴 Complete | P0 |
| **Backend Framework** | None | Spring Boot 4.1.x, Java 21 | 🔴 Complete | P0 |
| **Database & Migrations** | None | PostgreSQL 18 + PostGIS 3.6 + Flyway | 🔴 Complete | P0 |
| **Domain Model (OOAD)** | None | 15 bounded modules, rich domain, patterns | 🔴 Complete | P0 |
| **Asset & Grid APIs** | None | REST + OpenAPI, spatial queries | 🔴 Complete | P0 |
| **Risk Engine** | None | Strategy-based, explainable scoring | 🔴 Complete | P0 |
| **Observation & Detection** | None | Pipeline: validation → fusion → threat | 🔴 Complete | P1 |
| **Incident & Simulation** | None | Lifecycle + deterministic graph simulation | 🔴 Complete | P1 |
| **Response & Recovery** | None | State machine + prioritized plans | 🔴 Complete | P1 |
| **Frontend Screens (10)** | None | Command Center, Map, Asset, Protection, Threat, Incident, Simulation, Copilot, Audit, Landing | 🔴 Complete | P1 |
| **AI Copilot** | None | Grounded summarization/explanation only | 🔴 Complete | P2 |
| **Security & Audit** | None | RBAC, audit log, correlation IDs | 🔴 Complete | P0 |
| **Testing Pyramid** | None | Unit, Integration, ArchUnit, API Contract, E2E | 🔴 Complete | P0 |
| **CI/CD** | None | GitHub Actions: build, test, analyze, deploy | 🔴 Complete | P2 |

---

## Target Architecture (Per Master Prompt)

### Module Layering (per bounded context)

```
api          → Controllers, DTOs, OpenAPI mapping
application  → Use cases, orchestration, transactions
domain       → Aggregates, entities, value objects, domain events, strategies, specifications
infrastructure → Repositories (JPA), adapters (ONNX, OpenCV), Flyway migrations, external clients
```

### Bounded Modules (15)

| Module | Core Responsibility | Key Domain Objects |
|--------|---------------------|-------------------|
| `auth` | Authentication, tokens, sessions | `User`, `Role`, `Session` |
| `user` | User profile, preferences, assignments | `UserProfile`, `OperatorAssignment` |
| `asset` | Grid assets, criticality, health, geometry | `Asset`, `AssetType`, `Criticality`, `HealthIndex` |
| `grid` | Topology: nodes, edges, connectivity | `GridNode`, `GridEdge`, `TopologySnapshot` |
| `observation` | Raw evidence ingestion, validation, normalization | `Observation`, `ObservationSource`, `Evidence` |
| `risk` | Scoring strategies, factor breakdown, recommendations | `RiskScore`, `RiskFactor`, `ProtectionAction` |
| `detection` | Fusion pipeline, anomaly/CV, threat classification | `ThreatEvent`, `ThreatClassification`, `Alert` |
| `threat` | Threat lifecycle, escalation, correlation | `Threat`, `ThreatCorrelation` |
| `incident` | Operational lifecycle, state machine, audit | `Incident`, `IncidentState`, `IncidentTransition` |
| `simulation` | Deterministic what-if, graph failure, impact | `Simulation`, `Scenario`, `ImpactResult` |
| `response` | Planning, prioritization, action tracking | `ResponsePlan`, `ResponseAction`, `Priority` |
| `recovery` | Recovery state machine, progress tracking | `RecoveryState`, `RecoveryMilestone` |
| `protection` | Prevention planning, cost/risk-reduction optimization | `ProtectionPlan`, `ProtectionMeasure` |
| `copilot` | Grounded LLM: summarize, explain, draft | `CopilotRequest`, `CopilotResponse`, `GroundingContext` |
| `audit` | Immutable governance log | `AuditLog`, `AuditEvent` |
| `common` | Shared kernel: value objects, exceptions, utilities | `Geometry`, `TimeRange`, `CorrelationId`, `DomainException` |

### Key Abstractions (Interfaces for Variability)

| Interface | Variability Addressed |
|-----------|----------------------|
| `RiskScoringStrategy` | Deterministic vs contextual vs ML-based scoring |
| `ThreatDetectionStrategy` | Rule-based vs CV vs hybrid fusion |
| `EvidenceFusionPolicy` | Weighted, Dempster-Shafer, Bayesian |
| `ImpactSimulationStrategy` | Graph cascade, DC power flow, simplified |
| `ResponsePlanningStrategy` | Rule-based, optimization-based, manual |
| `ProtectionOptimizationStrategy` | Greedy, knapsack, multi-objective |
| `ObservationSourceAdapter` | Thermal, visual, GIS, weather, SCADA, inspection |
| `ModelInferencePort` | ONNX Runtime, Tribuo, custom |
| `ExplanationService` | SHAP, LIME, rule-trace, grounded template |

### Core Domain Model Entities

- `Asset` — critical infrastructure with type, criticality, health, redundancy, geometry
- `GridNode` / `GridEdge` — topology, dependency paths
- `Observation` — time-bound evidence from source
- `RiskScore` — explainable weighted result with model/version metadata
- `ThreatEvent` — evidence-backed physical threat/anomaly candidate
- `Incident` — operational lifecycle object (state machine)
- `Simulation` — deterministic what-if representation
- `ResponsePlan` / `ResponseAction` — prioritized mitigation workflow
- `RecoveryState` — explicit incident recovery progress
- `ProtectionPlan` — prevention action + expected risk reduction/cost
- `AuditLog` — immutable governance record

---

## Technical Decisions to Confirm

| Decision | Options | Recommendation |
|----------|---------|----------------|
| **Build Tool (Backend)** | Maven vs Gradle | **Maven** — enterprise standard, better Spring Boot plugin support |
| **Migration Tool** | Flyway vs Liquibase | **Flyway** — simpler, SQL-first, strong PostGIS support |
| **Map Library** | MapLibre GL JS vs Leaflet | **MapLibre GL JS 6.x** — vector tiles, WebGL, matches spec |
| **State Management (Frontend)** | Redux Toolkit vs Zustand vs React Context | **Zustand + React Query** — minimal boilerplate, server state separation |
| **UI Component Library** | Headless UI + Tailwind vs shadcn/ui vs MUI | **shadcn/ui + Tailwind 4** — accessible, customizable, no runtime overhead |
| **API Spec** | OpenAPI 3.1 (SpringDoc) | **SpringDoc OpenAPI 3.1** — standard, generates types for frontend |
| **Testcontainers** | PostgreSQL + PostGIS image | **postgis/postgis:18-3.6** — official, maintained |

---

## Risk Areas Requiring Early Validation

1. **PostGIS + Flyway + Spring Boot 4.1 compatibility** — verify spatial dialect works
2. **MapLibre GL JS 6.x + React 19 + TypeScript strict** — verify types and peer deps
3. **Spring Boot 4.1.x exact patch version** — pin after checking latest stable
4. **ONNX Runtime Java + OpenCV Java bindings** — verify Maven Central availability
5. **ArchUnit rules for module boundaries** — define early, enforce in CI
6. **Deterministic simulation** — same input + version = same output (seed control)

---

## Phase 1 Scope (Foundation Only)

| Task | Deliverable | Verification |
|------|-------------|--------------|
| 1.1 Maven multi-module project | `pom.xml` (parent + modules) | `mvn compile` succeeds |
| 1.2 Spring Boot 4.1.x parent POM | Pinned version, dependency management | `mvn dependency:tree` clean |
| 1.3 Flyway + PostGIS baseline migration | `V1__init_schema.sql` | `mvn flyway:migrate` on Testcontainers |
| 1.4 Domain package scaffolding | 15 module packages under `domain` | Compiles, ArchUnit passes |
| 1.5 Vite + React 19 + TS + Tailwind 4 | Frontend scaffold | `npm run build` succeeds |
| 1.6 MapLibre GL JS 6.x integration | Map component renders | Dev server loads map |
| 1.7 CI workflow skeleton | GitHub Actions: build, test, lint | Green on push |

**Out of Phase 1 Scope:** Any domain logic, controllers, repositories, UI screens beyond map placeholder, Copilot, detection pipeline, simulation engine.