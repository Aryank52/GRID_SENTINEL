import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Lock, User, AlertCircle, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import { Map } from 'lucide-react';

export function Login() {
  const navigate = useNavigate();
  const { login, isLoading: authLoading } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      await login(username, password);
      navigate('/command-center', { replace: true });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Invalid credentials');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-grid-50 via-white to-grid-100 px-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-grid-600 rounded-2xl mx-auto mb-4">
            <Map className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-grid-900">Grid Sentinel</h1>
          <p className="text-grid-500 mt-1">Grid Resilience Decision-Support Platform</p>
        </div>

        {/* Login Form */}
        <Card className="p-8">
          <h2 className="text-xl font-semibold text-grid-900 mb-2">Sign in to Command Center</h2>
          <p className="text-grid-500 mb-6">Enter your credentials to access the operational dashboard</p>

          {error && (
            <div className="mb-6 p-3 bg-alert-50 border border-alert-200 rounded-lg flex items-center gap-2 text-alert-700 text-sm" role="alert">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <Input
              label="Username"
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="Enter username"
              leftIcon={<User className="w-4 h-4" />}
              required
              disabled={isLoading || authLoading}
              autoComplete="username"
            />

            <Input
              label="Password"
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="Enter password"
              leftIcon={<Lock className="w-4 h-4" />}
              rightIcon={
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="text-grid-400 hover:text-grid-600"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              }
              required
              disabled={isLoading || authLoading}
              autoComplete="current-password"
            />

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" className="rounded border-grid-300 text-grid-600 focus:ring-grid-500" />
                <span className="text-sm text-grid-600">Remember me</span>
              </label>
              <a href="#" className="text-sm text-grid-600 hover:text-grid-900">Forgot password?</a>
            </div>

            <Button type="submit" className="w-full" size="lg" loading={isLoading || authLoading}>
              <Lock className="w-4 h-4 mr-2" />
              Sign In
            </Button>
          </form>

          <div className="mt-6 pt-6 border-t border-grid-200">
            <p className="text-sm text-grid-500 text-center mb-4">Demo Credentials</p>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <DemoCredential username="commander" role="INCIDENT_COMMANDER" />
              <DemoCredential username="dispatcher" role="DISPATCHER" />
              <DemoCredential username="analyst" role="RISK_ANALYST" />
              <DemoCredential username="viewer" role="VIEWER" />
            </div>
            <p className="text-xs text-grid-400 text-center mt-3">Password for all: <code className="bg-grid-100 px-1.5 py-0.5 rounded">demo123</code></p>
          </div>
        </Card>

        <p className="text-center text-xs text-grid-400 mt-6">
          Grid Sentinel v0.1.0 • Deterministic simulation • Grounded AI • Synthetic demo data
        </p>
      </div>
    </div>
  );
}

function DemoCredential({ username, role }: { username: string; role: string }) {
  const { login } = useAuth();
  return (
    <button
      type="button"
      onClick={() => login(username, 'demo123')}
      className="p-2 text-left bg-grid-50 hover:bg-grid-100 rounded-lg border border-grid-200 transition-colors"
    >
      <p className="font-mono font-medium text-grid-900">{username}</p>
      <p className="text-grid-500">{role.replace(/_/g, ' ')}</p>
    </button>
  );
}