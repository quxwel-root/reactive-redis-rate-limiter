package com.forge.rate_limiter.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RedisHealthIndicator implements ReactiveHealthIndicator {
    private final ReactiveRedisConnectionFactory connectionFactory;

    public RedisHealthIndicator(ReactiveRedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    @Override
    public Mono<Health> health() {
        return connectionFactory.getReactiveConnection()
                .ping()
                .map(ping -> Health.up().withDetail("redis", "connected").build())
                .onErrorResume(e -> Mono.just(Health.down(e).build()));
    }
}
