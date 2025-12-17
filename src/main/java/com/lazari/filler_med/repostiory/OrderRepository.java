package com.lazari.filler_med.repostiory;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByStripePaymentIntentId(String paymentIntentId);

    long countByStatusNotIn(Collection<OrderStatus> statuses);

    List<Order> findTop200ByStatusNotInOrderByCreatedAtDesc(Collection<OrderStatus> statuses);

    long countByStatus(OrderStatus orderStatus);
}
