package com.forge.rate_limiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "rate-limiter")
public record RateLimiterProperties(Map<String, Plan> plans) {
    public record Plan(int capacity, int fillRate) {}
}
