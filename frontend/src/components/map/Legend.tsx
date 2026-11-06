import { useState, useEffect } from 'react';
import { ChevronDown, ChevronUp, MapPin, AlertTriangle, Zap, Layers } from 'lucide-react';
import type { FeatureType } from '../../types/spatial';

export interface LegendProps {
  /** Visible layers */
  visibleLayers: FeatureType[];
  /** Callback when layer visibility toggles */
  onToggleLayer: (layer: FeatureType, visible: boolean) => void;
  /** Whether legend is collapsed */
  collapsed?: boolean;
  /** Callback when collapse state changes */
  onCollapseChange?: (collapsed: boolean) => void;
}

/** Incident legend item */
interface IncidentLegendItem {
  state: 'ACTIVE_RESPONSE' | 'CONTAINED' | 'RECOVERING' | 'RESOLVED' | 'CLOSED';
  label: string;
  color: string;
  icon: string;
}

/** Threat legend item */
interface ThreatLegendItem {
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
  label: string;
  color: string;
}

/** Asset legend item */
interface AssetLegendItem {
  band: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NEGLIGIBLE';
  label: string;
  color: string;
}

/** Node legend item */
interface NodeLegendItem {
  type: string;
  label: string;
  color: string;
}

/** Edge legend item */
interface EdgeLegendItem {
  type: string;
  label: string;
  color: string;
  dashed?: boolean;
}

/** Risk legend item */
interface RiskLegendItem {
  band: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NEGLIGIBLE';
  label: string;
  color: string;
}

/** Union of all legend item types */
type LegendItem =
  | IncidentLegendItem
  | ThreatLegendItem
  | AssetLegendItem
  | NodeLegendItem
  | EdgeLegendItem
  | RiskLegendItem;

interface LayerConfig {
  type: FeatureType;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  items: LegendItem[];
}

/**
 * Type guard for incident legend items
 */
function isIncidentItem(item: LegendItem): item is IncidentLegendItem {
  return 'state' in item;
}

/** Type guard for threat legend items */
function isThreatItem(item: LegendItem): item is ThreatLegendItem {
  return 'severity' in item;
}

/** Type guard for asset legend items */
function isAssetItem(item: LegendItem): item is AssetLegendItem {
  return 'band' in item && !('state' in item) && !('severity' in item) && !('type' in item) && !('dashed' in item);
}

/** Type guard for node legend items */
function isNodeItem(item: LegendItem): item is NodeLegendItem {
  return 'type' in item && !('dashed' in item) && !('band' in item) && !('severity' in item) && !('state' in item);
}

/** Type guard for edge legend items */
function isEdgeItem(item: LegendItem): item is EdgeLegendItem {
  return 'type' in item && 'dashed' in item;
}

/** Type guard for risk legend items */
function isRiskItem(item: LegendItem): item is RiskLegendItem {
  return 'band' in item && !('state' in item) && !('severity' in item) && !('type' in item) && !('dashed' in item);
}

interface LayerConfig {
  type: FeatureType;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  items: LegendItem[];
}

/**
 * Map Legend Component
 * Shows symbology for all visible layers
 */
