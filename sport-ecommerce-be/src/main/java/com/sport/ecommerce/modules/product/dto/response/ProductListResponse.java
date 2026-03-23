package com.sport.ecommerce.modules.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListResponse {

    private Long id;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String status;
    private String mainImageUrl;
    private Integer totalStock;

    // Category summary
    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
}
