import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { api } from '../services/api';
import type {
  GeoJsonFeatureCollection,
  SpatialQueryParams,
  GridNodeProperties,
  GridEdgeProperties,
  AssetProperties,
  ThreatEventProperties,
  IncidentProperties,
  RiskScoreProperties,
} from '@spatial/spatial';

/**
 * Query key factory for spatial data
 */
export const spatialKeys = {
  all: ['spatial'] as const,
  gridNodes: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'gridNodes', params] as const,
  gridEdges: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'gridEdges', params] as const,
  assets: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'assets', params] as const,
  threats: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'threats', params] as const,
  incidents: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'incidents', params] as const,
  riskScores: (params?: SpatialQueryParams) =>
    [...spatialKeys.all, 'riskScores', params] as const,
};

/**
 * Hook for fetching grid nodes
 */
export function useGridNodes(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<GridNodeProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.gridNodes(params),
    queryFn: () => api.getGridNodes(params),
    staleTime: 30_000, // 30 seconds
    gcTime: 5 * 60_000, // 5 minutes
    refetchOnWindowFocus: false,
    ...options,
  });
}

/**
 * Hook for fetching grid edges
 */
export function useGridEdges(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<GridEdgeProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.gridEdges(params),
    queryFn: () => api.getGridEdges(params),
    staleTime: 30_000,
    gcTime: 5 * 60_000,
    refetchOnWindowFocus: false,
    ...options,
  });
}

/**
 * Hook for fetching assets
 */
export function useAssets(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<AssetProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.assets(params),
    queryFn: () => api.getAssets(params),
    staleTime: 30_000,
    gcTime: 5 * 60_000,
    refetchOnWindowFocus: false,
    ...options,
  });
}

/**
 * Hook for fetching threat events
 */
export function useThreats(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<ThreatEventProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.threats(params),
    queryFn: () => api.getThreats(params),
    staleTime: 15_000, // 15 seconds for threats (more dynamic)
    gcTime: 5 * 60_000,
    refetchOnWindowFocus: false,
    refetchInterval: 30_000, // Auto-refetch every 30 seconds
    ...options,
  });
}

/**
 * Hook for fetching incidents
 */
export function useIncidents(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<IncidentProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.incidents(params),
    queryFn: () => api.getIncidents(params),
    staleTime: 15_000,
    gcTime: 5 * 60_000,
    refetchOnWindowFocus: false,
    refetchInterval: 30_000,
    ...options,
  });
}

/**
 * Hook for fetching risk scores
 */
export function useRiskScores(
  params?: SpatialQueryParams,
  options?: Partial<UseQueryOptions<GeoJsonFeatureCollection<RiskScoreProperties>, Error>>
) {
  return useQuery({
    queryKey: spatialKeys.riskScores(params),
    queryFn: () => api.getRiskScores(params),
    staleTime: 60_000, // 60 seconds for risk (less dynamic)
    gcTime: 10 * 60_000,
    refetchOnWindowFocus: false,
    ...options,
  });
}

/**
 * Combined hook for fetching all spatial layers for a viewport
 * Useful for initial map load
 */
export function useViewportSpatialData(
  viewport: {
    minLng: number;
    minLat: number;
    maxLng: number;
    maxLat: number;
  } | null,
  options?: {
    includeNodes?: boolean;
    includeEdges?: boolean;
    includeAssets?: boolean;
    includeThreats?: boolean;
    includeIncidents?: boolean;
    includeRisk?: boolean;
    layerFilters?: {
      assetType?: string[];
      criticality?: string[];
      riskBand?: string[];
      nodeType?: string[];
      edgeType?: string[];
      severity?: string[];
      threatType?: string[];
      incidentState?: string[];
    };
  }
) {
  const { includeNodes = true, includeEdges = true, includeAssets = true,
          includeThreats = true, includeIncidents = true, includeRisk = true,
          layerFilters = {} } = options || {};

  const bbox = viewport ? `${viewport.minLng},${viewport.minLat},${viewport.maxLng},${viewport.maxLat}` : undefined;

  const baseParams = {
    bbox,
    limit: 1000,
    ...layerFilters,
  };

  const nodes = useGridNodes(includeNodes ? baseParams : undefined, { enabled: includeNodes && !!viewport });
  const edges = useGridEdges(includeEdges ? baseParams : undefined, { enabled: includeEdges && !!viewport });
  const assets = useAssets(includeAssets ? baseParams : undefined, { enabled: includeAssets && !!viewport });
  const threats = useThreats(includeThreats ? baseParams : undefined, { enabled: includeThreats && !!viewport });
  const incidents = useIncidents(includeIncidents ? baseParams : undefined, { enabled: includeIncidents && !!viewport });
  const riskScores = useRiskScores(includeRisk ? baseParams : undefined, { enabled: includeRisk && !!viewport });

  return {
    nodes,
    edges,
    assets,
    threats,
    incidents,
    riskScores,
    isLoading: nodes.isLoading || edges.isLoading || assets.isLoading ||
               threats.isLoading || incidents.isLoading || riskScores.isLoading,
    isError: nodes.isError || edges.isError || assets.isError ||
             threats.isError || incidents.isError || riskScores.isError,
    error: nodes.error || edges.error || assets.error ||
           threats.error || incidents.error || riskScores.error,
    refetch: () => {
      nodes.refetch();
      edges.refetch();
      assets.refetch();
      threats.refetch();
      incidents.refetch();
      riskScores.refetch();
    },
  };
}