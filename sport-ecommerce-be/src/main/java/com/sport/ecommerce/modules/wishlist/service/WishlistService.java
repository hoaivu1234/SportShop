package com.sport.ecommerce.modules.wishlist.service;

import com.sport.ecommerce.modules.wishlist.dto.request.AddToWishlistRequest;
import com.sport.ecommerce.modules.wishlist.dto.response.WishlistItemResponse;

import java.util.List;

public interface WishlistService {

    List<WishlistItemResponse> getMyWishlist();

    WishlistItemResponse addItem(AddToWishlistRequest request);

    void removeItem(Long id);

    void clearWishlist();
}
