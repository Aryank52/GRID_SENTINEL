import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import type {
  GeoJsonFeatureCollection,
  SpatialQueryParams,
  GridNodeProperties,
  GridEdgeProperties,
  AssetProperties,
  ThreatEventProperties,
  IncidentProperties,
  RiskScoreProperties,
} from '@spatial/spatial';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';

class ApiClient {
  private client: AxiosInstance;
  private refreshTokenPromise: Promise<string> | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor - add auth token
    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = localStorage.getItem('access_token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        // Add correlation ID for tracing
        config.headers['X-Correlation-ID'] = crypto.randomUUID();
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor - handle token refresh
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const newToken = await this.refreshAccessToken();
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return this.client(originalRequest);
          } catch {
            // Refresh failed - redirect to login
            localStorage.removeItem('access_token');
            localStorage.removeItem('refresh_token');
            window.location.href = '/login';
            return Promise.reject(error);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  private async refreshAccessToken(): Promise<string> {
    if (this.refreshTokenPromise) {
      return this.refreshTokenPromise;
    }

    this.refreshTokenPromise = (async () => {
      const refreshToken = localStorage.getItem('refresh_token');
      if (!refreshToken) {
        throw new Error('No refresh token');
      }

      const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
        refresh_token: refreshToken,
      });

      const { access_token } = response.data;
      localStorage.setItem('access_token', access_token);
      return access_token;
    })();

    try {
      return await this.refreshTokenPromise;
    } finally {
      this.refreshTokenPromise = null;
    }
  }

  // Generic HTTP methods
  get<T>(url: string, params?: Record<string, unknown>) {
    return this.client.get<T>(url, { params });
  }

  post<T>(url: string, data?: unknown) {
    return this.client.post<T>(url, data);
  }

  put<T>(url: string, data?: unknown) {
    return this.client.put<T>(url, data);
  }

  patch<T>(url: string, data?: unknown) {
    return this.client.patch<T>(url, data);
  }

  delete<T>(url: string) {
    return this.client.delete<T>(url);
  }

  // =========================================================================
  // Spatial API Methods
  // =========================================================================

  /**
   * Get grid nodes as GeoJSON FeatureCollection
   */
  async getGridNodes(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<GridNodeProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<GridNodeProperties>>('/spatial/grid/nodes', { params });
    return response.data;
  }

  /**
   * Get grid edges as GeoJSON FeatureCollection
   */
  async getGridEdges(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<GridEdgeProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<GridEdgeProperties>>('/spatial/grid/edges', { params });
    return response.data;
  }

  /**
   * Get assets as GeoJSON FeatureCollection
   */
  async getAssets(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<AssetProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<AssetProperties>>('/spatial/assets', { params });
    return response.data;
  }

  /**
   * Get threat events as GeoJSON FeatureCollection
   */
  async getThreats(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<ThreatEventProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<ThreatEventProperties>>('/spatial/threats', { params });
    return response.data;
  }

  /**
   * Get incidents as GeoJSON FeatureCollection
   */
  async getIncidents(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<IncidentProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<IncidentProperties>>('/spatial/incidents', { params });
    return response.data;
  }

  /**
   * Get risk scores as GeoJSON FeatureCollection
   */
  async getRiskScores(params?: SpatialQueryParams): Promise<GeoJsonFeatureCollection<RiskScoreProperties>> {
    const response = await this.client.get<GeoJsonFeatureCollection<RiskScoreProperties>>('/spatial/risk', { params });
    return response.data;
  }
}

export const api = new ApiClient();