import { Link } from 'react-router-dom';
import {
  Shield,
  AlertTriangle,
  Zap,
  RotateCcw,
  Brain,
  ArrowRight,
  Map,
  Database,
  FileText,
  FlaskConical,
  ClipboardList,
  CheckCircle,
  ArrowUpRight,
  LayoutDashboard,
  Bot,
} from 'lucide-react';
import { clsx } from 'clsx';

const loopSteps = [
  { id: 'prevent', label: 'PREVENT', icon: Shield, description: 'Physical protection & hardening', color: 'safe', href: '/protection' },
  { id: 'detect', label: 'DETECT', icon: AlertTriangle, description: 'Surveillance & anomaly detection', color: 'warning', href: '/threats' },
  { id: 'assess', label: 'ASSESS', icon: Zap, description: 'Risk scoring & impact analysis', color: 'grid', href: '/assets' },
  { id: 'simulate', label: 'SIMULATE', icon: FlaskConical, description: 'Deterministic what-if scenarios', color: 'grid', href: '/simulation' },
  { id: 'mitigate', label: 'MITIGATE', icon: RotateCcw, description: 'Emergency response & containment', color: 'alert', href: '/incidents' },
  { id: 'recover', label: 'RECOVER', icon: ArrowUpRight, description: 'Restoration & verification', color: 'safe', href: '/incidents' },
  { id: 'learn', label: 'LEARN', icon: Brain, description: 'After-action & model improvement', color: 'grid', href: '/audit' },
];

const screens = [
  { name: 'Command Center', description: 'Unified operational view', icon: LayoutDashboard, href: '/command-center' },
  { name: 'Live Grid Map', description: 'Real-time topology & alerts', icon: Map, href: '/map' },
  { name: 'Asset Intelligence', description: 'Registry, risk scores, health', icon: Database, href: '/assets' },
  { name: 'Protection Planner', description: 'Prevention & optimization', icon: Shield, href: '/protection' },
  { name: 'Threat Detection', description: 'Pipeline & alert management', icon: AlertTriangle, href: '/threats' },
  { name: 'Incident Response', description: 'Lifecycle & response plans', icon: FileText, href: '/incidents' },
  { name: 'Simulation Lab', description: 'What-if & impact analysis', icon: FlaskConical, href: '/simulation' },
  { name: 'AI Copilot', description: 'Grounded AI assistance', icon: Bot, href: '/copilot' },
  { name: 'Audit Dashboard', description: 'Governance & compliance', icon: ClipboardList, href: '/audit' },
];