export function Legend({
  visibleLayers,
  onToggleLayer,
  collapsed = false,
  onCollapseChange,
}: LegendProps) {
  const [isCollapsed, setIsCollapsed] = useState(collapsed);

  useEffect(() => {
    setIsCollapsed(collapsed);
  }, [collapsed]);

  const layerConfig: LayerConfig[] = [
    {
      type: 'incident',
      icon: AlertTriangle,
      label: 'Incidents',
      items: [
        { state: 'ACTIVE_RESPONSE', label: 'Active Response', color: '#ef4444', icon: '🔴' },
        { state: 'CONTAINED', label: 'Contained', color: '#f59e0b', icon: '🟡' },
        { state: 'RECOVERING', label: 'Recovering', color: '#f97316', icon: '🟠' },
        { state: 'RESOLVED', label: 'Resolved', color: '#22c55e', icon: '🟢' },
        { state: 'CLOSED', label: 'Closed', color: '#64748b', icon: '⚪' },
      ],
    },
    {
      type: 'threat',
      icon: AlertTriangle,
      label: 'Threats',
      items: [
        { severity: 'CRITICAL', label: 'Critical', color: '#991b1b' },
        { severity: 'HIGH', label: 'High', color: '#dc2626' },
        { severity: 'MEDIUM', label: 'Medium', color: '#f59e0b' },
        { severity: 'LOW', label: 'Low', color: '#84cc16' },
        { severity: 'INFO', label: 'Info', color: '#3b82f6' },
      ],
    },
    {
      type: 'asset',
      icon: Zap,
      label: 'Assets (Risk)',
      items: [
        { band: 'CRITICAL', label: 'Critical', color: '#991b1b' },
        { band: 'HIGH', label: 'High', color: '#dc2626' },
        { band: 'MEDIUM', label: 'Medium', color: '#f59e0b' },
        { band: 'LOW', label: 'Low', color: '#84cc16' },
        { band: 'NEGLIGIBLE', label: 'Negligible', color: '#22c55e' },
      ],
    },
    {
      type: 'node',
      icon: MapPin,
      label: 'Grid Nodes',
      items: [
        { type: 'SUBSTATION', label: 'Substation', color: '#1e40af' },
        { type: 'SWITCHING_STATION', label: 'Switching Station', color: '#2563eb' },
        { type: 'GENERATION', label: 'Generation', color: '#059669' },
        { type: 'LOAD', label: 'Load', color: '#dc2626' },
        { type: 'JUNCTION', label: 'Junction', color: '#7c3aed' },
        { type: 'HVDC_CONVERTER', label: 'HVDC Converter', color: '#ea580c' },
        { type: 'FACTS_DEVICE', label: 'FACTS Device', color: '#d97706' },
        { type: 'INTERCONNECT', label: 'Interconnect', color: '#0891b2' },
      ],
    },
    {
      type: 'edge',
      icon: Layers,
      label: 'Grid Edges',
      items: [
        { type: 'TRANSMISSION_LINE', label: 'Transmission Line', color: '#1e3a8a' },
        { type: 'UNDERGROUND_CABLE', label: 'Underground Cable', color: '#065f46' },
        { type: 'SUBMARINE_CABLE', label: 'Submarine Cable', color: '#0c4a6e' },
        { type: 'TRANSFORMER', label: 'Transformer', color: '#7c2d12', dashed: true },
        { type: 'PHASE_SHIFTER', label: 'Phase Shifter', color: '#92400e', dashed: true },
        { type: 'HVDC_LINK', label: 'HVDC Link', color: '#4c1d95' },
        { type: 'BUSBAR', label: 'Busbar', color: '#78350f' },
      ],
    },
    {
      type: 'risk',
      icon: Zap,
      label: 'Risk Heatmap',
      items: [
        { band: 'CRITICAL', label: 'Critical', color: '#991b1b' },
        { band: 'HIGH', label: 'High', color: '#dc2626' },
        { band: 'MEDIUM', label: 'Medium', color: '#f59e0b' },
        { band: 'LOW', label: 'Low', color: '#84cc16' },
        { band: 'NEGLIGIBLE', label: 'Negligible', color: '#22c55e' },
      ],
    },
  ];

  const toggleCollapse = () => {
    const newState = !isCollapsed;
    setIsCollapsed(newState);
    onCollapseChange?.(newState);
  };

  const ChevronIcon = isCollapsed ? ChevronDown : ChevronUp;

  return (
    <div className="fixed bottom-4 left-4 z-50">
      <div className="card bg-white shadow-xl border border-grid-200 w-72 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-3 border-b border-grid-200 bg-grid-50">
          <h3 className="font-semibold text-grid-900 flex items-center gap-2">
            <Layers className="w-5 h-5" />
            Legend
          </h3>
          <button
            onClick={toggleCollapse}
            className="btn btn-ghost btn-icon p-1"
            aria-label={isCollapsed ? 'Expand legend' : 'Collapse legend'}
          >
            <ChevronIcon className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        {!isCollapsed && (
          <div className="p-3 space-y-4 max-h-[400px] overflow-y-auto">
            {layerConfig.map((layer) => {
              const isVisible = visibleLayers.includes(layer.type);
              return (
                <fieldset key={layer.type} className="space-y-2">
                  <legend className="flex items-center justify-between text-sm font-medium text-grid-900">
                    <span className="flex items-center gap-2">
                      <layer.icon className="w-4 h-4 text-grid-500" />
                      {layer.label}
                    </span>
                    <label className="flex items-center gap-1.5 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={isVisible}
                        onChange={(e) => onToggleLayer(layer.type, e.target.checked)}
                        className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                        aria-label={`Show ${layer.label}`}
                      />
                      <span className="text-xs text-grid-500">Show</span>
                    </label>
                  </legend>
                  {isVisible && (
                    <div className="space-y-1.5 pl-6">
                      {layer.items.map((item, index) => (
                        <div key={index} className="flex items-center gap-2 text-xs text-grid-700">
                          {/* Incident items */}
                          {isIncidentItem(item) && (
                            <>
                              <span className="w-4 h-4 flex items-center justify-center">{item.icon}</span>
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.state})</span>
                            </>
                          )}
                          {/* Threat items */}
                          {isThreatItem(item) && (
                            <>
                              <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.severity})</span>
                            </>
                          )}
                          {/* Asset items */}
                          {isAssetItem(item) && (
                            <>
                              <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.band})</span>
                            </>
                          )}
                          {/* Node items */}
                          {isNodeItem(item) && (
                            <>
                              <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.type})</span>
                            </>
                          )}
                          {/* Edge items */}
                          {isEdgeItem(item) && (
                            <>
                              {item.dashed ? (
                                <span className="w-6 h-1 border-t-2 border-dashed" style={{ borderColor: item.color }} />
                              ) : (
                                <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                              )}
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.type})</span>
                            </>
                          )}
                          {/* Risk items */}
                          {isRiskItem(item) && (
                            <>
                              <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                              <span>{item.label}</span>
                              <span className="text-grid-400">({item.band})</span>
                            </>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </fieldset>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}