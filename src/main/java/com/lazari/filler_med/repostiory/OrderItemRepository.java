package com.lazari.filler_med.repostiory;


import com.lazari.filler_med.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
