import { useState } from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  LayoutDashboard,
  Map,
  Database,
  Shield,
  AlertTriangle,
  FileText,
  FlaskConical,
  Bot,
  ClipboardList,
  LogOut,
  Menu,
  ChevronLeft,
  User,
  Settings,
  Bell,
  HelpCircle,
} from 'lucide-react';
import { clsx } from 'clsx';

const navigation = [
  { name: 'Command Center', href: '/command-center', icon: LayoutDashboard, description: 'Grid health, active threats, incidents' },
  { name: 'Live Grid Map', href: '/map', icon: Map, description: 'Real-time grid topology & alerts' },
  { name: 'Asset Intelligence', href: '/assets', icon: Database, description: 'Asset registry, risk scores, health' },
  { name: 'Protection Planner', href: '/protection', icon: Shield, description: 'Prevention planning & optimization' },
  { name: 'Threat Detection', href: '/threats', icon: AlertTriangle, description: 'Detection pipeline & alert management' },
  { name: 'Incident Response', href: '/incidents', icon: FileText, description: 'Incident lifecycle & response plans' },
  { name: 'Simulation Lab', href: '/simulation', icon: FlaskConical, description: 'What-if analysis & impact simulation' },
  { name: 'AI Copilot', href: '/copilot', icon: Bot, description: 'Grounded AI assistance' },
  { name: 'Audit Dashboard', href: '/audit', icon: ClipboardList, description: 'Governance & compliance logs' },
] as const;

export function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile menu backdrop */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={clsx(
          'fixed top-0 left-0 z-50 h-full bg-white border-r border-grid-200 transition-all duration-200',
          sidebarOpen ? 'w-64' : 'w-20',
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        )}
        aria-label="Main navigation"
      >
        {/* Logo & Toggle */}
        <div className="flex items-center justify-between h-16 px-4 border-b border-grid-200">
          <div className={clsx('flex items-center gap-3', sidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none')}>
            <div className="w-8 h-8 bg-grid-600 rounded-lg flex items-center justify-center">
              <Map className="w-5 h-5 text-white" />
            </div>
            <span className="font-semibold text-grid-900">Grid Sentinel</span>
          </div>
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 rounded-lg text-grid-500 hover:bg-grid-100 transition-colors lg:hidden"
            aria-label={sidebarOpen ? 'Close sidebar' : 'Open sidebar'}
            aria-expanded={sidebarOpen}
          >
            {sidebarOpen ? <ChevronLeft className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto p-4 space-y-1" role="navigation" aria-label="Main">
          {navigation.map((item) => {
            const isActive = location.pathname === item.href;
            return (
              <NavLink
                key={item.name}
                to={item.href}
                className={({ isActive }) => clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all duration-150',
                  sidebarOpen
                    ? isActive
                      ? 'bg-grid-50 text-grid-700 font-medium'
                      : 'text-grid-600 hover:bg-grid-50 hover:text-grid-900'
                    : 'justify-center'
                )}
                title={sidebarOpen ? undefined : item.name}
                aria-current={isActive ? 'page' : undefined}
              >
                <item.icon className="w-5 h-5 flex-shrink-0" aria-hidden="true" />
                {sidebarOpen && (
                  <>
                    <span className="truncate">{item.name}</span>
                    {isActive && <span className="ml-auto w-1.5 h-1.5 bg-grid-500 rounded-full" aria-hidden="true" />}
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* User & Bottom actions */}
        <div className="p-4 border-t border-grid-200">
          <div className={clsx('flex items-center gap-3', sidebarOpen ? 'opacity-100' : 'opacity-0 pointer-events-none')}>
            <div className="w-8 h-8 bg-grid-100 rounded-full flex items-center justify-center flex-shrink-0">
              <User className="w-4 h-4 text-grid-600" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-grid-900 truncate">{user?.fullName}</p>
              <p className="text-xs text-grid-500 truncate">{user?.roles?.[0]?.replace(/_/g, ' ')}</p>
            </div>
          </div>

          <div className="mt-4 flex items-center gap-2">
            <button
              className={clsx('flex-1 btn btn-ghost justify-center gap-2', sidebarOpen ? '' : 'px-2')}
              onClick={() => {}}
              disabled
              title={sidebarOpen ? 'Settings' : ''}
            >
              <Settings className="w-4 h-4" />
              {sidebarOpen && <span>Settings</span>}
            </button>
            <button
              className={clsx('btn btn-ghost justify-center gap-2 text-alert-600 hover:bg-alert-50', sidebarOpen ? '' : 'px-2')}
              onClick={handleLogout}
              title={sidebarOpen ? 'Sign out' : ''}
            >
              <LogOut className="w-4 h-4" />
              {sidebarOpen && <span>Sign out</span>}
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main
        className={clsx(
          'min-h-screen transition-all duration-200 lg:ml-64',
          sidebarOpen ? 'ml-64' : 'ml-20'
        )}
      >
        {/* Top bar */}
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-sm border-b border-grid-200">
          <div className="flex items-center justify-between h-16 px-6">
            <div className="flex items-center gap-4">
              <button
                className="lg:hidden p-2 rounded-lg text-grid-500 hover:bg-grid-100"
                onClick={() => setMobileMenuOpen(true)}
                aria-label="Open menu"
              >
                <Menu className="w-5 h-5" />
              </button>
              <h1 className="text-xl font-semibold text-grid-900 lg:hidden">
                {navigation.find(n => n.href === location.pathname)?.name || 'Grid Sentinel'}
              </h1>
            </div>

            <div className="flex items-center gap-4">
              {/* Notifications */}
              <button className="relative p-2 rounded-lg text-grid-500 hover:bg-grid-100 transition-colors" aria-label="Notifications">
                <Bell className="w-5 h-5" />
                <span className="absolute top-1 right-1 w-2 h-2 bg-alert-500 rounded-full" aria-hidden="true" />
              </button>

              {/* Help */}
              <button className="p-2 rounded-lg text-grid-500 hover:bg-grid-100 transition-colors" aria-label="Help">
                <HelpCircle className="w-5 h-5" />
              </button>
            </div>
          </div>
        </header>

        {/* Page content */}
        <div className="p-6">
          <Outlet />
        </div>
      </main>
    </div>
  );
}