package com.lazari.filler_med.controller;

import com.lazari.filler_med.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cos")
    public String cartPage(Model model) {
        model.addAttribute("cart", cartService.getCart());
        return "public/cart";
    }

    @PostMapping("/cos/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int qty,
                            HttpServletRequest request) {
        cartService.add(productId, qty);

        String ref = request.getHeader("Referer");
        return "redirect:" + (ref != null ? ref : "/cos");
    }

    @PostMapping("/cos/update")
    public String updateQty(@RequestParam Long productId,
                            @RequestParam int qty) {
        cartService.setQuantity(productId, qty);
        return "redirect:/cos";
    }

    @PostMapping("/cos/remove")
    public String remove(@RequestParam Long productId) {
        cartService.remove(productId);
        return "redirect:/cos";
    }

    @PostMapping("/cos/clear")
    public String clear() {
        cartService.clear();
        return "redirect:/cos";
    }
}
