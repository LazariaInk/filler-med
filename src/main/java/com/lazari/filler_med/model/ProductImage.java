package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    private String url;
    private Integer sortOrder = 0;
    private Boolean primaryImage = false;
    private String altText;
}
