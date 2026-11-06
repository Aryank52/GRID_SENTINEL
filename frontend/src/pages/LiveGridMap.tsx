import { useState, useCallback, useMemo, useEffect } from 'react';
import { Map, Layers, Maximize, Minimize, MapPin, AlertTriangle, Zap, Activity, RefreshCw, Home, WifiOff, Bell } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Button } from '../components/ui/Button';
import { MapLibreMap, useMapLibre } from '../components/map/MapLibreMap';
import { MapFilters } from '../components/map/MapFilters';
import { DetailDrawer } from '../components/map/DetailDrawer';
import { Legend } from '../components/map/Legend';
import { useViewportSpatialData } from '../hooks/useSpatialData';
import { useRealtimeEvents } from '../hooks/useRealtimeEvents';
import type { FeatureType, SpatialQueryParams, GeoJsonFeature, SelectedFeature } from '@spatial/spatial';

type LayerType = 'nodes' | 'edges' | 'assets' | 'threats' | 'incidents' | 'risk';

interface LayerConfig {
  id: LayerType;
  label: string;
  icon: React.ReactNode;
  visible: boolean;
}

const LAYER_CONFIG: LayerConfig[] = [
  { id: 'nodes', label: 'Grid Nodes', icon: <MapPin className="w-4 h-4" />, visible: true },
  { id: 'edges', label: 'Grid Edges', icon: <Activity className="w-4 h-4" />, visible: true },
  { id: 'assets', label: 'Assets', icon: <Zap className="w-4 h-4" />, visible: true },
  { id: 'threats', label: 'Threats', icon: <AlertTriangle className="w-4 h-4" />, visible: true },
  { id: 'incidents', label: 'Incidents', icon: <Activity className="w-4 h-4" />, visible: true },
  { id: 'risk', label: 'Risk Heatmap', icon: <Zap className="w-4 h-4" />, visible: false },
];

