package com.sport.ecommerce.modules.wishlist.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WishlistItemResponse {

    private Long id;
    private Long productId;
    private Long variantId;
    private String productName;
    private String productSlug;
    private String mainImageUrl;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private int totalStock;
    private LocalDateTime createdAt;
}
