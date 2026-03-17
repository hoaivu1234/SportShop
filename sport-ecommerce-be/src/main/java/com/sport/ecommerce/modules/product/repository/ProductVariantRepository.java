package com.sport.ecommerce.modules.product.repository;

import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("SELECT CASE WHEN COUNT(pv) > 0 THEN true ELSE false END FROM ProductVariant pv WHERE pv.sku = :sku AND pv.id <> :excludeId")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("excludeId") Long excludeId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.stock > 0")
    List<ProductVariant> findAvailableByProductId(@Param("productId") Long productId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.color = :color")
    List<ProductVariant> findByProductIdAndColor(@Param("productId") Long productId, @Param("color") String color);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.size = :size")
    List<ProductVariant> findByProductIdAndSize(@Param("productId") Long productId, @Param("size") String size);
}