export function LiveGridMap() {
  const [viewport, setViewport] = useState<{ center: [number, number]; zoom: number } | null>(null);
  const [selectedFeature, setSelectedFeature] = useState<{ feature: GeoJsonFeature<any>; type: FeatureType } | null>(null);
  const [layerVisibility, setLayerVisibility] = useState<Record<LayerType, boolean>>(
    LAYER_CONFIG.reduce((acc, layer) => ({ ...acc, [layer.id]: layer.visible }), {}) as Record<LayerType, boolean>
  );
  const [filtersExpanded, setFiltersExpanded] = useState(false);
  const [legendCollapsed, setLegendCollapsed] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [riskHeatmapMode, setRiskHeatmapMode] = useState(false);

  // Real-time events integration
  const { isConnected, isReconnecting, isOffline, connectionDisplayText, latestEvent, reconnect } = useRealtimeEvents();

  // Get map methods
  const mapMethods = useMapLibre();
  const _map = mapMethods?.getMap();

  // Spatial query parameters derived from viewport and UI state
  const spatialParams = useMemo((): SpatialQueryParams => {
    if (!viewport) return { limit: 1000 };

    const bbox = [
      viewport.center[0] - 0.5 / Math.pow(2, viewport.zoom - 10),
      viewport.center[1] - 0.5 / Math.pow(2, viewport.zoom - 10),
      viewport.center[0] + 0.5 / Math.pow(2, viewport.zoom - 10),
      viewport.center[1] + 0.5 / Math.pow(2, viewport.zoom - 10),
    ].join(',');

    return {
      bbox,
      limit: 2000,
      // Asset filters
      assetType: undefined,
      criticality: undefined,
      riskBand: undefined,
      minHealthIndex: undefined,
      maxHealthIndex: undefined,
      // Grid filters
      nodeType: undefined,
      edgeType: undefined,
      minVoltageKv: undefined,
      maxVoltageKv: undefined,
      // Threat filters
      severity: undefined,
      threatType: undefined,
      threatStatus: undefined,
      minConfidence: 0.5,
      showOnlyActionable: false,
      // Incident filters
      incidentState: undefined,
      incidentSeverity: undefined,
      activeOnly: true,
    };
  }, [viewport]);

  // Fetch spatial data for current viewport
  const {
    nodes: _nodes,
    edges: _edges,
    assets: _assets,
    threats: _threats,
    incidents: _incidents,
    riskScores: _riskScores,
    isLoading,
    isError,
    error,
    refetch,
  } = useViewportSpatialData(viewport ? {
    minLng: parseFloat(spatialParams.bbox?.split(',')[0] || '0'),
    minLat: parseFloat(spatialParams.bbox?.split(',')[1] || '0'),
    maxLng: parseFloat(spatialParams.bbox?.split(',')[2] || '0'),
    maxLat: parseFloat(spatialParams.bbox?.split(',')[3] || '0'),
  } : null, {
    includeNodes: layerVisibility.nodes,
    includeEdges: layerVisibility.edges,
    includeAssets: layerVisibility.assets,
    includeThreats: layerVisibility.threats,
    includeIncidents: layerVisibility.incidents,
    includeRisk: layerVisibility.risk,
  });

  // Trigger refetch when relevant real-time events arrive
  useEffect(() => {
    if (!latestEvent) return;
    
    const eventType = latestEvent.eventType;
    // Refetch spatial data for relevant event types
    if (
      eventType === 'ThreatDetected' ||
      eventType === 'ThreatEscalated' ||
      eventType === 'IncidentCreated' ||
      eventType === 'IncidentStateChanged' ||
      eventType === 'RiskScoreUpdated' ||
      eventType === 'GridAssetStatusChanged' ||
      eventType === 'SimulationCompleted'
    ) {
      // Debounce refetch to avoid excessive requests
      const timer = setTimeout(() => {
        refetch();
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [latestEvent, refetch]);

  // Handle feature selection
  const handleFeatureClick = useCallback((selectedFeature: SelectedFeature) => {
    setSelectedFeature({ feature: selectedFeature.feature, type: selectedFeature.feature.properties?.type as FeatureType || 'unknown' });
  }, []);

  // Handle layer visibility toggle
  const toggleLayer = useCallback((layerId: LayerType, visible: boolean) => {
    setLayerVisibility(prev => ({ ...prev, [layerId]: visible }));
  }, []);

  // Handle viewport change from map
  const handleViewportChange = useCallback((newViewport: any) => {
    setViewport(newViewport);
  }, []);

  // Handle map load - fit to data bounds if available
  const handleMapLoad = useCallback((_map: any) => {
    console.log('Map loaded');
  }, []);

  // Toggle fullscreen
  const toggleFullscreen = useCallback(() => {
    if (_map) {
      const container = _map.getContainer();
      if (!isFullscreen) {
        if (container.requestFullscreen) {
          container.requestFullscreen();
        }
        setIsFullscreen(true);
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
        setIsFullscreen(false);
      }
    }
  }, [isFullscreen, _map]);

  // Reset view to default
  const resetView = useCallback(() => {
    if (_map) {
      _map.flyTo({
        center: [-98.5795, 39.8283],
        zoom: 4,
        duration: 1000,
      });
    }
  }, [_map]);

  // Refresh data
  const handleRefresh = useCallback(() => {
    refetch();
  }, [refetch]);

  // Determine which feature type is selected for drawer
  const selectedFeatureType = selectedFeature?.type;
  const selectedFeatureData = selectedFeature?.feature ?? null;

  const isDrawerOpen = selectedFeatureData !== null;

  return (
    <PageTemplate
      title="Live Grid Map"
      description="Real-time grid topology visualization with active alerts and asset overlay"
      icon={Map}
      actions={
        <div className="flex items-center gap-2">
          {/* Connection Status Indicator */}
          <div className="flex items-center gap-2 px-2 py-1 rounded-lg bg-white/90 backdrop-blur border border-grid-200">
            <span className={`
              w-2 h-2 rounded-full
              ${isConnected ? 'bg-safe-500 animate-pulse' : isReconnecting ? 'bg-warning-500 animate-bounce' : 'bg-alert-500'}
            `} />
            <span className={`
              text-xs font-medium
              ${isConnected ? 'text-safe-700' : isReconnecting ? 'text-warning-700' : 'text-alert-700'}
            `}>
              {connectionDisplayText}
            </span>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={resetView}
            aria-label="Reset view"
            title="Reset View"
          >
            <Home className="w-4 h-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={handleRefresh}
            disabled={isLoading}
            aria-label="Refresh data"
            title="Refresh Data"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={toggleFullscreen}
            aria-label={isFullscreen ? 'Exit fullscreen' : 'Enter fullscreen'}
            title={isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'}
          >
            {isFullscreen ? <Minimize className="w-4 h-4" /> : <Maximize className="w-4 h-4" />}
          </Button>
          {isOffline && (
            <Button
              variant="outline"
              size="sm"
              onClick={reconnect}
              aria-label="Reconnect to real-time stream"
            >
              <WifiOff className="w-4 h-4 mr-1" />
              Reconnect
            </Button>
          )}
        </div>
      }
    >
      <div className="relative h-[calc(100vh-200px)] min-h-[500px]">
        {/* Map */}
        <MapLibreMap
          initialViewport={{
            center: [-98.5795, 39.8283],
            zoom: 4,
          }}
          style="https://demotiles.maplibre.org/style.json"
          className="w-full h-full"
          onLoad={handleMapLoad}
          onViewportChange={handleViewportChange}
          onFeatureClick={handleFeatureClick}
          onMapClick={() => setSelectedFeature(null)}
          showControls={true}
          customAttribution="Grid Sentinel | MapLibre | OpenStreetMap"
        />

        {/* Loading Overlay */}
        {isLoading && (
          <div className="absolute inset-0 bg-white/80 flex items-center justify-center z-20">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto mb-2"></div>
              <p className="text-sm text-grid-500">Loading spatial data...</p>
            </div>
          </div>
        )}

        {/* Error Overlay */}
        {isError && (
          <div className="absolute top-4 left-4 right-4 z-30">
            <div className="max-w-md mx-auto alert alert-warning">
              <AlertTriangle className="w-5 h-5" />
              <span className="ml-2">Failed to load spatial data: {error?.message || 'Unknown error'}</span>
              <Button variant="ghost" size="sm" onClick={handleRefresh} className="ml-2">
                Retry
              </Button>
            </div>
          </div>
        )}

        {/* Connection Status - Top Right */}
        <div className="absolute top-4 right-4 z-20 flex flex-col gap-2">
          <div className={`
            card bg-white/90 backdrop-blur shadow-lg border border-grid-200 px-3 py-2 flex items-center gap-2
            ${isConnected ? 'border-safe-300' : isReconnecting ? 'border-warning-300' : 'border-alert-300'}
          `}>
            <span className={`
              w-2 h-2 rounded-full
              ${isConnected ? 'bg-safe-500 animate-pulse' : isReconnecting ? 'bg-warning-500 animate-bounce' : 'bg-alert-500'}
            `} />
            <span className="text-sm font-medium text-grid-900">
              {connectionDisplayText}
              {isReconnecting && <span className="ml-1 text-xs text-warning-600">({isReconnecting ? '...' : ''})</span>}
            </span>
          </div>

          {/* Layer Controls */}
          <div className="card bg-white/90 backdrop-blur shadow-lg border border-grid-200 p-2">
            <div className="flex items-center gap-2 mb-2">
              <Layers className="w-5 h-5 text-grid-600" />
              <span className="font-medium text-grid-900">Layers</span>
            </div>
            <div className="space-y-1.5">
              {LAYER_CONFIG.map((layer) => (
                <label
                  key={layer.id}
                  className={`flex items-center gap-2 cursor-pointer px-2 py-1.5 rounded transition-colors ${
                    layerVisibility[layer.id] ? 'bg-primary-50 text-primary-700' : 'text-grid-600 hover:bg-grid-50'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={layerVisibility[layer.id]}
                    onChange={(e) => toggleLayer(layer.id, e.target.checked)}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="flex items-center gap-2 text-sm">
                    {layer.icon}
                    {layer.label}
                  </span>
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Risk Heatmap Toggle */}
        {layerVisibility.risk && (
          <div className="absolute top-4 right-4 z-20 mt-10">
            <div className="card bg-white/90 backdrop-blur shadow-lg border border-grid-200 p-2">
              <label className="flex items-center gap-2 cursor-pointer text-sm text-grid-700">
                <input
                  type="checkbox"
                  checked={riskHeatmapMode}
                  onChange={(e) => setRiskHeatmapMode(e.target.checked)}
                  className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                />
                Heatmap View
              </label>
            </div>
          </div>
        )}

        {/* Filters Panel - Top Left */}
        <div className="absolute top-4 left-4 z-20">
          <MapFilters
            filters={{}}
            onFiltersChange={() => {}}
            isExpanded={filtersExpanded}
            onExpandChange={setFiltersExpanded}
            viewportBbox={viewport ? `${viewport.center[0].toFixed(4)},${viewport.center[1].toFixed(4)},${(viewport.center[0] + 0.1).toFixed(4)},${(viewport.center[1] + 0.1).toFixed(4)}` : undefined}
          />
        </div>

        {/* Legend - Bottom Left */}
        <div className="absolute bottom-4 left-4 z-20">
          <Legend
            visibleLayers={Object.entries(layerVisibility).filter(([_, v]) => v).map(([k]) => k as any)}
            onToggleLayer={(layer, visible) => toggleLayer(layer as LayerType, visible)}
            collapsed={legendCollapsed}
            onCollapseChange={setLegendCollapsed}
          />
        </div>

        {/* Viewport Info - Bottom Center */}
        {viewport && (
          <div className="absolute bottom-4 left-1/2 -translate-x-1/2 z-20">
            <div className="card bg-white/90 backdrop-blur shadow-lg border border-grid-200 px-4 py-2">
              <div className="flex items-center gap-4 text-sm text-grid-600">
                <span className="flex items-center gap-1">
                  <MapPin className="w-3 h-3" />
                  {viewport.center[1].toFixed(4)}°N, {Math.abs(viewport.center[0]).toFixed(4)}°{viewport.center[0] >= 0 ? 'E' : 'W'}
                </span>
                <span className="flex items-center gap-1">
                  <Zap className="w-3 h-3" />
                  Zoom {viewport.zoom.toFixed(1)}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* Latest Event Toast - Bottom Right */}
        {latestEvent && isConnected && (
          <div className="absolute bottom-4 right-4 z-20 animate-slide-up">
            <div className="card bg-white/95 backdrop-blur shadow-xl border border-grid-200 px-4 py-3 max-w-sm">
              <div className="flex items-start gap-3">
                <div className="p-1.5 rounded-lg bg-primary-100 text-primary-700 flex-shrink-0">
                  <Bell className="w-5 h-5" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-grid-900 text-sm">{latestEvent.eventType}</span>
                    <span className="badge badge-xs badge-primary">LIVE</span>
                  </div>
                  <p className="text-sm text-grid-600 mt-1 line-clamp-2">Event received for {latestEvent.aggregateType} {latestEvent.aggregateId.slice(0, 8)}...</p>
                  <p className="text-xs text-grid-400 mt-1">Just now</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Detail Drawer */}
      <DetailDrawer
        feature={selectedFeatureData}
        featureType={selectedFeatureType as any}
        isOpen={isDrawerOpen}
        onClose={() => setSelectedFeature(null)}
        onViewIntelligence={(assetId) => console.log('View intelligence for', assetId)}
        onRunSimulation={(assetId) => console.log('Run simulation for', assetId)}
        onOpenIncident={(incidentId) => console.log('Open incident', incidentId)}
        onInspectTopology={(nodeId) => console.log('Inspect topology for', nodeId)}
        position="right"
        width={420}
      />
    </PageTemplate>
  );
}