import { useEffect, useMemo } from 'react';
import type { Map } from 'maplibre-gl';
import type { AssetProperties, GeoJsonFeatureCollection } from '@spatial/spatial';
import maplibregl, { FilterSpecification } from 'maplibre-gl';

export interface AssetLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<AssetProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  visible?: boolean;
  /** Filter by asset type */
  assetTypeFilter?: string[];
  /** Filter by criticality */
  criticalityFilter?: string[];
  /** Filter by risk band */
  riskBandFilter?: string[];
  /** Show only assets with active threats */
  showOnlyThreatened?: boolean;
  /** Show only assets with active incidents */
  showOnlyIncidents?: boolean;
}

const DEFAULT_SOURCE_ID = 'assets';
const DEFAULT_LAYER_ID = 'assets-layer';

/**
 * Asset Layer Component
 * Renders assets as circles with styling based on asset type, criticality, health, and risk
 */
export function AssetLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  visible = true,
  assetTypeFilter,
  criticalityFilter,
  riskBandFilter,
  showOnlyThreatened = false,
  showOnlyIncidents = false,
}: AssetLayerProps) {
  // Build filter expression
  const filter = useMemo(() => {
    const conditions: any[] = ['==', '$type', 'Point'];

    if (assetTypeFilter?.length) {
      conditions.push(['in', ['get', 'type'], ...assetTypeFilter]);
    }
    if (criticalityFilter?.length) {
      conditions.push(['in', ['get', 'criticality'], ...criticalityFilter]);
    }
    if (riskBandFilter?.length) {
      conditions.push(['in', ['get', 'riskBand'], ...riskBandFilter]);
    }
    if (showOnlyThreatened) {
      conditions.push(['>', ['get', 'activeThreatCount'], 0]);
    }
    if (showOnlyIncidents) {
      conditions.push(['>', ['get', 'activeIncidentCount'], 0]);
    }

    return conditions.length > 1 ? ['all', ...conditions] : conditions[0];
  }, [assetTypeFilter, criticalityFilter, riskBandFilter, showOnlyThreatened, showOnlyIncidents]);

  // Initialize source and layer
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;

    // Add source if not exists
    if (!map.getSource(sourceId)) {
      map.addSource(sourceId, {
        type: 'geojson',
        data: data || { type: 'FeatureCollection', features: [] },
        cluster: false,
        buffer: 0,
      });
    } else if (data) {
      const source = map.getSource(sourceId);
      if (source && 'setData' in source) {
        (source as any).setData(data);
      }
    }

    // Base asset layer
    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter,
        paint: {
          // Radius based on criticality and zoom
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 6, 'HIGH', 5, 'MEDIUM', 4, 3],
            6, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 8, 'HIGH', 7, 'MEDIUM', 6, 4],
            10, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 10, 'HIGH', 9, 'MEDIUM', 8, 6],
            14, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 12, 'HIGH', 11, 'MEDIUM', 10, 8],
            18, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 14, 'HIGH', 13, 'MEDIUM', 12, 10],
          ],
          // Color based on risk band
          'circle-color': [
            'match',
            ['get', 'riskBand'],
            'CRITICAL', '#991b1b',    // Red-900
            'HIGH', '#dc2626',        // Red-600
            'MEDIUM', '#f59e0b',      // Amber-500
            'LOW', '#84cc16',         // Lime-500
            'NEGLIGIBLE', '#22c55e',  // Green-500
            '#64748b',                // Slate-500 default
          ],
          'circle-stroke-width': [
            'match',
            ['get', 'criticality'],
            'MISSION_CRITICAL', 3,
            'HIGH', 2,
            1.5,
          ],
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.9,
        },
      });
    }

    // Health indicator ring (for degraded/critical health)
    const healthLayerId = `${layerId}-health`;
    if (!map.getLayer(healthLayerId)) {
      map.addLayer({
        id: healthLayerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: [
          'all',
          ['==', '$type', 'Point'],
          ['in', ['get', 'healthStatus'], 'DEGRADED', 'CRITICAL'],
        ] as unknown as FilterSpecification,
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 8, 'HIGH', 7, 'MEDIUM', 6, 5],
            6, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 10, 'HIGH', 9, 'MEDIUM', 8, 6],
            10, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 12, 'HIGH', 11, 'MEDIUM', 10, 8],
            14, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 14, 'HIGH', 13, 'MEDIUM', 12, 10],
            18, ['match', ['get', 'criticality'], 'MISSION_CRITICAL', 16, 'HIGH', 15, 'MEDIUM', 14, 12],
          ],
          'circle-color': [
            'match',
            ['get', 'healthStatus'],
            'CRITICAL', '#ef4444',    // Red-500
            'DEGRADED', '#f97316',    // Orange-500
            'transparent',
          ],
          'circle-stroke-width': 0,
          'circle-opacity': 0.6,
        },
      });
    }

    // Highlight layer for selected asset
    const highlightLayerId = `${layerId}-highlight`;
    if (!map.getLayer(highlightLayerId)) {
      map.addLayer({
        id: highlightLayerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['all', ['==', '$type', 'Point'], ['==', 'id', '']],
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 8,
            6, 10,
            10, 12,
            14, 14,
            18, 16,
          ],
          'circle-color': '#fbbf24', // Amber-400
          'circle-stroke-width': 3,
          'circle-stroke-color': '#f59e0b', // Amber-500
          'circle-opacity': 1,
        },
      });
    }

    return () => {
      if (map.getLayer(`${layerId}-highlight`)) {
        map.removeLayer(`${layerId}-highlight`);
      }
      if (map.getLayer(`${layerId}-health`)) {
        map.removeLayer(`${layerId}-health`);
      }
      if (map.getLayer(layerId)) {
        map.removeLayer(layerId);
      }
      if (map.getSource(sourceId)) {
        map.removeSource(sourceId);
      }
    };
  }, [map, sourceId, layerId, minZoom, maxZoom]);

  // Update data when it changes
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const source = map.getSource(sourceId);
    if (source && 'setData' in source && data) {
      (source as any).setData(data);
    }
  }, [map, sourceId, data]);

  // Update filter when filters change
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    if (map.getLayer(layerId)) {
      map.setFilter(layerId, filter);
    }
    if (map.getLayer(`${layerId}-health`)) {
      map.setFilter(`${layerId}-health`, [
        'all',
        ['==', '$type', 'Point'],
        ['in', ['get', 'healthStatus'], 'DEGRADED', 'CRITICAL'],
      ] as unknown as FilterSpecification);
    }
  }, [map, layerId, filter]);

  // Update selected feature highlight
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const highlightLayerId = `${layerId}-highlight`;
    if (map.getLayer(highlightLayerId)) {
      map.setFilter(highlightLayerId, selectedFeatureId
        ? ['all', ['==', '$type', 'Point'], ['==', 'id', selectedFeatureId]] as unknown as FilterSpecification
        : ['all', ['==', '$type', 'Point'], ['==', 'id', '']] as unknown as FilterSpecification);
    }
  }, [map, layerId, selectedFeatureId]);

  // Update visibility
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const layers = [layerId, `${layerId}-health`, `${layerId}-highlight`];
    layers.forEach(l => {
      if (map.getLayer(l)) {
        map.setLayoutProperty(l, 'visibility', visible ? 'visible' : 'none');
      }
    });
  }, [map, layerId, visible]);

  return null;
}

