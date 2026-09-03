package com.fundraise.engine.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security headers configuration.
 * Adds standard security headers to all responses.
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setName("securityHeadersFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // XSS Protection
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Prevent MIME sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Clickjacking protection
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // Referrer policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Content Security Policy (basic)
            httpResponse.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");

            // Strict Transport Security (for HTTPS)
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

            chain.doFilter(request, response);
        }
    }
}
