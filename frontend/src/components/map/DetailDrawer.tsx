import { X, Eye, Play, FileText, MapPin } from 'lucide-react';
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

interface DetailSectionProps {
  title: string;
  children: React.ReactNode;
}

function DetailSection({ title, children }: DetailSectionProps) {
  return (
    <div className="space-y-2">
      <h4 className="font-medium text-grid-900 text-sm">{title}</h4>
      <div className="space-y-2">{children}</div>
    </div>
  );
}

interface DetailRowProps {
  label: string;
  value: React.ReactNode;
  badge?: React.ReactNode;
}

function DetailRow({ label, value, badge }: DetailRowProps) {
  return (
    <div className="flex items-start justify-between gap-3">
      <span className="text-sm text-grid-500">{label}</span>
      <div className="flex items-center gap-2 text-sm font-medium text-grid-900 flex-1 text-right">
        {value}
        {badge}
      </div>
    </div>
  );
}

// Type-safe badge helper functions
function getRiskBadgeVariant(band: string): 'danger' | 'warning' | 'info' | 'secondary' {
  if (band === 'CRITICAL') return 'danger';
  if (band === 'HIGH') return 'warning';
  if (band === 'MEDIUM') return 'info';
  return 'secondary';
}

function getHealthBadgeVariant(status: string): 'success' | 'warning' | 'danger' {
  if (status === 'HEALTHY') return 'success';
  if (status === 'DEGRADED') return 'warning';
  return 'danger';
}

function getSeverityBadgeVariant(severity: string): 'danger' | 'warning' | 'info' | 'secondary' {
  if (severity === 'CRITICAL') return 'danger';
  if (severity === 'HIGH') return 'warning';
  if (severity === 'MEDIUM') return 'info';
  return 'secondary';
}

function getStatusBadgeVariant(status: string): 'info' | 'warning' | 'success' | 'secondary' {
  if (status === 'NEW') return 'info';
  if (status === 'TRIAGED') return 'warning';
  if (status === 'CONFIRMED') return 'success';
  return 'secondary';
}

function getStateBadgeClass(state: string): string {
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
  return stateColors[state] || 'bg-grid-100 text-grid-700';
}

function getSeverityBadgeClass(severity: string): string {
  const severityColors: Record<string, string> = {
    CRITICAL: 'bg-alert-100 text-alert-800',
    HIGH: 'bg-alert-100 text-alert-800',
    MEDIUM: 'bg-warning-100 text-warning-800',
    LOW: 'bg-grid-100 text-grid-700',
    INFO: 'bg-grid-100 text-grid-700',
  };
  return severityColors[severity] || 'bg-grid-100 text-grid-700';
}

function getRiskColor(band: string): string {
  const colors: Record<string, string> = {
    CRITICAL: 'text-alert-600',
    HIGH: 'text-warning-600',
    MEDIUM: 'text-warning-500',
    LOW: 'text-safe-600',
    NEGLIGIBLE: 'text-safe-500',
  };
  return colors[band] || 'text-grid-500';
}

function getHealthColor(status: string): string {
  const colors: Record<string, string> = {
    HEALTHY: 'text-safe-600',
    DEGRADED: 'text-warning-600',
    CRITICAL: 'text-alert-600',
  };
  return colors[status] || 'text-grid-500';
}

function getCriticalityBadgeVariant(criticality: string): 'alert' | 'danger' | 'warning' | 'secondary' {
  if (criticality === 'MISSION_CRITICAL') return 'alert';
  if (criticality === 'HIGH') return 'danger';
  if (criticality === 'MEDIUM') return 'warning';
  return 'secondary';
}

interface NodeDetailProps {
  name: string;
  type: string;
  voltageLevelKv: number;
  primaryAssetId?: string;
  operator?: string;
  degree: number;
  isSubstation: boolean;
  isGeneration: boolean;
  isLoad: boolean;
}

function renderNodeDetails(props: NodeDetailProps) {
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

interface EdgeDetailProps {
  name: string;
  type: string;
  fromNodeId: string;
  toNodeId: string;
  voltageLevelKv: number;
  thermalRatingAmps: number;
  mvaRating?: number;
  lengthKm?: number;
  circuitCount: number;
  conductorType?: string;
  yearInstalled?: number;
  isTransformer: boolean;
  primaryAssetId?: string;
}

interface AssetDetailProps {
  name: string;
  type: string;
  typeDisplayName?: string;
  criticality: string;
  healthStatus: string;
  healthIndex: number;
  riskBand: string;
  riskScore?: number;
  redundancyLevel: number;
  hasRedundancy: boolean;
  baseRiskWeight: number;
  installedAt?: string;
  lastInspectedAt?: string;
  assetId: string;
}

function renderAssetDetails(props: AssetDetailProps) {
  const healthBadgeVariant = getHealthBadgeVariant(props.healthStatus);

  const healthBadge = (
    <span className={getHealthColor(props.healthStatus)}>
      {Math.round(props.healthIndex * 100)}%
      <Badge variant={healthBadgeVariant}>{props.healthStatus}</Badge>
    </span>
  );

  const riskBadge = (
    <span className={getRiskColor(props.riskBand)} font-medium>
      {props.riskBand}
      <Badge variant={getRiskBadgeVariant(props.riskBand)}>{Math.round((props.riskScore || 0) * 100)}%</Badge>
    </span>
  );

  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Name" value={props.name} />
        <DetailRow label="Type" value={<Badge variant="info">{props.typeDisplayName || props.type}</Badge>} />
        <DetailRow label="Criticality" value={<Badge variant={getCriticalityBadgeVariant(props.criticality)}>{props.criticality}</Badge>} />
      </DetailSection>
      <DetailSection title="Health & Risk">
        <DetailRow label="Health Index" value={healthBadge} />
        <DetailRow label="Risk Band" value={riskBadge} />
        <DetailRow
          label="Redundancy"
          value={`N+${props.redundancyLevel}`}
          badge={props.hasRedundancy ? <Badge variant="success">Yes</Badge> : <Badge variant="secondary">No</Badge>}
        />
        <DetailRow label="Base Risk Weight" value={`${(props.baseRiskWeight * 100).toFixed(0)}%`} />
      </DetailSection>
      <DetailSection title="Timeline">
        <DetailRow label="Installed" value={props.installedAt ? new Date(props.installedAt).toLocaleDateString() : '—'} />
        <DetailRow label="Last Inspected" value={props.lastInspectedAt ? new Date(props.lastInspectedAt).toLocaleDateString() : '—'} />
      </DetailSection>
    </div>
  );
}

