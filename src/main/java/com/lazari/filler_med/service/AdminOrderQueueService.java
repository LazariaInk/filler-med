package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import com.lazari.filler_med.repostiory.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class AdminOrderQueueService {

    private final OrderRepository orderRepository;

    public AdminOrderQueueService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public long getToFulfillCount() {
        return orderRepository.countByStatus(OrderStatus.PLACED);
    }

    public List<Order> getToFulfillOrders() {
        return orderRepository.findTop200ByStatusNotInOrderByCreatedAtDesc(List.of(OrderStatus.PLACED));
    }
}
