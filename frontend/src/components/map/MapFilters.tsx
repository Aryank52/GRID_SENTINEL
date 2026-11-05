import { useState, useCallback } from 'react';
import { X, Filter, ChevronDown, ChevronUp, Layers, AlertTriangle, MapPin, Activity, Zap } from 'lucide-react';
import type { SpatialQueryParams } from '../../types/spatial';

export interface MapFiltersProps {
  /** Current filters */
  filters: SpatialQueryParams;
  /** Callback when filters change */
  onFiltersChange: (filters: SpatialQueryParams) => void;
  /** Available asset types for filtering */
  assetTypes?: string[];
  /** Available node types */
  nodeTypes?: string[];
  /** Available edge types */
  edgeTypes?: string[];
  /** Available threat types */
  threatTypes?: string[];
  /** Available incident states */
  incidentStates?: string[];
  /** Whether filters panel is expanded */
  isExpanded?: boolean;
  /** Callback when expansion changes */
  onExpandChange?: (expanded: boolean) => void;
  /** Viewport bbox for context */
  viewportBbox?: string;
}

/**
 * Map Filters Panel Component
 * Collapsible panel for spatial filtering
 */
export function MapFilters({
  filters,
  onFiltersChange,
  assetTypes = [],
  nodeTypes = [],
  edgeTypes = [],
  threatTypes = [],
  incidentStates = [],
  isExpanded = false,
  onExpandChange,
  viewportBbox,
}: MapFiltersProps) {
  const [localFilters, setLocalFilters] = useState<SpatialQueryParams>(filters);

  // Sync with parent filters
  useState(() => {
    setLocalFilters(filters);
  }, [filters]);

  const updateFilter = useCallback((key: keyof SpatialQueryParams, value: any) => {
    const newFilters = { ...localFilters, [key]: value };
    setLocalFilters(newFilters);
    onFiltersChange(newFilters);
  }, [localFilters, onFiltersChange]);

  const clearFilters = useCallback(() => {
    const cleared: SpatialQueryParams = {
      limit: 1000,
      offset: 0,
    };
    setLocalFilters(cleared);
    onFiltersChange(cleared);
  }, [onFiltersChange]);

  const hasActiveFilters = useMemo(() => {
    return Object.entries(localFilters).some(([key, value]) => {
      if (['limit', 'offset', 'bbox'].includes(key)) return false;
      return value !== undefined && value !== null && value !== '';
    });
  }, [localFilters]);

  const toggleExpand = useCallback(() => {
    onExpandChange?.(!isExpanded);
  }, [isExpanded, onExpandChange]);

  if (!isExpanded && !hasActiveFilters) {
    return (
      <button
        onClick={toggleExpand}
        className="btn btn-ghost btn-sm flex items-center gap-2 px-3 py-2"
        aria-label="Show filters"
      >
        <Filter className="w-4 h-4" />
        <span className="hidden sm:inline">Filters</span>
      </button>
    );
  }

  return (
    <div className="fixed top-16 right-4 z-50 w-80 max-h-[calc(100vh-120px)] overflow-y-auto">
      <div className="card bg-white shadow-xl border border-grid-200">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-grid-200">
          <h3 className="font-semibold text-grid-900 flex items-center gap-2">
            <Filter className="w-5 h-5" />
            Map Filters
          </h3>
          <div className="flex items-center gap-2">
            {hasActiveFilters && (
              <button
                onClick={clearFilters}
                className="text-xs text-grid-500 hover:text-grid-700 font-medium"
              >
                Clear all
              </button>
            )}
            <button
              onClick={toggleExpand}
              className="btn btn-ghost btn-icon p-1"
              aria-label={isExpanded ? 'Collapse filters' : 'Expand filters'}
            >
              {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Active Filters Summary */}
        {hasActiveFilters && (
          <div className="px-4 py-2 border-b border-grid-100 bg-grid-50">
            <div className="flex flex-wrap gap-1.5">
              {Object.entries(localFilters).map(([key, value]) => {
                if (!value || ['limit', 'offset', 'bbox'].includes(key)) return null;
                const label = key.replace(/([A-Z])/g, ' $1').replace(/^./, c => c.toUpperCase());
                return (
                  <span
                    key={key}
                    className="inline-flex items-center gap-1 px-2 py-0.5 text-xs bg-grid-100 text-grid-700 rounded-full"
                  >
                    <span className="font-medium">{label}:</span>
                    <span>{Array.isArray(value) ? value.join(', ') : String(value)}</span>
                    <button
                      onClick={() => updateFilter(key as keyof SpatialQueryParams, undefined)}
                      className="ml-1 hover:text-grid-500"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </span>
                );
              })}
            </div>
          </div>
        )}

        {/* Filter Sections */}
        <div className="p-4 space-y-6 max-h-[calc(100vh-280px)] overflow-y-auto">
          {/* Viewport Info */}
          {viewportBbox && (
            <div className="text-xs text-grid-500 p-2 bg-grid-50 rounded">
              <span className="font-medium">Viewport:</span>{' '}
              {viewportBbox}
            </div>
          )}

          {/* Asset Filters */}
          <fieldset className="space-y-3">
            <legend className="flex items-center gap-2 text-sm font-medium text-grid-900">
              <Layers className="w-4 h-4" />
              Assets
            </legend>
            <div className="grid grid-cols-2 gap-2">
              {assetTypes.map(type => (
                <label key={type} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.assetType?.includes(type) || false}
                    onChange={(e) => {
                      const current = localFilters.assetType || [];
                      updateFilter('assetType', e.target.checked
                        ? [...current, type]
                        : current.filter(t => t !== type));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700 capitalize">{type.toLowerCase().replace('_', ' ')}</span>
                </label>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-2">
              {['LOW', 'MEDIUM', 'HIGH', 'MISSION_CRITICAL'].map(crit => (
                <label key={crit} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.criticality?.includes(crit) || false}
                    onChange={(e) => {
                      const current = localFilters.criticality || [];
                      updateFilter('criticality', e.target.checked
                        ? [...current, crit]
                        : current.filter(c => c !== crit));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700">{crit}</span>
                </label>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-2">
              {['NEGLIGIBLE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(band => (
                <label key={band} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.riskBand?.includes(band) || false}
                    onChange={(e) => {
                      const current = localFilters.riskBand || [];
                      updateFilter('riskBand', e.target.checked
                        ? [...current, band]
                        : current.filter(b => b !== band));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700">{band}</span>
                </label>
              ))}
            </div>
          </fieldset>

          {/* Grid Topology Filters */}
          <fieldset className="space-y-3">
            <legend className="flex items-center gap-2 text-sm font-medium text-grid-900">
              <MapPin className="w-4 h-4" />
              Grid Topology
            </legend>
            <div className="grid grid-cols-2 gap-2">
              {nodeTypes.map(type => (
                <label key={type} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.nodeType?.includes(type) || false}
                    onChange={(e) => {
                      const current = localFilters.nodeType || [];
                      updateFilter('nodeType', e.target.checked
                        ? [...current, type]
                        : current.filter(t => t !== type));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700 capitalize">{type.toLowerCase().replace('_', ' ')}</span>
                </label>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-2">
              {edgeTypes.map(type => (
                <label key={type} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.edgeType?.includes(type) || false}
                    onChange={(e) => {
                      const current = localFilters.edgeType || [];
                      updateFilter('edgeType', e.target.checked
                        ? [...current, type]
                        : current.filter(t => t !== type));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700 capitalize">{type.toLowerCase().replace('_', ' ')}</span>
                </label>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <label className="flex items-center gap-2 text-sm text-grid-700">
                <input
                  type="checkbox"
                  checked={!!localFilters.minVoltageKv || !!localFilters.maxVoltageKv}
                  className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                />
                Voltage Range (kV)
              </label>
            </div>
            {localFilters.minVoltageKv || localFilters.maxVoltageKv ? (
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="number"
                  placeholder="Min kV"
                  value={localFilters.minVoltageKv || ''}
                  onChange={(e) => updateFilter('minVoltageKv', e.target.value ? Number(e.target.value) : undefined)}
                  className="input text-sm"
                  min="0"
                />
                <input
                  type="number"
                  placeholder="Max kV"
                  value={localFilters.maxVoltageKv || ''}
                  onChange={(e) => updateFilter('maxVoltageKv', e.target.value ? Number(e.target.value) : undefined)}
                  className="input text-sm"
                  min="0"
                />
              </div>
            ) : null}
          </fieldset>

          {/* Threat Filters */}
          <fieldset className="space-y-3">
            <legend className="flex items-center gap-2 text-sm font-medium text-grid-900">
              <AlertTriangle className="w-4 h-4" />
              Threats
            </legend>
            <div className="grid grid-cols-2 gap-2">
              {['INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(sev => (
                <label key={sev} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.severity?.includes(sev) || false}
                    onChange={(e) => {
                      const current = localFilters.severity || [];
                      updateFilter('severity', e.target.checked
                        ? [...current, sev]
                        : current.filter(s => s !== sev));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700">{sev}</span>
                </label>
              ))}
            </div>
            <div className="grid grid-cols-2 gap-2">
              {threatTypes.map(type => (
                <label key={type} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.threatType?.includes(type) || false}
                    onChange={(e) => {
                      const current = localFilters.threatType || [];
                      updateFilter('threatType', e.target.checked
                        ? [...current, type]
                        : current.filter(t => t !== type));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700 capitalize">{type.toLowerCase().replace('_', ' ')}</span>
                </label>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <label className="flex items-center gap-2 text-sm text-grid-700">
                <input
                  type="checkbox"
                  checked={localFilters.showOnlyActionable || false}
                  onChange={(e) => updateFilter('showOnlyActionable', e.target.checked)}
                  className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                />
                Actionable only (confidence ≥ 0.7, severity ≥ MEDIUM)
              </label>
            </div>
          </fieldset>

          {/* Incident Filters */}
          <fieldset className="space-y-3">
            <legend className="flex items-center gap-2 text-sm font-medium text-grid-900">
              <Activity className="w-4 h-4" />
              Incidents
            </legend>
            <div className="grid grid-cols-2 gap-2">
              {incidentStates.map(state => (
                <label key={state} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.incidentState?.includes(state) || false}
                    onChange={(e) => {
                      const current = localFilters.incidentState || [];
                      updateFilter('incidentState', e.target.checked
                        ? [...current, state]
                        : current.filter(s => s !== state));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700">{state.replace('_', ' ')}</span>
                </label>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <label className="flex items-center gap-2 text-sm text-grid-700">
                <input
                  type="checkbox"
                  checked={localFilters.activeOnly || false}
                  onChange={(e) => updateFilter('activeOnly', e.target.checked)}
                  className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                />
                Active only (not resolved/closed)
              </label>
            </div>
          </fieldset>

          {/* Risk Filters */}
          <fieldset className="space-y-3">
            <legend className="flex items-center gap-2 text-sm font-medium text-grid-900">
              <Zap className="w-4 h-4" />
              Risk
            </legend>
            <div className="grid grid-cols-2 gap-2">
              {['NEGLIGIBLE', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map(band => (
                <label key={band} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={localFilters.riskBand?.includes(band) || false}
                    onChange={(e) => {
                      const current = localFilters.riskBand || [];
                      updateFilter('riskBand', e.target.checked
                        ? [...current, band]
                        : current.filter(b => b !== band));
                    }}
                    className="w-4 h-4 rounded border-grid-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-grid-700">{band}</span>
                </label>
              ))}
            </div>
          </fieldset>
        </div>
      </div>
    </div>
  );
}

import { useMemo } from 'react';