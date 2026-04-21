package com.ecommerce.orderservice.scheduler;

import com.ecommerce.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job for processing pending orders
 * Runs every 5 minutes to transition PENDING orders to PROCESSING
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProcessingScheduler {

    private final OrderService orderService;

    /**
     * Process pending orders every 5 minutes
     * Fixed rate: 300000 ms = 5 minutes
     */
    @Scheduled(fixedRateString = "${scheduler.order-processing.fixed-rate}")
    public void processPendingOrders() {
        log.info("Starting scheduled job: Processing pending orders");
        
        try {
            int processedCount = orderService.processPendingOrders();
            log.info("Scheduled job completed: Processed {} orders", processedCount);
        } catch (Exception e) {
            log.error("Error in scheduled job: {}", e.getMessage(), e);
        }
    }
}

