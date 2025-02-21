package com.myblogbackend.blog.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    public ResilienceConfig(final RetryRegistry retryRegistry, final CircuitBreakerRegistry circuitBreakerRegistry) {
        // Retry Events
        Retry retry = retryRegistry.retry("albumService");
        retry.getEventPublisher().onRetry(event -> log.warn("🔁 Retry #{} for '{}': {}",
                event.getNumberOfRetryAttempts(), event.getName(), event.getLastThrowable().getMessage()));
        retry.getEventPublisher().onSuccess(event -> log.info("✅ Retry success after {} attempts", event.getNumberOfRetryAttempts()));
        retry.getEventPublisher().onError(event -> log.error("❌ Retry failed after {} attempts", event.getNumberOfRetryAttempts()));

        // CircuitBreaker Events
//        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("albumService");
//        cb.getEventPublisher().onStateTransition(event -> log.info("⚡ CircuitBreaker '{}' state changed to '{}'",
//        event.getCircuitBreakerName(), event.getStateTransition()));
//        cb.getEventPublisher().onError(event -> log.error("💥 CircuitBreaker '{}' recorded error: {}",
//        event.getCircuitBreakerName(), event.getThrowable().getMessage()));
    }
}
