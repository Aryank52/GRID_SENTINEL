import { X, Eye, Play, FileText, MapPin, AlertTriangle } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import type { FeatureType, GeoJsonFeature } from '../../types/spatial';

export interface DetailDrawerProps {
  feature: GeoJsonFeature<any> | null;
  featureType: FeatureType;
  isOpen: boolean;
  onClose: () => void;
  onViewIntelligence?: (assetId: string) => void;
  onRunSimulation?: (assetId: string) => void;
  onOpenIncident?: (incidentId: string) => void;
  onInspectTopology?: (nodeId: string) => void;
  position?: 'right' | 'bottom';
  width?: number;
}

function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <h4 className="font-medium text-grid-900 text-sm">{title}</h4>
      <div className="space-y-2">{children}</div>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <span className="text-sm text-grid-500">{label}</span>
      <div className="flex items-center gap-2 text-sm font-medium text-grid-900 flex-1 text-right">
        {value}
      </div>
    </div>
  );
}

// Helper functions to create badge elements - no complex ternaries in JSX
function createHealthBadge(props: any) {
  const healthColor = props.healthStatus === 'HEALTHY' ? 'text-safe-600' : props.healthStatus === 'DEGRADED' ? 'text-warning-600' : 'text-alert-600';
  const healthBadgeVariant = props.healthStatus === 'HEALTHY' ? 'success' : props.healthStatus === 'DEGRADED' ? 'warning' : 'danger';
  return (
    <span className={healthColor}>
      {Math.round(props.healthIndex * 100)}%
      <Badge variant={healthBadgeVariant}>{props.healthStatus}</Badge>
    </span>
  );
}

function createRiskBadge(props: any) {
  const riskColor = {
    CRITICAL: 'text-alert-600',
    HIGH: 'text-warning-600',
    MEDIUM: 'text-warning-500',
    LOW: 'text-safe-600',
    NEGLIGIBLE: 'text-safe-500',
  }[props.riskBand] || 'text-grid-500';

  const riskBadgeVariant = props.riskBand === 'CRITICAL' ? 'danger' : props.riskBand === 'HIGH' ? 'warning' : props.riskBand === 'MEDIUM' ? 'info' : 'secondary';

  return (
    <span className={riskColor} font-medium>
      {props.riskBand}
      <Badge variant={props.riskBand === 'CRITICAL' ? 'danger' : props.riskBand === 'HIGH' ? 'warning' : props.riskBand === 'MEDIUM' ? 'info' : 'secondary'}>
        {Math.round((props.riskScore || 0) * 100)}%
      </Badge>
    </span>
  );
}

function createSeverityBadge(severity: string) {
  const severityBadgeVariant = severity === 'CRITICAL' ? 'danger' : severity === 'HIGH' ? 'warning' : severity === 'MEDIUM' ? 'info' : 'secondary';
  const severityColors: Record<string, string> = {
    CRITICAL: 'bg-alert-100 text-alert-800',
    HIGH: 'bg-alert-100 text-alert-800',
    MEDIUM: 'bg-warning-100 text-warning-800',
    LOW: 'bg-grid-100 text-grid-700',
    INFO: 'bg-grid-100 text-grid-700',
  };
  const severityColor = severityColors[severity] || 'bg-grid-100 text-grid-700';
  return <Badge variant={severityBadgeVariant} className={severityColor}>{severity}</Badge>;
}

function createStatusBadge(status: string) {
  const statusBadgeVariant = status === 'NEW' ? 'info' : status === 'TRIAGED' ? 'warning' : status === 'CONFIRMED' ? 'success' : 'secondary';
  return <Badge variant={statusBadgeVariant}>{status}</Badge>;
}

function createStateBadge(state: string) {
  const stateColors: Record<string, string> = {
    DETECTED: 'bg-grid-100 text-grid-700',
    TRIAGED: 'bg-warning-100 text-warning-800',
    CONFIRMED: 'bg-info-100 text-info-800',
    ACTIVE_RESPONSE: 'bg-alert-100 text-alert-800',
    CONTAINED: 'bg-warning-100 text-warning-800',
    RECOVERING: 'bg-info-100 text-info-800',
    RESOLVED: 'bg-success-100 text-success-800',
    CLOSED: 'bg-grid-100 text-grid-700',
  };
  const stateColor = stateColors[state] || 'bg-grid-100 text-grid-700';
  return <Badge className={stateColor}>{state.replace('_', ' ')}</Badge>;
}

function createSeverityBadge(severity: string) {
  const severityBadgeVariant = severity === 'CRITICAL' ? 'danger' : severity === 'HIGH' ? 'warning' : severity === 'MEDIUM' ? 'info' : 'secondary';
  return <Badge variant={severityBadgeVariant}>{severity}</Badge>;
}

