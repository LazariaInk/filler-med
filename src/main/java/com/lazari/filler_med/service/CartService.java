package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Cart;
import com.lazari.filler_med.model.CartItem;
import com.lazari.filler_med.model.Product;
import com.lazari.filler_med.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

@Service
@SessionScope
public class CartService {

    private final ProductService productService;
    private final Cart cart = new Cart();

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public Cart getCart() {
        return cart;
    }

    public void add(Long productId, int qty) {
        int quantity = Math.max(qty, 1);

        Product p = productService.findById(productId);
        if (Boolean.FALSE.equals(p.getActive())) return;

        int unitPrice = (p.getDiscountedPrice() != null) ? p.getDiscountedPrice() : p.getPrice();
        String img = (p.getPrimaryImage() != null) ? p.getPrimaryImage().getUrl() : "/images/logo1.png";

        cart.getItems().merge(productId,
                new CartItem(p.getId(), p.getTitle(), p.getSlug(), img, unitPrice, quantity),
                (oldItem, newItem) -> {
                    oldItem.setQuantity(oldItem.getQuantity() + quantity);
                    // dacă s-a schimbat prețul în timp, îl actualizăm
                    oldItem.setUnitPrice(unitPrice);
                    oldItem.setImageUrl(img);
                    oldItem.setTitle(p.getTitle());
                    oldItem.setSlug(p.getSlug());
                    return oldItem;
                }
        );
    }

    public void setQuantity(Long productId, int qty) {
        if (qty <= 0) {
            cart.getItems().remove(productId);
            return;
        }
        CartItem item = cart.getItems().get(productId);
        if (item != null) item.setQuantity(qty);
    }

    public void remove(Long productId) {
        cart.getItems().remove(productId);
    }

    public void clear() {
        cart.getItems().clear();
    }
}
