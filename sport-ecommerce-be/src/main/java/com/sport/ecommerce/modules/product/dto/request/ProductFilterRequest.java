package com.sport.ecommerce.modules.product.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {

    private String keyword;
    private Long categoryId;
    /** Category resolved by slug (e.g. "football") — takes priority over categoryId when set. */
    private String categorySlug;
    private String brand;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    /** When true, only products with a discountPrice are returned. */
    private boolean onSale;

    // Pagination
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
