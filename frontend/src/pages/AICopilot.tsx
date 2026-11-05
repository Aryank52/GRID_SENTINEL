import { useState } from 'react';
import { Bot, Send, Loader2 } from 'lucide-react';
import { PageTemplate } from '../components/ui/PageTemplate';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';

const suggestions = [
  { prompt: 'Summarize incident INC-2026-000123', type: 'SUMMARIZE_INCIDENT' },
  { prompt: 'Why is TX-Line 500kV #1 at critical risk?', type: 'EXPLAIN_RISK' },
  { prompt: 'Explain simulation results for Wildfire Corridor N-1', type: 'EXPLAIN_SIMULATION' },
  { prompt: 'Draft incident report for wildfire threat', type: 'DRAFT_INCIDENT_REPORT' },
  { prompt: 'What are the top 3 protection measures for Substation Alpha-T?', type: 'SUGGEST_ACTIONS' },
  { prompt: 'Compare flood vs seismic risk for Metro substations', type: 'COMPARE_SCENARIOS' },
];

export function AICopilot() {
  const [messages, setMessages] = useState<Array<{ role: 'user' | 'assistant'; content: string; citations?: string[]; timestamp: Date }>>([
    { role: 'assistant', content: 'Welcome to Grid Sentinel AI Copilot. I can help you summarize incidents, explain risk scores, interpret simulation results, draft reports, and answer questions grounded in current system state. What would you like to know?', timestamp: new Date() },
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [selectedContext, setSelectedContext] = useState<string>('incident:INC-2026-000123');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage = { role: 'user' as const, content: input, timestamp: new Date() };
    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    // Simulate API call
    await new Promise(r => setTimeout(r, 1500));

    const mockResponse = generateMockResponse(input);
    setMessages(prev => [...prev, { role: 'assistant', content: mockResponse.content, citations: mockResponse.citations, timestamp: new Date() }]);
    setIsLoading(false);
  };

  return (
    <PageTemplate
      title="AI Operations Copilot"
      description="Grounded AI assistance: summarize incidents, explain risk, interpret simulations, draft reports"
      icon={Bot}
      actions={
        <div className="flex items-center gap-2">
          <select className="input w-auto" value={selectedContext} onChange={e => setSelectedContext(e.target.value)}>
            <option value="incident:INC-2026-000123">📋 INC-2026-000123</option>
            <option value="asset:AST-001">⚡ TX-Line 500kV #1</option>
            <option value="simulation:SIM-001">🧪 Wildfire Corridor N-1</option>
            <option value="global">🌐 Global Context</option>
          </select>
          <Badge variant="info" size="sm">Grounded Mode</Badge>
        </div>
      }
    >
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Chat */}
        <Card className="lg:col-span-2 flex flex-col h-[calc(100vh-300px)] min-h-[600px]">
          <div className="flex-1 overflow-y-auto p-4 space-y-6" id="chat-messages">
            {messages.map((msg, i) => (
              <MessageBubble key={i} message={msg} />
            ))}
            {isLoading && <TypingIndicator />}
          </div>
          <div className="border-t border-grid-200 p-4">
            <form onSubmit={handleSubmit} className="flex gap-2">
              <Input
                value={input}
                onChange={e => setInput(e.target.value)}
                placeholder="Ask about incidents, risk, simulations..."
                disabled={isLoading}
                className="flex-1"
              />
              <Button type="submit" disabled={isLoading || !input.trim()} size="lg">
                {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Send className="w-5 h-5" />}
              </Button>
            </form>
            <div className="flex flex-wrap gap-2 mt-3">
              {suggestions.map((s, i) => (
                <button
                  key={i}
                  onClick={() => setInput(s.prompt)}
                  className="px-3 py-1 text-xs text-grid-600 hover:text-grid-900 bg-grid-100 hover:bg-grid-200 rounded-full transition-colors"
                >
                  {s.prompt}
                </button>
              ))}
            </div>
          </div>
        </Card>

        {/* Context Panel */}
        <Card className="flex flex-col">
          <CardHeader title="Grounding Context" description="System state injected into AI prompt" />
          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            <ContextCard
              title="Incident INC-2026-000123"
              data={[
                { label: 'State', value: 'ACTIVE_RESPONSE' },
                { label: 'Severity', value: 'HIGH' },
                { label: 'Primary Asset', value: 'TX-Line 500kV #1' },
                { label: 'Commander', value: 'J. Martinez' },
                { label: 'Active Actions', value: '3/5' },
                { label: 'Containment', value: '35%' },
              ]}
            />
            <ContextCard
              title="Asset TX-Line 500kV #1"
              data={[
                { label: 'Risk Score', value: '87% (CRITICAL)' },
                { label: 'Health Index', value: '92%' },
                { label: 'Criticality', value: 'MISSION_CRITICAL' },
                { label: 'Redundancy', value: 'N-1' },
                { label: 'Load Served', value: '1.2 GW' },
                { label: 'Last Inspection', value: '2026-10-15' },
              ]}
            />
            <ContextCard
              title="Simulation SIM-001"
              data={[
                { label: 'Scenario', value: 'Wildfire Corridor N-1' },
                { label: 'Iterations', value: '47/100' },
                { label: 'Converged', value: 'Yes' },
                { label: 'Load Lost', value: '2.3 GW' },
                { label: 'Customers Affected', value: '1.2M' },
                { label: 'Est. Restoration', value: '72 hrs' },
              ]}
            />
          </div>
        </Card>
      </div>
    </PageTemplate>
  );
}

