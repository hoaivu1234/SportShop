package com.sport.ecommerce.modules.cart.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {

    private Long id;

    // Variant info
    private Long variantId;
    private String sku;
    private String size;
    private String color;

    // Product info
    private Long productId;
    private String productName;
    private String brand;
    private String imageUrl;

    // Pricing & qty
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal subtotal;
}
