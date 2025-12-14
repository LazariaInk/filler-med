package com.lazari.filler_med.model;

import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Cart {

    private final Map<Long, CartItem> items = new LinkedHashMap<>();

    public Collection<CartItem> getItemsList() {
        return items.values();
    }

    public int getTotalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public int getTotalAmount() {
        return items.values().stream().mapToInt(CartItem::getLineTotal).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
