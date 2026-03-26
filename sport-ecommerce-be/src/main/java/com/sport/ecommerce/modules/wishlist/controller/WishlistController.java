package com.sport.ecommerce.modules.wishlist.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.wishlist.dto.request.AddToWishlistRequest;
import com.sport.ecommerce.modules.wishlist.dto.response.WishlistItemResponse;
import com.sport.ecommerce.modules.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponse>>> getMyWishlist() {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getMyWishlist()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addItem(
            @Valid @RequestBody AddToWishlistRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(wishlistService.addItem(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@PathVariable Long id) {
        wishlistService.removeItem(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearWishlist() {
        wishlistService.clearWishlist();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
