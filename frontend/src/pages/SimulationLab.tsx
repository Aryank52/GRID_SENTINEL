import { Tabs, TabsList, TabsTrigger, TabsContent } from '../components/ui/Tabs';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { FlaskConical, Play, Pause, RotateCcw } from 'lucide-react';

export function SimulationLab() {
  return (
    <PageTemplate
      title="Simulation Lab"
      description="Deterministic what-if analysis: failure scenarios, cascade simulation, impact assessment"
      icon={FlaskConical}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm"><RotateCcw className="w-4 h-4" /> Reset</Button>
          <Button variant="primary" size="sm"><Play className="w-4 h-4" /> Run Simulation</Button>
        </div>
      }
    >
      <Tabs defaultValue="scenarios">
        <TabsList>
          <TabsTrigger value="scenarios">Scenarios</TabsTrigger>
          <TabsTrigger value="running">Running (1)</TabsTrigger>
          <TabsTrigger value="results">Results (12)</TabsTrigger>
          <TabsTrigger value="templates">Templates</TabsTrigger>
        </TabsList>

        <TabsContent value="scenarios">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
            <Card>
              <CardHeader title="Create New Scenario" />
              <div className="space-y-4">
                <div>
                  <label className="label">Scenario Name</label>
                  <Input placeholder="e.g., Wildfire TX Corridor N-1" />
                </div>
                <div>
                  <label className="label">Template</label>
                  <select className="input">
                    <option value="cascade">Cascade Failure</option>
                    <option value="load-shed">Load Shed Analysis</option>
                    <option value="voltage-collapse">Voltage Collapse</option>
                    <option value="custom">Custom</option>
                  </select>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="label">Initial Failed Assets</label>
                    <Input placeholder="Select assets..." />
                  </div>
                  <div>
                    <label className="label">Time Horizon (hours)</label>
                    <Input type="number" value="4" />
                  </div>
                </div>
                <div>
                  <label className="label">Simulation Parameters</label>
                  <div className="space-y-2">
                    <label className="flex items-center gap-2"><input type="checkbox" className="rounded border-grid-300" defaultChecked /> Cascade Enabled</label>
                    <label className="flex items-center gap-2"><input type="checkbox" className="rounded border-grid-300" defaultChecked /> Load Redistribution</label>
                    <label className="flex items-center gap-2"><input type="checkbox" className="rounded border-grid-300" /> Include Cyber Effects</label>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="secondary"><RotateCcw className="w-4 h-4" /> Reset</Button>
                  <Button variant="primary"><Play className="w-4 h-4" /> Run Simulation</Button>
                </div>
              </div>
            </Card>

            <Card>
              <CardHeader title="Scenario Templates" />
              <div className="space-y-3">
                {[
                  { name: 'Wildfire Corridor N-1', type: 'Cascade', assets: 3, duration: '4h', runs: 47 },
                  { name: 'Substation Flood Impact', type: 'Load Shed', assets: 1, duration: '2h', runs: 23 },
                  { name: 'Seismic Event Regional', type: 'Cascade', assets: 8, duration: '8h', runs: 12 },
                  { name: 'Cyber-Physical Attack', type: 'Custom', assets: 5, duration: '6h', runs: 8 },
                ].map((t, i) => (
                  <div key={i} className="p-3 border border-grid-200 rounded-lg hover:bg-grid-50 transition-colors">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-medium text-grid-900">{t.name}</p>
                        <p className="text-xs text-grid-500">{t.type} - {t.assets} initial assets - {t.duration}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <Badge variant="secondary" size="sm">{t.runs} runs</Badge>
                        <Button variant="ghost" size="sm" className="p-1"><span>Play</span></Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="running">
          <Card>
            <CardHeader title="Running Simulation" />
            <div className="space-y-6">
              <div className="p-6 bg-grid-50 rounded-lg">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <p className="font-medium text-grid-900">Wildfire Corridor N-1</p>
                    <p className="text-sm text-grid-500">Cascade Failure - Started 10:23:15 - Iteration 3/100</p>
                  </div>
                  <Badge variant="warning">RUNNING</Badge>
                </div>
                <div className="h-4 bg-grid-200 rounded-full overflow-hidden">
                  <div className="h-full bg-grid-500 rounded-full transition-all duration-300" style={{ width: '3%' }} />
                </div>
                <div className="grid grid-cols-4 gap-4 mt-4 text-sm">
                  <div><span className="text-grid-500">Iterations</span><br /><span className="font-mono font-bold text-grid-900">3 / 100</span></div>
                  <div><span className="text-grid-500">Assets Failed</span><br /><span className="font-mono font-bold text-alert-600">2</span></div>
                  <div><span className="text-grid-500">Load Lost</span><br /><span className="font-mono font-bold text-alert-600">1.2 GW</span></div>
                  <div><span className="text-grid-500">Est. Time</span><br /><span className="font-mono font-bold text-grid-900">~45s</span></div>
                </div>
              </div>
              <Button variant="danger" className="w-full"><Pause className="w-4 h-4" /> Cancel Simulation</Button>
            </div>
          </Card>
        </TabsContent>

        <TabsContent value="results">
          <Card>
            <CardHeader title="Simulation Results" action={<Button variant="ghost" size="sm">Export CSV</Button>} />
            <div className="table-container">
              <Table>
                <thead>
                  <tr>
                    <th>Scenario</th>
                    <th>Type</th>
                    <th className="text-right">Iterations</th>
                    <th>Converged</th>
                    <th className="text-right">Load Lost</th>
                    <th className="text-right">Customers</th>
                    <th className="text-right">Restoration (hrs)</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {[
                    { name: 'Wildfire Corridor N-1', type: 'Cascade', iter: 47, converged: true, load: 2.3, cust: 1.2, rest: 72, status: 'COMPLETED' },
                    { name: 'Substation Flood', type: 'Load Shed', iter: 23, converged: true, load: 0.8, cust: 0.4, rest: 24, status: 'COMPLETED' },
                    { name: 'Seismic Regional', type: 'Cascade', iter: 100, converged: false, load: 5.1, cust: 3.2, rest: 168, status: 'FAILED' },
                    { name: 'Cyber-Physical', type: 'Custom', iter: 56, converged: true, load: 1.7, cust: 0.9, rest: 48, status: 'COMPLETED' },
                  ].map((r, i) => (
                    <tr key={i}>
                      <td className="font-medium text-grid-900">{r.name}</td>
                      <td><Badge variant="secondary" size="sm">{r.type}</Badge></td>
                      <td className="text-right font-mono">{r.iter}</td>
                      <td><Badge variant={r.converged ? 'success' : 'danger'} size="sm">{r.converged ? 'Yes' : 'No'}</Badge></td>
                      <td className="text-right font-mono text-alert-600">{r.load} GW</td>
                      <td className="text-right font-mono">{r.cust}M</td>
                      <td className="text-right font-mono">{r.rest}h</td>
                      <td><Badge variant={r.status === 'COMPLETED' ? 'success' : 'danger'}>{r.status}</Badge></td>
                      <td>
                        <div className="flex items-center gap-1">
                          <Button variant="ghost" size="sm" className="p-1" aria-label="View">View</Button>
                          <Button variant="ghost" size="sm" className="p-1" aria-label="Download">Download</Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          </Card>
        </TabsContent>

        <TabsContent value="templates">
          <Card>
            <CardHeader title="Scenario Templates" description="Pre-defined scenario templates for common threat types" />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {[
                { name: 'Wildfire Corridor', desc: 'N-1 contingency with vegetation fire', params: { assets: 3, horizon: '4h', cascade: true } },
                { name: 'Substation Flood', desc: 'Single substation flood impact', params: { assets: 1, horizon: '2h', cascade: false } },
                { name: 'Seismic Event', desc: 'Regional earthquake with multiple failures', params: { assets: 8, horizon: '8h', cascade: true } },
                { name: 'Cyber-Physical', desc: 'Coordinated cyber-physical attack', params: { assets: 5, horizon: '6h', cascade: true } },
                { name: 'Ice Storm', desc: 'Transmission line ice accretion', params: { assets: 6, horizon: '12h', cascade: true } },
                { name: 'Load Shed Drill', desc: 'Planned load shedding exercise', params: { assets: 4, horizon: '1h', cascade: false } },
              ].map((t, i) => {
                return (
                  <div key={i} className="card p-6">
                    <div className="flex items-start justify-between">
                      <div>
                        <h4 className="font-semibold text-grid-900">{t.name}</h4>
                        <p className="text-sm text-grid-500 mt-1">{t.desc}</p>
                        <div className="flex flex-wrap gap-2 mt-3 text-xs">
                          <Badge variant="secondary" size="sm">{t.params.assets} assets</Badge>
                          <Badge variant="secondary" size="sm">{t.params.horizon}</Badge>
                          <Badge variant={t.params.cascade ? 'info' : 'secondary'} size="sm">{t.params.cascade ? 'Cascade' : 'Static'}</Badge>
                        </div>
                      </div>
                      <Button variant="primary" size="sm">Use</Button>
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </PageTemplate>
  );
}