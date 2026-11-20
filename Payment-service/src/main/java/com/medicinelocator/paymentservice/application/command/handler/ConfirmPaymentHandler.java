package com.medicinelocator.paymentservice.application.command.handler;

import com.medicinelocator.paymentservice.application.command.ConfirmPaymentCommand;
import com.medicinelocator.paymentservice.application.service.*;
import com.medicinelocator.paymentservice.domain.model.Payment;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Confirms a payment by verifying server-side with Chapa, then activates the
 * subscription. Idempotent: a tx_ref already marked SUCCESS is a no-op, so the
 * webhook and the return-url verification can both call this safely.
 */
@Component
public class ConfirmPaymentHandler {

    private static final Logger log = LoggerFactory.getLogger(ConfirmPaymentHandler.class);

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGateway paymentGateway;
    private final SubscriptionActivator activator;
    private final NotificationNotifier notifier;

    public ConfirmPaymentHandler(PaymentRepository paymentRepository,
                                 SubscriptionRepository subscriptionRepository,
                                 PaymentGateway paymentGateway,
                                 SubscriptionActivator activator,
                                 NotificationNotifier notifier) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentGateway = paymentGateway;
        this.activator = activator;
        this.notifier = notifier;
    }

    @Transactional
    public void handle(ConfirmPaymentCommand command) {
        Optional<Payment> paymentOpt = paymentRepository.findByTxRef(command.getTxRef());
        if (paymentOpt.isEmpty()) {
            log.warn("Confirm ignored; unknown txRef={}", command.getTxRef());
            return;
        }
        Payment payment = paymentOpt.get();

        if (payment.isSuccessful()) {
            log.debug("Confirm no-op; txRef={} already successful", command.getTxRef());
            return;
        }

        PaymentGateway.VerificationResult result = paymentGateway.verify(command.getTxRef());

        if (!result.success()) {
            payment.markFailed(result.rawPayload());
            paymentRepository.save(payment);
            notifier.notify(
                    payment.getPharmacyId(),
                    PaymentNotifications.PHARMACY,
                    PaymentNotifications.PAYMENT_FAILED,
                    "Payment not completed",
                    "We could not confirm your subscription payment. Please try again.",
                    PaymentNotifications.LINK_SUBSCRIPTION);
            log.info("Payment failed/unverified: txRef={}", command.getTxRef());
            return;
        }

        payment.markSuccess(result.rawPayload());
        paymentRepository.save(payment);

        Subscription subscription = subscriptionRepository.findById(payment.getSubscriptionId())
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription missing for payment " + payment.getTxRef()));

        activator.activatePaid(subscription, payment);
    }
}
