import { ClipboardList, Download, Eye, Shield, AlertTriangle, Settings, Bot } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { StatCard } from '../components/ui/StatCard';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Tabs, TabsList, TabsTrigger, TabsContent } from '../components/ui/Tabs';

export function AuditDashboard() {
  return (
    <PageTemplate
      title="Audit Dashboard"
      description="Immutable governance log: actions, decisions, AI-assisted recommendations, and compliance tracking"
      icon={ClipboardList}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm"><Download className="w-4 h-4" /> Export Logs</Button>
          <Button variant="primary" size="sm"><Shield className="w-4 h-4" /> Verify Integrity</Button>
        </div>
      }
    >
      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard label="Total Events (30d)" value="12,847" trend="+2.3% vs prior" trendUp={false} icon={ClipboardList} color="grid" />
        <StatCard label="AI-Assisted Actions" value="342" trend="2.7% of total" trendUp={null} icon={Bot} color="grid" />
        <StatCard label="Security Events" value="18" trend="3 failed logins" trendUp={false} icon={AlertTriangle} color="alert" />
        <StatCard label="Config Changes" value="7" trend="2 by admins" trendUp={null} icon={Settings} color="warning" />
        <StatCard label="Integrity Status" value="VERIFIED" trend="Last check: 1h ago" trendUp={true} icon={Shield} color="safe" />
      </div>

      {/* Tabs */}
      <Tabs defaultValue="all">
        <TabsList>
          <TabsTrigger value="all">All Events</TabsTrigger>
          <TabsTrigger value="ai">AI-Assisted</TabsTrigger>
          <TabsTrigger value="security">Security</TabsTrigger>
          <TabsTrigger value="config">Config Changes</TabsTrigger>
          <TabsTrigger value="incidents">Incidents</TabsTrigger>
        </TabsList>

        <TabsContent value="all">
          <AuditTable logs={allLogs} />
        </TabsContent>

        <TabsContent value="ai">
          <AuditTable logs={allLogs.filter(l => l.aiAssisted)} />
        </TabsContent>

        <TabsContent value="security">
          <AuditTable logs={allLogs.filter(l => ['USER_LOGIN', 'USER_LOGOUT', 'PERMISSION_DENIED', 'ROLE_CHANGED', 'DATA_EXPORTED'].includes(l.eventType))} />
        </TabsContent>

        <TabsContent value="config">
          <AuditTable logs={allLogs.filter(l => l.eventType === 'CONFIGURATION_CHANGED')} />
        </TabsContent>

        <TabsContent value="incidents">
          <AuditTable logs={allLogs.filter(l => l.aggregateType === 'Incident')} />
        </TabsContent>
      </Tabs>
    </PageTemplate>
  );
}

