import { Link } from 'react-router-dom';
import { Home, Search, AlertCircle } from 'lucide-react';
import { Button } from '../components/ui/Button';

export function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-grid-50 via-white to-grid-100 px-4">
      <div className="text-center max-w-md">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-alert-100 rounded-full mx-auto mb-6">
          <AlertCircle className="w-10 h-10 text-alert-500" />
        </div>
        <h1 className="text-4xl font-bold text-grid-900 mb-2">404</h1>
        <h2 className="text-xl text-grid-600 mb-4">Page Not Found</h2>
        <p className="text-grid-500 mb-8">
          The page you're looking for doesn't exist or has been moved.
          Check the URL or navigate to one of the main screens.
        </p>
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <Button variant="primary" size="lg" asChild>
            <Link to="/command-center">
              <Home className="w-4 h-4 mr-2" />
              Go to Command Center
            </Link>
          </Button>
          <Button variant="secondary" size="lg" asChild>
            <Link to="/map">
              <Search className="w-4 h-4 mr-2" />
              Open Grid Map
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}