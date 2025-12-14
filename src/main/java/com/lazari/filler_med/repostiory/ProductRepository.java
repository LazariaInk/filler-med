package com.lazari.filler_med.repostiory;

import com.lazari.filler_med.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByTitleContainingIgnoreCase(String title);
    Optional<Product> findBySlug(String slug);
    List<Product> findByActiveTrueOrderByIdDesc();
    List<Product> findByActiveTrueAndTitleContainingIgnoreCase(String q);
    List<Product> findTop8ByActiveTrueOrderByIdDesc();
    List<Product> findTop12ByActiveTrueAndDiscountedPriceNotNullOrderByIdDesc();

    @Query(value = "SELECT * FROM product p WHERE p.active = b'1' AND p.discounted_price IS NOT NULL ORDER BY p.id DESC LIMIT :limit", nativeQuery = true)
    List<Product> findTopByActiveAndDiscountedPriceNotNull(@Param("limit") int limit);

    @Query(value = "SELECT * FROM product p WHERE p.active = b'1' ORDER BY p.id DESC LIMIT :limit", nativeQuery = true)
    List<Product> findTopByActiveOrderByIdDesc(@Param("limit") int limit);

    // dropdown filtre (doar active)
    @Query("select distinct p.category from Product p " +
            "where p.active = true and p.category is not null and p.category <> '' " +
            "order by p.category")
    List<String> findDistinctActiveCategories();

    @Query("select distinct p.brand from Product p " +
            "where p.active = true and p.brand is not null and p.brand <> '' " +
            "order by p.brand")
    List<String> findDistinctActiveBrands();
}
