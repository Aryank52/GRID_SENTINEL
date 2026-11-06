import { useMemo } from 'react';
import { 
  AlertTriangle, 
  MapPin, 
  TrendingUp, 
  Users, 
  Activity, 
  Map, 
  FileText, 
  RotateCcw, 
  Zap,
  Database,
  Shield,
  RefreshCw,
  Bell,
  BellOff,
  Wifi,
  WifiOff,
} from 'lucide-react';
import { StatCard } from '../components/ui/StatCard';
import { useRealtimeEvents } from '../hooks/useRealtimeEvents';
import clsx from 'clsx';

export function CommandCenter() {
  const {
    connectionStatus,
    isConnected,
    isReconnecting,
    isOffline,
    connectionDisplayText,
    eventHistory,
    activeThreatsCount,
    activeIncidentsCount,
    disconnect,
    reconnect,
  } = useRealtimeEvents();

  // Compute stats from real-time events and query data
  const stats = useMemo(() => {
    // In a real implementation, these would come from TanStack Query cache
    // For now, we compute from event history
    const recentThreats = eventHistory.filter(e => 
      e.eventType === 'ThreatDetected' && 
      (e.severity === 'HIGH' || e.severity === 'CRITICAL')
    ).length;
    
    const recentIncidents = eventHistory.filter(e => 
      e.eventType === 'IncidentCreated' ||
      (e.eventType === 'IncidentStateChanged' && 
       ['DETECTED', 'TRIAGED', 'CONFIRMED', 'ACTIVE_RESPONSE', 'CONTAINED', 'RECOVERING'].includes((e as any).payload?.newState))
    ).length;

    const statItems = [
      { 
        label: 'Connection', 
        value: connectionDisplayText, 
        trend: isConnected ? 'Real-time active' : isReconnecting ? 'Reconnecting...' : 'Offline', 
        trendUp: isConnected ? true : null, 
        icon: isConnected ? Wifi : WifiOff, 
        color: (isConnected ? 'safe' : isOffline ? 'alert' : 'warning') as 'safe' | 'warning' | 'alert' | 'grid'
      },
      { 
        label: 'Active Threats', 
        value: String(recentThreats), 
        trend: recentThreats > 0 ? 'Requires attention' : 'No active threats', 
        trendUp: recentThreats === 0, 
        icon: AlertTriangle, 
        color: (recentThreats > 2 ? 'alert' : recentThreats > 0 ? 'warning' : 'safe') as 'safe' | 'warning' | 'alert' | 'grid'
      },
      { 
        label: 'Open Incidents', 
        value: String(recentIncidents), 
        trend: recentIncidents > 0 ? 'Monitoring' : 'No open incidents', 
        trendUp: recentIncidents === 0, 
        icon: FileText, 
        color: (recentIncidents > 1 ? 'alert' : recentIncidents > 0 ? 'warning' : 'safe') as 'safe' | 'warning' | 'alert' | 'grid'
      },
      { 
        label: 'Risk Updates (1h)', 
        value: String(eventHistory.filter(e => 
          e.eventType === 'RiskScoreUpdated' && 
          new Date(e.timestamp) > new Date(Date.now() - 3600000)
        ).length), 
        trend: 'Risk engine active', 
        trendUp: null, 
        icon: TrendingUp, 
        color: 'grid' as const
      },
      { 
        label: 'Asset Changes (1h)', 
        value: String(eventHistory.filter(e => 
          e.eventType === 'GridAssetStatusChanged' && 
          new Date(e.timestamp) > new Date(Date.now() - 3600000)
        ).length), 
        trend: 'Asset monitoring active', 
        trendUp: null, 
        icon: Database, 
        color: 'grid' as const
      },
      { 
        label: 'Simulations (24h)', 
        value: String(eventHistory.filter(e => 
          e.eventType === 'SimulationCompleted' && 
          new Date(e.timestamp) > new Date(Date.now() - 86400000)
        ).length), 
        trend: 'Simulation engine ready', 
        trendUp: null, 
        icon: Zap, 
        color: 'grid' as const
      },
    ];
    
    return statItems;
  }, [connectionStatus, eventHistory, isConnected, isReconnecting, isOffline, connectionDisplayText]);

  // Get recent events for the activity feed
  const recentEvents = useMemo(() => 
    eventHistory.slice(0, 20),
    [eventHistory]
  );

  // Format relative time
  const formatRelativeTime = (timestamp: string): string => {
    const diff = Date.now() - new Date(timestamp).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    
    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return new Date(timestamp).toLocaleDateString();
  };

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
          
          {/* Connection Status Indicator */}
          <div className={clsx(
            'flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium transition-all duration-300',
            isConnected && 'bg-safe-100 text-safe-700',
            isReconnecting && 'bg-warning-100 text-warning-700 animate-pulse',
            isOffline && 'bg-alert-100 text-alert-700'
          )}>
            <span className={clsx(
              'w-2 h-2 rounded-full',
              isConnected && 'bg-safe-500 animate-pulse',
              isReconnecting && 'bg-warning-500 animate-bounce',
              isOffline && 'bg-alert-500'
            )} />
            {connectionDisplayText}
            {isReconnecting && (
              <span className="text-xs">({connectionStatus.reconnectAttempts}/{10})</span>
            )}
          </div>
          
          <button 
            className="btn btn-secondary" 
            onClick={reconnect}
            disabled={isConnected || isReconnecting}
            aria-label={isConnected ? 'Already connected' : 'Reconnect'}
          >
            <RefreshCw className={clsx('w-4 h-4', isReconnecting && 'animate-spin')} />
            {isReconnecting ? 'Reconnecting...' : 'Reconnect'}
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
        {/* Left Column - Map Panel & Activity Feed */}
        <div className="lg:col-span-2 space-y-6">
          {/* Live Grid Map Panel */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <Map className="w-5 h-5" />
                Live Grid Overview
              </h2>
              <div className="flex items-center gap-2">
                <span className={clsx(
                  'badge text-xs',
                  isConnected && 'badge-safe',
                  isReconnecting && 'badge-warning',
                  isOffline && 'badge-alert'
                )}>
                  {isConnected ? 'LIVE' : isReconnecting ? 'RECONNECTING' : 'OFFLINE'}
                </span>
                <button className="btn btn-ghost btn-icon" aria-label="Refresh map">
                  <RotateCcw className="w-4 h-4" />
                </button>
              </div>
            </div>
            <div className="panel-content">
              <div className="map-container aspect-[16/9]">
                <LiveGridMapPlaceholder isConnected={isConnected} />
              </div>
            </div>
          </section>

          {/* Real-time Activity Feed */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <Bell className="w-5 h-5" />
                Real-time Event Stream
              </h2>
              <div className="flex items-center gap-2">
                <button 
                  className="btn btn-ghost btn-icon text-xs" 
                  onClick={disconnect}
                  disabled={isOffline}
                  aria-label="Disconnect from real-time stream"
                >
                  <BellOff className="w-4 h-4" />
                  Disconnect
                </button>
              </div>
            </div>
            <div className="panel-content p-0 max-h-96 overflow-y-auto">
              <EventFeed events={recentEvents} formatRelativeTime={formatRelativeTime} />
            </div>
          </section>
        </div>

        {/* Right Column */}
        <div className="space-y-6">
          {/* Active Threats */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <AlertTriangle className="w-5 h-5" />
                Active Threats
              </h2>
              <span className={clsx('badge text-xs', activeThreatsCount > 0 ? 'badge-warning' : 'badge-safe')}>
                {activeThreatsCount} active
              </span>
            </div>
            <div className="panel-content p-0">
              <ThreatList events={eventHistory} formatRelativeTime={formatRelativeTime} />
            </div>
          </section>

          {/* Active Incidents */}
          <section className="card">
            <div className="panel-header">
              <h2 className="panel-title flex items-center gap-2">
                <FileText className="w-5 h-5" />
                Active Incidents
              </h2>
              <span className={clsx('badge text-xs', activeIncidentsCount > 0 ? 'badge-warning' : 'badge-safe')}>
                {activeIncidentsCount} active
              </span>
            </div>
            <div className="panel-content p-0">
              <IncidentList events={eventHistory} formatRelativeTime={formatRelativeTime} />
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
              <ActionQueue events={eventHistory} />
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

// ============================================================================
// Sub-components
// ============================================================================

function LiveGridMapPlaceholder({ isConnected }: { isConnected: boolean }) {
  return (
    <div className="relative w-full h-full bg-grid-100 flex items-center justify-center">
      <div className="text-center text-grid-500">
        <Map className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p className="text-lg font-medium">Live Grid Map</p>
        <p className="text-sm">MapLibre GL JS vector tiles with real-time asset overlay</p>
        <div className="mt-4 flex justify-center gap-2 text-xs text-grid-400">
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full bg-alert-500" /> Critical
          </span>
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full bg-warning-500" /> High Risk
          </span>
          <span className="flex items-center gap-1">
            <span className="w-3 h-3 rounded-full bg-grid-500" /> Normal
          </span>
        </div>
        <div className="mt-3 text-xs text-grid-400">
          {isConnected ? '🟢 WebSocket connected — real-time updates active' : '🔴 WebSocket disconnected — using cached data'}
        </div>
      </div>
    </div>
  );
}

function EventFeed({ 
  events, 
  formatRelativeTime,
}: { 
  events: any[];
  formatRelativeTime: (ts: string) => string;
}) {
  // Map event types to their icons
  const eventIcons: Record<string, React.ComponentType<{ className?: string }>> = {
    ThreatDetected: AlertTriangle,
    ThreatEscalated: Zap,
    IncidentCreated: FileText,
    IncidentStateChanged: Shield,
    RiskScoreUpdated: TrendingUp,
    SimulationCompleted: Activity,
    ResponsePlanUpdated: MapPin,
    RecoveryProgressUpdated: RotateCcw,
    GridAssetStatusChanged: Database,
  };

  if (events.length === 0) {
    return (
      <div className="p-8 text-center text-grid-400">
        <BellOff className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p>No real-time events yet</p>
        <p className="text-sm mt-1">Events will appear here as they occur</p>
      </div>
    );
  }

  return (
    <div className="divide-y divide-grid-200">
      {events.map((event) => {
        const EventIcon = eventIcons[event.eventType] || Bell;
        return (
          <div 
            key={event.eventId} 
            className="p-4 hover:bg-grid-50 transition-colors border-l-2 border-transparent"
            style={{ borderLeftColor: getEventSeverityColor(event.severity) }}
          >
            <div className="flex items-start gap-3">
              <div className={clsx('p-1.5 rounded-lg flex-shrink-0', getEventColor(event.severity))}>
                <EventIcon className="w-5 h-5" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-medium text-grid-900">{event.eventType}</span>
                  <span className={clsx('badge badge-xs', getEventSeverityBadge(event.severity))}>
                    {event.severity}
                  </span>
                </div>
                <p className="text-sm text-grid-700 mt-1">{event.message}</p>
                <div className="flex items-center gap-3 mt-2 text-xs text-grid-400">
                  <span className="font-mono">{event.aggregateType} {event.aggregateId.slice(0, 8)}...</span>
                  <span>{formatRelativeTime(event.timestamp)}</span>
                  <span className="font-mono text-grid-500">{event.correlationId?.slice(0, 8)}...</span>
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function getEventSeverityColor(severity: string): string {
  switch (severity) {
    case 'CRITICAL': case 'EMERGENCY': return '#dc2626'; // alert-600
    case 'HIGH': return '#ea580c'; // warning-600
    case 'MEDIUM': return '#52525b'; // grid-600
    case 'LOW': return '#a1a1aa'; // grid-400
    case 'INFO': return '#2563eb'; // blue-600
    default: return '#a1a1aa';
  }
}

function getEventSeverityBadge(severity: string): string {
  switch (severity) {
    case 'CRITICAL': case 'EMERGENCY': return 'badge-critical';
    case 'HIGH': return 'badge-high';
    case 'MEDIUM': return 'badge-medium';
    case 'LOW': return 'badge-grid';
    case 'INFO': return 'badge-info';
    default: return 'badge-grid';
  }
}

function getEventColor(severity: string): string {
  switch (severity) {
    case 'CRITICAL':
    case 'EMERGENCY': return 'text-alert-600 bg-alert-100';
    case 'HIGH': return 'text-warning-600 bg-warning-100';
    case 'MEDIUM': return 'text-grid-600 bg-grid-100';
    case 'LOW': return 'text-grid-500 bg-grid-100';
    case 'INFO': return 'text-blue-600 bg-blue-100';
    default: return 'text-grid-500 bg-grid-100';
  }
}

function ThreatList({ events, formatRelativeTime }: { events: any[]; formatRelativeTime: (ts: string) => string }) {
  const threats = events
    .filter(e => e.eventType === 'ThreatDetected')
    .slice(0, 10);

  if (threats.length === 0) {
    return (
      <div className="p-8 text-center text-grid-400">
        <AlertTriangle className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p>No active threats detected</p>
      </div>
    );
  }

  return (
    <div className="divide-y divide-grid-200">
      {threats.map((event) => {
        const payload = event.payload as any;
        return (
          <div key={event.eventId} className="p-4 hover:bg-grid-50 transition-colors">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-medium text-grid-900">{payload.threatType}</span>
                  <span className={clsx('badge badge-xs', getEventSeverityBadge(payload.severity))}>
                    {payload.severity}
                  </span>
                  <span className="text-xs text-grid-400">{payload.confidence ? (payload.confidence * 100).toFixed(0) + '% conf.' : ''}</span>
                </div>
                <p className="text-sm text-grid-500 mt-1">{payload.description}</p>
                {payload.affectedAssetId && (
                  <p className="text-xs text-grid-400 mt-1">Asset: {payload.affectedAssetId}</p>
                )}
              </div>
              <span className="text-xs text-grid-400 whitespace-nowrap">{formatRelativeTime(event.timestamp)}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function IncidentList({ events, formatRelativeTime }: { events: any[]; formatRelativeTime: (ts: string) => string }) {
  const incidents = events
    .filter(e => e.eventType === 'IncidentCreated' || e.eventType === 'IncidentStateChanged')
    .slice(0, 10);

  if (incidents.length === 0) {
    return (
      <div className="p-8 text-center text-grid-400">
        <FileText className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p>No active incidents</p>
      </div>
    );
  }

  return (
    <div className="divide-y divide-grid-200">
      {incidents.map((event) => {
        const payload = event.payload as any;
        const state = payload.newState || payload.state;
        const severity = payload.severity || payload.newSeverity;
        
        return (
          <div key={event.eventId} className="p-4 hover:bg-grid-50 transition-colors">
            <div className="flex items-start justify-between gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-mono text-sm text-grid-500">{payload.incidentNumber || payload.incidentId?.slice(0, 12)}</span>
                  {state && <span className={clsx('badge badge-xs', getEventSeverityBadge(severity || 'MEDIUM'))}>
                    {state.replace(/_/g, ' ')}
                  </span>}
                </div>
                <p className="text-sm text-grid-900 mt-1">{payload.description || payload.title || 'Incident update'}</p>
                {payload.incidentNumber && (
                  <p className="text-xs text-grid-500 mt-1">State: {state?.replace(/_/g, ' ')}</p>
                )}
              </div>
              <span className="text-xs text-grid-400 whitespace-nowrap">{formatRelativeTime(event.timestamp)}</span>
            </div>
          </div>
        );
      })}
    </div>
  );
}

function ActionQueue({ events }: { events: any[] }) {
  // Generate actions from recent events
  const actions = events
    .filter(e => 
      e.eventType === 'IncidentCreated' || 
      e.eventType === 'IncidentStateChanged' ||
      e.eventType === 'ResponsePlanUpdated'
    )
    .slice(0, 5)
    .map((event) => {
      const payload = event.payload as any;
      const priority = event.severity === 'CRITICAL' || event.severity === 'EMERGENCY' ? 'IMMEDIATE' :
                       event.severity === 'HIGH' ? 'URGENT' : 'HIGH';
      
      return {
        priority,
        action: event.eventType === 'ResponsePlanUpdated' 
          ? `Response plan ${payload.action?.toLowerCase()}: ${payload.name}` 
          : `Review ${event.eventType.replace('Event', '').replace(/([A-Z])/g, ' $1').trim()} for ${payload.incidentNumber || payload.incidentId?.slice(0, 8)}`,
        incidentId: payload.incidentNumber || payload.incidentId,
        assignee: payload.assignedOperatorId || payload.actorId || 'Unassigned',
      };
    });

  if (actions.length === 0) {
    return (
      <div className="p-8 text-center text-grid-400">
        <Activity className="w-12 h-12 mx-auto mb-3 opacity-50" />
        <p>No pending actions</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {actions.map((action, i) => (
        <div key={i} className="p-4 bg-grid-50 rounded-lg border border-grid-200">
          <div className="flex items-start justify-between gap-3">
            <span className={clsx('badge badge-xs font-mono', action.priority === 'IMMEDIATE' && 'badge-critical', action.priority === 'URGENT' && 'badge-high', action.priority === 'HIGH' && 'badge-warning')}>
              {action.priority}
            </span>
          </div>
          <p className="text-sm font-medium text-grid-900 mt-2">{action.action}</p>
          <div className="flex items-center gap-4 mt-2 text-xs text-grid-500">
            <span className="flex items-center gap-1"><FileText className="w-3 h-3" />{action.incidentId || 'N/A'}</span>
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