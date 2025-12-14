package com.lazari.filler_med.controller;

import com.lazari.filler_med.model.AboutSection;
import com.lazari.filler_med.model.Product;
import com.lazari.filler_med.model.SiteSettings;
import com.lazari.filler_med.service.ProductService;
import com.lazari.filler_med.service.SiteSettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping
public class SiteSettingsController {

    private final SiteSettingsService service;
    private final ProductService productService;

    public SiteSettingsController(SiteSettingsService service,ProductService productService) {
        this.service = service;
        this.productService = productService;
    }

    @GetMapping("/admin/site-settings")
    public String editSettings(Model model) {
        SiteSettings settings = service.getSettings();

        if (settings.getAboutSections().isEmpty()) {
            settings.getAboutSections().add(new AboutSection());
        }

        model.addAttribute("settings", settings);
        return "admin/site-settings";
    }

    @PostMapping("/admin/site-settings")
    public String saveSettings(@ModelAttribute("settings") SiteSettings settings) {
        settings.getAboutSections().removeIf(sec ->
                (sec.getSubtitle() == null || sec.getSubtitle().isBlank()) &&
                        (sec.getContent() == null || sec.getContent().isBlank())
        );

        service.saveSettings(settings);
        return "redirect:/admin/site-settings?success";
    }

    @GetMapping("/admin/products")
    public String listProducts(@RequestParam(value = "q", required = false) String query,
                               Model model) {
        List<Product> products = productService.search(query); // sau findAll()
        model.addAttribute("products", products);
        model.addAttribute("product", new Product()); // pentru formular "Adaugă"
        return "admin/products";
    }

    @GetMapping("/admin/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        List<Product> products = productService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("product", product);
        return "admin/products";
    }

    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/products?deleted";
    }

    @PostMapping("/admin/products/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "productImages", required = false)
                              List<MultipartFile> images) {
        Product saved = productService.save(product);
        productService.addImages(saved.getId(), images);
        return "redirect:/admin/products?success";
    }


    @PostMapping("/admin/products/{productId}/images/{imageId}/delete")
    public String deleteProductImage(@PathVariable Long productId,
                                     @PathVariable Long imageId) {
        productService.deleteImage(productId, imageId);
        return "redirect:/admin/products/edit/" + productId + "?imgDeleted";
    }

    @PostMapping("/admin/products/{productId}/images/{imageId}/primary")
    public String setPrimaryProductImage(@PathVariable Long productId,
                                         @PathVariable Long imageId) {
        productService.setPrimaryImage(productId, imageId);
        return "redirect:/admin/products/edit/" + productId + "?imgPrimary";
    }

    @PostMapping("/admin/products/{productId}/images/{imageId}/alt")
    public String updateAltText(@PathVariable Long productId,
                                @PathVariable Long imageId,
                                @RequestParam("altText") String altText) {
        productService.updateImageAltText(productId, imageId, altText);
        return "redirect:/admin/products/edit/" + productId + "?imgAltSaved";
    }

    @PostMapping("/admin/products/{productId}/images/reorder")
    @ResponseBody
    public void reorderImages(@PathVariable Long productId,
                              @RequestBody List<Long> imageIdsInOrder) {
        productService.reorderImages(productId, imageIdsInOrder);
    }

}
