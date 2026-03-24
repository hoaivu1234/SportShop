package com.sport.ecommerce.modules.product.repository;

import com.sport.ecommerce.modules.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            LEFT JOIN FETCH p.category
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Product p WHERE p.slug = :slug AND p.id <> :excludeId
            """)
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("excludeId") Long excludeId);

    // ── Soft-delete aware lookups ─────────────────────────────────────────────

    /** Used by service for all single-product lookups — excludes soft-deleted rows */
    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    /** Used by slug-based storefront lookups — excludes soft-deleted rows */
    Optional<Product> findBySlugAndIsDeletedFalse(String slug);

    /** Slug uniqueness check on CREATE — allows reuse of slugs from deleted products */
    boolean existsBySlugAndIsDeletedFalse(String slug);

    /** Slug uniqueness check on UPDATE — excludes self and deleted products */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM Product p WHERE p.slug = :slug AND p.id <> :excludeId AND p.isDeleted = false
            """)
    boolean existsBySlugAndIdNotAndIsDeletedFalse(@Param("slug") String slug, @Param("excludeId") Long excludeId);
}
