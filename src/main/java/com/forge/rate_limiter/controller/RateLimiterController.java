package com.forge.rate_limiter.controller;

import com.forge.rate_limiter.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    @GetMapping("/resource")
    public Mono<ResponseEntity<String>> getProtectedResource(
            @RequestHeader(value = "X-Client-id", required = false) String clientId) {

        if (clientId == null || clientId.isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Missing X-Client-id header"));
        }

        return rateLimiterService.isAllowed(clientId)
                .map(allowed -> allowed
                        ? ResponseEntity.ok("Успіх! Дані для: " + clientId)
                        : ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests"));
    }
}