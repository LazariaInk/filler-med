package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Integer totalAmountRon;     // total în RON (int)
    private String currency;            // "ron"

    private String stripePaymentIntentId;

    // Date livrare
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    private Instant createdAt;
    private Instant paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (currency == null) currency = "ron";
    }
}
