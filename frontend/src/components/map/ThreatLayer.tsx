import { useEffect, useMemo } from 'react';
import type { Map } from 'maplibre-gl';
import type { ThreatEventProperties, GeoJsonFeatureCollection } from '@spatial/spatial';
import maplibregl, { FilterSpecification } from 'maplibre-gl';

export interface ThreatLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<ThreatEventProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  visible?: boolean;
  /** Filter by severity */
  severityFilter?: string[];
  /** Filter by threat type */
  typeFilter?: string[];
  /** Filter by status */
  statusFilter?: string[];
  /** Show only actionable threats */
  showOnlyActionable?: boolean;
}

const DEFAULT_SOURCE_ID = 'threats';
const DEFAULT_LAYER_ID = 'threats-layer';

/**
 * Threat Events Layer Component
 * Renders threat events as styled circles with severity-based colors
 */
export function ThreatLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  visible = true,
  severityFilter,
  typeFilter,
  statusFilter,
  showOnlyActionable = false,
}: ThreatLayerProps) {
  // Build filter expression
  const filter = useMemo(() => {
    const conditions: any[] = ['==', '$type', 'Point'];

    if (severityFilter?.length) {
      conditions.push(['in', ['get', 'severity'], ...severityFilter]);
    }
    if (typeFilter?.length) {
      conditions.push(['in', ['get', 'type'], ...typeFilter]);
    }
    if (statusFilter?.length) {
      conditions.push(['in', ['get', 'status'], ...statusFilter]);
    }
    if (showOnlyActionable) {
      conditions.push(['==', ['get', 'isActionable'], true]);
    }

    return conditions.length > 1 ? ['all', ...conditions] : conditions[0];
  }, [severityFilter, typeFilter, statusFilter, showOnlyActionable]);

  // Initialize source and layer
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;

    if (!map.getSource(sourceId)) {
      map.addSource(sourceId, {
        type: 'geojson',
        data: data || { type: 'FeatureCollection', features: [] },
        cluster: false,
      });
    } else if (data) {
      const source = map.getSource(sourceId);
      if (source && 'setData' in source) {
        (source as any).setData(data);
      }
    }

    // Base threat layer
    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter,
        paint: {
          // Radius based on severity
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, ['match', ['get', 'severity'], 'CRITICAL', 8, 'HIGH', 7, 'MEDIUM', 6, 'LOW', 5, 4],
            6, ['match', ['get', 'severity'], 'CRITICAL', 12, 'HIGH', 10, 'MEDIUM', 8, 'LOW', 6, 5],
            10, ['match', ['get', 'severity'], 'CRITICAL', 16, 'HIGH', 14, 'MEDIUM', 12, 'LOW', 8, 6],
            14, ['match', ['get', 'severity'], 'CRITICAL', 20, 'HIGH', 18, 'MEDIUM', 16, 'LOW', 10, 8],
            18, ['match', ['get', 'severity'], 'CRITICAL', 24, 'HIGH', 22, 'MEDIUM', 20, 'LOW', 12, 10],
          ],
          // Color based on severity
          'circle-color': [
            'match',
            ['get', 'severity'],
            'CRITICAL', '#991b1b',    // Red-900
            'HIGH', '#dc2626',        // Red-600
            'MEDIUM', '#f59e0b',      // Amber-500
            'LOW', '#84cc16',         // Lime-500
            'INFO', '#3b82f6',        // Blue-500
            '#64748b',                // Slate-500 default
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.95,
        },
      });
    }

    // Pulse animation for critical/high threats
    const pulseLayerId = `${layerId}-pulse`;
    if (!map.getLayer(pulseLayerId)) {
      map.addLayer({
        id: pulseLayerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: [
          'all',
          ['==', '$type', 'Point'],
          ['in', ['get', 'severity'], 'CRITICAL', 'HIGH'],
        ] as unknown as FilterSpecification,
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, ['match', ['get', 'severity'], 'CRITICAL', 16, 'HIGH', 12, 8],
            6, ['match', ['get', 'severity'], 'CRITICAL', 24, 'HIGH', 18, 12],
            10, ['match', ['get', 'severity'], 'CRITICAL', 32, 'HIGH', 24, 16],
            14, ['match', ['get', 'severity'], 'CRITICAL', 40, 'HIGH', 30, 20],
            18, ['match', ['get', 'severity'], 'CRITICAL', 48, 'HIGH', 36, 24],
          ],
          'circle-color': [
            'match',
            ['get', 'severity'],
            'CRITICAL', '#ef4444',
            'HIGH', '#f97316',
            'transparent',
          ],
          'circle-opacity': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 0.3,
            6, 0.2,
            10, 0.15,
            14, 0.1,
          ],
        },
      });
    }

    // Confidence-based opacity overlay
    const confidenceLayerId = `${layerId}-confidence`;
    if (!map.getLayer(confidenceLayerId)) {
      map.addLayer({
        id: confidenceLayerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter,
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 4,
            6, 6,
            10, 8,
            14, 10,
            18, 12,
          ],
          'circle-color': '#ffffff',
          'circle-opacity': [
            'interpolate',
            ['linear'],
            ['get', 'confidence'],
            0.5, 0.3,
            0.7, 0.6,
            0.9, 0.9,
            1.0, 1.0,
          ],
        },
      });
    }

    // Highlight layer for selected threat
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
            0, 10,
            6, 14,
            10, 18,
            14, 22,
            18, 26,
          ],
          'circle-color': '#fbbf24',
          'circle-stroke-width': 3,
          'circle-stroke-color': '#f59e0b',
          'circle-opacity': 1,
        },
      });
    }

    return () => {
      if (map.getLayer(`${layerId}-highlight`)) map.removeLayer(`${layerId}-highlight`);
      if (map.getLayer(`${layerId}-pulse`)) map.removeLayer(`${layerId}-pulse`);
      if (map.getLayer(`${layerId}-confidence`)) map.removeLayer(`${layerId}-confidence`);
      if (map.getLayer(layerId)) map.removeLayer(layerId);
      if (map.getSource(sourceId)) map.removeSource(sourceId);
    };
  }, [map, sourceId, layerId, minZoom, maxZoom]);

  // Update data
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const source = map.getSource(sourceId);
    if (source && 'setData' in source && data) {
      (source as any).setData(data);
    }
  }, [map, sourceId, data]);

  // Update filter
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    if (map.getLayer(layerId)) {
      map.setFilter(layerId, filter);
    }
  }, [map, layerId, filter]);

  // Update selected highlight
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const highlightLayerId = `${layerId}-highlight`;
    if (map.getLayer(highlightLayerId)) {
      map.setFilter(highlightLayerId, selectedFeatureId
        ? ['all', ['==', '$type', 'Point'], ['==', 'id', selectedFeatureId]]
        : ['all', ['==', '$type', 'Point'], ['==', 'id', '']]);
    }
  }, [map, layerId, selectedFeatureId]);

  // Update visibility
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const layers = [layerId, `${layerId}-pulse`, `${layerId}-confidence`, `${layerId}-highlight`];
    layers.forEach(l => {
      if (map.getLayer(l)) {
        map.setLayoutProperty(l, 'visibility', visible ? 'visible' : 'none');
      }
    });
  }, [map, layerId, visible]);

  return null;
}

