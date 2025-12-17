package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import com.lazari.filler_med.repostiory.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminBadgeService {

    private final OrderRepository orderRepository;

    // ce considerăm “închis”
    private static final Set<OrderStatus> CLOSED = EnumSet.of(
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELED
    );

    public AdminBadgeService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public long openOrdersCount() {
        return orderRepository.countByStatusNotIn(CLOSED);
    }

    public List<Order> openOrders() {
        return orderRepository.findTop200ByStatusNotInOrderByCreatedAtDesc(CLOSED);
    }
}
