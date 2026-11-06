import { useEffect, useMemo, useRef } from 'react';
import maplibregl from 'maplibre-gl';
import type { Map } from 'maplibre-gl';
import type { RiskScoreProperties, GeoJsonFeatureCollection } from '@spatial/spatial';

export interface RiskLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<RiskScoreProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  visible?: boolean;
  /** Filter by risk band */
  bandFilter?: string[];
  /** Show as heatmap instead of circles */
  showHeatmap?: boolean;
}

const DEFAULT_SOURCE_ID = 'risk';
const DEFAULT_LAYER_ID = 'risk-layer';

/**
 * Risk Score Layer Component
 * Renders risk scores as heatmap or circles based on risk band
 */
export function RiskLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  visible = true,
  bandFilter,
  showHeatmap = false,
}: RiskLayerProps) {
  const filter = useMemo(() => {
    const conditions: any[] = ['==', '$type', 'Point'];
    if (bandFilter?.length) {
      conditions.push(['in', ['get', 'band'], ...bandFilter]);
    }
    return conditions.length > 1 ? ['all', ...conditions] : conditions[0];
  }, [bandFilter]);

  // Main effect - setup layers
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;

    // Setup source
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

    // Add layers based on showHeatmap
    if (showHeatmap) {
      addHeatmapLayer(map, sourceId, layerId, minZoom, maxZoom, filter);
    } else {
      addCircleLayer(map, sourceId, layerId, minZoom, maxZoom);
    }

    // Highlight layer
    addHighlightLayer(map, sourceId, layerId, minZoom, maxZoom);

    // Cleanup function
    return () => {
      cleanupLayers(map, layerId, sourceId);
    };
  }, [map, sourceId, layerId, minZoom, maxZoom, showHeatmap, filter, data]);

  // Update data
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    const source = map.getSource(sourceId);
    if (source && 'setData' in source && data) {
      (source as any).setData(data);
    }
  }, [map, sourceId, data]);

  // Toggle heatmap/circles
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    if (showHeatmap) {
      if (map.getLayer(`${layerId}-heatmap`)) map.setLayoutProperty(`${layerId}-heatmap`, 'visibility', visible ? 'visible' : 'none');
      if (map.getLayer(`${layerId}-circles`)) map.setLayoutProperty(`${layerId}-circles`, 'visibility', 'none');
    } else {
      if (map.getLayer(`${layerId}-heatmap`)) map.setLayoutProperty(`${layerId}-heatmap`, 'visibility', 'none');
      if (map.getLayer(`${layerId}-circles`)) map.setLayoutProperty(`${layerId}-circles`, 'visibility', visible ? 'visible' : 'none');
    }
  }, [map, layerId, showHeatmap, visible]);

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
    const layers = [`${layerId}-circles`, `${layerId}-heatmap`, `${layerId}-highlight`];
    layers.forEach(l => {
      if (map.getLayer(l)) {
        map.setLayoutProperty(l, 'visibility', visible ? 'visible' : 'none');
      }
    });
  }, [map, layerId, visible]);

  return null;
}

// Helper functions for layer management
function addHeatmapLayer(map: Map, sourceId: string, layerId: string, minZoom: number, maxZoom: number, filter: any) {
  const heatmapLayerId = `${layerId}-heatmap`;
  if (!map.getLayer(heatmapLayerId)) {
    map.addLayer({
      id: heatmapLayerId,
      type: 'heatmap',
      source: sourceId,
      minzoom: minZoom,
      maxzoom: maxZoom,
      filter,
      paint: {
        'heatmap-weight': [
          'interpolate',
          ['linear'],
          ['get', 'score'],
          0, 0,
          0.3, 0.3,
          0.6, 0.6,
          0.8, 0.9,
          1.0, 1.0,
        ],
        'heatmap-intensity': [
          'interpolate',
          ['linear'],
          ['zoom'],
          0, 0.5,
          10, 1.0,
          14, 1.5,
        ],
        'heatmap-color': [
          'interpolate',
          ['linear'],
          ['heatmap-density'],
          0, 'rgba(34,197,94,0)',
          0.2, 'rgba(132,204,22,0.5)',
          0.4, 'rgba(245,158,11,0.7)',
          0.6, 'rgba(249,115,22,0.8)',
          0.8, 'rgba(239,68,68,0.9)',
          1.0, 'rgba(153,27,27,1)',
        ],
        'heatmap-radius': [
          'interpolate',
          ['linear'],
          ['zoom'],
          0, 10,
          10, 25,
          14, 40,
        ],
        'heatmap-opacity': [
          'interpolate',
          ['linear'],
          ['zoom'],
          0, 0.5,
          6, 0.7,
          10, 0.9,
        ],
      },
    });
  }
}

