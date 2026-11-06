# Phase 2D — TypeScript Error Inventory

**Date:** 2025-01-XX
**Command:** `npm run typecheck`
**Total Errors:** 146

---

## Error Groups by Component

### 1. DetailDrawer.tsx — 75 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS2393 | 8 | Duplicate function implementation |
| TS6133 | 33 | Unused variable/function |
| TS7053 | 5 | Implicit any from dynamic indexing |
| TS2769 | 1 | Badge prop not in DetailRow props |
| TS2322 | 1 | Badge prop type mismatch |
| TS2451 | 4 | Redeclared block-scoped variable |
| TS2393 | 1 | Duplicate function implementation |
| TS6133 | 2 | Unused imports |

**Key Issues:**
- 8 duplicate function implementations (getTitle, getSubtitle, renderNodeDetails, renderEdgeDetails, renderAssetDetails, renderEdgeDetails, renderThreatDetails, renderIncidentDetails, renderRiskDetails, DetailSection, DetailRow - many duplicated)
- 33 unused variables/functions (helper functions, badge variants, render functions)
- 5 implicit any from dynamic property access on risk/health maps
- Badge prop not in DetailRow component interface
- 4 redeclared block-scoped variables (getTitle, getSubtitle defined twice)

### 2. Legend.tsx — 15 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 3 | Unused imports (Circle, Minus, Activity) |
| TS2339 | 12 | Property access on union type without narrowing |

**Key Issues:**
- Union type property access without proper type narrowing
- 3 unused icon imports

### 3. MapFilters.tsx — 18 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS2554 | 1 | Expected 0-1 arguments, got 2 |
| TS2339 | 8 | Property 'filter' does not exist on type 'string \| never[]' |
| TS7006 | 10 | Parameter implicitly has 'any' type |
| TS2339 | 1 | Property 'showOnlyActionable' does not exist on SpatialQueryParams |
| TS2345 | 1 | Argument type mismatch for keyof SpatialQueryParams |

**Key Issues:**
- Filter property access on union type `string | never[]`
- Implicit any in callback parameters
- `showOnlyActionable` not in SpatialQueryParams interface
- Duplicate property in object literal

### 4. LiveGridMap.tsx — 18 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 14 | Unused imports/variables |
| TS6192 | 1 | All imports in declaration unused |
| TS2353 | 1 | Unknown property 'showOnlyActionable' |
| TS1117 | 1 | Duplicate property in object literal |
| TS18047 | 2 | 'mapMethods' possibly null |
| TS2322 | 2 | Prop type mismatches |

**Key Issues:**
- 14 unused imports/variables (layers, hooks, icons)
- mapMethods possibly null
- Prop type mismatch for onFeatureClick
- showOnlyActionable not in SpatialQueryParams
- Duplicate property in object literal

### 5. RiskLayer.tsx — 4 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 1 | Unused visible parameter |
| TS18047 | 2 | popupRef possibly null |
| TS2339 | 2 | Property 'current' on Popup type |

**Key Issues:**
- popupRef type issue - Popup doesn't have .current property
- Unused visible parameter

### 5. GridNodeLayer.tsx — 2 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 1 | Unused onFeatureClick parameter |
| TS6133 | 1 | Unused visible parameter (in WithPopup) |

### 6. GridEdgeLayer.tsx — 2 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 2 | Unused visible parameters |

### 7. IncidentLayer.tsx — 5 errors

| Error Code | Count | Description |
|------------|-------|-------------|
| TS6133 | 1 | Unused visible parameter |
| TS18047 | 2 | popupRef possibly null |
| TS2339 | 2 | Property 'current' on Popup type |

### 8. Other Shared Types — 0 errors

All module resolution and shared type issues have been resolved.

### Remaining Module/Type Issues — 0 errors

All module resolution issues (TS2307, TS2305, TS2304) have been resolved.

---

## Summary by Error Code

| Error Code | Total | Primary Components |
|------------|-------|-------------------|
| TS6133 | 63 | Unused variables/imports/parameters |
| TS2393 | 10 | Duplicate function implementations |
| TS7053 | 5 | Implicit any from dynamic indexing |
| TS2339 | 30 | Property access on union types / missing properties |
| TS2393 | 8 | Duplicate function implementations |
| TS2451 | 4 | Redeclared block-scoped variables |
| TS2322 | 3 | Type assignment mismatches |
| TS2451 | 4 | Redeclared variables |
| TS2353 | 1 | Unknown property in object literal |
| TS1117 | 1 | Duplicate property in object |
| TS18047 | 6 | Possibly null reference |
| TS2339 | 2 | Property 'current' on Popup |
| TS2554 | 1 | Wrong argument count |
| TS7006 | 10 | Implicit any in callbacks |
| TS2353 | 1 | Unknown property |
| TS1117 | 1 | Duplicate property |
| TS18047 | 2 | Possibly null (mapMethods) |
| TS2322 | 3 | Type assignment mismatch |
| TS2305 | 0 | Module resolution (FIXED) |
| TS2307 | 0 | Module resolution (FIXED) |

---

## Priority Fix Order

1. **DetailDrawer.tsx** - 75 errors (largest group)
2. **Legend.tsx** - 15 errors (union type access)
3. **MapFilters.tsx** - 18 errors (filter typing, showOnlyActionable)
4. **LiveGridMap.tsx** - 18 errors (imports, prop contracts)
5. **RiskLayer.tsx** - 4 errors (popup ref)
6. **IncidentLayer.tsx** - 5 errors (popup ref)
6. **Legend.tsx** - 15 errors
7. **RiskLayer.tsx** - 4 errors
8. **GridNodeLayer.tsx** - 2 errors
9. **GridEdgeLayer.tsx** - 2 errors
10. **IncidentLayer.tsx** - 5 errors
11. **LiveGridMap.tsx** - 18 errors
12. **MapFilters.tsx** - 18 errors