package com.lazari.filler_med.model;

import com.lazari.filler_med.model.Order;
import com.lazari.filler_med.model.OrderStatus;
import com.lazari.filler_med.model.PaymentMethod;
import com.lazari.filler_med.model.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecs {

    public static Specification<Order> statusEq(OrderStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> paymentMethodEq(PaymentMethod pm) {
        return (root, q, cb) -> pm == null ? cb.conjunction() : cb.equal(root.get("paymentMethod"), pm);
    }

    public static Specification<Order> paymentStatusEq(PaymentStatus ps) {
        return (root, q, cb) -> ps == null ? cb.conjunction() : cb.equal(root.get("paymentStatus"), ps);
    }

    public static Specification<Order> totalGte(Integer min) {
        return (root, q, cb) -> min == null ? cb.conjunction() : cb.ge(root.get("totalAmountRon"), min);
    }

    public static Specification<Order> totalLte(Integer max) {
        return (root, q, cb) -> max == null ? cb.conjunction() : cb.le(root.get("totalAmountRon"), max);
    }

    // search simplu în: id, email, nume, telefon, stripe PI
    public static Specification<Order> query(String qStr) {
        return (root, q, cb) -> {
            if (qStr == null || qStr.isBlank()) return cb.conjunction();
            String like = "%" + qStr.trim().toLowerCase() + "%";

            // dacă e număr, îl căutăm și ca ID
            try {
                long id = Long.parseLong(qStr.trim());
                return cb.or(
                        cb.equal(root.get("id"), id),
                        cb.like(cb.lower(root.get("customerEmail")), like),
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like),
                        cb.like(cb.lower(root.get("stripePaymentIntentId")), like)
                );
            } catch (NumberFormatException ignore) {
                return cb.or(
                        cb.like(cb.lower(root.get("customerEmail")), like),
                        cb.like(cb.lower(root.get("customerName")), like),
                        cb.like(cb.lower(root.get("customerPhone")), like),
                        cb.like(cb.lower(root.get("stripePaymentIntentId")), like)
                );
            }
        };
    }
}