function addCircleLayer(map: Map, sourceId: string, layerId: string, minZoom: number, maxZoom: number) {
  const circleLayerId = `${layerId}-circles`;
  if (!map.getLayer(circleLayerId)) {
    map.addLayer({
      id: circleLayerId,
      type: 'circle',
      source: sourceId,
      minzoom: minZoom,
      maxzoom: maxZoom,
      filter: ['all', ['==', '$type', 'Point']],
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
        'circle-color': [
          'match',
          ['get', 'band'],
          'CRITICAL', '#991b1b',
          'HIGH', '#dc2626',
          'MEDIUM', '#f59e0b',
          'LOW', '#84cc16',
          'NEGLIGIBLE', '#22c55e',
          '#64748b',
        ],
        'circle-stroke-width': 1.5,
        'circle-stroke-color': '#ffffff',
        'circle-stroke-opacity': 0.8,
        'circle-opacity': 0.85,
      },
    });
  }
}

function addHighlightLayer(map: Map, sourceId: string, layerId: string, minZoom: number, maxZoom: number) {
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
        'circle-color': '#fbbf24',
        'circle-stroke-width': 3,
        'circle-stroke-color': '#f59e0b',
        'circle-opacity': 1,
      },
    });
  }
}

function cleanupLayers(map: Map, layerId: string, sourceId: string) {
  if (map.getLayer(`${layerId}-highlight`)) map.removeLayer(`${layerId}-highlight`);
  if (map.getLayer(`${layerId}-circles`)) map.removeLayer(`${layerId}-circles`);
  if (map.getLayer(`${layerId}-heatmap`)) map.removeLayer(`${layerId}-heatmap`);
  if (map.getSource(sourceId)) map.removeSource(sourceId);
}

/**
 * Risk Layer with Popup on Hover
 */
export function RiskLayerWithPopup({
  map,
  data,
  sourceId = 'risk',
  layerId = 'risk-layer',
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: any) {
  const popupRef = useRef<maplibregl.Popup | null>(null);

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
            'match', ['get', 'band'],
            'CRITICAL', '#991b1b',
            'HIGH', '#dc2626',
            'MEDIUM', '#f59e0b',
            'LOW', '#84cc16',
            'NEGLIGIBLE', '#22c55e',
            '#64748b',
          ],
          'circle-stroke-width': 1.5,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.8,
          'circle-opacity': 0.85,
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
          className: 'risk-popup',
        });
      }

      const props = feature.properties as any;
      const bandColors: Record<string, string> = {
        CRITICAL: '#991b1b',
        HIGH: '#dc2626',
        MEDIUM: '#f59e0b',
        LOW: '#84cc16',
        NEGLIGIBLE: '#22c55e',
      };
      const bandColor = bandColors[props.band] || '#64748b';

      const content = `
        <div class="p-2 min-w-[220px]">
          <h4 class="font-semibold text-grid-900">Risk Score</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>Asset ID: <span class="font-medium">${props.assetId?.substring(0, 8)}...</span></div>
            <div>Band: <span class="font-medium" style="color: ${bandColor}">${props.band}</span></div>
            <div>Score: <span class="font-medium">${Math.round(props.score * 100)}%</span></div>
            <div>Model: <span class="font-medium">${props.modelName} v${props.modelVersion}</span></div>
            <div>Computed: <span class="font-medium">${new Date(props.computedAt).toLocaleString()}</span></div>
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