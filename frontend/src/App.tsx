import { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { PageLoader } from './components/ui/PageLoader';
import { useAuth } from './hooks/useAuth';

// Lazy load pages for code splitting
const CommandCenter = lazy(() => import('./pages/CommandCenter').then(m => ({ default: m.CommandCenter })));
const LiveGridMap = lazy(() => import('./pages/LiveGridMap').then(m => ({ default: m.LiveGridMap })));
const AssetIntelligence = lazy(() => import('./pages/AssetIntelligence').then(m => ({ default: m.AssetIntelligence })));
const ProtectionPlanner = lazy(() => import('./pages/ProtectionPlanner').then(m => ({ default: m.ProtectionPlanner })));
const ThreatDetectionCenter = lazy(() => import('./pages/ThreatDetectionCenter').then(m => ({ default: m.ThreatDetectionCenter })));
const IncidentResponseCenter = lazy(() => import('./pages/IncidentResponseCenter').then(m => ({ default: m.IncidentResponseCenter })));
const SimulationLab = lazy(() => import('./pages/SimulationLab').then(m => ({ default: m.SimulationLab })));
const AICopilot = lazy(() => import('./pages/AICopilot').then(m => ({ default: m.AICopilot })));
const AuditDashboard = lazy(() => import('./pages/AuditDashboard').then(m => ({ default: m.AuditDashboard })));
const Login = lazy(() => import('./pages/Login').then(m => ({ default: m.Login })));
const NotFound = lazy(() => import('./pages/NotFound').then(m => ({ default: m.NotFound })));

// Route guards
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated, isLoading } = useAuth();
  
  if (isLoading) {
    return <PageLoader fullScreen />;
  }
  
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

function App() {
  return (
    <Suspense fallback={<PageLoader fullScreen />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        
        <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
          <Route path="/" element={<Navigate to="/command-center" replace />} />
          <Route path="/command-center" element={<CommandCenter />} />
          <Route path="/map" element={<LiveGridMap />} />
          <Route path="/assets" element={<AssetIntelligence />} />
          <Route path="/protection" element={<ProtectionPlanner />} />
          <Route path="/threats" element={<ThreatDetectionCenter />} />
          <Route path="/incidents" element={<IncidentResponseCenter />} />
          <Route path="/simulation" element={<SimulationLab />} />
          <Route path="/copilot" element={<AICopilot />} />
          <Route path="/audit" element={<AuditDashboard />} />
        </Route>
        
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  );
}

export default App;