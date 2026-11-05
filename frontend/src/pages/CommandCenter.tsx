import { AlertTriangle, MapPin, Clock, TrendingUp, Users, Activity, Map, FileText, RotateCcw, Link, MapPin as MapPinIcon, LucideIcon } from 'lucide-react';
import { StatCard } from '../components/ui/StatCard';
import clsx from 'clsx';

export function CommandCenter() {
  // TODO: Connect to real API data
  const stats: Array<{
    label: string;
    value: string;
    trend: string;
    trendUp: boolean | null;
    icon: LucideIcon;
    color: 'safe' | 'warning' | 'alert' | 'grid';
  }> = [
    { label: 'Grid Health', value: '94.2%', trend: '+0.3%', trendUp: true, icon: Activity, color: 'safe' },
    { label: 'Critical Assets', value: '127', trend: '2 at risk', trendUp: false, icon: MapPin, color: 'alert' },
    { label: 'Active Threats', value: '3', trend: '1 new (1h)', trendUp: false, icon: AlertTriangle, color: 'warning' },
    { label: 'Open Incidents', value: '2', trend: '1 contained', trendUp: true, icon: FileText, color: 'grid' },
    { label: 'Risk Trend (7d)', value: '-2.1%', trend: 'Improving', trendUp: true, icon: TrendingUp, color: 'safe' },
    { label: 'On-Call Operators', value: '8/12', trend: 'Shift change in 2h', trendUp: null, icon: Users, color: 'grid' },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-grid-900">Command Center</h1>
          <p className="text-grid-500 mt-1">Unified operational view — grid health, threats, incidents, and next actions</p>
        </div>
        <div className="flex items-center gap-3">
          <select className="input w-auto">
            <option value="all">All Regions</option>
            <option value="north">North Region</option>
            <option value="south">South Region</option>
            <option value="metro">Metro Region</option>
          </select>
          <button className="btn btn-secondary">
            <Clock className="w-4 h-4" />
            Last updated: 2 min ago
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        {stats.map((stat) => (
          <StatCard key={stat.label} {...stat} />
        ))}
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Map Panel */}
        <section className="lg:col-span-2 card">
          <div className="panel-header">
            <h2 className="panel-title flex items-center gap-2">
              <Map className="w-5 h-5" />
              Live Grid Overview
            </h2>
            <div className="flex items-center gap-2">
              <span className="badge badge-info">Auto-refresh: 30s</span>
              <button className="btn btn-ghost btn-icon" aria-label="Refresh map">
                <RotateCcw className="w-4 h-4" />
              </button>
            </div>
          </div>
          <div className="panel-content">
            <div className="map-container aspect-[16/9]">
              <MapPlaceholder />
            </div>
          </div>
        </section>

        {/* Right Column */}
        <div className="space-y-6">
          {/* Active Threats */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <AlertTriangle className="w-5 h-5" />
                Active Threats
              </h2>
              <Link to="/threats" className="text-sm text-grid-600 hover:text-grid-900">View all</Link>
            </div>
            <div className="panel-content p-0">
              <ThreatList />
            </div>
          </section>

          {/* Active Incidents */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <FileText className="w-5 h-5" />
                Active Incidents
              </h2>
              <Link to="/incidents" className="text-sm text-grid-600 hover:text-grid-900">View all</Link>
            </div>
            <div className="panel-content p-0">
              <IncidentList />
            </div>
          </section>

          {/* Next Action Queue */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <Activity className="w-5 h-5" />
                Next Action Queue
              </h2>
            </div>
            <div className="panel-content p-0">
              <ActionQueue />
            </div>
          </section>
        </div>
      </div>

      {/* Bottom Row - Risk Trend & Asset Health */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <section className="card">
          <div className="panel-header">
            <h2 className="panel-title">Risk Trend (7 Days)</h2>
          </div>
          <div className="panel-content">
            <ChartPlaceholder title="Risk Score Trend" />
          </div>
        </section>

        <section className="card">
          <div className="panel-header">
            <h2 className="panel-title">Top Risk Assets</h2>
          </div>
          <div className="panel-content">
            <AssetRiskTable />
          </div>
        </section>
      </div>
    </div>
  );
}

// Placeholder components
function MapPlaceholder() {
  return (
    <div className="relative w-full h-full bg-grid-100 flex items-center justify-center">
      <div className="text-center text-grid-500">
        <Map className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p className="text-lg">Live Grid Map</p>
        <p className="text-sm">MapLibre GL JS vector tiles with real-time asset overlay</p>
        <div className="mt-4 flex justify-center gap-2 text-xs text-grid-400">
          <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-alert-500" /> Critical</span>
          <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-warning-500" /> High Risk</span>
          <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-grid-500" /> Normal</span>
        </div>
      </div>
    </div>
  );
}

function ThreatList() {
  const threats = [
    { id: 'THR-001', type: 'Wildfire proximity', severity: 'CRITICAL', asset: 'TX-Line 500kV #1', time: '5 min ago', confidence: 0.91 },
    { id: 'THR-002', type: 'Thermal anomaly', severity: 'HIGH', asset: 'Substation Alpha-T', time: '12 min ago', confidence: 0.84 },
    { id: 'THR-003', type: 'Unauthorized access', severity: 'MEDIUM', asset: 'Switching Station Beta', time: '45 min ago', confidence: 0.72 },
  ];

  return (
    <div className="divide-y divide-grid-200">
      {threats.map((threat) => (
        <div key={threat.id} className="p-4 hover:bg-grid-50 transition-colors">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="font-medium text-grid-900">{threat.type}</span>
                <span className={clsx('badge', threat.severity === 'CRITICAL' && 'badge-critical', threat.severity === 'HIGH' && 'badge-high', threat.severity === 'MEDIUM' && 'badge-medium')}>
                  {threat.severity}
                </span>
                <span className="text-xs text-grid-400">{threat.confidence * 100}% conf.</span>
              </div>
              <p className="text-sm text-grid-500 mt-1">{threat.asset}</p>
            </div>
            <span className="text-xs text-grid-400 whitespace-nowrap">{threat.time}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function IncidentList() {
  const incidents = [
    { id: 'INC-2026-000123', state: 'ACTIVE_RESPONSE', severity: 'HIGH', title: 'Wildfire threat to TX corridor', time: '23 min ago' },
    { id: 'INC-2026-000118', state: 'CONTAINED', severity: 'MEDIUM', title: 'Thermal anomaly Substation Alpha-T', time: '2h ago' },
  ];

  return (
    <div className="divide-y divide-grid-200">
      {incidents.map((inc) => (
        <div key={inc.id} className="p-4 hover:bg-grid-50 transition-colors">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <span className="font-mono text-sm text-grid-500">{inc.id}</span>
                <span className={clsx('badge', inc.severity === 'CRITICAL' && 'badge-critical', inc.severity === 'HIGH' && 'badge-high', inc.severity === 'MEDIUM' && 'badge-medium')}>
                  {inc.severity}
                </span>
              </div>
              <p className="text-sm text-grid-900 mt-1">{inc.title}</p>
              <p className="text-xs text-grid-500 mt-1">State: {inc.state.replace(/_/g, ' ')}</p>
            </div>
            <span className="text-xs text-grid-400 whitespace-nowrap">{inc.time}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function ActionQueue() {
  const actions = [
    { priority: 'IMMEDIATE', action: 'Deploy crew to TX-Line 500kV #1', asset: 'TX-Line 500kV #1', assignee: 'Crew Alpha' },
    { priority: 'URGENT', action: 'Activate load shed sequence Metro-7', asset: 'Substation Metro-7', assignee: 'Dispatcher' },
    { priority: 'HIGH', action: 'Pre-position spare conductors at Depot 3', asset: 'Depot 3', assignee: 'Logistics' },
  ];

  return (
    <div className="space-y-3">
      {actions.map((action, i) => (
        <div key={i} className="p-4 bg-grid-50 rounded-lg border border-grid-200">
          <div className="flex items-start justify-between gap-3">
            <span className={clsx('badge badge-info font-mono text-xs', action.priority === 'IMMEDIATE' && 'badge-critical')}>
              {action.priority}
            </span>
          </div>
          <p className="text-sm font-medium text-grid-900 mt-2">{action.action}</p>
          <div className="flex items-center gap-4 mt-2 text-xs text-grid-500">
            <span className="flex items-center gap-1"><MapPinIcon className="w-3 h-3" />{action.asset}</span>
            <span className="flex items-center gap-1"><Users className="w-3 h-3" />{action.assignee}</span>
          </div>
        </div>
      ))}
    </div>
  );
}

function ChartPlaceholder({ title }: { title: string }) {
  return (
    <div className="h-64 bg-grid-50 rounded-lg flex items-center justify-center text-grid-400">
      <span>{title} — Recharts integration pending</span>
    </div>
  );
}

function AssetRiskTable() {
  const assets = [
    { name: 'TX-Line 500kV #1', risk: 0.87, band: 'CRITICAL', trend: 'up' },
    { name: 'Substation Alpha-T', risk: 0.82, band: 'CRITICAL', trend: 'up' },
    { name: 'Substation Metro-7', risk: 0.76, band: 'HIGH', trend: 'stable' },
    { name: 'TX-Line 500kV #2', risk: 0.71, band: 'HIGH', trend: 'up' },
    { name: 'Switching Station Beta', risk: 0.68, band: 'HIGH', trend: 'down' },
  ];

  return (
    <div className="table-container">
      <table className="table">
        <thead>
          <tr>
            <th>Asset</th>
            <th className="text-right">Risk Score</th>
            <th>Band</th>
            <th className="text-right">Trend</th>
          </tr>
        </thead>
        <tbody>
          {assets.map((asset) => (
            <tr key={asset.name}>
              <td className="font-medium text-grid-900">{asset.name}</td>
              <td className="text-right font-mono text-grid-900">{(asset.risk * 100).toFixed(0)}%</td>
              <td><span className={clsx('badge', asset.band === 'CRITICAL' && 'badge-critical', asset.band === 'HIGH' && 'badge-high')}>{asset.band}</span></td>
              <td className="text-right">
                <span className={clsx('inline-flex items-center gap-1 text-xs font-medium', asset.trend === 'up' && 'text-alert-600', asset.trend === 'down' && 'text-safe-600', asset.trend === 'stable' && 'text-grid-500')}>
                  {asset.trend === 'up' && <TrendingUp className="w-3 h-3" />}
                  {asset.trend === 'down' && <TrendingUp className="w-3 h-3 rotate-180" />}
                  {asset.trend === 'stable' && <span>—</span>}
                  {asset.trend.charAt(0).toUpperCase() + asset.trend.slice(1)}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}