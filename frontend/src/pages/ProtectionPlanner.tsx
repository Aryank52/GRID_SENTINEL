import { Shield, Plus, TrendingUp, Target, Calculator, FileText } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { StatCard } from '../components/ui/StatCard';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';

export function ProtectionPlanner() {
  return (
    <PageTemplate
      title="Protection Planner"
      description="Prevention planning with cost/risk-reduction optimization and measure prioritization"
      icon={Shield}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm"><FileText className="w-4 h-4" /> Export Plan</Button>
          <Button variant="primary" size="sm"><Plus className="w-4 h-4" /> New Plan</Button>
        </div>
      }
    >
      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Active Plans" value="3" trend="1 in review" trendUp={null} icon={Shield} color="grid" />
        <StatCard label="Total Budget" value="$2.4M" trend="$800K remaining" trendUp={true} icon={Calculator} color="safe" />
        <StatCard label="Expected Risk Reduction" value="23.4%" trend="Target: 30%" trendUp={false} icon={TrendingUp} color="warning" />
        <StatCard label="Measures Planned" value="47" trend="12 pending approval" trendUp={null} icon={Target} color="grid" />
      </div>

      {/* Plans List */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader
            title="Protection Plan: Metro Wildfire Corridor"
            description="FY2026 — Approved • $1.2M budget • 23.4% expected risk reduction"
            action={<Button variant="ghost" size="sm">View Details</Button>}
          />
          <div className="space-y-4">
            {[
              { measure: 'Vegetation clearance corridor (50ft)', category: 'VEGETATION_MANAGEMENT', cost: 350000, riskReduction: 0.08, status: 'APPROVED' },
              { measure: 'Install thermal monitoring cameras', category: 'MONITORING_INSTALLATION', cost: 180000, riskReduction: 0.05, status: 'IN_PROGRESS' },
              { measure: 'Fire suppression system at Substation Alpha', category: 'FIRE_SUPPRESSION', cost: 420000, riskReduction: 0.06, status: 'PENDING' },
              { measure: 'Pre-position spare conductors at Depot 3', category: 'EMERGENCY_PREPOSITIONING', cost: 250000, riskReduction: 0.03, status: 'PLANNED' },
              { measure: 'Hardened access control at switching stations', category: 'ACCESS_CONTROL', cost: 150000, riskReduction: 0.014, status: 'PLANNED' },
            ].map((m, i) => (
              <div key={i} className="p-4 bg-grid-50 rounded-lg border border-grid-200">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-grid-900">{m.measure}</p>
                      <Badge variant="info" size="sm">{m.category.replace(/_/g, ' ')}</Badge>
                    </div>
                    <p className="text-sm text-grid-500 mt-1">${m.cost.toLocaleString()} • {Math.round(m.riskReduction * 10000) / 100}% risk reduction</p>
                  </div>
                  <Badge variant={m.status === 'APPROVED' ? 'success' : m.status === 'IN_PROGRESS' ? 'warning' : 'secondary'}>{m.status.replace(/_/g, ' ')}</Badge>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <CardHeader title="Optimization Results" action={<Button variant="ghost" size="sm">View Details</Button>} />
          <div className="table-container">
            <Table>
              <thead>
                <tr>
                  <th>Measure</th>
                  <th className="text-right">Cost</th>
                  <th className="text-right">Risk Reduction</th>
                  <th className="text-right">Cost/Effectiveness</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {[
                  { name: 'Vegetation clearance', cost: 350000, riskRed: 8.0, ce: 22.9, selected: true },
                  { name: 'Thermal cameras', cost: 180000, riskRed: 5.0, ce: 27.8, selected: true },
                  { name: 'Fire suppression', cost: 420000, riskRed: 6.0, ce: 14.3, selected: true },
                  { name: 'Spare conductors', cost: 250000, riskRed: 3.0, ce: 12.0, selected: true },
                  { name: 'Access control', cost: 150000, riskRed: 1.4, ce: 9.3, selected: false },
                  { name: 'Seismic retrofit', cost: 800000, riskRed: 4.0, ce: 5.0, selected: false },
                  { name: 'Flood barriers', cost: 600000, riskRed: 2.5, ce: 4.2, selected: false },
                ].map((m, i) => (
                  <tr key={i} className={m.selected ? 'bg-safe-50/50' : ''}>
                    <td className="font-medium text-grid-900">{m.name}</td>
                    <td className="text-right">${m.cost.toLocaleString()}</td>
                    <td className="text-right">{m.riskRed}%</td>
                    <td className="text-right font-mono font-medium text-grid-600">{m.ce.toFixed(1)}</td>
                    <td><Badge variant={m.selected ? 'success' : 'secondary'}>{m.selected ? 'Selected' : 'Available'}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        </Card>
      </div>

      {/* Optimization Controls */}
      <Card>
        <CardHeader title="Budget Optimization" action={<Button variant="primary"><Calculator className="w-4 h-4 mr-2" /> Re-optimize</Button>} />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div>
            <label className="label">Budget Limit ($)</label>
            <Input type="number" value="1200000" placeholder="Enter budget" />
          </div>
          <div>
            <label className="label">Target Risk Reduction (%)</label>
            <Input type="number" value="25" placeholder="Enter target" />
          </div>
          <div>
            <label className="label">Optimization Strategy</label>
            <select className="input">
              <option value="greedy">Greedy (cost-effectiveness)</option>
              <option value="knapsack">0/1 Knapsack (exact)</option>
              <option value="multi-objective">Multi-objective (Pareto)</option>
            </select>
          </div>
        </div>
      </Card>
    </PageTemplate>
  );
}