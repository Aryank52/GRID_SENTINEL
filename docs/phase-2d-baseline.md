# Phase 2D Baseline — Digital Grid Twin + MapLibre Implementation

## Current Spatial Backend Capability

### Database Schema (PostGIS + Flyway V1)
- **PostGIS Extension**: Enabled (geometry types, spatial indexes)
- **SRID**: 4326 (WGS84) enforced at DB and domain level
- **Spatial Indexes**: GIST indexes on all geometry columns

### Spatial Tables

| Table | Geometry Type | Spatial Column | GIST Index |
|-------|---------------|----------------|------------|
| `assets` | GEOMETRY(GEOMETRY, 4326) | `geometry` | `idx_assets_geometry` |
| `grid_nodes` | GEOMETRY(POINT, 4326) | `geometry` | `idx_grid_nodes_geometry` |
| `grid_edges` | GEOMETRY(LINESTRING, 4326) | `geometry` | `idx_grid_edges_geometry` |
| `observations` | GEOMETRY(GEOMETRY, 4326) | `geometry` | `idx_observations_geometry` |
| `threat_events` | GEOMETRY(GEOMETRY, 4326) | `location` | `idx_threat_events_location` |
| `incidents` | GEOMETRY(GEOMETRY, 4326) | `location` | `idx_incidents_location` |

### Domain Entities with Geometry

1. **Asset** — `CommonKernel.GeometryValueObject` (any valid geometry, typically Point)
2. **GridNode** — `CommonKernel.GeometryValueObject` (must be Point)
3. **GridEdge** — `CommonKernel.GeometryValueObject` (must be LineString)
4. **ThreatEvent** — `CommonKernel.GeometryValueObject` (Point or Polygon for area threats)
5. **Incident** — `CommonKernel.GeometryValueObject` (Point or Polygon)
6. **Observation** — `CommonKernel.GeometryValueObject` (optional)

### Geometry Value Object
```java
// CommonKernel.GeometryValueObject
// - Wraps JTS Geometry
// - Validates SRID = 4326
// - Validates geometry.isValid()
// - Provides intersects, contains, distance operations
```

### Enums / Classification
- `AssetType` (51 types, each with default `Criticality`, `baseRiskWeight()`)
- `Criticality`: LOW, MEDIUM, HIGH, MISSION_CRITICAL
- `RiskBand`: NEGLIGIBLE, LOW, MEDIUM, HIGH, CRITICAL
- `ThreatSeverity`: INFO, LOW, MEDIUM, HIGH, CRITICAL
- `IncidentState`: DETECTED, TRIAGED, CONFIRMED, ACTIVE_RESPONSE, CONTAINED, RECOVERING, RESOLVED, CLOSED
- `GridNode.NodeType`: SUBSTATION, SWITCHING_STATION, GENERATION, LOAD, JUNCTION, HVDC_CONVERTER, FACTS_DEVICE, INTERCONNECT
- `GridEdge.EdgeType`: TRANSMISSION_LINE, UNDERGROUND_CABLE, TRANSFORMER, etc.

### Existing Views
- `asset_risk_view` — Assets with latest risk score
- `active_incidents_view` — Active incidents with response status
- `actionable_threats_view` — High-confidence, actionable threats

---

## Current Spatial API Capability

**NONE** — No REST controllers exist yet. The backend has:
- Domain entities with JPA mappings
- Flyway schema with PostGIS
- JPA repositories not yet created (only `UserRepository` exists)
- No API layer (controllers, DTOs, services)

### Required API Contract (Design Target)

