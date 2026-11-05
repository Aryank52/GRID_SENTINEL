import { FileText, Users, Clock, CheckCircle, MapPin, TrendingUp, Plus, Eye, Copy } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { StatCard } from '../components/ui/StatCard';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '../components/ui/Tabs';
import clsx from 'clsx';

export function IncidentResponseCenter() {
  return (
    <PageTemplate
      title="Incident Response Center"
      description="Incident lifecycle management: triage → response → containment → recovery → closure"
      icon={FileText}
      actions={
        <Button variant="primary" size="sm"><Plus className="w-4 h-4" /> New Incident</Button>
      }
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard label="Active Incidents" value="3" trend="2 in response" trendUp={false} icon={FileText} color="alert" />
        <StatCard label="Contained" value="12" trend="This week" trendUp={true} icon={CheckCircle} color="safe" />
        <StatCard label="Avg Response Time" value="18 min" trend="Target: <15 min" trendUp={false} icon={Clock} color="warning" />
        <StatCard label="Avg Resolution" value="4.2 hrs" trend="Improving" trendUp={true} icon={TrendingUp} color="safe" />
        <StatCard label="On-Call Crews" value="8/12" trend="Shift change 14:00" trendUp={null} icon={Users} color="grid" />
      </div>

      <Tabs defaultValue="active">
        <TabsList>
          <TabsTrigger value="active">Active (3)</TabsTrigger>
          <TabsTrigger value="recent">Recent (24h)</TabsTrigger>
          <TabsTrigger value="recovery">In Recovery (2)</TabsTrigger>
          <TabsTrigger value="closed">Closed (7d)</TabsTrigger>
        </TabsList>

        <TabsContent value="active">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
            <Card className="lg:col-span-2">
              <CardHeader
                title="INC-2026-000123 — Wildfire Threat to TX Corridor"
                description="HIGH severity • ACTIVE_RESPONSE • 23 min ago • Commander: J. Martinez"
              />
              <div className="space-y-4">
                <div>
                  <h4 className="font-medium text-grid-900 mb-3">Timeline</h4>
                  <div className="space-y-3">
                    {[
                      { time: '10:15', event: 'Threat detected: Wildfire proximity (THR-001)', actor: 'System', type: 'system' },
                      { time: '10:18', event: 'Incident created from threat escalation', actor: 'Dispatcher Chen', type: 'user' },
                      { time: '10:20', event: 'Triage completed — confirmed HIGH severity', actor: 'Dispatcher Chen', type: 'user' },
                      { time: '10:22', event: 'Response plan approved — Crew Alpha dispatched', actor: 'Commander Martinez', type: 'user' },
                      { time: '10:25', event: 'Load shed sequence Metro-7 activated', actor: 'Dispatcher Park', type: 'user' },
                      { time: '10:30', event: 'Crew Alpha on-site — establishing perimeter', actor: 'Crew Alpha Lead', type: 'user' },
                    ].map((t, i) => (
                      <div key={i} className="flex gap-3">
                        <div className="relative flex flex-col items-center">
                          <div className={clsx('w-3 h-3 rounded-full border-2', t.type === 'system' ? 'bg-grid-500 border-grid-500' : 'bg-white border-grid-500')}>
                            {t.type === 'user' && <span className="w-2 h-2 text-grid-500 -mt-0.5 -ml-0.5">✓</span>}
                          </div>
                          {i < 5 && <div className="absolute top-7 bottom-0 left-1.5 w-0.5 bg-grid-300" />}
                        </div>
                        <div className="flex-1 p-3 bg-grid-50 rounded-lg">
                          <p className="text-sm text-grid-900">{t.event}</p>
                          <p className="text-xs text-grid-500 mt-1">{t.time} • {t.actor}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="font-medium text-grid-900">Response Actions</h4>
                    <Button variant="primary" size="sm"><Plus className="w-4 h-4" /> Add Action</Button>
                  </div>
                  <div className="space-y-2">
                    {[
                      { priority: 'IMMEDIATE', action: 'Deploy crew to TX-Line 500kV #1', assignee: 'Crew Alpha', status: 'IN_PROGRESS', started: '10:22' },
                      { priority: 'URGENT', action: 'Activate load shed Metro-7', assignee: 'Dispatcher Park', status: 'COMPLETED', started: '10:25', completed: '10:26' },
                      { priority: 'HIGH', action: 'Pre-position spare conductors', assignee: 'Logistics Team', status: 'IN_PROGRESS', started: '10:28' },
                      { priority: 'HIGH', action: 'Establish fire perimeter 500m', assignee: 'Crew Alpha', status: 'PENDING' },
                    ].map((a, i) => {
                      const statusClass = clsx('p-3 rounded-lg border', a.status === 'COMPLETED' ? 'bg-safe-50 border-safe-200' : a.status === 'IN_PROGRESS' ? 'bg-warning-50 border-warning-200' : 'bg-grid-50 border-grid-200');
                      return (
                        <div key={i} className={statusClass}>
                          <div className="flex items-start justify-between gap-3">
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center gap-2">
                                <Badge variant={a.priority === 'IMMEDIATE' ? 'danger' : a.priority === 'URGENT' ? 'warning' : 'info'} size="sm">{a.priority}</Badge>
                                <Badge variant={a.status === 'COMPLETED' ? 'success' : a.status === 'IN_PROGRESS' ? 'warning' : 'secondary'} size="sm">{a.status.replace(/_/g, ' ')}</Badge>
                              </div>
                              <p className="text-sm font-medium text-grid-900 mt-1">{a.action}</p>
                              <p className="text-xs text-grid-500 mt-1">Assignee: {a.assignee} • Started: {a.started}{a.completed && ` • Completed: ${a.completed}`}</p>
                            </div>
                            <Button variant="ghost" size="sm">Update</Button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </Card>

            <Card>
              <CardHeader title="Incident Details" />
              <div className="space-y-4">
                <DetailRow label="Incident ID" value="INC-2026-000123" />
                <DetailRow label="Originating Threat" value="THR-001 (Wildfire proximity)" />
                <DetailRow label="Primary Asset" value="TX-Line 500kV #1" />
                <DetailRow label="Location" value="34.0522, -118.2437" Icon={MapPin} />
                <DetailRow label="Severity" value={<Badge variant="danger">HIGH</Badge>} />
                <DetailRow label="State" value={<Badge variant="warning">ACTIVE_RESPONSE</Badge>} />
                <DetailRow label="Commander" value="J. Martinez" Icon={Users} />
                <DetailRow label="Created" value="2026-10-31 10:18 UTC" Icon={Clock} />
                <DetailRow label="Correlation ID" value="a1b2c3d4-e5f6-7890" copyable />
              </div>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="recent">
          <IncidentTable incidents={[
            { id: 'INC-2026-000123', state: 'ACTIVE_RESPONSE', severity: 'HIGH', title: 'Wildfire threat to TX corridor', time: '23 min ago' },
            { id: 'INC-2026-000122', state: 'CONTAINED', severity: 'MEDIUM', title: 'Thermal anomaly Substation Alpha-T', time: '2h ago' },
            { id: 'INC-2026-000121', state: 'RESOLVED', severity: 'LOW', title: 'Vegetation encroachment DL-112', time: '4h ago' },
            { id: 'INC-2026-000120', state: 'CLOSED', severity: 'MEDIUM', title: 'Unauthorized access Switching Beta', time: '6h ago' },
          ]} />
        </TabsContent>

        <TabsContent value="recovery">
          <IncidentTable incidents={[
            { id: 'INC-2026-000115', state: 'RECOVERING', severity: 'HIGH', title: 'Post-fire restoration TX corridor', time: '2d ago' },
            { id: 'INC-2026-000110', state: 'RECOVERING', severity: 'MEDIUM', title: 'Substation Alpha-T repair', time: '5d ago' },
          ]} />
        </TabsContent>

        <TabsContent value="closed">
          <IncidentTable incidents={[
            { id: 'INC-2026-000118', state: 'CLOSED', severity: 'MEDIUM', title: 'Thermal anomaly resolved', time: '2d ago' },
            { id: 'INC-2026-000112', state: 'CLOSED', severity: 'LOW', title: 'Vegetation cleared DL-112', time: '4d ago' },
            { id: 'INC-2026-000108', state: 'CLOSED', severity: 'HIGH', title: 'Fire contained Sector 7', time: '7d ago' },
          ]} />
        </TabsContent>
      </Tabs>
    </PageTemplate>
  );
}

function DetailRow({ label, value, Icon, copyable }: { label: string; value: React.ReactNode; Icon?: React.ElementType; copyable?: boolean }) {
  return (
    <div className="flex items-start justify-between gap-4 p-3 bg-grid-50 rounded-lg">
      <div className="flex items-center gap-3">
        {Icon && <Icon className="w-4 h-4 text-grid-400" />}
        <span className="text-sm text-grid-500">{label}</span>
      </div>
      <div className="flex items-center gap-2 text-sm font-medium text-grid-900">
        {value}
        {copyable && <Button variant="ghost" size="sm" className="p-1" aria-label="Copy"><Copy className="w-3 h-3" /></Button>}
      </div>
    </div>
  );
}

function IncidentTable({ incidents }: { incidents: Array<{ id: string; state: string; severity: string; title: string; time: string }> }) {
  return (
    <div className="table-container">
      <Table>
        <thead>
          <tr>
            <th>Incident ID</th>
            <th>Title</th>
            <th>Severity</th>
            <th>State</th>
            <th>Time</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {incidents.map((inc) => (
            <tr key={inc.id}>
              <td className="font-mono text-sm">{inc.id}</td>
              <td className="font-medium text-grid-900">{inc.title}</td>
              <td><Badge variant={inc.severity === 'CRITICAL' ? 'danger' : inc.severity === 'HIGH' ? 'warning' : inc.severity === 'MEDIUM' ? 'info' : 'secondary'}>{inc.severity}</Badge></td>
              <td><Badge variant={inc.state === 'ACTIVE_RESPONSE' ? 'warning' : inc.state === 'CONTAINED' ? 'info' : inc.state === 'RECOVERING' ? 'info' : inc.state === 'RESOLVED' ? 'success' : 'secondary'}>{inc.state.replace(/_/g, ' ')}</Badge></td>
              <td className="text-sm text-grid-500">{inc.time}</td>
              <td><Button variant="ghost" size="sm"><Eye className="w-3 h-3" /></Button></td>
            </tr>
          ))}
        </tbody>
      </Table>
    </div>
  );
}