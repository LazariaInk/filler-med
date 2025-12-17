package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import com.lazari.filler_med.model.PaymentStatus;
import com.lazari.filler_med.model.Product;
import com.lazari.filler_med.repostiory.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderPaymentService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    public void markPaidByPaymentIntentId(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Order not found for PI: " + paymentIntentId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) return;

        order.getItems().forEach(oi -> {
            Product p = productService.findById(oi.getProductId());
            int stock = (p.getStock() == null) ? 0 : p.getStock();
            int newStock = stock - oi.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Stock negative for product " + p.getId());
            }
            p.setStock(newStock);
            productService.save(p);
        });

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(Instant.now());
        order.setStatus(OrderStatus.PLACED);
        orderRepository.save(order);
    }

    @Transactional
    public void markFailedByPaymentIntentId(String paymentIntentId) {
        Order order = orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Order not found for PI: " + paymentIntentId));
        if (order.getPaymentStatus() == PaymentStatus.PAID) return;

        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
    }
}
