import { Database, Search, Filter, Eye, Download, AlertTriangle, TrendingUp, Clock } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { StatCard } from '../components/ui/StatCard';
import { Table } from '../components/ui/Table';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { clsx } from 'clsx';

export function AssetIntelligence() {
  return (
    <PageTemplate
      title="Asset Intelligence"
      description="Asset registry with risk scores, health indices, and criticality rankings"
      icon={Database}
      actions={
        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm"><Download className="w-4 h-4" /> Export</Button>
          <Button variant="primary" size="sm">+ Add Asset</Button>
        </div>
      }
    >
      {/* Stats Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total Assets" value="1,247" trend="+12 this month" trendUp={true} icon={Database} color="grid" />
        <StatCard label="Critical Assets" value="23" trend="2 new" trendUp={false} icon={AlertTriangle} color="alert" />
        <StatCard label="Avg Risk Score" value="34%" trend="-2% vs last month" trendUp={true} icon={TrendingUp} color="safe" />
        <StatCard label="Overdue Inspections" value="7" trend="3 critical" trendUp={false} icon={Clock} color="warning" />
      </div>

      {/* Filters & Table */}
      <Card>
        <CardHeader title="Asset Registry" description="Filter and sort assets by type, criticality, and risk band" />
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">
          <div className="flex flex-col sm:flex-row gap-3 w-full lg:w-auto">
            <Input placeholder="Search assets..." className="w-full sm:w-64" leftIcon={<Search className="w-4 h-4" />} />
            <select className="input w-full sm:w-48">
              <option value="">All Types</option>
              <option value="TRANSMISSION">Transmission</option>
              <option value="SUBSTATION">Substation</option>
              <option value="DISTRIBUTION">Distribution</option>
              <option value="GENERATION">Generation</option>
            </select>
            <select className="input w-full sm:w-40">
              <option value="">All Criticality</option>
              <option value="MISSION_CRITICAL">Mission Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
            <select className="input w-full sm:w-40">
              <option value="">All Risk Bands</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
              <option value="NEGLIGIBLE">Negligible</option>
            </select>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="ghost" size="sm"><Eye className="w-4 h-4" /> Columns</Button>
            <Button variant="ghost" size="sm"><Filter className="w-4 h-4" /> Filters</Button>
          </div>
        </div>

        <div className="table-container">
          <Table>
            <thead>
              <tr>
                <th>Asset</th>
                <th>Type</th>
                <th className="hidden md:table-cell">Criticality</th>
                <th>Health</th>
                <th className="text-right">Risk Score</th>
                <th>Risk Band</th>
                <th className="hidden lg:table-cell">Last Inspected</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {[
                { id: 'AST-001', name: 'TX-Line 500kV #1', type: 'TRANSMISSION_LINE', criticality: 'MISSION_CRITICAL', health: 0.92, risk: 0.87, band: 'CRITICAL', lastInspected: '2026-10-15' },
                { id: 'AST-002', name: 'Substation Alpha-T', type: 'SUBSTATION', criticality: 'MISSION_CRITICAL', health: 0.78, risk: 0.82, band: 'CRITICAL', lastInspected: '2026-09-28' },
                { id: 'AST-003', name: 'Substation Metro-7', type: 'SUBSTATION', criticality: 'HIGH', health: 0.85, risk: 0.76, band: 'HIGH', lastInspected: '2026-10-10' },
                { id: 'AST-004', name: 'TX-Line 500kV #2', type: 'TRANSMISSION_LINE', criticality: 'HIGH', health: 0.91, risk: 0.71, band: 'HIGH', lastInspected: '2026-10-05' },
                { id: 'AST-005', name: 'Distribution Transformer DT-442', type: 'DISTRIBUTION_TRANSFORMER', criticality: 'MEDIUM', health: 0.65, risk: 0.45, band: 'MEDIUM', lastInspected: '2026-08-20' },
              ].map((asset) => (
                <tr key={asset.id}>
                  <td>
                    <div>
                      <p className="font-medium text-grid-900">{asset.name}</p>
                      <p className="text-xs text-grid-500 font-mono">{asset.id}</p>
                    </div>
                  </td>
                  <td><Badge variant="info">{asset.type.replace(/_/g, ' ')}</Badge></td>
                  <td className="hidden md:table-cell">
                    <Badge variant={asset.criticality === 'MISSION_CRITICAL' ? 'alert' : asset.criticality === 'HIGH' ? 'danger' : asset.criticality === 'MEDIUM' ? 'warning' : 'secondary'}>
                      {asset.criticality.replace(/_/g, ' ')}
                    </Badge>
                  </td>
                  <td>
                    <div className="flex items-center gap-2">
                      <div className="w-24 h-2 bg-grid-200 rounded-full overflow-hidden">
                        <div className={clsx('h-full rounded-full', asset.health > 0.8 ? 'bg-safe-500' : asset.health > 0.5 ? 'bg-warning-500' : 'bg-alert-500')} style={{ width: `${asset.health * 100}%` }} />
                      </div>
                      <span className="text-sm font-mono text-grid-600 w-10">{Math.round(asset.health * 100)}%</span>
                    </div>
                  </td>
                  <td className="text-right font-mono text-grid-900">{(asset.risk * 100).toFixed(0)}%</td>
                  <td><Badge variant={asset.band === 'CRITICAL' ? 'danger' : asset.band === 'HIGH' ? 'warning' : asset.band === 'MEDIUM' ? 'info' : 'secondary'}>{asset.band}</Badge></td>
                  <td className="hidden lg:table-cell text-sm text-grid-500">{asset.lastInspected}</td>
                  <td>
                    <div className="flex items-center gap-1">
                      <Button variant="ghost" size="sm" aria-label="View details"><Eye className="w-4 h-4" /></Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      </Card>
    </PageTemplate>
  );
}