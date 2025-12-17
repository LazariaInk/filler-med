package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Order order;

    private Long productId;
    private String title;
    private String slug;
    private String imageUrl;

    private Integer unitPriceRon;
    private Integer quantity;

    public Integer getLineTotalRon() {
        return unitPriceRon * quantity;
    }
}
