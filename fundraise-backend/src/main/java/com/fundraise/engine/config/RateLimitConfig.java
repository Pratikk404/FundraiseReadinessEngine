package com.fundraise.engine.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting configuration.
 * Uses a simple sliding window counter per IP address.
 * Limits: 100 requests/minute for general endpoints, 10/minute for auth.
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("rateLimitFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class RateLimitFilter implements Filter {

        private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String clientIp = getClientIp(httpRequest);
            String path = httpRequest.getRequestURI();

            // Different limits for different endpoints
            int maxRequests;
            int windowSeconds;

            if (path.startsWith("/api/auth/")) {
                maxRequests = 10;      // 10 auth requests/minute
                windowSeconds = 60;
            } else if (path.startsWith("/api/compliance/check/")) {
                maxRequests = 5;       // 5 compliance checks/minute
                windowSeconds = 60;
            } else if (path.startsWith("/api/documents/upload/")) {
                maxRequests = 20;      // 20 uploads/minute
                windowSeconds = 60;
            } else {
                maxRequests = 100;     // 100 general requests/minute
                windowSeconds = 60;
            }

            RateLimiter limiter = limiters.computeIfAbsent(clientIp,
                    k -> new RateLimiter(maxRequests, windowSeconds));

            if (!limiter.allowRequest()) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                        "{\"error\":\"Too many requests. Please try again later.\",\"retryAfter\":" +
                                limiter.getSecondsUntilReset() + "}");
                httpResponse.setHeader("Retry-After", String.valueOf(limiter.getSecondsUntilReset()));
                return;
            }

            // Add rate limit headers
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(limiter.getRemainingRequests()));

            chain.doFilter(request, response);
        }

        private String getClientIp(HttpServletRequest request) {
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
    }

    static class RateLimiter {
        private final int maxRequests;
        private final long windowMs;
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();

        RateLimiter(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowMs = windowSeconds * 1000L;
        }

        synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMs) {
                count.set(0);
                windowStart = now;
            }
            if (count.get() >= maxRequests) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }

        int getRemainingRequests() {
            return Math.max(0, maxRequests - count.get());
        }

        int getSecondsUntilReset() {
            long elapsed = System.currentTimeMillis() - windowStart;
            return (int) Math.max(1, (windowMs - elapsed) / 1000);
        }
    }
}
