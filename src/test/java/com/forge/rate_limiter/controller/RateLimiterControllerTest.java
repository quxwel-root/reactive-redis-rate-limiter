package com.forge.rate_limiter.controller;

import com.forge.rate_limiter.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(RateLimiterController.class)
class RateLimiterControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @Test
    void whenMissingClientId_thenReturns401() {
        webTestClient.get()
                .uri("/api/v1/resource")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class).isEqualTo("Missing X-Client-id header");
    }
     @Test
    void whenRateLimitAllowed_thenReturns200() {
         Mockito.when(rateLimiterService.isAllowed("test_client")).thenReturn(Mono.just(true));

         webTestClient.get()
                 .uri("/api/v1/resource")
                 .header("X-Client-id", "test_client")
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(String.class).isEqualTo("Успіх! Дані для: test_client");
     }

     @Test
    void whenRateLimitExceeded_thenReturns429() {
        Mockito.when(rateLimiterService.isAllowed("hacker_client")).thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/api/v1/resource")
                .header("X-Client-id", "hacker_client")
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody(String.class).isEqualTo("Too many requests");
     }
}
