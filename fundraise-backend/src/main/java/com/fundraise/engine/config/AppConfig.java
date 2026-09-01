package com.fundraise.engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    public String getUploadDir() {
        return uploadDir;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }
}