function MessageBubble({ message }: { message: { role: 'user' | 'assistant'; content: string; citations?: string[]; timestamp: Date } }) {
  return (
    <div className={`flex gap-3 ${message.role === 'user' ? 'justify-end' : ''}`}>
      <div className={clsx('max-w-[80%] p-4 rounded-2xl', message.role === 'user' ? 'bg-grid-600 text-white rounded-tr-sm' : 'bg-grid-100 text-grid-900 rounded-tl-sm')}>
        <p className="whitespace-pre-wrap">{message.content}</p>
        {message.citations && message.citations.length > 0 && (
          <div className="mt-3 pt-3 border-t border-grid-200/50">
            <p className="text-xs text-grid-500 mb-1">Grounding citations:</p>
            <div className="flex flex-wrap gap-1">
              {message.citations.map((c, i) => (
                <Badge key={i} variant="secondary" size="sm">{c}</Badge>
              ))}
            </div>
          </div>
        )}
        <p className="text-xs text-grid-400 mt-2 text-right">{message.timestamp.toLocaleTimeString()}</p>
      </div>
    </div>
  );
}

function TypingIndicator() {
  return (
    <div className="flex gap-3 justify-start">
      <div className="max-w-[80%] p-4 bg-grid-100 rounded-2xl rounded-tl-sm">
        <div className="flex gap-1">
          <span className="w-2 h-2 bg-grid-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
          <span className="w-2 h-2 bg-grid-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
          <span className="w-2 h-2 bg-grid-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
        </div>
      </div>
    </div>
  );
}

function ContextCard({ title, data }: { title: string; data: Array<{ label: string; value: string }> }) {
  return (
    <div className="p-4 bg-grid-50 rounded-lg border border-grid-200">
      <h4 className="font-medium text-grid-900 mb-3">{title}</h4>
      <dl className="space-y-2 text-sm">
        {data.map((d, i) => (
          <div key={i} className="flex justify-between">
            <dt className="text-grid-500">{d.label}</dt>
            <dd className="font-medium text-grid-900 text-right max-w-[60%] truncate">{d.value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}

function generateMockResponse(input: string) {
  const lower = input.toLowerCase();
  if (lower.includes('summarize') && lower.includes('incident')) {
    return {
      content: `**Incident Summary: INC-2026-000123**

**Overview:** Wildfire threat to TX 500kV transmission corridor (Lines 1-3) feeding Metro-7 substation complex. Severity: HIGH. State: ACTIVE_RESPONSE.

**Timeline:**
• 10:15 — Threat THR-001 detected (satellite thermal + weather fusion, 91% confidence)
• 10:18 — Incident created, triaged by Dispatcher Chen
• 10:22 — Response plan approved, Crew Alpha dispatched
• 10:25 — Load shed Metro-7 activated (preventive)
• 10:30 — Crew Alpha on-site, establishing perimeter

**Current Status:** Fire perimeter 2.3km from Line 1. Load shed holding. Crew establishing firebreaks. Containment estimate: 35%.

**Risk:** Cascade probability 78% if Line 1 trips (simulation SIM-001). 2.3 GW at risk, 1.2M customers.

**Next Actions:** Complete firebreak (IMMEDIATE), pre-position spare conductors (URGENT), validate restoration sequence (HIGH).`,
      citations: ['INC-2026-000123', 'THR-001', 'SIM-001', 'AST-001', 'AST-002']
    };
  }
  if (lower.includes('risk') && lower.includes('tx-line')) {
    return {
      content: `**Risk Explanation: TX-Line 500kV #1 (AST-001)**

**Current Score: 87% — CRITICAL Band**

**Top Contributing Factors:**
1. **Network Centrality (22%)** — Betweenness centrality 0.91. Carries 89% of Metro-7 load. No alternative paths at 500kV.
2. **Asset Criticality (20%)** — MISSION_CRITICAL. Feeds 1.2M customers, 2.3 GW peak load.
3. **Environmental Exposure (15%)** — Wildfire risk zone (Tier 3). 2.3km to active fire perimeter.
4. **Historical Incidents (12%)** — 3 vegetation-related incidents in 5 years. Mean time to repair: 18 hrs.
5. **Physical Exposure (10%)** — Accessible terrain, ROW near hiking trails.
6. **Redundancy (8%)** — N-1 only. Loss of Line 1 forces full load on Lines 2-3 (115% thermal).
7. **Asset Health (8%)** — 92% health. Last inspected 2026-10-15. Minor corrosion on Tower 47.

**Recommended Protection (from Protection Plan PP-2026-003):**
• Vegetation clearance 50ft corridor ($350K, 8% risk reduction)
• Thermal monitoring cameras ($180K, 5% risk reduction)
• Fire suppression at Substation Alpha ($420K, 6% risk reduction)`,
      citations: ['AST-001', 'RiskScore-RS-047', 'PP-2026-003', 'TopologySnapshot-TS-012']
    };
  }
  return {
    content: `I can help you with that. Could you clarify which specific aspect you'd like me to address? I have access to current incident data, asset risk scores, simulation results, and protection plans.`,
    citations: []
  };
}

import { clsx } from 'clsx';