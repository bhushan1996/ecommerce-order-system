package com.ecommerce.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Simulated Payment Service
 * Used to demonstrate Circuit Breaker pattern
 * 
 * In production, this would call an external payment gateway
 */
@Service
@Slf4j
public class PaymentService {

    private final Random random = new Random();

    /**
     * Simulate payment processing
     * Randomly fails to demonstrate circuit breaker
     */
    public boolean processPayment(BigDecimal amount) {
        log.info("Processing payment for amount: {}", amount);

        // Simulate random failures (30% failure rate)
        if (random.nextInt(100) < 30) {
            log.error("Payment processing failed for amount: {}", amount);
            throw new RuntimeException("Payment gateway timeout");
        }

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Payment processed successfully for amount: {}", amount);
        return true;
    }
}

