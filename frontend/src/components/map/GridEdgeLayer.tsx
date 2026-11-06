import { useEffect, useRef } from 'react';
import type { Map } from 'maplibre-gl';
import type { GridEdgeProperties, GeoJsonFeatureCollection } from '@spatial/spatial';
import maplibregl, { FilterSpecification } from 'maplibre-gl';

export interface GridEdgeLayerProps {
  map: Map | null;
  data: GeoJsonFeatureCollection<GridEdgeProperties> | null;
  sourceId?: string;
  layerId?: string;
  minZoom?: number;
  maxZoom?: number;
  selectedFeatureId?: string | null;
  visible?: boolean;
}

const DEFAULT_SOURCE_ID = 'grid-edges';
const DEFAULT_LAYER_ID = 'grid-edges-layer';

/**
 * Grid Edges Layer Component
 * Renders grid topology edges (transmission lines, transformers, cables)
 * as lines with styling based on edge type, voltage, and status
 */
export function GridEdgeLayer({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  selectedFeatureId = null,
  visible = true,
}: GridEdgeLayerProps) {
  // Initialize source and layer
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;

    // Add source if not exists
    if (!map.getSource(sourceId)) {
      map.addSource(sourceId, {
        type: 'geojson',
        data: data || { type: 'FeatureCollection', features: [] },
        lineMetrics: true, // Enable line metrics for gradient/pattern
      });
    } else if (data) {
      const source = map.getSource(sourceId);
      if (source && 'setData' in source) {
        (source as any).setData(data);
      }
    }

    // Base edge layer
    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'line',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['==', '$type', 'LineString'],
        layout: {
          'line-cap': 'round',
          'line-join': 'round',
        },
        paint: {
          'line-width': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 1,
            6, 2,
            10, 3,
            14, 4,
            18, 5,
          ],
          'line-color': [
            'match',
            ['get', 'type'],
            'TRANSMISSION_LINE', '#1e3a8a',       // Blue-900
            'UNDERGROUND_CABLE', '#065f46',       // Emerald-900
            'SUBMARINE_CABLE', '#0c4a6e',         // Sky-900
            'TRANSFORMER', '#7c2d12',             // Orange-900
            'PHASE_SHIFTER', '#92400e',           // Amber-800
            'SERIES_COMPENSATOR', '#422006',      // Yellow-900
            'SHUNT_COMPENSATOR', '#365314',       // Lime-900
            'HVDC_LINK', '#4c1d95',               // Violet-900
            'BUSBAR', '#78350f',                  // Amber-900
            'GENERATOR_LEAD', '#be123c',          // Rose-700
            'LOAD_TAP', '#7f1d1d',                // Red-900
            '#374151',                            // Gray-700 default
          ],
          'line-opacity': 0.8,
          'line-dasharray': [
            'match',
            ['get', 'type'],
            'TRANSFORMER', [4, 4],
            'PHASE_SHIFTER', [6, 3],
            'SERIES_COMPENSATOR', [2, 2],
            'SHUNT_COMPENSATOR', [8, 4],
            [],
          ] as unknown as any[],
        },
      });
    }

    // Voltage-based width overlay (for high voltage emphasis)
    const voltageLayerId = `${layerId}-voltage`;
    if (!map.getLayer(voltageLayerId)) {
      map.addLayer({
        id: voltageLayerId,
        type: 'line',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: [
          'all',
          ['==', '$type', 'LineString'],
          ['>=', ['get', 'voltageLevelKv'], 230],
        ] as unknown as FilterSpecification,
        layout: {
          'line-cap': 'round',
          'line-join': 'round',
        },
        paint: {
          'line-width': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 1,
            6, 1.5,
            10, 2,
            14, 2.5,
          ],
          'line-color': '#fbbf24', // Amber-400
          'line-opacity': 0.4,
        },
      });
    }

    // Highlight layer for selected edge
    const highlightLayerId = `${layerId}-highlight`;
    if (!map.getLayer(highlightLayerId)) {
      map.addLayer({
        id: highlightLayerId,
        type: 'line',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['all', ['==', '$type', 'LineString'], ['==', 'id', '']],
        layout: {
          'line-cap': 'round',
          'line-join': 'round',
        },
        paint: {
          'line-width': [
            'interpolate',
            ['linear'],
            ['zoom'],
            0, 3,
            6, 4,
            10, 5,
            14, 6,
          ],
          'line-color': '#fbbf24', // Amber-400
          'line-opacity': 1,
          'line-dasharray': [0, 0], // Solid
        },
      });
    }

    return () => {
      if (map.getLayer(`${layerId}-highlight`)) {
        map.removeLayer(`${layerId}-highlight`);
      }
      if (map.getLayer(`${layerId}-voltage`)) {
        map.removeLayer(`${layerId}-voltage`);
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
        ? ['all', ['==', '$type', 'LineString'], ['==', 'id', selectedFeatureId]]
        : ['all', ['==', '$type', 'LineString'], ['==', 'id', '']]);
    }
  }, [map, layerId, selectedFeatureId]);

  // Update visibility
  useEffect(() => {
    if (!map || !map.isStyleLoaded()) return;
    if (map.getLayer(layerId)) {
      map.setLayoutProperty(layerId, 'visibility', visible ? 'visible' : 'none');
    }
    if (map.getLayer(`${layerId}-voltage`)) {
      map.setLayoutProperty(`${layerId}-voltage`, 'visibility', visible ? 'visible' : 'none');
    }
    if (map.getLayer(`${layerId}-highlight`)) {
      map.setLayoutProperty(`${layerId}-highlight`, 'visibility', visible ? 'visible' : 'none');
    }
  }, [map, layerId, visible]);

  return null;
}

