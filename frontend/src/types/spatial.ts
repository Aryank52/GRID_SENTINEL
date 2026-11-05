/**
 * GeoJSON Type Definitions for Grid Sentinel Spatial API
 * Matches the backend GeoJSON FeatureCollection structure
 */

export type GeoJsonBoundingBox = [number, number, number, number]; // [minLng, minLat, maxLng, maxLat]

export interface GeoJsonGeometry {
  type: string;
  coordinates: unknown;
}

export interface GeoJsonPoint extends GeoJsonGeometry {
  type: 'Point';
  coordinates: [number, number]; // [longitude, latitude]
}

export interface GeoJsonLineString extends GeoJsonGeometry {
  type: 'LineString';
  coordinates: [number, number][]; // [[lng, lat], ...]
}

export interface GeoJsonPolygon extends GeoJsonGeometry {
  type: 'Polygon';
  coordinates: [number, number][][]; // [[[lng, lat], ...], [[lng, lat], ...]] (exterior + holes)
}

export type GeoJsonGeometryType = GeoJsonPoint | GeoJsonLineString | GeoJsonPolygon;

export interface GeoJsonFeature<Properties = Record<string, unknown>> {
  type: 'Feature';
  id?: string;
  geometry: GeoJsonGeometryType;
  properties: Properties;
}

export interface GeoJsonFeatureCollection<Properties = Record<string, unknown>> {
  type: 'FeatureCollection';
  features: GeoJsonFeature<Properties>[];
  bbox?: GeoJsonBoundingBox;
  properties?: Record<string, unknown>;
}

/**
 * Spatial Query Parameters
 */
export type FeatureType = 'node' | 'edge' | 'asset' | 'threat' | 'incident' | 'risk';

export interface SpatialQueryParams {
  bbox?: string; // "minLng,minLat,maxLng,maxLat"
  limit?: number;
  offset?: number;
  assetType?: string;
  criticality?: string;
  riskBand?: string;
  minHealthIndex?: number;
  maxHealthIndex?: number;
  nodeType?: string;
  minVoltageKv?: number;
  maxVoltageKv?: number;
  edgeType?: string;
  severity?: string;
  threatType?: string;
  threatStatus?: string;
  minConfidence?: number;
  detectedAfter?: string;
  detectedBefore?: string;
  incidentState?: string;
  incidentSeverity?: string;
  activeOnly?: boolean;
  minRiskScore?: number;
  maxRiskScore?: number;
}

/**
 * Grid Node Properties
 */
export interface GridNodeProperties {
  id: string;
  name: string;
  type: string;
  voltageLevelKv: number;
  primaryAssetId?: string;
  operator?: string;
  degree: number;
  isSubstation: boolean;
  isGeneration: boolean;
  isLoad: boolean;
}

/**
 * Grid Edge Properties
 */
export interface GridEdgeProperties {
  id: string;
  name: string;
  type: string;
  fromNodeId: string;
  toNodeId: string;
  voltageLevelKv: number;
  thermalRatingAmps: number;
  mvaRating: number;
  lengthKm: number;
  circuitCount: number;
  conductorType?: string;
  yearInstalled?: number;
  primaryAssetId?: string;
  isTransformer: boolean;
}

/**
 * Asset Properties
 */
export interface AssetProperties {
  id: string;
  name: string;
  type: string;
  typeDisplayName: string;
  criticality: 'LOW' | 'MEDIUM' | 'HIGH' | 'MISSION_CRITICAL';
  healthIndex: number;
  healthStatus: 'HEALTHY' | 'DEGRADED' | 'CRITICAL';
  redundancyLevel: number;
  hasRedundancy: boolean;
  gridNodeId?: string;
  description?: string;
  installedAt?: string;
  lastInspectedAt?: string;
  riskBand?: string;
  riskScore?: number;
  baseRiskWeight: number;
}

/**
 * Threat Event Properties
 */
export interface ThreatEventProperties {
  id: string;
  type: string;
  severity: 'INFO' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  confidence: number;
  detectedAt: string;
  firstObservedAt?: string;
  affectedAssetId?: string;
  nearbyGridNodeId?: string;
  description: string;
  rationale?: string;
  status: string;
  detectionStrategy?: string;
  strategyVersion?: string;
  isActionable: boolean;
  evidenceCount: number;
}

/**
 * Incident Properties
 */
export interface IncidentProperties {
  id: string;
  incidentNumber: string;
  state: string;
  severity: string;
  originatingThreatId?: string;
  primaryAssetId?: string;
  primaryGridNodeId?: string;
  description: string;
  operatorNotes?: string;
  assignedOperatorId?: string;
  incidentCommanderId?: string;
  detectedAt: string;
  triagedAt?: string;
  confirmedAt?: string;
  responseStartedAt?: string;
  containedAt?: string;
  recoveringAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  isActive: boolean;
  isTerminal: boolean;
  stateHistory: Array<{
    toState: string;
    actorId?: string;
    reason?: string;
    timestamp: string;
  }>;
}

/**
 * Risk Score Properties
 */
export interface RiskScoreProperties {
  id: string;
  assetId: string;
  score: number;
  band: 'NEGLIGIBLE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  modelName: string;
  modelVersion: string;
  strategyName?: string;
  factorBreakdown: Record<string, {
    weight: number;
    rawValue: number;
    normalizedValue: number;
    description: string;
  }>;
  computedAt: string;
  correlationId?: string;
}

/**
 * Map Layer Configuration
 */
export interface MapLayerConfig {
  id: string;
  type: 'circle' | 'line' | 'fill' | 'symbol' | 'heatmap';
  source: string;
  paint?: Record<string, unknown>;
  layout?: Record<string, unknown>;
  filter?: unknown[];
  minZoom?: number;
  maxZoom?: number;
}

/**
 * Map Viewport State
 */
export interface MapViewport {
  center: [number, number]; // [longitude, latitude]
  zoom: number;
  bearing: number;
  pitch: number;
}

/**
 * Selected Feature State
 */
export interface SelectedFeature<Properties = Record<string, unknown>> {
  feature: GeoJsonFeature<Properties>;
  layerId: string;
  sourceId: string;
}