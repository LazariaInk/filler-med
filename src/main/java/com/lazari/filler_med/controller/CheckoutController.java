package com.lazari.filler_med.controller;

import com.lazari.filler_med.model.PaymentMethod;
import com.lazari.filler_med.service.OrderCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckoutController {

    private final OrderCheckoutService checkoutService;

    @Value("${stripe.publishableKey}")
    private String stripePublishableKey;
    public CheckoutController(OrderCheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model) {
        return "public/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@RequestParam String customerName,
                             @RequestParam String customerEmail,
                             @RequestParam String customerPhone,
                             @RequestParam String shippingAddress,
                             @RequestParam String paymentMethod,
                             Model model) {

        PaymentMethod pm = "COD".equalsIgnoreCase(paymentMethod) ? PaymentMethod.COD : PaymentMethod.STRIPE;

        var result = checkoutService.createOrderAndStartPayment(
                new OrderCheckoutService.CheckoutInput(
                        customerName, customerEmail, customerPhone, shippingAddress, pm
                )
        );

        if (pm == PaymentMethod.COD) {
            return "redirect:/order/thank-you?orderId=" + result.orderId();
        }
        model.addAttribute("stripePk", stripePublishableKey);
        model.addAttribute("orderId", result.orderId());
        model.addAttribute("clientSecret", result.stripeClientSecret());
        return "public/stripe-pay";
    }

    @GetMapping("/order/thank-you")
    public String thankYou(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "public/thank-you";
    }
}
