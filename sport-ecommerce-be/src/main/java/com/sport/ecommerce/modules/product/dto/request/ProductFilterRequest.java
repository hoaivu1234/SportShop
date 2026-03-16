package com.sport.ecommerce.modules.product.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {

    private String keyword;
    private Long categoryId;
    private String brand;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Pagination
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";
}
