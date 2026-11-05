export { MapLibreMap, useMapLibre } from './MapLibreMap';
export { GridNodeLayer, GridNodeLayerWithPopup } from './GridNodeLayer';
export { GridEdgeLayer, GridEdgeLayerWithPopup } from './GridEdgeLayer';
export { AssetLayer, AssetLayerWithPopup } from './AssetLayer';
export { ThreatLayer, ThreatLayerWithPopup } from './ThreatLayer';
export { IncidentLayer, IncidentLayerWithPopup } from './IncidentLayer';
export { RiskLayer, RiskLayerWithPopup } from './RiskLayer';
export { MapFilters } from './MapFilters';
export { DetailDrawer } from './DetailDrawer';
export { Legend } from './Legend';

// Re-export types
export type {
  GridNodeLayerProps,
} from './GridNodeLayer';
export type {
  GridEdgeLayerProps,
} from './GridEdgeLayer';
export type {
  AssetLayerProps,
} from './AssetLayer';
export type {
  ThreatLayerProps,
} from './ThreatLayer';
export type {
  IncidentLayerProps,
} from './IncidentLayer';
export type {
  RiskLayerProps,
} from './RiskLayer';
export type {
  MapFiltersProps,
} from './MapFilters';
export type {
  DetailDrawerProps,
} from './DetailDrawer';
export type {
  LegendProps,
} from './Legend';
export type {
  GridNodeProperties,
  GridEdgeProperties,
  AssetProperties,
  ThreatEventProperties,
  IncidentProperties,
  RiskScoreProperties,
  GeoJsonFeatureCollection,
  GeoJsonFeature,
  SpatialQueryParams,
  MapViewport,
  SelectedFeature,
} from '../../types/spatial';