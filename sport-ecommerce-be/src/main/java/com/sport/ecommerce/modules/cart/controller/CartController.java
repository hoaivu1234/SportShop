package com.sport.ecommerce.modules.cart.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.cart.dto.request.AddToCartRequest;
import com.sport.ecommerce.modules.cart.dto.request.UpdateCartItemRequest;
import com.sport.ecommerce.modules.cart.dto.response.CartResponse;
import com.sport.ecommerce.modules.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /** Returns the current user's cart (auto-created if it doesn't exist). */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart()));
    }

    /**
     * Adds a variant to the cart.
     * If the variant is already present its quantity is incremented.
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(cartService.addItem(request)));
    }

    /** Updates the quantity of an existing cart item. */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateItem(itemId, request)));
    }

    /** Removes a single item from the cart. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
