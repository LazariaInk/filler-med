package com.lazari.filler_med.controller;

import com.lazari.filler_med.model.Product;
import com.lazari.filler_med.model.SiteSettings;
import com.lazari.filler_med.service.ProductService;
import com.lazari.filler_med.service.SiteSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class PublicController {

    private final ProductService productService;
    private final SiteSettingsService siteSettingsService;

    public PublicController(ProductService productService, SiteSettingsService siteSettingsService) {
        this.productService = productService;
        this.siteSettingsService = siteSettingsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> offers = productService.findOffers(12);

        // produse noi: cele mai recente (după id desc) + active
        List<Product> newest = productService.findNewest(8);

        model.addAttribute("offers", offers);
        model.addAttribute("newest", newest);
        model.addAttribute("settings", siteSettingsService.getSettings());
        return "public/index";
    }

    @GetMapping("/produse")
    public String products(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "24") int size,
            Model model
    ) {
        var result = productService.searchPublicPage(q, category, brand, sort, page, size);

        model.addAttribute("page", result);                 // Page<Product>
        model.addAttribute("products", result.getContent()); // list

        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("brand", brand);
        model.addAttribute("sort", sort);
        model.addAttribute("size", size);

        model.addAttribute("categories", productService.getActiveCategories());
        model.addAttribute("brands", productService.getActiveBrands());

        model.addAttribute("settings", siteSettingsService.getSettings());
        return "public/products";
    }

    @GetMapping("/produse/{slug}")
    public String productPage(@PathVariable String slug, Model model, HttpServletRequest request) {
        Product product = productService.findBySlug(slug);
        if (product == null || Boolean.FALSE.equals(product.getActive())) {
            return "redirect:/produse?notfound";
        }
        String canonicalUrl = request.getRequestURL().toString();
        String waPhone = "407XXXXXXXX";
        String text = "Bună! Sunt interesat de: " + product.getTitle() + " - " + canonicalUrl;
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String waUrl = "https://wa.me/" + waPhone + "?text=" + encodedText;
        model.addAttribute("product", product);
        model.addAttribute("canonicalUrl", canonicalUrl);
        model.addAttribute("waUrl", waUrl);
        model.addAttribute("settings", siteSettingsService.getSettings());
        return "public/product";
    }

    @GetMapping("/despre-noi")
    public String despreNoi(Model model) {
        SiteSettings settings = siteSettingsService.getSettings();
        model.addAttribute("settings", settings);
        return "public/despre-noi";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        SiteSettings settings = siteSettingsService.getSettings();
        model.addAttribute("settings", settings);
        return "public/contact";
    }

    @GetMapping("/politica-gdpr")
    public String politicaGdpr(Model model) {
        return "public/politica-gdpr";
    }

    @GetMapping("/politica-retur")
    public String politicaRetur(Model model) {
        return "public/politica-retur";
    }

    @GetMapping("/termeni-si-conditii")
    public String termeniSiConditii(Model model) {
        return "public/termeni-si-conditii";
    }

}
