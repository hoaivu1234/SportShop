package com.sport.ecommerce.modules.wishlist.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToWishlistRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Optional — records which variant the user had selected. */
    private Long variantId;
}
