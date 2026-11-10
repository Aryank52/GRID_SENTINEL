package com.gridsentinel.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting configuration for API endpoints.
 * Provides in-memory rate limiting based on IP address and user.
 */
@Configuration
public class RateLimitingConfig {

    /**
     * Rate limiting filter registration.
     * Applies rate limiting to all API endpoints.
     */
    @Bean
    @org.springframework.core.annotation.Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/api/v1/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registrationBean;
    }

    /**
     * Rate limiting filter implementation.
     * Uses a sliding window algorithm with in-memory storage.
     */
    public static class RateLimitingFilter implements jakarta.servlet.Filter {

        // Rate limit configuration
        private static final int DEFAULT_LIMIT = 100; // requests per window
        private static final long WINDOW_MS = 60_000; // 1 minute window
        private static final int AUTH_LIMIT = 10; // login attempts per window
        private static final long AUTH_WINDOW_MS = 300_000; // 5 minutes for auth

        // In-memory storage: key -> (windowStart, count)
        private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
        private final Map<String, WindowCounter> authCounters = new ConcurrentHashMap<>();

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request,
                             jakarta.servlet.ServletResponse response,
                             jakarta.servlet.FilterChain chain)
                throws java.io.IOException, jakarta.servlet.ServletException {

            jakarta.servlet.http.HttpServletRequest httpRequest = (jakarta.servlet.http.HttpServletRequest) request;
            jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;

            String path = httpRequest.getRequestURI();
            String clientIp = getClientIp(httpRequest);
            String userId = getUserId(httpRequest);

            // Determine rate limit based on endpoint
            boolean isAuthEndpoint = httpRequest.getRequestURI().startsWith("/api/v1/auth/");
            boolean isSimulationEndpoint = httpRequest.getRequestURI().contains("/simulations/");
            boolean isExportEndpoint = httpRequest.getRequestURI().contains("/export");

            int limit = DEFAULT_LIMIT;
            long windowMs = WINDOW_MS;

            if (isAuthEndpoint) {
                limit = AUTH_LIMIT;
                windowMs = AUTH_WINDOW_MS;
            } else if (isSimulationEndpoint || path.contains("/export")) {
                limit = 5; // Stricter limit for expensive operations
                windowMs = 60_000;
            }

            String key = buildKey(clientIp, userId, httpRequest.getRequestURI());
            boolean allowed = checkRateLimit(key, limit, windowMs, isAuthEndpoint);

            if (!allowed) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("""
                    {
                        "error": "rate_limit_exceeded",
                        "message": "Too many requests. Please try again later.",
                        "retryAfterSeconds": 60
                    }
                    """);
                return;
            }

            // Add rate limit headers
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(getRemainingRequests(key, limit)));
            httpResponse.setHeader("X-RateLimit-Reset", String.valueOf(getResetTimeSeconds()));

            chain.doFilter(request, response);
        }

        private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            return request.getRemoteAddr();
        }

        private String getUserId(jakarta.servlet.http.HttpServletRequest request) {
            // Try to get user ID from JWT claims if available
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // In a real implementation, you'd decode the JWT to get user ID
                // For now, use a hash of the token as a pseudo-user identifier
                return "user:" + Integer.toHexString(token.hashCode());
            }
            return "anon";
        }

        private String buildKey(String ip, String userId, String path) {
            return ip + ":" + userId + ":" + path;
        }

        private boolean checkRateLimit(String key, int limit, long windowMs, boolean isAuth) {
            Map<String, WindowCounter> store = isAuth ? authCounters : counters;
            long now = Instant.now().toEpochMilli();
            long windowStart = now - windowMs;

            WindowCounter counter = store.compute(key, (k, existing) -> {
                if (existing == null || existing.windowStart < windowStart) {
                    return new WindowCounter(now, 1);
                }
                existing.count++;
                return existing;
            });

            return counter.count <= limit;
        }

        private int getRemainingRequests(String key, int limit) {
            WindowCounter counter = counters.get(key);
            if (counter == null) {
                return limit;
            }
            return Math.max(0, limit - counter.count);
        }

        private long getResetTimeSeconds() {
            return (Instant.now().toEpochMilli() + 60_000) / 1000;
        }

        private static class WindowCounter {
            long windowStart;
            int count;

            WindowCounter(long windowStart, int count) {
                this.windowStart = windowStart;
                this.count = count;
            }
        }
    }
}