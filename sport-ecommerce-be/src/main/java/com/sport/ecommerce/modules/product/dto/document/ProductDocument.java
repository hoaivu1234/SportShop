package com.sport.ecommerce.modules.product.dto.document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "products")
@Builder
@Data
public class ProductDocument {

    private Long id;

    private String name;
    private String slug;
    private String brand;

    private Long categoryId;
    private String categoryName;

    private BigDecimal price;
    private BigDecimal discountPrice;

    private Boolean onSale;
    private String status;

    private String mainImageUrl;

    private Integer totalStock;

    private LocalDateTime createdAt;
}
