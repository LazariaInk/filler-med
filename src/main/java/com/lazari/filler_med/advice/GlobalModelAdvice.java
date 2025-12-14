package com.lazari.filler_med.advice;

import com.lazari.filler_med.service.CartService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final CartService cartService;

    public GlobalModelAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Object cart() {
        return cartService.getCart();
    }
}
