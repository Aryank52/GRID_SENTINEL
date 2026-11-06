import { useEffect, useMemo } from 'react';
import type { Map } from 'maplibre-gl';
import type { IncidentProperties, GeoJsonFeatureCollection } from '@spatial/spatial';
import maplibregl from 'maplibre-gl';

export interface IncidentLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<IncidentProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  visible?: boolean;
  /** Filter by incident state */
  stateFilter?: string[];
  /** Filter by severity */
  severityFilter?: string[];
  /** Show only active incidents */
  showActiveOnly?: boolean;
}

const DEFAULT_SOURCE_ID = 'incidents';
const DEFAULT_LAYER_ID = 'incidents-layer';

/**
 * Incident Layer Component
 * Renders incidents as styled markers with state-based colors
 */
export function IncidentLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  visible = true,
  stateFilter,
  severityFilter,
  showActiveOnly = false,
}: IncidentLayerProps) {
  const filter = useMemo(() => {
    const conditions: any[] = ['==', '$type', 'Point'];

    if (stateFilter?.length) {
      conditions.push(['in', ['get', 'state'], ...stateFilter]);
    }
    if (severityFilter?.length) {
      conditions.push(['in', ['get', 'severity'], ...severityFilter]);
    }
    if (showActiveOnly) {
      conditions.push(['==', ['get', 'isActive'], true]);
    }

    return conditions.length > 1 ? ['all', ...conditions] : conditions[0];
  }, [stateFilter, severityFilter, showActiveOnly]);

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

    // Base incident layer - using symbol layer for icons
    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'symbol',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter,
        layout: {
          'icon-image': [
            'match',
            ['get', 'state'],
            'DETECTED', 'incident-detected',
            'TRIAGED', 'incident-triaged',
            'CONFIRMED', 'incident-confirmed',
            'ACTIVE_RESPONSE', 'incident-active',
            'CONTAINED', 'incident-contained',
            'RECOVERING', 'incident-recovering',
            'RESOLVED', 'incident-resolved',
            'CLOSED', 'incident-closed',
            'incident-default',
          ],
          'icon-size': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 0.8,
            6, 1.0,
            10, 1.2,
            14, 1.4,
            18, 1.6,
          ],
          'icon-allow-overlap': true,
          'icon-ignore-placement': true,
          'text-field': [
            'match',
            ['get', 'state'],
            'ACTIVE_RESPONSE', '🔴',
            'CONTAINED', '🟡',
            'RECOVERING', '🟠',
            'RESOLVED', '🟢',
            'CLOSED', '⚪',
            '●',
          ],
          'text-size': 10,
          'text-offset': [0, -12],
          'text-anchor': 'top',
        },
        paint: {
          'icon-color': [
            'match',
            ['get', 'state'],
            'DETECTED', '#3b82f6',          // Blue-500
            'TRIAGED', '#6366f1',           // Indigo-500
            'CONFIRMED', '#8b5cf6',         // Violet-500
            'ACTIVE_RESPONSE', '#ef4444',   // Red-500
            'CONTAINED', '#f59e0b',         // Amber-500
            'RECOVERING', '#f97316',        // Orange-500
            'RESOLVED', '#22c55e',          // Green-500
            'CLOSED', '#64748b',            // Slate-500
            '#64748b',
          ],
          'text-halo-color': '#ffffff',
          'text-halo-width': 2,
        },
      });
    }

    // Circle layer as fallback/alternative (for simpler rendering)
    const circleLayerId = `${layerId}-circle`;
    if (!map.getLayer(circleLayerId)) {
      map.addLayer({
        id: circleLayerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: [
          'all',
          ['==', '$type', 'Point'],
          ['>=', ['zoom'], 6], // Only show circles at higher zoom
        ],
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 6,
            6, 8,
            10, 10,
            14, 12,
            18, 14,
          ],
          'circle-color': [
            'match',
            ['get', 'state'],
            'DETECTED', '#3b82f6',
            'TRIAGED', '#6366f1',
            'CONFIRMED', '#8b5cf6',
            'ACTIVE_RESPONSE', '#ef4444',
            'CONTAINED', '#f59e0b',
            'RECOVERING', '#f97316',
            'RESOLVED', '#22c55e',
            'CLOSED', '#64748b',
            '#64748b',
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.9,
        },
      });
    }

    // Active incidents pulse
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
          ['==', ['get', 'isActive'], true],
        ],
        paint: {
          'circle-radius': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 12,
            6, 18,
            10, 24,
            14, 30,
            18, 36,
          ],
          'circle-color': [
            'match',
            ['get', 'state'],
            'ACTIVE_RESPONSE', '#ef4444',
            'CONTAINED', '#f59e0b',
            'RECOVERING', '#f97316',
            'transparent',
          ],
          'circle-opacity': 0.3,
        },
      });
    }

    // Highlight layer
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
      if (map.getLayer(`${layerId}-circle`)) map.removeLayer(`${layerId}-circle`);
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
    if (map.getLayer(layerId)) map.setFilter(layerId, filter);
    if (map.getLayer(`${layerId}-circle`)) map.setFilter(`${layerId}-circle`, filter);
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
    const layers = [layerId, `${layerId}-circle`, `${layerId}-pulse`, `${layerId}-highlight`];
    layers.forEach(l => {
      if (map.getLayer(l)) {
        map.setLayoutProperty(l, 'visibility', visible ? 'visible' : 'none');
      }
    });
  }, [map, layerId, visible]);

  return null;
}

/**
 * Incident Layer with Popup on Hover
 */
export function IncidentLayerWithPopup({
  map,
  data,
  sourceId = 'incidents',
  layerId = 'incidents-layer',
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: IncidentLayerProps) {
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
            0, 6, 6, 8, 10, 10, 14, 12, 18, 14,
          ],
          'circle-color': [
            'match', ['get', 'state'],
            'DETECTED', '#3b82f6',
            'TRIAGED', '#6366f1',
            'CONFIRMED', '#8b5cf6',
            'ACTIVE_RESPONSE', '#ef4444',
            'CONTAINED', '#f59e0b',
            'RECOVERING', '#f97316',
            'RESOLVED', '#22c55e',
            'CLOSED', '#64748b',
            '#64748b',
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.9,
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
          className: 'incident-popup',
        });
      }

      const props = feature.properties as any;
      const stateColors: Record<string, string> = {
        DETECTED: '#3b82f6',
        TRIAGED: '#6366f1',
        CONFIRMED: '#8b5cf6',
        ACTIVE_RESPONSE: '#ef4444',
        CONTAINED: '#f59e0b',
        RECOVERING: '#f97316',
        RESOLVED: '#22c55e',
        CLOSED: '#64748b',
      };
      const stateColor = stateColors[props.state] || '#64748b';

      const content = `
        <div class="p-2 min-w-[240px]">
          <h4 class="font-semibold text-grid-900">${props.incidentNumber}</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>State: <span class="font-medium" style="color: ${stateColor}">${props.state.replace('_', ' ')}</span></div>
            <div>Severity: <span class="font-medium">${props.severity}</span></div>
            <div>Title: <span class="font-medium">${props.description?.substring(0, 50)}...</span></div>
            ${props.primaryAssetId ? `<div>Primary Asset: <span class="font-medium">${props.primaryAssetId}</span></div>` : ''}
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