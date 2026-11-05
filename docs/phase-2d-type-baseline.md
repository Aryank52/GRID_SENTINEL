# Phase 2D — Type & Runtime Baseline

**Date:** 2025-01-XX

## Commands Run

```bash
npm run typecheck
npm run lint
npm run build
```

## TypeScript Typecheck Results

### Summary
- **Total TypeScript Errors:** ~180
- **Total Warnings:** ~30

### Error Categories

| Category | Count | Examples |
|----------|-------|----------|
| Import/Module Resolution (TS2307) | ~15 | Cannot find module '../types/spatial' |
| Missing Export (TS2305) | ~10 | Module has no exported member |
| Missing Type Definitions (TS2304) | ~3 | Cannot find name 'GeoJsonFeatureCollection' |
| Prop Mismatch (TS2769, TS2322) | ~10 | Badge prop not in DetailRow props |
| Duplicate Implementation (TS2393) | ~8 | Duplicate function implementations in DetailDrawer |
| Redeclared Variables (TS2451) | ~4 | getTitle, getSubtitle redeclared |
| MapLibre Typing (TS2339, TS2694) | ~10 | Property 'features' not on MapMouseEvent, Layer export missing |
| TanStack Query Typing (TS2769) | ~6 | AxiosResponse not assignable to QueryFunction |
| Unused Variables (TS6133) | ~60 | Unused functions, variables, imports |
| Explicit Any (TS7053, @typescript-eslint/no-explicit-any) | ~50 | Implicit any, explicit any usage |
| Event Typing (TS2339) | ~15 | Property 'features' on MapMouseEvent, 'current' on Popup |
| Nullable/Undefined (TS18047) | ~10 | popupRef possibly null |
| Filter Specification (TS2345) | ~5 | Filter type incompatibility |
| Duplicate Identifier (TS2300) | ~3 | Duplicate 'maplibregl' import |
| Object Literal Type (TS2353, TS1117) | ~2 | Unknown property, duplicate property |

### Files with Most Errors

1. **DetailDrawer.tsx** - ~45 errors (duplicates, unused, prop mismatch, any)
2. **useSpatialData.ts** - ~30 errors (TanStack Query typing)
3. **MapLibreMap.tsx** - ~15 errors (MapLibre typing, module resolution)
4. **GridEdgeLayer.tsx** - ~10 errors (module resolution, filter typing, nullability)
5. **GridNodeLayer.tsx** - ~10 errors (module resolution, nullability)
6. **IncidentLayer.tsx** - ~10 errors (module resolution, duplicate import, nullability)
6. **RiskLayer.tsx** - ~10 errors (module resolution, any, nullability)
7. **ThreatLayer.tsx** - ~10 errors (filter typing, duplicate import, nullability)
7. **GridNodeLayer.tsx** - ~10 errors (module resolution, nullability)
8. **MapFilters.tsx** - ~15 errors (filter typing, unknown properties)
9. **Legend.tsx** - ~15 errors (module resolution, union type property access)
9. **LiveGridMap.tsx** - ~15 errors (module resolution, unused, prop mismatch)
10. **AssetLayer.tsx** - ~8 errors (explicit any, nullability)

## Lint Results

### Summary
- **Errors:** 58
- **Warnings:** 84

### Error Categories

| Rule | Count |
|------|-------|
| @typescript-eslint/no-explicit-any | 40 |
| @typescript-eslint/no-unused-vars | 10 |
| react-hooks/exhaustive-deps | 8 |
| Expected 0-1 arguments | 1 |
| Object literal known properties | 1 |

## Build Status

**Not yet run** (typecheck fails first)

## Module Resolution Issues

The most critical issue: **Cannot find module '../types/spatial'** affecting:
- GridEdgeLayer.tsx
- GridNodeLayer.tsx
- IncidentLayer.tsx
- MapLibreMap.tsx
- RiskLayer.tsx
- ThreatLayer.tsx
- Legend.tsx
- LiveGridMap.tsx

This suggests `frontend/src/types/spatial.ts` may not exist or is not properly exported.

## Next Steps

1. Fix module resolution (spatial.ts)
2. Fix TanStack Query typing (useSpatialData.ts)
3. Fix MapLibre typing (MapLibreMap.tsx)
5. Fix DetailDrawer.tsx duplicates and prop contracts
6. Fix layer components (props, nullability, filter typing)
7. Fix Legend.tsx union type property access
8. Fix MapFilters.tsx filter typing
9. Fix LiveGridMap.tsx imports and prop contracts
10. Fix layer components (AssetLayer, GridEdgeLayer, GridNodeLayer, IncidentLayer, ThreatLayer)
11. Fix lint @typescript-eslint/no-explicit-any
12. Fix unused variables/imports