package com.lazari.filler_med.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public PaymentIntent createPaymentIntent(long amountInCents, String currency, Long orderId) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();

            return PaymentIntent.create(params);
        } catch (Exception e) {
            throw new RuntimeException("Stripe create PaymentIntent failed: " + e.getMessage(), e);
        }
    }
}