/**
 * Asset Layer with Popup on Hover
 */
export function AssetLayerWithPopup({
  map,
  data,
  sourceId = 'assets',
  layerId = 'assets-layer',
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: AssetLayerProps) {
  const popupRef = { current: null as maplibregl.Popup | null };

  useEffect(() => {
    if (!map || !map.isStyleLoaded() || !data) return;

    if (!map.getSource(sourceId)) {
      map.addSource(sourceId, {
        type: 'geojson',
        data,
        cluster: false,
      });
    }

    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['==', '$type', 'Point'],
        paint: {
          'circle-radius': [
            'interpolate', ['linear'], ['zoom'],
            0, 4, 6, 6, 10, 8, 14, 10, 18, 12,
          ],
          'circle-color': [
            'match', ['get', 'riskBand'],
            'CRITICAL', '#991b1b',
            'HIGH', '#dc2626',
            'MEDIUM', '#f59e0b',
            'LOW', '#84cc16',
            'NEGLIGIBLE', '#22c55e',
            '#64748b',
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.9,
        },
      });
    }

    // Mouse enter - show popup
    const handleMouseEnter = (e: any) => {
      if (!map || !e.features?.length) return;
      const feature = e.features[0];
      if (!feature.properties) return;

      map.getCanvas().style.cursor = 'pointer';

      // Create popup if it doesn't exist
      if (!popupRef.current) {
        popupRef.current = new maplibregl.Popup({
          closeButton: false,
          closeOnClick: false,
          offset: 15,
          className: 'asset-popup',
        });
      }

      const props = feature.properties as AssetProperties;
      const riskBand = props.riskBand || 'UNKNOWN';
      const riskColorMap: Record<string, string> = {
        CRITICAL: '#991b1b',
        HIGH: '#dc2626',
        MEDIUM: '#f59e0b',
        LOW: '#84cc16',
        NEGLIGIBLE: '#22c55e',
      };
      const riskColor = riskColorMap[riskBand] || '#64748b';

      const healthColorMap: Record<string, string> = {
        HEALTHY: '#22c55e',
        DEGRADED: '#f97316',
        CRITICAL: '#ef4444',
      };
      const healthColor = healthColorMap[props.healthStatus] || '#64748b';

      const content = `
        <div class="p-2 min-w-[240px]">
          <h4 class="font-semibold text-grid-900">${props.name}</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>Type: <span class="font-medium">${props.typeDisplayName || props.type}</span></div>
            <div>Criticality: <span class="font-medium">${props.criticality}</span></div>
            <div>Risk: <span class="font-medium" style="color: ${riskColor}">${riskBand}</span> ${props.riskScore !== undefined ? `(${Math.round(props.riskScore * 100)}%)` : ''}</div>
            <div>Health: <span class="font-medium" style="color: ${healthColor}">${props.healthStatus}</span> (${Math.round(props.healthIndex * 100)}%)</div>
            <div>Redundancy: <span class="font-medium">N+${props.redundancyLevel}</span></div>
          </div>
        </div>
      `;

      popupRef.current?.setLngLat(e.lngLat).setHTML(content).addTo(map);
    };

    const handleMouseLeave = () => {
      if (map) map.getCanvas().style.cursor = '';
      popupRef.current?.remove();
    };

    map.on('mouseenter', layerId, handleMouseEnter);
    map.on('mouseleave', layerId, handleMouseLeave);

    return () => {
      map.off('mouseenter', layerId, handleMouseEnter);
      map.off('mouseleave', layerId, handleMouseLeave);
    };
  }, [map, sourceId, layerId, data, minZoom, maxZoom]);

  return null;
}

// Need to import maplibregl for Popup
// import maplibregl from 'maplibre-gl'; // Already imported at top