function createRiskBadge(riskBand: string, riskScore: number) {
  const riskColor = {
    CRITICAL: 'text-alert-600',
    HIGH: 'text-warning-600',
    MEDIUM: 'text-warning-500',
    LOW: 'text-safe-600',
    NEGLIGIBLE: 'text-safe-500',
  }[riskBand] || 'text-grid-500';

  const riskBadgeVariant = riskBand === 'CRITICAL' ? 'danger' : riskBand === 'HIGH' ? 'warning' : riskBand === 'MEDIUM' ? 'info' : 'secondary';

  return (
    <span className={riskColor} font-medium>
      {riskBand}
      <Badge variant={riskBadgeVariant}>{Math.round((riskScore || 0) * 100)}%</Badge>
    </span>
  );
}

function createHealthBadge(healthStatus: string, healthIndex: number) {
  const healthColor = healthStatus === 'HEALTHY' ? 'text-safe-600' : healthStatus === 'DEGRADED' ? 'text-warning-600' : 'text-alert-600';
  const healthBadgeVariant = healthStatus === 'HEALTHY' ? 'success' : healthStatus === 'DEGRADED' ? 'warning' : 'danger';
  return (
    <span className={healthColor}>
      {Math.round(healthIndex * 100)}%
      <Badge variant={healthBadgeVariant}>{healthStatus}</Badge>
    </span>
  );
}

function createCriticalityBadge(criticality: string) {
  const criticalityBadgeVariant = criticality === 'MISSION_CRITICAL' ? 'alert' : criticality === 'HIGH' ? 'danger' : criticality === 'MEDIUM' ? 'warning' : 'secondary';
  return <Badge variant={criticalityBadgeVariant}>{criticality}</Badge>;
}

// Simple render functions - no complex ternaries in JSX
function renderNodeDetails(props: any) {
  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Name" value={props.name} />
        <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
        <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
        <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
        <DetailRow label="Operator" value={props.operator || '—'} />
      </DetailSection>
      <DetailSection title="Connectivity">
        <DetailRow label="Connected Edges" value={props.degree} />
        <DetailRow label="Is Substation" value={props.isSubstation ? 'Yes' : 'No'} />
        <DetailRow label="Is Generation" value={props.isGeneration ? 'Yes' : 'No'} />
        <DetailRow label="Is Load" value={props.isLoad ? 'Yes' : 'No'} />
      </DetailSection>
    </div>
  );
}

function renderEdgeDetails(props: any) {
  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Name" value={props.name} />
        <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
        <DetailRow label="From Node" value={props.fromNodeId} />
        <DetailRow label="To Node" value={props.toNodeId} />
        <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
      </DetailSection>
      <DetailSection title="Electrical Properties">
        <DetailRow label="Thermal Rating" value={`${props.thermalRatingAmps} A`} />
        <DetailRow label="MVA Rating" value={`${props.mvaRating?.toFixed(1) || 'N/A'} MVA`} />
        <DetailRow label="Length" value={`${props.lengthKm?.toFixed(2) || 'N/A'} km`} />
        <DetailRow label="Circuits" value={props.circuitCount} />
        <DetailRow label="Conductor" value={props.conductorType || '—'} />
        <DetailRow label="Year Installed" value={props.yearInstalled || '—'} />
      </DetailSection>
      <DetailSection title="Status">
        <DetailRow label="Is Transformer" value={props.isTransformer ? 'Yes' : 'No'} />
        <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
      </DetailSection>
    </div>
  );
}