export function Landing() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-grid-50 via-white to-white">
      {/* Hero */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-[url('/grid-pattern.svg')] opacity-5" aria-hidden="true" />
        <div className="relative max-w-7xl mx-auto px-6 py-24 lg:py-32">
          <div className="text-center max-w-4xl mx-auto">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-grid-100 text-grid-700 text-sm font-medium mb-6">
              <span className="w-2 h-2 bg-safe-500 rounded-full animate-pulse" aria-hidden="true" />
              Grid Resilience Platform v0.1.0
            </div>
            <h1 className="text-4xl lg:text-6xl font-bold text-grid-900 tracking-tight mb-6">
              Grid Resilience & Physical Protection
              <br />
              <span className="text-grid-600">of Energy Infrastructure</span>
            </h1>
            <p className="text-lg lg:text-xl text-grid-600 mb-10 max-w-3xl mx-auto">
              A decision-support platform implementing the closed operational loop:
              <strong className="text-grid-900"> PREVENT → DETECT → ASSESS → SIMULATE → MITIGATE → RECOVER → LEARN </strong>
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
              <Link to="/command-center" className="btn btn-primary text-base px-8 py-3">
                Enter Command Center
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link to="/map" className="btn btn-secondary text-base px-8 py-3">
                View Live Grid Map
                <Map className="w-4 h-4" />
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Operational Loop Diagram */}
      <section className="py-20 lg:py-28 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-grid-900 mb-4">The Closed Operational Loop</h2>
            <p className="text-grid-600 max-w-2xl mx-auto">
              Each phase feeds the next. Deterministic simulation, explainable risk scoring, and grounded AI assistance
              ensure every decision is traceable and auditable.
            </p>
          </div>

          <div className="relative">
            {/* Connecting line */}
            <div className="hidden lg:block absolute top-10 left-10 right-10 h-0.5 bg-gradient-to-r from-transparent via-grid-300 to-transparent" aria-hidden="true" />

            <div className="grid grid-cols-2 lg:grid-cols-7 gap-4 lg:gap-0 relative z-10">
              {loopSteps.map((step, index) => (
                <article
                  key={step.id}
                  className={clsx(
                    'relative flex flex-col items-center text-center p-4 lg:p-6',
                    index === 0 && 'lg:pl-0',
                    index === loopSteps.length - 1 && 'lg:pr-0'
                  )}
                >
                  {/* Step circle */}
                  <div
                    className={clsx(
                      'relative flex items-center justify-center w-20 h-20 lg:w-24 lg:h-24 rounded-full mx-auto mb-4 transition-all duration-300',
                      `bg-${step.color}-100 text-${step.color}-700`,
                      index === 3 && 'ring-4 ring-grid-500/30'
                    )}
                  >
                    <step.icon className="w-10 h-10 lg:w-12 lg:h-12" aria-hidden="true" />
                    {/* Pulse indicator for simulate step */}
                    {index === 3 && (
                      <span className="absolute -inset-1 bg-grid-500/20 rounded-full animate-ping" aria-hidden="true" />
                    )}
                  </div>

                  {/* Step content */}
                  <div className="w-full">
                    <h3 className="text-sm font-bold text-grid-900 uppercase tracking-wider mb-1">{step.label}</h3>
                    <p className="text-xs text-grid-500 mb-3">{step.description}</p>
                    <Link
                      to={step.href}
                      className="inline-flex items-center justify-center gap-1 text-sm font-medium text-grid-600 hover:text-grid-900 transition-colors"
                    >
                      Explore <ArrowRight className="w-3 h-3" />
                    </Link>
                  </div>

                  {/* Arrow between steps */}
                  {index < loopSteps.length - 1 && (
                    <div className="hidden lg:block absolute top-10 left-full w-full h-0.5 bg-gradient-to-r from-grid-300 to-transparent" aria-hidden="true" />
                  )}
                </article>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Key Capabilities */}
      <section className="py-20 lg:py-28 px-6 bg-white">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-grid-900 mb-4">Purpose-Built Screens</h2>
            <p className="text-grid-600 max-w-2xl mx-auto">
              Each screen is designed for a specific operational role. No generic admin dashboards.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {screens.map((screen) => (
              <Link
                key={screen.name}
                to={screen.href}
                className="card card-hover p-6 group"
              >
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-12 h-12 bg-grid-100 rounded-xl flex items-center justify-center group-hover:bg-grid-200 transition-colors">
                    <screen.icon className="w-6 h-6 text-grid-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-grid-900 group-hover:text-grid-700 transition-colors">{screen.name}</h3>
                    <p className="text-sm text-grid-500 mt-1">{screen.description}</p>
                  </div>
                  <ArrowRight className="w-5 h-5 text-grid-400 group-hover:text-grid-600 transition-colors" />
                </div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Technical Foundation */}
      <section className="py-20 lg:py-28 px-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-grid-900 mb-4">Technical Foundation</h2>
            <p className="text-grid-600 max-w-2xl mx-auto">
              Built with enterprise-grade technologies and OOAD principles for reliability and maintainability.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center p-6">
              <div className="w-16 h-16 bg-grid-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Shield className="w-8 h-8 text-grid-600" />
              </div>
              <h3 className="text-lg font-semibold text-grid-900 mb-2">Backend</h3>
              <ul className="text-sm text-grid-600 space-y-1 text-left max-w-xs mx-auto">
                <li>• Java 21 + Spring Boot 3.4</li>
                <li>• Spring Security + RBAC</li>
                <li>• Spring Data JPA + Hibernate</li>
                <li>• Flyway + PostgreSQL 18 + PostGIS 3.6</li>
                <li>• OpenAPI 3.1 + SpringDoc</li>
              </ul>
            </div>

            <div className="text-center p-6">
              <div className="w-16 h-16 bg-grid-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Database className="w-8 h-8 text-grid-600" />
              </div>
              <h3 className="text-lg font-semibold text-grid-900 mb-2">Domain Model (OOAD)</h3>
              <ul className="text-sm text-grid-600 space-y-1 text-left max-w-xs mx-auto">
                <li>• 15 bounded contexts</li>
                <li>• Rich aggregates & value objects</li>
                <li>• Strategy & Specification patterns</li>
                <li>• Domain events & state machines</li>
                <li>• Dependency inversion</li>
              </ul>
            </div>

            <div className="text-center p-6">
              <div className="w-16 h-16 bg-grid-100 rounded-xl flex items-center justify-center mx-auto mb-4">
                <Map className="w-8 h-8 text-grid-600" />
              </div>
              <h3 className="text-lg font-semibold text-grid-900 mb-2">Frontend</h3>
              <ul className="text-sm text-grid-600 space-y-1 text-left max-w-xs mx-auto">
                <li>• React 19 + TypeScript strict</li>
                <li>• Tailwind CSS 4 + shadcn/ui</li>
                <li>• MapLibre GL JS 6 (vector tiles)</li>
                <li>• TanStack Query + Zustand</li>
                <li>• Playwright E2E testing</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* Demo Scenario */}
      <section className="py-20 lg:py-28 px-6 bg-grid-950 text-white">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-4">Flagship Demo Scenario</h2>
            <p className="text-grid-300 max-w-2xl mx-auto">
              A deterministic, physically localized threat near concentrated critical assets.
              All data is synthetic — no private operator data is used.
            </p>
          </div>

          <div className="grid lg:grid-cols-3 gap-8">
            <div className="card bg-grid-900 border-grid-800 p-6">
              <h3 className="font-semibold text-white mb-3 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-safe-400" />
                Scenario: Wildfire Encroachment
              </h3>
              <p className="text-grid-300 text-sm mb-4">
                Fast-moving wildfire approaching a transmission corridor with three 500kV lines
                feeding a major metropolitan substation. Redundancy: N-1. Criticality: MISSION_CRITICAL.
              </p>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-grid-400">Assets at risk</span><span className="font-medium text-white">47</span></div>
                <div className="flex justify-between"><span className="text-grid-400">Load served</span><span className="font-medium text-white">2.3 GW</span></div>
                <div className="flex justify-between"><span className="text-grid-400">Population affected</span><span className="font-medium text-white">1.2M</span></div>
                <div className="flex justify-between"><span className="text-grid-400">Est. restoration</span><span className="font-medium text-warning-400">72-96 hrs</span></div>
              </div>
            </div>

            <div className="card bg-grid-900 border-grid-800 p-6">
              <h3 className="font-semibold text-white mb-3 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-safe-400" />
                Loop Execution
              </h3>
              <ol className="space-y-3 text-sm">
                {[
                  'PREVENT: Risk scores identify corridor as CRITICAL (0.87) — vegetation clearance prioritized',
                  'DETECT: Thermal + satellite fusion detects fire perimeter 2.3km from Line 1 (confidence 0.91)',
                  'ASSESS: Graph centrality shows Lines 1-3 carry 89% of metro load — cascade risk HIGH',
                  'SIMULATE: Deterministic cascade sim shows 94% load loss if Line 1 trips (seed=42)',
                  'MITIGATE: Response plan — preposition crews, stage spare conductors, load shed sequence ready',
                  'RECOVER: Milestone tracking — damage assessment → repair → test → energize → verify',
                  'LEARN: After-action captured — update vegetation model, adjust risk weights, retrain fusion',
                ].map((step, i) => (
                  <li key={i} className="flex gap-3 text-grid-300">
                    <span className="flex-shrink-0 w-5 h-5 rounded-full bg-grid-700 text-xs flex items-center justify-center font-mono">{i + 1}</span>
                    <span>{step}</span>
                  </li>
                ))}
              </ol>
            </div>

            <div className="card bg-grid-900 border-grid-800 p-6">
              <h3 className="font-semibold text-white mb-3 flex items-center gap-2">
                <CheckCircle className="w-5 h-5 text-safe-400" />
                Verification Checklist
              </h3>
              <ul className="space-y-2 text-sm text-grid-300">
                {[
                  '✓ Deterministic simulation (same input = same output)',
                  '✓ Explainable risk scores with factor breakdown',
                  '✓ Evidence-backed threat alerts (no LLM hallucination)',
                  '✓ Immutable audit log with correlation IDs',
                  '✓ RBAC with field-level authorization',
                  '✓ Grounded AI Copilot (cites system state)',
                  '✓ Reduced motion & accessibility support',
                  '✓ E2E test: full loop execution',
                ].map((item, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <CheckCircle className="w-4 h-4 text-safe-400 flex-shrink-0 mt-0.5" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 px-6 text-center">
        <div className="max-w-2xl mx-auto">
          <h2 className="text-3xl font-bold text-grid-900 mb-4">Ready to explore?</h2>
          <p className="text-grid-600 mb-8">
            Start with the Command Center for an operational overview, or dive into the Live Grid Map
            to see the topology and active alerts.
          </p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link to="/command-center" className="btn btn-primary text-base px-8 py-3">
              Launch Command Center
              <ArrowRight className="w-4 h-4" />
            </Link>
            <Link to="/map" className="btn btn-secondary text-base px-8 py-3">
              Open Live Grid Map
              <Map className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-grid-200 py-8 px-6 bg-white">
        <div className="max-w-7xl mx-auto text-center text-sm text-grid-500">
          <p>Grid Sentinel — Grid Resilience Decision-Support Platform</p>
          <p className="mt-1">Built with OOAD principles • Deterministic simulation • Grounded AI • Synthetic demo data</p>
        </div>
      </footer>
    </div>
  );
}