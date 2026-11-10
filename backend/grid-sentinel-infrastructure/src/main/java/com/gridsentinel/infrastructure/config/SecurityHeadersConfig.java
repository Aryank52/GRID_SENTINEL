package com.gridsentinel.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.io.IOException;

/**
 * Security headers configuration for HTTP responses.
 * Adds security headers to all HTTP responses.
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * Security headers filter that adds security headers to all responses.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<Filter> securityHeadersFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

    /**
     * Filter that adds security headers to all HTTP responses.
     */
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Prevent clickjacking
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // Prevent MIME type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Referrer policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions policy (feature policy)
            httpResponse.setHeader("Permissions-Policy",
                    "accelerometer=(), camera=(), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), payment=(), usb=()");

            // Cache control for sensitive endpoints
            String path = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
            if (path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/dev/") ||
                path.startsWith("/actuator/") || path.contains("/auth/")) {
                httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Expires", "0");
            }

            chain.doFilter(request, response);
        }
    }

    /**
     * Security headers for production profile - adds CSP and HSTS.
     */
    @Bean
    @org.springframework.context.annotation.Profile("prod")
    public FilterRegistrationBean<Filter> prodSecurityHeadersFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ProdSecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

    /**
     * Production-specific security headers filter.
     * Adds CSP, HSTS, and other production-only headers.
     */
    public static class ProdSecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;

            // HTTP Strict Transport Security (HSTS)
            // max-age=31536000 = 1 year, includeSubDomains, preload
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

            // Content Security Policy
            // Restrictive CSP for API endpoints
            String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
            if (requestURI.startsWith("/api/")) {
                // API endpoints - restrictive CSP
                httpResponse.setHeader("Content-Security-Policy",
                        "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'");
            } else {
                // Frontend pages - more permissive CSP for frontend assets
                httpResponse.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com; " +
                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data: https://cdn.jsdelivr.net https://unpkg.com; " +
                        "connect-src 'self' wss: https:; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");
            }

            // Additional security headers
            httpResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            httpResponse.setHeader("X-Download-Options", "noopen");
            httpResponse.setHeader("X-DNS-Prefetch-Control", "off");

            chain.doFilter(request, response);
        }
    }
}