import { useState, useEffect } from 'react';
import { ChevronDown, ChevronUp, Circle, Minus, MapPin, AlertTriangle, Activity, Zap, Layers } from 'lucide-react';
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

  const layerConfig = [
    {
      type: 'incident' as FeatureType,
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
      type: 'threat' as FeatureType,
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
      type: 'asset' as FeatureType,
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
      type: 'node' as FeatureType,
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
      type: 'edge' as FeatureType,
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
      type: 'risk' as FeatureType,
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
            {isCollapsed ? <ChevronDown className="w-4 h-4" /> : <ChevronUp className="w-4 h-4" />}
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
                          {item.icon ? (
                            <span className="w-4 h-4 flex items-center justify-center">{item.icon}</span>
                          ) : item.dashed ? (
                            <span className="w-6 h-1 border-t-2 border-dashed" style={{ borderColor: item.color }} />
                          ) : (
                            <span className="w-4 h-4 rounded-full" style={{ backgroundColor: item.color }} />
                          )}
                          <span>{item.label}</span>
                          {item.severity && <span className="text-grid-400">({item.severity})</span>}
                          {item.band && <span className="text-grid-400">({item.band})</span>}
                          {item.state && <span className="text-grid-400">({item.state})</span>}
                          {item.type && <span className="text-grid-400">({item.type})</span>}
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