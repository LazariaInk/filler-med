package com.lazari.filler_med.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private String title;
    private String slug;
    private String imageUrl;

    private int unitPrice;
    private int quantity;

    public int getLineTotal() {
        return unitPrice * quantity;
    }
}
