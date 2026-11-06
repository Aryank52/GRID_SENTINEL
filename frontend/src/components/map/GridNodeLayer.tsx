import { useEffect, useRef } from 'react';
import type { Map } from 'maplibre-gl';
import type { GridNodeProperties, GeoJsonFeatureCollection, GeoJsonFeature } from '@spatial/spatial';

export interface GridNodeLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<GridNodeProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  onFeatureClick?: (feature: GeoJsonFeature<GridNodeProperties>) => void;
  visible?: boolean;
}

const DEFAULT_SOURCE_ID = 'grid-nodes';
const DEFAULT_LAYER_ID = 'grid-nodes-layer';

/**
 * Grid Nodes Layer Component
 * Renders grid topology nodes (substations, switching stations, etc.)
 * as circles with styling based on node type and voltage level
 */
export function GridNodeLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  onFeatureClick: _onFeatureClick,
  visible = true,
}: GridNodeLayerProps) {
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
      // Update existing source
      const source = map.getSource(sourceId);
      if (source && 'setData' in source) {
        (source as any).setData(data);
      }
    }

    // Add layer if not exists
    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'circle',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['==', '$type', 'Point'],
        paint: {
          // Base circle
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
            ['get', 'type'],
            'SUBSTATION', '#1e40af',       // Blue-800
            'SWITCHING_STATION', '#2563eb', // Blue-600
            'GENERATION', '#059669',        // Emerald-600
            'LOAD', '#dc2626',              // Red-600
            'JUNCTION', '#7c3aed',          // Violet-600
            'HVDC_CONVERTER', '#ea580c',    // Orange-600
            'FACTS_DEVICE', '#d97706',      // Amber-600
            'INTERCONNECT', '#0891b2',      // Cyan-600
            '#64748b',                      // Slate-500 default
          ],
          'circle-stroke-width': 2,
          'circle-stroke-color': '#ffffff',
          'circle-stroke-opacity': 0.9,
          'circle-opacity': 0.9,
        },
      });
    }

    // Highlight layer for selected node
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
            0, 6,
            6, 8,
            10, 10,
            14, 12,
            18, 14,
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

  // Update selected feature highlight
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
    if (map.getLayer(layerId)) {
      map.setLayoutProperty(layerId, 'visibility', visible ? 'visible' : 'none');
    }
    if (map.getLayer(`${layerId}-highlight`)) {
      map.setLayoutProperty(`${layerId}-highlight`, 'visibility', visible ? 'visible' : 'none');
    }
  }, [map, layerId, visible]);

  // Return nothing - this is a side-effect component
  return null;
}

/**
 * Grid Nodes Layer with Popup on Hover
 */
export function GridNodeLayerWithPopup({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: GridNodeLayerProps) {
  const popupRef = useRef<maplibregl.Popup | null>(null);

  useEffect(() => {
    if (!map || !map.isStyleLoaded() || !data) return;

    // Initialize source and layer (same as GridNodeLayer)
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
            'match', ['get', 'type'],
            'SUBSTATION', '#1e40af',
            'SWITCHING_STATION', '#2563eb',
            'GENERATION', '#059669',
            'LOAD', '#dc2626',
            'JUNCTION', '#7c3aed',
            'HVDC_CONVERTER', '#ea580c',
            'FACTS_DEVICE', '#d97706',
            'INTERCONNECT', '#0891b2',
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
          className: 'grid-node-popup',
        });
      }

      const props = feature.properties as GridNodeProperties;
      const content = `
        <div class="p-2 min-w-[200px]">
          <h4 class="font-semibold text-grid-900">${props.name}</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>Type: <span class="font-medium capitalize">${props.type.toLowerCase().replace('_', ' ')}</span></div>
            <div>Voltage: <span class="font-medium">${props.voltageLevelKv} kV</span></div>
            <div>Degree: <span class="font-medium">${props.degree} connections</span></div>
            ${props.operator ? `<div>Operator: <span class="font-medium">${props.operator}</span></div>` : ''}
          </div>
        </div>
      `;

      popupRef.current?.setLngLat(e.lngLat).setHTML(content).addTo(map);
    };

    // Mouse leave - hide popup
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
import maplibregl from 'maplibre-gl';