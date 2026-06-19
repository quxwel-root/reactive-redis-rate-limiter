# Reactive Distributed Rate Limiter (Token Bucket)

A high-performance, non-blocking distributed rate limiting service built on top of the **Spring WebFlux reactive stack** and **Redis (Lua scripting)** using the Token Bucket algorithm.

## Core Features (Senior-Level Implementation)
* **Atomic Distributed Execution:** The entire token calculation logic resides within a Redis Lua script. This guarantees atomicity across multiple application instances, completely preventing race conditions in a distributed cluster.
* **Defensive Programming (Fail-Open Strategy):** Redis operations are guarded with a strict 200ms timeout block. If Redis experiences high latency or fails entirely, the system seamlessly transitions into a fail-open mode to preserve core business uptime.
* **Memory Leak Protection:** Automatic TTL configuration (5 minutes) for all client keys inside Redis ensures that idle keys are cleared, protecting against Out-of-Memory (OOM) exceptions.
* **Configuration over Code:** Rate limits for distinct tiers (`premium_user` vs `default`) are cleanly driven via `application.yml` externalized properties mapped onto modern Java records.
* **Production Observability:** Integrated with Spring Boot Actuator and Micrometer to expose clean Prometheus scraping metrics.

## Production Metrics & Observability
* System Health Indicators: `http://localhost:8080/actuator/health`
* Prometheus Scraping Registry: `http://localhost:8080/actuator/prometheus`

## Local Setup (Without Docker)

1. Ensure you have a local **Redis server** running on the default port `6379`.
2. Build the executable artifact:
   ```bash
   mvn clean compile  mvn spring-boot:run
