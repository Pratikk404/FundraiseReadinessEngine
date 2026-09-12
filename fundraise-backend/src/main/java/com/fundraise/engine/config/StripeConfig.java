package com.fundraise.engine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${app.stripe.secret-key:}")
    private String secretKey;

    @Value("${app.stripe.pro-price-id:}")
    private String proPriceId;

    @Value("${app.stripe.advisor-price-id:}")
    private String advisorPriceId;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public String getSecretKey() {
        return secretKey;
    }

    public String getProPriceId() {
        return proPriceId;
    }

    public String getAdvisorPriceId() {
        return advisorPriceId;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }
}
