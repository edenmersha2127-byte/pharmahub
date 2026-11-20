package com.medicinelocator.paymentservice.presentation.controller;

import com.medicinelocator.paymentservice.application.command.ConfirmPaymentCommand;
import com.medicinelocator.paymentservice.application.command.handler.ConfirmPaymentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Chapa webhook receiver. Public (not behind the gateway auth), but safe: it
 * only extracts the tx_ref and asks ConfirmPaymentHandler to VERIFY server-side
 * with Chapa before activating anything — a spoofed call cannot grant access.
 * Idempotent, so Chapa retries are harmless.
 */
@RestController
@RequestMapping("/api/payments/webhook")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final ConfirmPaymentHandler confirmPaymentHandler;

    public PaymentWebhookController(ConfirmPaymentHandler confirmPaymentHandler) {
        this.confirmPaymentHandler = confirmPaymentHandler;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody(required = false) Map<String, Object> payload) {
        String txRef = extractTxRef(payload);
        if (txRef == null) {
            log.warn("Webhook received without a tx_ref: {}", payload);
            return ResponseEntity.ok().build();
        }
        log.info("Webhook received for txRef={}", txRef);
        try {
            confirmPaymentHandler.handle(new ConfirmPaymentCommand(txRef));
        } catch (Exception e) {
            // Never fail the webhook; the verify job / return-url can reconcile.
            log.error("Webhook processing error for txRef={}: {}", txRef, e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private String extractTxRef(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        for (String key : new String[]{"tx_ref", "trx_ref", "reference"}) {
            Object value = payload.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }
}