function renderAssetDetails(props: any) {
  const riskColor = {
    CRITICAL: 'text-alert-600',
    HIGH: 'text-warning-600',
    MEDIUM: 'text-warning-500',
    LOW: 'text-safe-600',
    NEGLIGIBLE: 'text-safe-500',
  }[props.riskBand] || 'text-grid-500';

  const healthColor = {
    HEALTHY: 'text-safe-600',
    DEGRADED: 'text-warning-600',
    CRITICAL: 'text-alert-600',
  }[props.healthStatus] || 'text-grid-500';

  const healthBadgeVariant = props.healthStatus === 'HEALTHY' ? 'success' : props.healthStatus === 'DEGRADED' ? 'warning' : 'danger';
  const riskBadgeVariant = props.riskBand === 'CRITICAL' ? 'danger' : props.riskBand === 'HIGH' ? 'warning' : props.riskBand === 'MEDIUM' ? 'info' : 'secondary';
  const criticalityBadgeVariant = props.criticality === 'MISSION_CRITICAL' ? 'alert' : props.criticality === 'HIGH' ? 'danger' : props.criticality === 'MEDIUM' ? 'warning' : 'secondary';

  const healthBadge = createHealthBadge(props.healthStatus, props.healthIndex);
  const riskBadge = createRiskBadge(props);

  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Name" value={props.name} />
        <DetailRow label="Type" value={<Badge variant="info">{props.typeDisplayName || props.type}</Badge>} />
        <DetailRow label="Criticality" value={<Badge variant={criticalityBadgeVariant}>{props.criticality}</Badge>} />
      </DetailSection>
      <DetailSection title="Health & Risk">
        <DetailRow label="Health Index" value={createHealthBadge(props.healthStatus, props.healthIndex)} />
        <DetailRow label="Risk Band" value={createRiskBadge(props)} />
        <DetailRow label="Redundancy" value={`N+${props.redundancyLevel}`} badge={props.hasRedundancy ? <Badge variant="success">Yes</Badge> : <Badge variant="secondary">No</Badge>} />
        <DetailRow label="Base Risk Weight" value={`${(props.baseRiskWeight * 100).toFixed(0)}%`} />
      </DetailSection>
      <DetailSection title="Timeline">
        <DetailRow label="Installed" value={props.installedAt ? new Date(props.installedAt).toLocaleDateString() : '—'} />
        <DetailRow label="Last Inspected" value={props.lastInspectedAt ? new Date(props.lastInspectedAt).toLocaleDateString() : '—'} />
      </DetailSection>
    </div>
  );
}

function renderEdgeDetails(props: any) {
  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Name" value={props.name} />
        <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
        <DetailRow label="From Node" value={props.fromNodeId} />
        <DetailRow label="To Node" value={props.toNodeId} />
        <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
      </DetailSection>
      <DetailSection title="Electrical Properties">
        <DetailRow label="Thermal Rating" value={`${props.thermalRatingAmps} A`} />
        <DetailRow label="MVA Rating" value={`${props.mvaRating?.toFixed(1) || 'N/A'} MVA`} />
        <DetailRow label="Length" value={`${props.lengthKm?.toFixed(2) || 'N/A'} km`} />
        <DetailRow label="Circuits" value={props.circuitCount} />
        <DetailRow label="Conductor" value={props.conductorType || '—'} />
        <DetailRow label="Year Installed" value={props.yearInstalled || '—'} />
      </DetailSection>
      <DetailSection title="Status">
        <DetailRow label="Is Transformer" value={props.isTransformer ? 'Yes' : 'No'} />
        <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
      </DetailSection>
    </div>
  );
}

function renderThreatDetails(props: any) {
  const severityColors: Record<string, string> = {
    CRITICAL: 'bg-alert-100 text-alert-800',
    HIGH: 'bg-alert-100 text-alert-800',
    MEDIUM: 'bg-warning-100 text-warning-800',
    LOW: 'bg-grid-100 text-grid-700',
    INFO: 'bg-grid-100 text-grid-700',
  };
  const severityBadgeVariant = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
  const statusBadgeVariant = props.status === 'NEW' ? 'info' : props.status === 'TRIAGED' ? 'warning' : props.status === 'CONFIRMED' ? 'success' : 'secondary';
  const severityColor = severityColors[props.severity] || 'bg-grid-100 text-grid-700';
  const statusBadge = props.status === 'NEW' ? 'info' : props.status === 'TRIAGED' ? 'warning' : props.status === 'CONFIRMED' ? 'success' : 'secondary';

  return (
    <div className="space-y-4">
      <DetailSection title="Threat Details">
        <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
        <DetailRow label="Severity" value={<Badge variant={severityBadgeVariant} className={severityColors[props.severity]}>{props.severity}</Badge>} />
        <DetailRow label="Confidence" value={`${Math.round(props.confidence * 100)}%`} />
        <DetailRow label="Status" value={<Badge variant={statusBadge}>{props.status}</Badge>} />
        <DetailRow label="Detected" value={new Date(props.detectedAt).toLocaleString()} />
        <DetailRow label="First Observed" value={props.firstObservedAt ? new Date(props.firstObservedAt).toLocaleString() : '—'} />
      </DetailSection>
      <DetailSection title="Context">
        <DetailRow label="Affected Asset" value={props.affectedAssetId || '—'} />
        <DetailRow label="Nearby Node" value={props.nearbyGridNodeId || '—'} />
        <DetailRow label="Detection Strategy" value={props.detectionStrategy || '—'} />
        <DetailRow label="Strategy Version" value={props.strategyVersion || '—'} />
      </DetailSection>
      <DetailSection title="Description">
        <p className="text-sm text-grid-700">{props.description}</p>
        {props.rationale && <p className="text-sm text-grid-500 mt-2">{props.rationale}</p>}
      </DetailSection>
    </div>
  );
}

