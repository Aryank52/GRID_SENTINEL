import { useEffect, useRef, useState, useCallback } from 'react';
import maplibregl, { Map, LngLatBoundsLike, MapMouseEvent, FilterSpecification, MapGeoJSONFeature } from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import type { MapViewport, SelectedFeature, GeoJsonFeature } from '@spatial/spatial';

// Module-level store for map methods (singleton pattern for map instance)
const mapMethodsStore = {
  current: null as null | {
    flyTo: (options: any) => void;
    setLayerVisibility: (layerId: string, visible: boolean) => void;
    setLayerFilter: (layerId: string, filter: FilterSpecification) => void;
    getFeatureState: (sourceId: string, featureId: string | number) => any;
    setFeatureState: (sourceId: string, featureId: string | number, state: Record<string, unknown>) => void;
    removeFeatureState: (sourceId: string, featureId: string | number) => void;
    queryRenderedFeatures: (point: [number, number], options?: { layers?: string[] }) => any;
    getMap: () => Map | null;
    addLayer: (layer: maplibregl.LayerSpecification, beforeId?: string) => void;
    removeLayer: (layerId: string) => void;
  },
};

interface MapLibreMapProps {
  /** Initial viewport state */
  initialViewport?: Partial<MapViewport>;
  /** Map style URL (defaults to MapLibre basic style) */
  style?: string;
  /** Container className */
  className?: string;
  /** Callback when map is loaded */
  onLoad?: (map: Map) => void;
  /** Callback when viewport changes (debounced) */
  onViewportChange?: (viewport: MapViewport) => void;
  /** Callback when a feature is clicked */
  onFeatureClick?: (feature: SelectedFeature) => void;
  /** Callback when map is clicked (no feature) */
  onMapClick?: (lngLat: [number, number]) => void;
  /** Enable map controls (zoom, rotation, etc.) */
  showControls?: boolean;
  /** Custom attribution */
  customAttribution?: string;
}

/**
 * MapLibre GL Map Wrapper Component
 * Provides a robust MapLibre integration with lifecycle management
 */
