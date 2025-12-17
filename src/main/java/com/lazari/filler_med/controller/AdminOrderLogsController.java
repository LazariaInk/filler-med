package com.lazari.filler_med.controller;

import com.lazari.filler_med.model.*;
import com.lazari.filler_med.repostiory.OrderRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderLogsController {

    private final OrderRepository orderRepository;

    public AdminOrderLogsController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/logs")
    public String logs(@RequestParam(required = false) String q,
                       @RequestParam(required = false) OrderStatus status,
                       @RequestParam(required = false) PaymentMethod paymentMethod,
                       @RequestParam(required = false) PaymentStatus paymentStatus,
                       @RequestParam(required = false) Integer minTotal,
                       @RequestParam(required = false) Integer maxTotal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "25") int size,
                       Model model) {

        Specification<Order> spec = OrderSpecs.query(q)
                .and(OrderSpecs.statusEq(status))
                .and(OrderSpecs.paymentMethodEq(paymentMethod))
                .and(OrderSpecs.paymentStatusEq(paymentStatus))
                .and(OrderSpecs.totalGte(minTotal))
                .and(OrderSpecs.totalLte(maxTotal));

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> ordersPage = orderRepository.findAll(spec, pageable);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("allPaymentMethods", PaymentMethod.values());
        model.addAttribute("allPaymentStatuses", PaymentStatus.values());

        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("size", size);

        return "admin/orders-logs";
    }
}
