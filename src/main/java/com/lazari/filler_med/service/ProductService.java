package com.lazari.filler_med.service;

import com.lazari.filler_med.model.Product;
import com.lazari.filler_med.model.ProductImage;
import com.lazari.filler_med.repostiory.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    @Value("${app.products.max-images:10}")
    private int maxImagesPerProduct;

    @Value("${app.products.max-image-bytes:5242880}")
    private long maxImageBytes;

    private static final List<String> ALLOWED_MIME = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public ProductService(FileStorageService fileStorageService, ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> search(String query) {
        if (!StringUtils.hasText(query)) {
            return productRepository.findAll();
        }
        return productRepository.findByTitleContainingIgnoreCase(query.trim());
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
    }

    public Product findBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produsul nu a fost găsit"));
    }

    public Product save(Product product) {
        if (!StringUtils.hasText(product.getSlug())) {
            product.setSlug(generateSlug(product.getTitle()));
        }
        if (product.getActive() == null) {
            product.setActive(true);
        }

        if (product.getStock() == null) {
            product.setStock(0);
        }

        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    private String generateSlug(String input) {
        if (input == null) {
            return null;
        }

        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return slug;
    }

    public void addImages(Long productId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        List<MultipartFile> clean = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
        if (clean.isEmpty()) return;

        Product p = findById(productId);

        int existing = (p.getImages() == null) ? 0 : p.getImages().size();
        int incoming = clean.size();

        if (existing + incoming > maxImagesPerProduct) {
            throw new RuntimeException("Prea multe imagini. Max " + maxImagesPerProduct + " / produs.");
        }

        for (MultipartFile f : clean) {
            String ct = f.getContentType();
            if (ct == null || !ALLOWED_MIME.contains(ct)) {
                throw new RuntimeException("Tip fișier neacceptat: " + ct + ". Accept: JPG/PNG/WebP.");
            }
            if (f.getSize() > maxImageBytes) {
                throw new RuntimeException("Fișier prea mare. Max " + (maxImageBytes / 1024 / 1024) + "MB.");
            }
        }

        List<String> urls = fileStorageService.saveProductImages(productId, clean);

        int startOrder = p.getImages() == null ? 0 : p.getImages().size();
        for (int i = 0; i < urls.size(); i++) {
            ProductImage img = new ProductImage();
            img.setProduct(p);
            img.setUrl(urls.get(i));
            img.setSortOrder(startOrder + i);
            img.setAltText(p.getTitle());
            if (p.getPrimaryImage() == null && i == 0) {
                img.setPrimaryImage(true);
            }
            p.getImages().add(img);
        }
        productRepository.save(p);
    }

    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {
        Product product = findById(productId);
        for (ProductImage img : product.getImages()) {
            img.setPrimaryImage(img.getId().equals(imageId));
        }
        productRepository.save(product);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Product product = findById(productId);

        ProductImage toDelete = product.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Imaginea nu există"));
        boolean wasPrimary = Boolean.TRUE.equals(toDelete.getPrimaryImage());
        String url = toDelete.getUrl();
        product.getImages().remove(toDelete);
        productRepository.save(product);
        fileStorageService.deleteByMediaUrl(url);
        if (wasPrimary && !product.getImages().isEmpty()) {
            product.getImages().get(0).setPrimaryImage(true);
            productRepository.save(product);
        }
    }

    @Transactional
    public void updateImageAltText(Long productId, Long imageId, String altText) {
        Product product = findById(productId);
        ProductImage img = product.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Imaginea nu există"));
        String cleaned = (altText == null) ? "" : altText.trim();
        if (cleaned.length() > 160) cleaned = cleaned.substring(0, 160);
        img.setAltText(cleaned);
        productRepository.save(product);
    }

    @Transactional
    public void reorderImages(Long productId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        Product product = findById(productId);
        var map = product.getImages().stream()
                .collect(java.util.stream.Collectors.toMap(ProductImage::getId, x -> x));
        for (int i = 0; i < ids.size(); i++) {
            ProductImage img = map.get(ids.get(i));
            if (img != null) img.setSortOrder(i);
        }
        productRepository.save(product);
    }

    public List<Product> findOffers(int limit) {
        return productRepository.findTopByActiveAndDiscountedPriceNotNull(limit);
    }

    public List<Product> findNewest(int limit) {
        return productRepository.findTopByActiveOrderByIdDesc(limit);
    }

    public List<Product> searchPublic(String query) {
        if (!org.springframework.util.StringUtils.hasText(query)) {
            return productRepository.findByActiveTrueOrderByIdDesc();
        }
        return productRepository.findByActiveTrueAndTitleContainingIgnoreCase(query.trim());
    }

    public Page<Product> searchPublicPage(String q, String category, String brand, String sort, int page, int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 6), 60);

        Pageable pageable = PageRequest.of(safePage, safeSize); // fără sort aici

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (org.springframework.util.StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), like));
            }
            if (org.springframework.util.StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }
            if (org.springframework.util.StringUtils.hasText(brand)) {
                predicates.add(cb.equal(root.get("brand"), brand.trim()));
            }

            // IMPORTANT: doar dacă nu e count query
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                var effectivePrice = cb.coalesce(root.get("discountedPrice"), root.get("price"));

                if ("price_asc".equals(sort)) {
                    query.orderBy(cb.asc(effectivePrice), cb.desc(root.get("id")));
                } else if ("price_desc".equals(sort)) {
                    query.orderBy(cb.desc(effectivePrice), cb.desc(root.get("id")));
                } else {
                    query.orderBy(cb.desc(root.get("id")));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    public List<String> getActiveCategories() {
        return productRepository.findDistinctActiveCategories();
    }

    public List<String> getActiveBrands() {
        return productRepository.findDistinctActiveBrands();
    }

    private Sort mapSort(String sort) {
        String s = (sort == null) ? "" : sort.trim().toLowerCase();

        return switch (s) {
            case "price_asc" -> Sort.by(
                    Sort.Order.asc("discountedPrice").nullsLast(),
                    Sort.Order.asc("price").nullsLast(),
                    Sort.Order.desc("id")
            );
            case "price_desc" -> Sort.by(
                    Sort.Order.desc("discountedPrice").nullsLast(),
                    Sort.Order.desc("price").nullsLast(),
                    Sort.Order.desc("id")
            );
            case "discount" -> Sort.by(
                    Sort.Order.desc("discountedPrice").nullsLast(),
                    Sort.Order.desc("id")
            );
            default -> Sort.by(Sort.Order.desc("id")); // newest
        };
    }

}