| Endpoint | Purpose | Filters |
|----------|---------|---------|
| `GET /api/grid/nodes` | Grid nodes as GeoJSON FeatureCollection | `bbox`, `type`, `voltageLevelKv`, `operator` |
| `GET /api/grid/edges` | Grid edges as GeoJSON FeatureCollection | `bbox`, `type`, `voltageLevelKv`, `status` |
| `GET /api/assets/spatial` | Assets as GeoJSON FeatureCollection | `bbox`, `assetType`, `criticality`, `riskBand`, `healthIndex` |
| `GET /api/threats/spatial` | Threat events as GeoJSON FeatureCollection | `bbox`, `severity`, `type`, `status`, `confidence` |
| `GET /api/incidents/spatial` | Incidents as GeoJSON FeatureCollection | `bbox`, `state`, `severity`, `incidentNumber` |
| `GET /api/risk/spatial` | Risk scores as GeoJSON FeatureCollection | `bbox`, `band`, `scoreRange` |

**Response Format**: GeoJSON FeatureCollection with Feature properties containing domain-relevant fields.

---

## Frontend Map Status

### Current State
- **LiveGridMap.tsx**: Placeholder only (`MapViewPlaceholder` component)
- **CommandCenter.tsx**: Contains `MapPlaceholder` in left panel
- **MapLibre GL**: Installed (`maplibre-gl@^4.7.0`)
- **Dependencies**: React 18, TanStack Query, Zustand, Axios, Tailwind CSS
- **No spatial query hooks**, no TanStack Query integration for map data

### Component Architecture
```
CommandCenter
  └── MapPlaceholder (left panel, 2/3 width)
LiveGridMap (full page)
  └── MapViewPlaceholder
```

### Missing Frontend Contracts
1. No TypeScript types for GeoJSON features
2. No API service methods for spatial endpoints
3. No TanStack Query hooks for spatial data
4. No MapLibre integration layer
5. No layer management (nodes, edges, assets, risk, threats, incidents)
6. No asset detail drawer
7. No topology inspection
8. No spatial filtering UI
9. No legend component
10. No viewport-aware querying

---

## Missing Contracts Summary

| Layer | Missing |
|-------|---------|
| **Backend** | JPA Repositories, Spatial Query Services, REST Controllers, GeoJSON DTOs, Geometry Mapping |
| **Frontend** | GeoJSON Types, API Client Methods, Query Hooks, MapLibre Wrapper, Layer Components, Detail Drawer, Filters, Legend |

---

## Implementation Plan

### Phase 2D.1 — Backend Spatial API (Week 1)
1. Create JPA repositories with spatial queries
2. Create GeoJSON DTO classes (Feature, FeatureCollection, Geometry types)
3. Create Spatial Query Services (viewport, bbox, filtering)
4. Create REST Controllers for 6 spatial endpoints
5. Add bounding box parameter parsing and validation
6. Unit/integration tests for spatial queries

### Phase 2D.2 — Frontend Spatial Layer (Week 1-2)
1. Define TypeScript GeoJSON types
2. Add API client methods for spatial endpoints
3. Create TanStack Query hooks with viewport-aware caching
4. Create MapLibre wrapper component with lifecycle management
5. Implement base map with navigation controls

### Phase 2D.3 — Map Layers (Week 2)
1. Grid Node Layer (styled by NodeType, voltage)
2. Grid Edge Layer (styled by EdgeType, status, risk)
3. Asset Layer (styled by AssetType, criticality, health)
4. Risk Heatmap Layer (choropleth or point-based)
5. Threat Layer (styled by severity, confidence)
6. Incident Layer (styled by state, severity)

### Phase 2D.4 — Interaction & UX (Week 2-3)
1. Asset/Node/Edge selection with highlight
2. Detail Drawer (right/bottom panel)
3. Topology Inspection (connected nodes/edges)
4. Spatial Filters Panel (collapsible)
5. Legend Component
6. Empty/Loading/Error states

### Phase 2D.5 — Integration (Week 3)
1. Command Center ↔ Map synchronization
2. Simulation launch from map context
3. Threat/Incident highlighting
4. Performance optimization (debouncing, caching, dedup)
5. Accessibility & Mobile responsive

### Phase 2D.6 — QA & Documentation (Week 3)
1. Browser QA checklist
2. typecheck / lint / build verification
3. docs/phase-2d-completion-report.md
4. Update docs/architecture.md