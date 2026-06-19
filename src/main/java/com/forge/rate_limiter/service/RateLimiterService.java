package com.forge.rate_limiter.service;

import com.forge.rate_limiter.config.RateLimiterProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;
    private final RateLimiterProperties properties;
    private final MeterRegistry meterRegistry;

   public Mono<Boolean> isAllowed(String clientId) {
       String key = "rate_limit:tb:" + clientId;
       long now = Instant.now().getEpochSecond();

       RateLimiterProperties.Plan plan = properties.plans().getOrDefault(clientId, properties.plans().get("default"));

       return redisTemplate.execute(
               tokenBucketScript,
               List.of(key),
               List.of(String.valueOf(plan.capacity()), String.valueOf(plan.fillRate()), String.valueOf(now), "1")
       )
               .singleOrEmpty()
               .timeout(Duration.ofMillis(200))
               .onErrorResume(throwable -> {
                   log.error("Redis timeout or error for client {}: {}", clientId, throwable.getMessage());
                   return Mono.just(1L);
               })
               .map(result -> {
                   boolean allowed = ((Number) result).longValue() == 1L;
                   String status = allowed ? "allowed" : "rejected";
                   meterRegistry.counter("rate_limiter_requests_total", "clientId", clientId, "status", status).increment();
                   return allowed;
               })
               .defaultIfEmpty(false);
   }
}