export function MapLibreMap({
  initialViewport = {},
  style = 'https://demotiles.maplibre.org/style.json',
  className = '',
  onLoad,
  onViewportChange,
  onFeatureClick,
  onMapClick,
  showControls = true,
  customAttribution,
}: MapLibreMapProps) {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<Map | null>(null);
  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const [mapError, setMapError] = useState<Error | null>(null);
  const viewportChangeTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Initialize map
  useEffect(() => {
    if (!mapContainerRef.current || mapRef.current) return;

    // Check for MapLibre GL support
    if (typeof window === 'undefined' || !window.WebGLRenderingContext) {
      const error = new Error('MapLibre GL is not supported in this browser');
      setMapError(error);
      return;
    }

    try {
      const map = new Map({
        container: mapContainerRef.current,
        style,
        center: initialViewport.center ?? [-98.5795, 39.8283], // Center of US
        zoom: initialViewport.zoom ?? 4,
        bearing: initialViewport.bearing ?? 0,
        pitch: initialViewport.pitch ?? 0,
        attributionControl: false, // We'll add custom attribution
        preserveDrawingBuffer: false,
        antialias: true,
      });

      mapRef.current = map;

      // Add navigation controls
      if (showControls) {
        map.addControl(new maplibregl.NavigationControl({
          showCompass: true,
          showZoom: true,
          visualizePitch: true,
        }), 'top-right');

        map.addControl(new maplibregl.ScaleControl({
          maxWidth: 200,
          unit: 'metric',
        }), 'bottom-right');

        map.addControl(new maplibregl.FullscreenControl(), 'top-right');
      }

      // Custom attribution
      if (customAttribution) {
        map.addControl(new maplibregl.AttributionControl({
          compact: true,
          customAttribution,
        }), 'bottom-left');
      }

      // Map loaded event
      map.on('load', () => {
        setIsMapLoaded(true);
        onLoad?.(map);
      });

      // Error handling
      map.on('error', (event) => {
        console.error('MapLibre error:', event.error);
        setMapError(event.error instanceof Error ? event.error : new Error(String(event.error)));
      });

      // Viewport change handler (debounced)
      const handleViewportChange = () => {
        if (!mapRef.current) return;

        if (viewportChangeTimeoutRef.current) {
          clearTimeout(viewportChangeTimeoutRef.current);
        }

        viewportChangeTimeoutRef.current = setTimeout(() => {
          const viewport: MapViewport = {
            center: map.getCenter().toArray() as [number, number],
            zoom: map.getZoom(),
            bearing: map.getBearing(),
            pitch: map.getPitch(),
          };
          onViewportChange?.(viewport);
        }, 150); // 150ms debounce
      };

      map.on('moveend', handleViewportChange);

      // Feature click handler
      const handleFeatureClick = (event: MapMouseEvent & { features?: MapGeoJSONFeature[] }) => {
        if (!event.features || event.features.length === 0) return;

        // Find the topmost feature with properties
        const feature = event.features.find(f => f.properties && Object.keys(f.properties).length > 0);
        if (!feature) return;

        // Get source and layer info
        const sourceId = (feature as any).source;
        const layerId = (feature as any).layer?.id;

        onFeatureClick?.({
          feature: feature as unknown as GeoJsonFeature,
          layerId,
          sourceId,
        });
      };

      map.on('click', handleFeatureClick);

      // Map click (no feature) handler
      const handleMapClick = (event: MapMouseEvent) => {
        // Check if click was on a feature
        const features = map.queryRenderedFeatures(event.point);
        if (features.length === 0) {
          const lngLat = event.lngLat.toArray() as [number, number];
          onMapClick?.(lngLat);
        }
      };

      map.on('click', handleMapClick);

      // Cleanup
      return () => {
        if (viewportChangeTimeoutRef.current) {
          clearTimeout(viewportChangeTimeoutRef.current);
        }
        map.off('moveend', handleViewportChange);
        map.off('click', handleFeatureClick);
        map.off('click', handleMapClick);
        map.remove();
        mapRef.current = null;
        setIsMapLoaded(false);
      };
    } catch (error) {
      console.error('Failed to initialize MapLibre map:', error);
      setMapError(error instanceof Error ? error : new Error(String(error)));
    }
  }, [style, initialViewport, onLoad, onViewportChange, onFeatureClick, onMapClick, showControls, customAttribution]);

  // Public methods for external control
  const mapMethods = {
    // Fly to a location
    flyTo: useCallback((options: {
      center?: [number, number];
      zoom?: number;
      bearing?: number;
      pitch?: number;
      duration?: number;
      essential?: boolean;
    }) => {
      mapRef.current?.flyTo({
        ...options,
        essential: options.essential ?? true,
        duration: options.duration ?? 2000,
      });
    }, []),

    // Fit to bounds
    fitBounds: useCallback((bounds: LngLatBoundsLike, options?: { padding?: number; duration?: number }) => {
      mapRef.current?.fitBounds(bounds, {
        padding: options?.padding ?? 50,
        duration: options?.duration ?? 2000,
      });
    }, []),

    // Set viewport
    setViewport: useCallback((viewport: Partial<MapViewport>) => {
      if (!mapRef.current) return;
      if (viewport.center) mapRef.current.setCenter(viewport.center);
      if (viewport.zoom !== undefined) mapRef.current.setZoom(viewport.zoom);
      if (viewport.bearing !== undefined) mapRef.current.setBearing(viewport.bearing);
      if (viewport.pitch !== undefined) mapRef.current.setPitch(viewport.pitch);
    }, []),

    // Get current viewport
    getViewport: useCallback((): MapViewport | null => {
      if (!mapRef.current) return null;
      return {
        center: mapRef.current.getCenter().toArray() as [number, number],
        zoom: mapRef.current.getZoom(),
        bearing: mapRef.current.getBearing(),
        pitch: mapRef.current.getPitch(),
      };
    }, []),

    // Add source
    addSource: useCallback((sourceId: string, source: maplibregl.GeoJSONSourceSpecification) => {
      if (mapRef.current?.getSource(sourceId)) {
        (mapRef.current.getSource(sourceId) as maplibregl.GeoJSONSource).setData(source.data as GeoJSON.FeatureCollection);
      } else {
        mapRef.current?.addSource(sourceId, source);
      }
    }, []),

    // Update source data
    updateSource: useCallback((sourceId: string, data: GeoJSON.FeatureCollection) => {
      const source = mapRef.current?.getSource(sourceId);
      if (source && 'setData' in source) {
        (source as maplibregl.GeoJSONSource).setData(data);
      }
    }, []),

    // Add layer
    addLayer: useCallback((layer: maplibregl.LayerSpecification, beforeId?: string) => {
      if (!mapRef.current?.getLayer(layer.id)) {
        mapRef.current?.addLayer(layer, beforeId);
      }
    }, []),

    // Remove layer
    removeLayer: useCallback((layerId: string) => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current?.removeLayer(layerId);
      }
    }, []),

    // Set layer visibility
    setLayerVisibility: useCallback((layerId: string, visible: boolean) => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current.setLayoutProperty(layerId, 'visibility', visible ? 'visible' : 'none');
      }
    }, []),

    // Set filter on layer
    setLayerFilter: useCallback((layerId: string, filter: FilterSpecification) => {
      if (mapRef.current?.getLayer(layerId)) {
        mapRef.current.setFilter(layerId, filter);
      }
    }, []),

    // Get feature state
    getFeatureState: useCallback((sourceId: string, featureId: string | number) => {
      return mapRef.current?.getFeatureState({ source: sourceId, id: featureId });
    }, []),

    // Set feature state (for highlighting)
    setFeatureState: useCallback((sourceId: string, featureId: string | number, state: Record<string, unknown>) => {
      mapRef.current?.setFeatureState({ source: sourceId, id: featureId }, state);
    }, []),

    // Remove feature state
    removeFeatureState: useCallback((sourceId: string, featureId: string | number) => {
      mapRef.current?.removeFeatureState({ source: sourceId, id: featureId });
    }, []),

    // Query rendered features at point
    queryRenderedFeatures: useCallback((point: [number, number], options?: { layers?: string[] }) => {
      return mapRef.current?.queryRenderedFeatures(point, options);
    }, []),

    // Get map instance (for advanced usage)
    getMap: useCallback(() => mapRef.current, []),
  };

  // Store map methods in module-level store for external access
  mapMethodsStore.current = mapMethods;

  // Error state
  if (mapError) {
    return (
      <div className={`relative w-full h-full ${className}`}>
        <div className="absolute inset-0 flex items-center justify-center bg-grid-100 rounded-lg border border-alert-200 p-8">
          <div className="text-center text-alert-700">
            <div className="text-2xl mb-2">⚠</div>
            <p className="font-medium">Map Failed to Load</p>
            <p className="text-sm text-alert-500 mt-1">{mapError.message}</p>
            <button
              onClick={() => window.location.reload()}
              className="mt-4 px-4 py-2 btn btn-primary text-sm"
            >
              Reload Page
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={`relative w-full h-full ${className}`}>
      <div
        ref={mapContainerRef}
        className="absolute inset-0"
        style={{ width: '100%', height: '100%' }}
      />
      {!isMapLoaded && (
        <div className="absolute inset-0 flex items-center justify-center bg-grid-100 z-10">
          <div className="text-center text-grid-500">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-grid-600 mx-auto mb-3"></div>
            <p className="text-sm">Loading map...</p>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * Hook for using map methods outside the component
 */
export function useMapLibre() {
  return mapMethodsStore.current;
}