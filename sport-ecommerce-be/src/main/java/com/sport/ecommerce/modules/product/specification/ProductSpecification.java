package com.sport.ecommerce.modules.product.specification;

import com.sport.ecommerce.modules.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    /**
     * Filter by keyword: searches name, description, brand
     */
    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("brand")), pattern)
            );
        };
    }

    public static Specification<Product> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return cb.conjunction();
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> {
            if (brand == null || brand.isBlank()) return cb.conjunction();
            return cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
        };
    }

    public static Specification<Product> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return cb.conjunction();
            return cb.equal(root.get("status"), status.toUpperCase());
        };
    }

    public static Specification<Product> hasPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Always excludes soft-deleted products from every list query */
    public static Specification<Product> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    /**
     * Builds a combined Specification from all filter criteria.
     * Soft-deleted products are always excluded.
     */
    public static Specification<Product> withFilters(
            String keyword,
            Long categoryId,
            String brand,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return Specification
                .where(notDeleted())
                .and(hasKeyword(keyword))
                .and(hasCategoryId(categoryId))
                .and(hasBrand(brand))
                .and(hasStatus(status))
                .and(hasPriceBetween(minPrice, maxPrice));
    }
}
