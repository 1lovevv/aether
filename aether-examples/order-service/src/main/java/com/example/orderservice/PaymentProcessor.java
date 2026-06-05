package com.example.orderservice;

import com.aether.spring.annotation.ActorBean;
import com.aether.spring.annotation.OnMessage;
import com.aether.spring.annotation.OnFailure;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Payment Processor demonstrating:
 * <ul>
 *   <li>Supervised actor with failure handling</li>
 *   <li>Payment processing with timeout simulation</li>
 *   <li>Idempotent operations</li>
 * </ul>
 */
@ActorBean(name = "payment-processor")
public class PaymentProcessor {

    // Track processed payment IDs for idempotency
    private final Map<String, PaymentResult> processedPayments = new ConcurrentHashMap<>();
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /**
     * Processes a payment request.
     * Simulates payment processing with potential failures.
     */
    @OnMessage
    public void onProcessPayment(ProcessPaymentMsg msg) {
        String paymentId = msg.paymentId();

        // Check if already processed (idempotency)
        if (processedPayments.containsKey(paymentId)) {
            System.out.println("[PaymentProcessor] Payment already processed: " + paymentId);
            return;
        }

        System.out.println("[PaymentProcessor] Processing payment: " + paymentId +
                           ", Amount: " + msg.amount());

        // Simulate payment processing
        try {
            Thread.sleep(100); // Simulate processing time

            PaymentResult result = new PaymentResult(
                    paymentId,
                    PaymentStatus.SUCCESS,
                    Instant.now(),
                    "Payment processed successfully"
            );

            processedPayments.put(paymentId, result);
            System.out.println("[PaymentProcessor] Payment completed: " + paymentId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted", e);
        }
    }

    /**
     * Handles payment failures.
     */
    @OnFailure
    public void onFailure(FailureEvent event) {
        failureCount.incrementAndGet();
        System.err.println("[PaymentProcessor] Payment failed: " + event.reason() +
                          ", Failure count: " + failureCount.get());
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    public PaymentResult getPaymentResult(String paymentId) {
        return processedPayments.get(paymentId);
    }

    // Message records
    public record ProcessPaymentMsg(String paymentId, java.math.BigDecimal amount) {}
    public record PaymentResult(String paymentId, PaymentStatus status, Instant timestamp, String message) {}

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING, REFUNDED
    }

    public record FailureEvent(String reason, Throwable cause) {}
}
