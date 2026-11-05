import { AlertTriangle, Eye, Play, Pause } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export function ThreatDetectionCenter() {
  return (
    <PageTemplate
      title="Threat Detection Center"
      description="Real-time detection pipeline: observation → validation → fusion → classification → alert"
      icon={AlertTriangle}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm"><Pause className="w-4 h-4" /> Pause Pipeline</Button>
          <Button variant="primary" size="sm"><Play className="w-4 h-4" /> Resume</Button>
        </div>
      }
    >
      {/* Pipeline Status */}
      <Card>
        <CardHeader title="Detection Pipeline Status" description="End-to-end flow monitoring" />
        <div className="flex flex-wrap gap-4">
          {[
            { label: 'Ingestion', status: 'healthy', rate: '1,247/min', latency: '12ms' },
            { label: 'Validation', status: 'healthy', rate: '1,240/min', latency: '8ms' },
            { label: 'Normalization', status: 'healthy', rate: '1,235/min', latency: '15ms' },
            { label: 'Feature Extraction', status: 'degraded', rate: '1,180/min', latency: '45ms' },
            { label: 'Fusion Engine', status: 'healthy', rate: '340/min', latency: '22ms' },
            { label: 'Classification', status: 'healthy', rate: '340/min', latency: '18ms' },
            { label: 'Alert Dispatch', status: 'healthy', rate: '12/min', latency: '5ms' },
          ].map((stage, i) => (
            <div key={i} className="flex-1 min-w-[180px] p-4 bg-grid-50 rounded-lg border border-grid-200">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-grid-900">{stage.label}</span>
                <Badge variant={stage.status === 'healthy' ? 'success' : 'warning'}>
                  {stage.status}
                </Badge>
              </div>
              <div className="mt-2 space-y-1 text-sm text-grid-500">
                <div className="flex justify-between"><span>Rate</span><span className="font-mono text-grid-900">{stage.rate}</span></div>
                <div className="flex justify-between"><span>Latency</span><span className="font-mono text-grid-900">{stage.latency}</span></div>
              </div>
              <div className="mt-2 h-2 bg-grid-200 rounded-full overflow-hidden">
                <div className="h-full bg-grid-500 rounded-full" style={{ width: stage.status === 'healthy' ? '100%' : '70%' }} />
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Active Alerts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <Card className="lg:col-span-2">
          <CardHeader
            title="Active Alerts"
            description="Evidence-backed threats requiring attention"
            action={<Button variant="ghost" size="sm">Acknowledge Selected</Button>}
          />
          <div className="divide-y divide-grid-200">
            {[
              { id: 'ALT-001', type: 'Wildfire proximity', severity: 'CRITICAL', confidence: 0.91, asset: 'TX-Line 500kV #1', evidence: ['Satellite thermal', 'Weather station', 'Camera visual'], time: '3 min ago' },
              { id: 'ALT-002', type: 'Thermal anomaly', severity: 'HIGH', confidence: 0.84, asset: 'Substation Alpha-T', evidence: ['Drone thermal', 'SCADA temp'], time: '14 min ago' },
              { id: 'ALT-003', type: 'Unauthorized access', severity: 'MEDIUM', confidence: 0.72, asset: 'Switching Station Beta', evidence: ['Perimeter sensor', 'CCTV'], time: '42 min ago' },
              { id: 'ALT-004', type: 'Vegetation encroachment', severity: 'MEDIUM', confidence: 0.78, asset: 'TX-Line 230kV #4', evidence: ['LiDAR survey', 'Satellite'], time: '2h ago' },
              { id: 'ALT-005', type: 'Conductor sag', severity: 'LOW', confidence: 0.65, asset: 'Distribution Line DL-112', evidence: ['Visual inspection'], time: '5h ago' },
            ].map((alert) => (
              <div key={alert.id} className="p-4 hover:bg-grid-50 transition-colors">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="font-medium text-grid-900">{alert.type}</span>
                      <Badge variant={alert.severity === 'CRITICAL' ? 'danger' : alert.severity === 'HIGH' ? 'warning' : alert.severity === 'MEDIUM' ? 'info' : 'secondary'}>{alert.severity}</Badge>
                      <span className="text-xs text-grid-500 font-mono">{Math.round(alert.confidence * 100)}% conf.</span>
                    </div>
                    <p className="text-sm text-grid-500 mt-1">{alert.asset}</p>
                    <div className="flex flex-wrap gap-1 mt-2">
                      {alert.evidence.map((e, i) => (
                        <Badge key={i} variant="secondary" size="sm">{e}</Badge>
                      ))}
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <span className="text-xs text-grid-400 whitespace-nowrap">{alert.time}</span>
                    <Button variant="ghost" size="sm"><Eye className="w-3 h-3" /> Details</Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>

        {/* Pipeline Config */}
        <Card>
          <CardHeader title="Pipeline Configuration" />
          <div className="space-y-4">
            <div>
              <label className="label">Fusion Policy</label>
              <select className="input">
                <option value="weighted">Weighted Average</option>
                <option value="demster-shafer" selected>Dempster-Shafer</option>
                <option value="bayesian">Bayesian Fusion</option>
              </select>
            </div>
            <div>
              <label className="label">Confidence Threshold</label>
              <Input type="range" min="0" max="1" step="0.05" value="0.7" className="cursor-pointer" />
            </div>
            <div>
              <label className="label">Alert Severity Mapping</label>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span>CRITICAL</span><span className="font-mono">≥ 0.85</span></div>
                <div className="flex justify-between"><span>HIGH</span><span className="font-mono">≥ 0.70</span></div>
                <div className="flex justify-between"><span>MEDIUM</span><span className="font-mono">≥ 0.55</span></div>
                <div className="flex justify-between"><span>LOW</span><span className="font-mono">≥ 0.40</span></div>
              </div>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary" size="sm">Save Config</Button>
              <Button variant="primary" size="sm">Deploy to Pipeline</Button>
            </div>
          </div>
        </Card>
      </div>

      {/* Evidence Explorer */}
      <Card>
        <CardHeader title="Evidence Explorer" description="Raw observations feeding the detection pipeline" />
        <div className="table-container">
          <Table>
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Source</th>
                <th>Type</th>
                <th>Asset</th>
                <th>Validation</th>
                <th>Features Extracted</th>
              </tr>
            </thead>
            <tbody>
              {[
                { time: '10:23:15', source: 'SAT-THERMAL-01', type: 'THERMAL', asset: 'TX-Line 500kV #1', status: 'VALID', features: 3 },
                { time: '10:22:48', source: 'CAM-DRONE-07', type: 'VISUAL', asset: 'Substation Alpha-T', status: 'VALID', features: 2 },
                { time: '10:21:33', source: 'SCADA-RTU-22', type: 'TELEMETRY', asset: 'Switching Station Beta', status: 'VALID', features: 1 },
                { time: '10:19:12', source: 'SAT-OPTICAL-03', type: 'VISUAL', asset: 'TX-Line 230kV #4', status: 'PENDING', features: 0 },
                { time: '10:15:00', source: 'LIDAR-SURVEY-01', type: 'GIS', asset: 'Distribution Line DL-112', status: 'VALID', features: 1 },
              ].map((obs, i) => (
                <tr key={i}>
                  <td className="font-mono text-sm">{obs.time}</td>
                  <td><Badge variant="secondary" size="sm">{obs.source}</Badge></td>
                  <td><Badge variant="info" size="sm">{obs.type}</Badge></td>
                  <td>{obs.asset}</td>
                  <td><Badge variant={obs.status === 'VALID' ? 'success' : 'warning'} size="sm">{obs.status}</Badge></td>
                  <td className="font-mono text-grid-600">{obs.features}</td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      </Card>
    </PageTemplate>
  );
}