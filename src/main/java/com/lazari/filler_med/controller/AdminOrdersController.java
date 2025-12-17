package com.lazari.filler_med.controller;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import com.lazari.filler_med.repostiory.OrderRepository;
import com.lazari.filler_med.service.AdminBadgeService;
import com.lazari.filler_med.service.AdminOrderQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminOrdersController {

    private final AdminOrderQueueService queue;
    private final AdminBadgeService badgeService;
    private final OrderRepository orderRepository;

    public AdminOrdersController(AdminOrderQueueService queue, OrderRepository orderRepository, AdminBadgeService badgeService) {
        this.queue = queue;
        this.orderRepository = orderRepository;
        this.badgeService = badgeService;
    }

    // inbox: comenzi de procesat = PLACED
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", badgeService.openOrders());
        return "admin/orders";
    }

    // pentru polling badge
    @GetMapping("/orders/to-fulfill-count")
    @ResponseBody
    public Map<String, Long> toFulfillCount() {
        return Map.of("count", queue.getToFulfillCount());
    }

    // buton: marchează expediată
    @PostMapping("/orders/{id}/ship")
    public String markShipped(@PathVariable Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(o);
        return "redirect:/admin/orders?shipped";
    }

    // optional: anulare rapidă
    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        Order o = orderRepository.findById(id).orElseThrow();
        o.setStatus(OrderStatus.CANCELED);
        orderRepository.save(o);
        return "redirect:/admin/orders?canceled";
    }
}