interface EdgeDetailProps {
  name: string;
  type: string;
  fromNodeId: string;
  toNodeId: string;
  voltageLevelKv: number;
  thermalRatingAmps: number;
  mvaRating?: number;
  lengthKm?: number;
  circuitCount: number;
  conductorType?: string;
  yearInstalled?: number;
  isTransformer: boolean;
  primaryAssetId?: string;
}

function renderEdgeDetails(props: EdgeDetailProps) {
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

interface ThreatDetailProps {
  type: string;
  severity: string;
  confidence: number;
  status: string;
  detectedAt: string;
  firstObservedAt?: string;
  affectedAssetId?: string;
  nearbyGridNodeId?: string;
  detectionStrategy?: string;
  strategyVersion?: string;
  description: string;
  rationale?: string;
}

function renderThreatDetails(props: ThreatDetailProps) {
  const severityBadgeVariant = getSeverityBadgeVariant(props.severity);
  const severityColor = getSeverityBadgeClass(props.severity);

  return (
    <div className="space-y-4">
      <DetailSection title="Threat Details">
        <DetailRow label="Type" value={<Badge variant="info">{props.type}</Badge>} />
        <DetailRow label="Severity" value={<Badge variant={severityBadgeVariant} className={severityColor}>{props.severity}</Badge>} />
        <DetailRow label="Confidence" value={`${Math.round(props.confidence * 100)}%`} />
        <DetailRow label="Status" value={<Badge variant={getStatusBadgeVariant(props.status)}>{props.status}</Badge>} />
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

interface IncidentDetailProps {
  incidentNumber: string;
  state: string;
  severity: string;
  originatingThreatId?: string;
  primaryAssetId?: string;
  primaryGridNodeId?: string;
  detectedAt: string;
  triagedAt?: string;
  confirmedAt?: string;
  responseStartedAt?: string;
  containedAt?: string;
  resolvedAt?: string;
  assignedOperatorId?: string;
  incidentCommanderId?: string;
  description: string;
  operatorNotes?: string;
}

function renderIncidentDetails(props: IncidentDetailProps) {
  return (
    <div className="space-y-4">
      <DetailSection title="Identity">
        <DetailRow label="Incident Number" value={props.incidentNumber} />
        <DetailRow label="State" value={<Badge className={getStateBadgeClass(props.state)}>{props.state.replace('_', ' ')}</Badge>} />
        <DetailRow label="Severity" value={<Badge variant={getSeverityBadgeVariant(props.severity)}>{props.severity}</Badge>} />
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

interface RiskDetailProps {
  assetId: string;
  band: string;
  score: number;
  modelName: string;
  modelVersion: string;
  strategyName?: string;
  computedAt: string;
  factorBreakdown?: Record<string, { contribution: number; description?: string }>;
}

function renderRiskDetails(props: RiskDetailProps) {
  return (
    <div className="space-y-4">
      <DetailSection title="Risk Assessment">
        <DetailRow label="Asset ID" value={<span className="font-mono text-sm">{props.assetId}</span>} />
        <DetailRow label="Risk Band" value={<><span className={getRiskColor(props.band)} font-medium>{props.band}</span> <Badge variant={getRiskBadgeVariant(props.band)}>{Math.round(props.score * 100)}%</Badge></>} />
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
}: DetailDrawerProps) {
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
        return renderNodeDetails(props as any);
      case 'edge':
        return renderEdgeDetails(props as any);
      case 'asset':
        return renderAssetDetails(props as any);
      case 'threat':
        return renderThreatDetails(props as any);
      case 'incident':
        return renderIncidentDetails(props as any);
      case 'risk':
        return renderRiskDetails(props as any);
      default:
        return <div className="text-sm text-grid-500">No details available</div>;
    }
  };

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
              <Button variant="secondary" onClick={() => onRunSimulation?.(featureId)} className="w-full">
                <Play className="w-4 h-4" /> Run Simulation
              </Button>
            </>
          )}
          {featureType === 'incident' && (
            <Button variant="primary" onClick={() => onOpenIncident?.(featureId)} className="w-full">
              <FileText className="w-4 h-4" /> Open Incident
            </Button>
          )}
          {featureType === 'node' && (
            <Button variant="secondary" onClick={() => onInspectTopology?.(featureId)} className="w-full">
              <MapPin className="w-4 h-4" /> Inspect Topology
            </Button>
          )}
        </div>
      </div>
    </>
  );
}