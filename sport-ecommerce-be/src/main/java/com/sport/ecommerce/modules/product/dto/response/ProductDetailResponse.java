package com.sport.ecommerce.modules.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sport.ecommerce.modules.category.dto.response.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String status;

    private CategoryResponse category;
    private List<ImageResponse> images;
    private List<VariantResponse> variants;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
