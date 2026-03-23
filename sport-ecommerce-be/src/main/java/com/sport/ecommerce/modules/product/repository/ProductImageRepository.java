package com.sport.ecommerce.modules.product.repository;

import com.sport.ecommerce.modules.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    Optional<ProductImage> findByProductIdAndIsMainTrue(Long productId);

    /** Single query to get all main images for a set of products — replaces N per-product queries */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :ids AND pi.isMain = true")
    List<ProductImage> findMainImagesByProductIds(@Param("ids") List<Long> ids);

    @Transactional
    void deleteByProductId(Long productId);
}
