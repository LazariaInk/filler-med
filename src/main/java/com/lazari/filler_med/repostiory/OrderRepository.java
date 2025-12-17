package com.lazari.filler_med.repostiory;

import com.lazari.filler_med.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByStripePaymentIntentId(String paymentIntentId);
}