function renderIncidentDetails(props: any) {
  const stateColors: Record<string, string> = {
    DETECTED: 'bg-grid-100 text-grid-700',
    TRIAGED: 'bg-warning-100 text-warning-800',
    CONFIRMED: 'bg-info-100 text-info-800',
    ACTIVE_RESPONSE: 'bg-alert-100 text-alert-800',
    CONTAINED: 'bg-warning-100 text-warning-800',
    RECOVERING: 'bg-info-100 text-info-800',
    RESOLVED: 'bg-success-100 text-success-800',
    CLOSED: 'bg-grid-100 text-grid-700',
  };
  const severityBadgeVariant = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
  const stateColor = stateColors[props.state] || 'bg-grid-100 text-grid-700';

  const severityBadge = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
  const stateColorValue = stateColors[props.state] || 'bg-grid-100 text-grid-700';

  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Incident Number" value={props.incidentNumber} />
        <DetailRow label="State" value={<Badge className={stateColorValue}>{props.state.replace('_', ' ')}</Badge>} />
        <DetailRow label="Severity" value={<Badge variant={severityBadge}>{props.severity}</Badge>} />
        <DetailRow label="Originating Threat" value={props.originatingThreatId || '—'} />
      </DetailSection>
      <DetailSection title="Assets & Location">
        <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
        <DetailRow label="Primary Node" value={props.primaryGridNodeId || '—'} />
      </DetailSection>
      <DetailSection title="Timeline">
        <DetailRow label="Detected" value={new Date(props.detectedAt).toLocaleString()} />
        <DetailRow label="Triaged" value={props.triagedAt ? new Date(props.triagedAt).toLocaleString() : '—'} />
        <DetailRow label="Confirmed" value={props.confirmedAt ? new Date(props.confirmedAt).toLocaleString() : '—'} />
        <DetailRow label="Response Started" value={props.responseStartedAt ? new Date(props.responseStartedAt).toLocaleString() : '—'} />
        <DetailRow label="Contained" value={props.containedAt ? new Date(props.containedAt).toLocaleString() : '—'} />
        <DetailRow label="Resolved" value={props.resolvedAt ? new Date(props.resolvedAt).toLocaleString() : '—'} />
      </DetailSection>
      <DetailSection title="Assignment">
        <DetailRow label="Assigned Operator" value={props.assignedOperatorId || '—'} />
        <DetailRow label="Incident Commander" value={props.incidentCommanderId || '—'} />
      </DetailSection>
      <DetailSection title="Description">
        <p className="text-sm text-grid-700">{props.description}</p>
        {props.operatorNotes && <p className="text-sm text-grid-500 mt-2">{props.operatorNotes}</p>}
      </DetailSection>
    </div>
  );
}

function renderRiskDetails(props: any) {
  const riskColor = {
    CRITICAL: 'text-alert-600',
    HIGH: 'text-warning-600',
    MEDIUM: 'text-warning-500',
    LOW: 'text-safe-600',
    NEGLIGIBLE: 'text-safe-500',
  }[props.band] || 'text-grid-500';

  const riskBadgeVariant = props.band === 'CRITICAL' ? 'danger' : props.band === 'HIGH' ? 'warning' : props.band === 'MEDIUM' ? 'info' : 'secondary';

  return (
    <div className="space-y-4">
      <DetailSection title="Risk Assessment">
        <DetailRow label="Asset ID" value={<span className="font-mono text-sm">{props.assetId}</span>} />
        <DetailRow label="Risk Band" value={<><span className={riskColor} font-medium>{props.band}</span> <Badge variant={riskBadgeVariant}>{Math.round(props.score * 100)}%</Badge></>} />
        <DetailRow label="Model" value={`${props.modelName} v${props.modelVersion}`} />
        <DetailRow label="Strategy" value={props.strategyName || '—'} />
        <DetailRow label="Computed" value={new Date(props.computedAt).toLocaleString()} />
      </DetailSection>
      <DetailSection title="Factor Breakdown">
        {Object.entries(props.factorBreakdown || {}).map(([factor, contrib]: [string, any]) => (
          <DetailRow key={factor} label={factor} value={contrib.description || `${Math.round(contrib.contribution * 100)}%`} />
        ))}
      </DetailSection>
    </div>
  );
}

function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <h4 className="font-medium text-grid-900 text-sm">{title}</h4>
      <div className="space-y-2">{children}</div>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <span className="text-sm text-grid-500">{label}</span>
      <div className="flex items-center gap-2 text-sm font-medium text-grid-900 flex-1 text-right">
        {value}
      </div>
    </div>
  );
}

