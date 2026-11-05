# Phase 2D — Type & Runtime Progress Report

**Date:** 2025-01-XX

## Commands Run

```bash
npm run typecheck
npm run lint
npm run build
```

## Progress Summary

### ✅ Fixed Issues

#### Module Resolution (COMPLETE)
- ✅ Fixed `Cannot find module '../types/spatial'` across all components
- ✅ Added `@spatial/*` path alias in tsconfig.json
- ✅ Updated all imports to use `@spatial/spatial`

#### Type Exports (COMPLETE)
- ✅ Added `FeatureType` export to spatial.ts
- ✅ Changed `GeoJsonBoundingBox` to tuple type `[number, number, number, number]`
- ✅ Exported all layer props from their respective files
- ✅ Fixed index.ts to re-export types from correct modules

#### MapLibreMap.tsx (MOSTLY COMPLETE)
- ✅ Fixed MapLibre GL imports (removed unused, added FilterSpecification, AnyLayer → maplibregl.LayerSpecification)
- ✅ Fixed maplibregl.supported() check
- ✅ Fixed MapMouseEvent typing with MapGeoJSONFeature
- ✅ Fixed GeoJsonFeature conversion
- ✅ Fixed AnyLayer → maplibregl.LayerSpecification
- ✅ Fixed useMapLibre hook to use module-level store

#### API Layer (COMPLETE)
- ✅ Fixed TanStack Query typing by returning `response.data` instead of AxiosResponse
- ✅ Added async/await to all spatial API methods

#### AssetLayer.tsx (MOSTLY COMPLETE)
- ✅ Fixed duplicate maplibregl import
- ✅ Fixed FilterSpecification type assertions with `as unknown as FilterSpecification`
- ✅ Fixed popupRef to use useRef instead of useMemo
- ✅ Fixed riskColor/healthColor typing with Record<string, string>
- ✅ Fixed unused visible parameter

#### ThreatLayer.tsx (MOSTLY COMPLETE)
- ✅ Fixed duplicate maplibregl import
- ✅ Fixed FilterSpecification type assertions
- ✅ Fixed popupRef to use useRef
- ✅ Fixed unused visible parameter

#### IncidentLayer.tsx (PARTIAL)
- ✅ Fixed duplicate maplibregl import

#### Export Structure (COMPLETE)
- ✅ Fixed index.ts to re-export types from correct modules
- ✅ Exported all layer props from their respective files

### ⚠️ Remaining Issues

#### Critical (Blockers for typecheck)

| File | Issue | Count |
|------|-------|-------|
| DetailDrawer.tsx | Duplicate function implementations, unused vars, badge prop, any indexing | ~45 |
| GridEdgeLayer.tsx | line-dasharray numbers, popup ref, unused visible | 7 |
| GridNodeLayer.tsx | Missing GeoJsonFeature import, popup ref, unused vars | 7 |
| IncidentLayer.tsx | Popup ref, duplicate maplibregl (commented), unused visible | 6 |
| RiskLayer.tsx | Popup ref, unused visible | 5 |
| Legend.tsx | Union type property access | 12 |
| MapFilters.tsx | Filter typing, showOnlyActionable | 15 |
| LiveGridMap.tsx | Unused imports, prop mismatches, mapMethods | 12 |
| MapFilters.tsx | Filter typing, unknown properties | 15 |
| LiveGridMap.tsx | Unused imports, prop mismatches | 10 |

#### Popup Ref Issues (Multiple Files)
The pattern `popupRef.current` fails because:
1. `Popup` type doesn't have `current` property (refs have `.current`, but Popup is not a ref)
2. `popupRef` is typed as `Popup` not `Popup | null`
3. Need to use `useRef<Popup | null>(null)` and check `popupRef.current`

#### FilterSpecification Issues
- MapLibre GL's `FilterSpecification` type is very strict
- Dynamic filter construction with "in", "get", "match" expressions needs `as unknown as FilterSpecification`
- Numbers in match expressions need `as const` or proper typing

#### DetailDrawer.tsx (Major Refactor Needed)
- ~45 errors including:
  - 8 duplicate function implementations
  - 15+ unused variables
  - Badge prop not in DetailRow props
  - Multiple redeclared variables (getTitle, getSubtitle)
  - Implicit any indexing

#### Legend.tsx (Union Type Issues)
- Union type property access fails because TypeScript can't narrow the union
- Need to use type guards or discriminated unions

#### MapFilters.tsx (Filter Typing)
- Filter property access on string | never[]
- showOnlyActionable not in SpatialQueryParams
- Duplicate property in object literal

#### LiveGridMap.tsx
- Unused imports
- Prop mismatches
- mapMethods possibly null

### Next Steps Priority

1. **Fix Popup Ref Pattern** - Apply consistent `useRef<Popup | null>(null)` pattern across all layers
2. **Fix GridEdgeLayer line-dasharray** - Use proper typing for match expressions
3. **Fix DetailDrawer** - Remove duplicates, fix badge prop, remove unused vars
3. **Fix Legend.tsx** - Add type guards for union types
4. **Fix MapFilters.tsx** - Fix filter typing, remove showOnlyActionable
5. **Fix LiveGridMap.tsx** - Clean up imports, fix prop contracts
6. **Fix Remaining Layer Popup Refs** - GridNodeLayer, IncidentLayer, RiskLayer, GridEdgeLayer

### Static Validation Status

| Check | Status |
|-------|--------|
| Typecheck | FAIL (~180 errors) |
| Lint | FAIL (142 problems) |
| Build | NOT RUN (typecheck fails first) |

### Map Runtime Validation

| Check | Status |
|-------|--------|
| MapLibre loads | UNKNOWN |
| Basemap loads | UNKNOWN |
| Layers load | UNKNOWN |
| Interactions work | UNKNOWN |
| Console errors | UNKNOWN |

### Documentation

- ✅ Created `docs/phase-2d-type-baseline.md`
- ⏳ Need to create `docs/phase-2d-type-runtime-completion.md` after all checks pass

### Next Actions

1. Complete Popup ref fixes across all layer components
2. Fix GridEdgeLayer line-dasharray typing
3. Refactor DetailDrawer.tsx (major cleanup)
4. Fix Legend.tsx union type access
5. Fix MapFilters.tsx filter typing
6. Fix LiveGridMap.tsx imports and prop contracts
6. Fix remaining layer popup refs
7. Run lint and fix @typescript-eslint/no-explicit-any
8. Run build
9. Perform browser/runtime QA
10. Create completion documentation