/**
 * Grid Edges Layer with Popup on Hover
 */
export function GridEdgeLayerWithPopup({
  map,
  data,
  sourceId = DEFAULT_SOURCE_ID,
  layerId = DEFAULT_LAYER_ID,
  minZoom = 0,
  maxZoom = 22,
  visible: _visible = true,
}: GridEdgeLayerProps) {
  const popupRef = useRef<maplibregl.Popup | null>(null);

  useEffect(() => {
    if (!map || !map.isStyleLoaded() || !data) return;

    // Initialize source and layer (same as GridEdgeLayer)
    if (!map.getSource(sourceId)) {
      map.addSource(sourceId, {
        type: 'geojson',
        data,
        lineMetrics: true,
      });
    }

    if (!map.getLayer(layerId)) {
      map.addLayer({
        id: layerId,
        type: 'line',
        source: sourceId,
        minzoom: minZoom,
        maxzoom: maxZoom,
        filter: ['==', '$type', 'LineString'],
        layout: {
          'line-cap': 'round',
          'line-join': 'round',
        },
        paint: {
          'line-width': [
            'interpolate', ['linear'], ['zoom'],
            0, 1, 6, 2, 10, 3, 14, 4, 18, 5,
          ],
          'line-color': [
            'match', ['get', 'type'],
            'TRANSMISSION_LINE', '#1e3a8a',
            'UNDERGROUND_CABLE', '#065f46',
            'SUBMARINE_CABLE', '#0c4a6e',
            'TRANSFORMER', '#7c2d12',
            'PHASE_SHIFTER', '#92400e',
            'SERIES_COMPENSATOR', '#422006',
            'SHUNT_COMPENSATOR', '#365314',
            'HVDC_LINK', '#4c1d95',
            'BUSBAR', '#78350f',
            'GENERATOR_LEAD', '#be123c',
            'LOAD_TAP', '#7f1d1d',
            '#374151',
          ],
          'line-opacity': 0.8,
          'line-dasharray': [
            'match', ['get', 'type'],
            'TRANSFORMER', [4, 4],
            'PHASE_SHIFTER', [6, 3],
            'SERIES_COMPENSATOR', [2, 2],
            'SHUNT_COMPENSATOR', [8, 4],
            [],
          ] as unknown as any[],
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
          offset: 10,
          className: 'grid-edge-popup',
        });
      }

      const props = feature.properties as any;
      const mvaRating = props.mvaRating?.toFixed(1) || 'N/A';
      const content = `
        <div class="p-2 min-w-[220px]">
          <h4 class="font-semibold text-grid-900">${props.name}</h4>
          <div class="text-xs text-grid-500 mt-1 space-y-1">
            <div>Type: <span class="font-medium capitalize">${props.type.toLowerCase().replace('_', ' ')}</span></div>
            <div>Voltage: <span class="font-medium">${props.voltageLevelKv} kV</span></div>
            <div>MVA Rating: <span class="font-medium">${mvaRating} MVA</span></div>
            <div>Length: <span class="font-medium">${props.lengthKm?.toFixed(1) || 'N/A'} km</span></div>
            <div>Circuits: <span class="font-medium">${props.circuitCount}</span></div>
            ${props.conductorType ? `<div>Conductor: <span class="font-medium">${props.conductorType}</span></div>` : ''}
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