package com.lazari.filler_med.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String slug;
    private Integer price;
    private Integer discountedPrice;
    private Integer stock;
    private Boolean active;
    private String category;
    private String brand;
    private String volume;
    private String indication;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private java.util.List<ProductImage> images = new java.util.ArrayList<>();
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String shortDescription;
    public ProductImage getPrimaryImage() {
        if (images == null) return null;
        return images.stream().filter(i -> Boolean.TRUE.equals(i.getPrimaryImage()))
                .findFirst().orElse(images.isEmpty() ? null : images.get(0));
    }
}