const allLogs = [
  { id: 'AUD-001', time: '2026-10-31 10:35:12', eventType: 'INCIDENT_RESPONSE_STARTED', aggregateType: 'Incident', aggregateId: 'INC-2026-000123', action: 'START_RESPONSE', actor: 'j.martinez', role: 'INCIDENT_COMMANDER', ip: '10.0.1.45', aiAssisted: false },
  { id: 'AUD-002', time: '2026-10-31 10:32:08', eventType: 'COPILOT_RESPONSE_GENERATED', aggregateType: 'Incident', aggregateId: 'INC-2026-000123', action: 'SUMMARIZE_INCIDENT', actor: 'j.martinez', role: 'INCIDENT_COMMANDER', ip: '10.0.1.45', aiAssisted: true, aiModel: 'gpt-4o-mini', aiRequestId: 'COP-REQ-001' },
  { id: 'AUD-003', time: '2026-10-31 10:25:44', eventType: 'RESPONSE_ACTION_COMPLETED', aggregateType: 'ResponseAction', aggregateId: 'ACT-002', action: 'COMPLETE', actor: 'd.park', role: 'DISPATCHER', ip: '10.0.2.12', aiAssisted: false },
  { id: 'AUD-004', time: '2026-10-31 10:22:11', eventType: 'RESPONSE_PLAN_APPROVED', aggregateType: 'ResponsePlan', aggregateId: 'RP-001', action: 'APPROVE', actor: 'j.martinez', role: 'INCIDENT_COMMANDER', ip: '10.0.1.45', aiAssisted: false },
  { id: 'AUD-005', time: '2026-10-31 10:18:33', eventType: 'INCIDENT_CREATED', aggregateType: 'Incident', aggregateId: 'INC-2026-000123', action: 'CREATE', actor: 'c.chen', role: 'DISPATCHER', ip: '10.0.2.12', aiAssisted: false },
  { id: 'AUD-006', time: '2026-10-31 10:15:02', eventType: 'THREAT_ESCALATED', aggregateType: 'ThreatEvent', aggregateId: 'THR-001', action: 'ESCALATE_TO_INCIDENT', actor: 'c.chen', role: 'DISPATCHER', ip: '10.0.2.12', aiAssisted: false },
  { id: 'AUD-007', time: '2026-10-31 09:45:00', eventType: 'CONFIGURATION_CHANGED', aggregateType: 'DetectionPipeline', aggregateId: 'pipe-main', action: 'UPDATE_THRESHOLD', actor: 'a.kumar', role: 'ADMINISTRATOR', ip: '10.0.3.7', aiAssisted: false },
  { id: 'AUD-008', time: '2026-10-31 08:12:00', eventType: 'MODEL_DEPLOYED', aggregateType: 'RiskModel', aggregateId: 'risk-deterministic-v1.2', action: 'DEPLOY', actor: 's.ng', role: 'ADMINISTRATOR', ip: '10.0.3.7', aiAssisted: false },
];

function AuditTable({ logs }: { logs: typeof allLogs }) {
  return (
    <Card>
      <CardHeader title={`Audit Log (${logs.length} events)`} />
      <div className="table-container">
        <Table>
          <thead>
            <tr>
              <th className="w-40">Timestamp</th>
              <th>Event</th>
              <th className="w-48">Actor</th>
              <th className="w-40">Aggregate</th>
              <th className="w-24">AI</th>
              <th className="w-40">Correlation</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id}>
                <td className="font-mono text-xs text-grid-500">{log.time}</td>
                <td>
                  <div className="flex items-center gap-2">
                    <Badge variant={log.eventType.includes('AI') ? 'info' : log.eventType.includes('SECURITY') || log.eventType.includes('DENIED') ? 'danger' : log.eventType.includes('CONFIG') ? 'warning' : 'secondary'} size="sm">
                      {log.eventType.replace(/_/g, ' ')}
                    </Badge>
                    {log.aiAssisted && <span className="text-xs text-grid-500 flex items-center gap-1"><Bot className="w-3 h-3" />AI</span>}
                  </div>
                </td>
                <td>
                  <div>
                    <p className="font-medium text-grid-900">{log.actor}</p>
                    <p className="text-xs text-grid-500">{log.role}</p>
                  </div>
                </td>
                <td>
                  <div>
                    <p className="font-mono text-xs text-grid-700">{log.aggregateType}</p>
                    <p className="font-mono text-xs text-grid-500 truncate max-w-[120px]">{log.aggregateId}</p>
                  </div>
                </td>
                <td className="text-center">
                  {log.aiAssisted ? (
                    <Badge variant="info" size="sm">Yes</Badge>
                  ) : (
                    <Badge variant="secondary" size="sm">No</Badge>
                  )}
                </td>
                <td className="font-mono text-xs text-grid-500 truncate max-w-[100px]">
                  {log.aiRequestId || '—'}
                </td>
                <td>
                  <Button variant="ghost" size="sm" className="p-1"><Eye className="w-3 h-3" /></Button>
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </div>
    </Card>
  );
}