export function DetailDrawer({
  feature,
  featureType,
  isOpen,
  onClose,
  onViewIntelligence,
  onRunSimulation,
  onOpenIncident,
  onInspectTopology,
  position = 'right',
  width = 400,
}: {
  feature: GeoJsonFeature<any> | null;
  featureType: FeatureType;
  isOpen: boolean;
  onClose: () => void;
  onViewIntelligence?: (assetId: string) => void;
  onRunSimulation?: (assetId: string) => void;
  onOpenIncident?: (incidentId: string) => void;
  onInspectTopology?: (nodeId: string) => void;
  position?: 'right' | 'bottom';
  width?: number;
}) {
  if (!isOpen || !feature) return null;

  const props = feature.properties;
  const featureId = feature.id || '';

  const getTitle = () => {
    switch (featureType) {
      case 'node': return props.name || 'Grid Node';
      case 'edge': return props.name || 'Grid Edge';
      case 'asset': return props.name || 'Asset';
      case 'threat': return props.type || 'Threat Event';
      case 'incident': return props.incidentNumber || 'Incident';
      case 'risk': return `Risk: ${props.band}`;
      default: return 'Details';
    }
  };

  const getSubtitle = () => {
    switch (featureType) {
      case 'node': return `${props.type} • ${props.voltageLevelKv} kV`;
      case 'edge': return `${props.type} • ${props.voltageLevelKv} kV • ${props.lengthKm?.toFixed(1) || '?'} km`;
      case 'asset': return `${props.typeDisplayName || props.type} • ${props.criticality} • ${props.riskBand || 'No risk'}`;
      case 'threat': return `${props.severity} • ${Math.round(props.confidence * 100)}% conf.`;
      case 'incident': return `${props.state.replace('_', ' ')} • ${props.severity}`;
      case 'risk': return `Score: ${Math.round(props.score * 100)}% • ${props.band}`;
      default: return '';
    }
  };

  const renderDetails = () => {
    switch (featureType) {
      case 'node':
        return renderNodeDetails(props);
      case 'edge':
        return renderEdgeDetails(props);
      case 'asset':
        return renderAssetDetails(props);
      case 'threat':
        return renderThreatDetails(props);
      case 'incident':
        return renderIncidentDetails(props);
      case 'risk':
        return renderRiskDetails(props);
      default:
        return <div className="text-sm text-grid-500">No details available</div>;
    }
  };

  const getTitle = () => {
    switch (featureType) {
      case 'node': return props.name || 'Grid Node';
      case 'edge': return props.name || 'Grid Edge';
      case 'asset': return props.name || 'Asset';
      case 'threat': return props.type || 'Threat Event';
      case 'incident': return props.incidentNumber || 'Incident';
      case 'risk': return `Risk: ${props.band}`;
      default: return 'Details';
    }
  };

  const getSubtitle = () => {
    switch (featureType) {
      case 'node': return `${props.type} • ${props.voltageLevelKv} kV`;
      case 'edge': return `${props.type} • ${props.voltageLevelKv} kV • ${props.lengthKm?.toFixed(1) || '?'} km`;
      case 'asset': return `${props.typeDisplayName || props.type} • ${props.criticality} • ${props.riskBand || 'No risk'}`;
      case 'threat': return `${props.severity} • ${Math.round(props.confidence * 100)}% conf.`;
      case 'incident': return `${props.state.replace('_', ' ')} • ${props.severity}`;
      case 'risk': return `Score: ${Math.round(props.score * 100)}% • ${props.band}`;
      default: return '';
    }
  };

  // Render functions
  function renderNodeDetails(props: any) {
    return (
      <div className="space-y-4">
        <DetailSection title="Identity">
          <DetailRow label="Name" value={props.name} />
          <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
          <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
          <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
          <DetailRow label="Operator" value={props.operator || '—'} />
        </DetailSection>
        <DetailSection title="Connectivity">
          <DetailRow label="Connected Edges" value={props.degree} />
          <DetailRow label="Is Substation" value={props.isSubstation ? 'Yes' : 'No'} />
          <DetailRow label="Is Generation" value={props.isGeneration ? 'Yes' : 'No'} />
          <DetailRow label="Is Load" value={props.isLoad ? 'Yes' : 'No'} />
        </DetailSection>
      </div>
    );
  }

  function renderEdgeDetails(props: any) {
    return (
      <div className="space-y-4">
        <DetailSection title="Identity">
          <DetailRow label="Name" value={props.name} />
          <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
          <DetailRow label="From Node" value={props.fromNodeId} />
          <DetailRow label="To Node" value={props.toNodeId} />
          <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
        </DetailSection>
        <DetailSection title="Electrical Properties">
          <DetailRow label="Thermal Rating" value={`${props.thermalRatingAmps} A`} />
          <DetailRow label="MVA Rating" value={`${props.mvaRating?.toFixed(1) || 'N/A'} MVA`} />
          <DetailRow label="Length" value={`${props.lengthKm?.toFixed(2) || 'N/A'} km`} />
          <DetailRow label="Circuits" value={props.circuitCount} />
          <DetailRow label="Conductor" value={props.conductorType || '—'} />
          <DetailRow label="Year Installed" value={props.yearInstalled || '—'} />
        </DetailSection>
        <DetailSection title="Status">
          <DetailRow label="Is Transformer" value={props.isTransformer ? 'Yes' : 'No'} />
          <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
        </DetailSection>
      </div>
    );
  }

  function renderAssetDetails(props: any) {
    const riskColor = {
      CRITICAL: 'text-alert-600',
      HIGH: 'text-warning-600',
      MEDIUM: 'text-warning-500',
      LOW: 'text-safe-600',
      NEGLIGIBLE: 'text-safe-500',
    }[props.riskBand] || 'text-grid-500';

    const healthColor = {
      HEALTHY: 'text-safe-600',
      DEGRADED: 'text-warning-600',
      CRITICAL: 'text-alert-600',
    }[props.healthStatus] || 'text-grid-500';

    const healthBadgeVariant = props.healthStatus === 'HEALTHY' ? 'success' : props.healthStatus === 'DEGRADED' ? 'warning' : 'danger';
    const riskBadgeVariant = props.riskBand === 'CRITICAL' ? 'danger' : props.riskBand === 'HIGH' ? 'warning' : props.riskBand === 'MEDIUM' ? 'info' : 'secondary';
    const criticalityBadgeVariant = props.criticality === 'MISSION_CRITICAL' ? 'alert' : props.criticality === 'HIGH' ? 'danger' : props.criticality === 'MEDIUM' ? 'warning' : 'secondary';

    const healthBadge = createHealthBadge(props.healthStatus, props.healthIndex);
    const riskBadge = createRiskBadge(props);

    return (
      <div className="space-y-4">
        <DetailSection title="Identity">
          <DetailRow label="Name" value={props.name} />
          <DetailRow label="Type" value={<Badge variant="info">{props.typeDisplayName || props.type}</Badge>} />
          <DetailRow label="Criticality" value={<Badge variant={criticalityBadgeVariant}>{props.criticality}</Badge>} />
        </DetailSection>
        <DetailSection title="Health & Risk">
          <DetailRow label="Health Index" value={createHealthBadge(props.healthStatus, props.healthIndex)} />
          <DetailRow label="Risk Band" value={createRiskBadge(props)} />
          <DetailRow label="Redundancy" value={`N+${props.redundancyLevel}`} badge={props.hasRedundancy ? <Badge variant="success">Yes</Badge> : <Badge variant="secondary">No</Badge>} />
          <DetailRow label="Base Risk Weight" value={`${(props.baseRiskWeight * 100).toFixed(0)}%`} />
        </DetailSection>
        <DetailSection title="Timeline">
          <DetailRow label="Installed" value={props.installedAt ? new Date(props.installedAt).toLocaleDateString() : '—'} />
          <DetailRow label="Last Inspected" value={props.lastInspectedAt ? new Date(props.lastInspectedAt).toLocaleDateString() : '—'} />
      </DetailSection>
    </div>
  );
}

  function renderEdgeDetails(props: any) {
    return (
      <div className="space-y-4">
        <DetailSection title="Identity">
          <DetailRow label="Name" value={props.name} />
          <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
          <DetailRow label="From Node" value={props.fromNodeId} />
          <DetailRow label="To Node" value={props.toNodeId} />
          <DetailRow label="Voltage" value={`${props.voltageLevelKv} kV`} />
        </DetailSection>
        <DetailSection title="Electrical Properties">
          <DetailRow label="Thermal Rating" value={`${props.thermalRatingAmps} A`} />
          <DetailRow label="MVA Rating" value={`${props.mvaRating?.toFixed(1) || 'N/A'} MVA`} />
          <DetailRow label="Length" value={`${props.lengthKm?.toFixed(2) || 'N/A'} km`} />
          <DetailRow label="Circuits" value={props.circuitCount} />
          <DetailRow label="Conductor" value={props.conductorType || '—'} />
          <DetailRow label="Year Installed" value={props.yearInstalled || '—'} />
        </DetailSection>
        <DetailSection title="Status">
          <DetailRow label="Is Transformer" value={props.isTransformer ? 'Yes' : 'No'} />
          <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
        </DetailSection>
      </div>
    );
  }

  function renderThreatDetails(props: any) {
    const severityColors: Record<string, string> = {
      CRITICAL: 'bg-alert-100 text-alert-800',
      HIGH: 'bg-alert-100 text-alert-800',
      MEDIUM: 'bg-warning-100 text-warning-800',
      LOW: 'bg-grid-100 text-grid-700',
      INFO: 'bg-grid-100 text-grid-700',
    };
    const severityBadgeVariant = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
    const statusBadgeVariant = props.status === 'NEW' ? 'info' : props.status === 'TRIAGED' ? 'warning' : props.status === 'CONFIRMED' ? 'success' : 'secondary';
    const severityColor = severityColors[props.severity] || 'bg-grid-100 text-grid-700';
    const statusBadge = props.status === 'NEW' ? 'info' : props.status === 'TRIAGED' ? 'warning' : props.status === 'CONFIRMED' ? 'success' : 'secondary';

    return (
      <div className="space-y-4">
        <DetailSection title="Threat Details">
          <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
          <DetailRow label="Severity" value={<Badge variant={severityBadgeVariant} className={severityColors[props.severity]}>{props.severity}</Badge>} />
          <DetailRow label="Confidence" value={`${Math.round(props.confidence * 100)}%`} />
          <DetailRow label="Status" value={<Badge variant={statusBadge}>{props.status}</Badge>} />
          <DetailRow label="Detected" value={new Date(props.detectedAt).toLocaleString()} />
          <DetailRow label="First Observed" value={props.firstObservedAt ? new Date(props.firstObservedAt).toLocaleString() : '—'} />
        </DetailSection>
        <DetailSection title="Context">
          <DetailRow label="Affected Asset" value={props.affectedAssetId || '—'} />
          <DetailRow label="Nearby Node" value={props.nearbyGridNodeId || '—'} />
          <DetailRow label="Detection Strategy" value={props.detectionStrategy || '—'} />
          <DetailRow label="Strategy Version" value={props.strategyVersion || '—'} />
        </DetailSection>
        <DetailSection title="Description">
          <p className="text-sm text-grid-700">{props.description}</p>
          {props.rationale && <p className="text-sm text-grid-500 mt-2">{props.rationale}</p>}
        </DetailSection>
      </div>
    );
  }

  function renderIncidentDetails(props: any) {
    const stateColors: Record<string, string> = {
      DETECTED: 'bg-grid-100 text-grid-700',
      TRIAGED: 'bg-warning-100 text-warning-800',
      CONFIRMED: 'bg-info-100 text-info-800',
      ACTIVE_RESPONSE: 'bg-alert-100 text-alert-800',
      CONTAINED: 'bg-warning-100 text-warning-800',
      RECOVERING: 'bg-info-100 text-info-800',
      RESOLVED: 'bg-success-100 text-success-800',
      CLOSED: 'bg-grid-100 text-grid-700',
    };
    const severityBadgeVariant = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
    const stateColor = stateColors[props.state] || 'bg-grid-100 text-grid-700';

    const severityBadge = props.severity === 'CRITICAL' ? 'danger' : props.severity === 'HIGH' ? 'warning' : props.severity === 'MEDIUM' ? 'info' : 'secondary';
    const stateColorValue = stateColors[props.state] || 'bg-grid-100 text-grid-700';

    return (
      <div className="space-y-4">
        <DetailSection title="Identity">
          <DetailRow label="Incident Number" value={props.incidentNumber} />
          <DetailRow label="State" value={<Badge className={stateColorValue}>{props.state.replace('_', ' ')}</Badge>} />
          <DetailRow label="Severity" value={<Badge variant={severityBadge}>{props.severity}</Badge>} />
          <DetailRow label="Originating Threat" value={props.originatingThreatId || '—'} />
        </DetailSection>
        <DetailSection title="Assets & Location">
          <DetailRow label="Primary Asset" value={props.primaryAssetId || '—'} />
          <DetailRow label="Primary Node" value={props.primaryGridNodeId || '—'} />
        </DetailSection>
        <DetailSection title="Timeline">
          <DetailRow label="Detected" value={new Date(props.detectedAt).toLocaleString()} />
          <DetailRow label="Triaged" value={props.triagedAt ? new Date(props.triagedAt).toLocaleString() : '—'} />
          <DetailRow label="Confirmed" value={props.confirmedAt ? new Date(props.confirmedAt).toLocaleString() : '—'} />
          <DetailRow label="Response Started" value={props.responseStartedAt ? new Date(props.responseStartedAt).toLocaleString() : '—'} />
          <DetailRow label="Contained" value={props.containedAt ? new Date(props.containedAt).toLocaleString() : '—'} />
          <DetailRow label="Resolved" value={props.resolvedAt ? new Date(props.resolvedAt).toLocaleString() : '—'} />
        </DetailSection>
        <DetailSection title="Assignment">
          <DetailRow label="Assigned Operator" value={props.assignedOperatorId || '—'} />
          <DetailRow label="Incident Commander" value={props.incidentCommanderId || '—'} />
        </DetailSection>
        <DetailSection title="Description">
          <p className="text-sm text-grid-700">{props.description}</p>
          {props.operatorNotes && <p className="text-sm text-grid-500 mt-2">{props.operatorNotes}</p>}
        </DetailSection>
      </div>
    );
  }

  function renderRiskDetails(props: any) {
    const riskColor = {
      CRITICAL: 'text-alert-600',
      HIGH: 'text-warning-600',
      MEDIUM: 'text-warning-500',
      LOW: 'text-safe-600',
      NEGLIGIBLE: 'text-safe-500',
    }[props.band] || 'text-grid-500';

    const riskBadgeVariant = props.band === 'CRITICAL' ? 'danger' : props.band === 'HIGH' ? 'warning' : props.band === 'MEDIUM' ? 'info' : 'secondary';

    return (
      <div className="space-y-4">
        <DetailSection title="Risk Assessment">
          <DetailRow label="Asset ID" value={<span className="font-mono text-sm">{props.assetId}</span>} />
          <DetailRow label="Risk Band" value={<><span className={riskColor} font-medium>{props.band}</span> <Badge variant={riskBadgeVariant}>{Math.round(props.score * 100)}%</Badge></>} />
          <DetailRow label="Model" value={`${props.modelName} v${props.modelVersion}`} />
          <DetailRow label="Strategy" value={props.strategyName || '—'} />
          <DetailRow label="Computed" value={new Date(props.computedAt).toLocaleString()} />
        </DetailSection>
        <DetailSection title="Factor Breakdown">
          {Object.entries(props.factorBreakdown || {}).map(([factor, contrib]: [string, any]) => (
            <DetailRow key={factor} label={factor} value={contrib.description || `${Math.round(contrib.contribution * 100)}%`} />
          ))}
        </DetailSection>
      </div>
    );
  }

  function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
    return (
      <div className="space-y-2">
        <h4 className="font-medium text-grid-900 text-sm">{title}</h4>
        <div className="space-y-2">{children}</div>
      </div>
    );
  }

  function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
    return (
      <div className="flex items-start justify-between gap-3">
        <span className="text-sm text-grid-500">{label}</span>
        <div className="flex items-center gap-2 text-sm font-medium text-grid-900 flex-1 text-right">
          {value}
        </div>
      </div>
    );
  }

  const isRight = position === 'right';

  return (
    <>
      <div className="fixed inset-0 bg-black/30 z-40" onClick={onClose} aria-hidden="true" />
      <div
        className={`fixed z-50 flex flex-col bg-white shadow-xl transition-transform duration-300 ease-out ${position === 'right' ? 'right-0 top-0 h-full w-96 max-w-full border-l border-grid-200' : 'bottom-0 left-0 right-0 h-96 max-h-[60vh] border-t border-grid-200'}`}
        style={{ width: `${width}px` }}
        role="dialog"
        aria-modal="true"
        aria-labelledby="drawer-title"
      >
        <div className="flex items-start justify-between p-4 border-b border-grid-200">
          <div className="flex-1 min-w-0 pr-4">
            <h2 id="drawer-title" className="font-semibold text-grid-900 truncate">{getTitle()}</h2>
            <p className="text-sm text-grid-500 mt-0.5">{getSubtitle()}</p>
          </div>
          <button onClick={onClose} className="btn btn-ghost btn-icon p-1 ml-2 flex-shrink-0" aria-label="Close detail drawer">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {renderDetails()}
        </div>

        <div className="flex flex-col gap-2 pt-4 border-t border-grid-200">
          {featureType === 'asset' && (
            <>
              <Button variant="primary" onClick={() => onViewIntelligence?.(featureId)} className="w-full">
                <Eye className="w-4 h-4" /> View Intelligence
              </Button>
              <Button variant="secondary" onClick={() => console.log('Run simulation for', featureId)} className="w-full">
                <Play className="w-4 h-4" /> Run Simulation
              </Button>
            </>
          )}
          {featureType === 'incident' && (
            <Button variant="primary" onClick={() => console.log('Open incident', featureId)} className="w-full">
              <FileText className="w-4 h-4" /> Open Incident
            </Button>
          )}
          {featureType === 'node' && (
            <Button variant="secondary" onClick={() => console.log('Inspect topology for', featureId)} className="w-full">
              <MapPin className="w-4 h-4" /> Inspect Topology
            </Button>
          )}
        </div>
      </div>
    </>
  );
}

function DetailSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-2">
      <h4 className="font-medium text-grid-900 text-sm">{title}</h4>
      <div className="space-y-2">{children}</div>
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-start justify-between gap-3">
      <span className="text-sm text-grid-500">{label}</span>
      <div className="flex items-center gap-2 text-sm font-medium text-grid-900 flex-1 text-right">
        {value}
      </div>
    </div>
  );
}