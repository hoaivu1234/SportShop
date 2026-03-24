package com.sport.ecommerce.modules.cart.service;

import com.sport.ecommerce.modules.cart.dto.request.AddToCartRequest;
import com.sport.ecommerce.modules.cart.dto.request.UpdateCartItemRequest;
import com.sport.ecommerce.modules.cart.dto.response.CartResponse;

public interface CartService {

    /** Returns the current user's cart, creating one if it doesn't exist yet. */
    CartResponse getCart();

    /**
     * Adds a variant to the cart.
     * If the variant is already in the cart the quantities are merged.
     * Throws if stock is insufficient.
     */
    CartResponse addItem(AddToCartRequest request);

    /**
     * Replaces the quantity of an existing cart item.
     * Throws if the new quantity exceeds available stock.
     */
    CartResponse updateItem(Long itemId, UpdateCartItemRequest request);

    /** Removes a single item from the cart. */
    void removeItem(Long itemId);
}
