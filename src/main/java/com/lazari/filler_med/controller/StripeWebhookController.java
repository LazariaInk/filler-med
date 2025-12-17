package com.lazari.filler_med.controller;

import com.lazari.filler_med.service.OrderPaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    private final OrderPaymentService orderPaymentService;

    public StripeWebhookController(OrderPaymentService orderPaymentService) {
        this.orderPaymentService = orderPaymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(HttpServletRequest request) throws Exception {
        String payload = request.getReader().lines().collect(Collectors.joining("\n"));
        String sigHeader = request.getHeader("Stripe-Signature");

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) orderPaymentService.markPaidByPaymentIntentId(pi.getId());
            }
            case "payment_intent.payment_failed" -> {
                PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (pi != null) orderPaymentService.markFailedByPaymentIntentId(pi.getId());
            }
            default -> { /* ignore */ }
        }

        return ResponseEntity.ok("ok");
    }
}