/**
 * Threat Layer with Popup on Hover
 */
export function ThreatLayerWithPopup({
  map,
  data,
  sourceId = 'threats',
  layerId = 'threats-layer',
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: ThreatLayerProps) {
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
            'match', ['get', 'severity'],
            'CRITICAL', '#991b1b',
            'HIGH', '#dc2626',
            'MEDIUM', '#f59e0b',
            'LOW', '#84cc16',
            'INFO', '#3b82f6',
            '#64748b',
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.95,
        },
      });
    }

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
          className: 'threat-popup',
        });
      }

      const props = feature.properties as any;
      const severityColors: Record<string, string> = {
        CRITICAL: '#991b1b',
        HIGH: '#dc2626',
        MEDIUM: '#f59e0b',
        LOW: '#84cc16',
        INFO: '#3b82f6',
      };
      const severityColor = severityColors[props.severity] || '#64748b';

      const content = `
        <div class="p-2 min-w-[240px]">
          <h4 class="font-semibold text-grid-900">${props.type}</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>Severity: <span class="font-medium" style="color: ${severityColor}">${props.severity}</span></div>
            <div>Confidence: <span class="font-medium">${Math.round(props.confidence * 100)}%</span></div>
            <div>Status: <span class="font-medium capitalize">${props.status.toLowerCase().replace('_', ' ')}</span></div>
            ${props.affectedAssetId ? `<div>Affected Asset: <span class="font-medium">${props.affectedAssetId}</span></div>` : ''}
            <div>Detected: <span class="font-medium">${new Date(props.detectedAt).toLocaleString()}</span></div>
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