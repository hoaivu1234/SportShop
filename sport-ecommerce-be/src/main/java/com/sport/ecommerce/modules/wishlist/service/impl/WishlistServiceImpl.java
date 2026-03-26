package com.sport.ecommerce.modules.wishlist.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.repository.ProductImageRepository;
import com.sport.ecommerce.modules.product.repository.ProductRepository;
import com.sport.ecommerce.modules.product.repository.ProductVariantRepository;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.wishlist.dto.request.AddToWishlistRequest;
import com.sport.ecommerce.modules.wishlist.dto.response.WishlistItemResponse;
import com.sport.ecommerce.modules.wishlist.entity.WishlistItem;
import com.sport.ecommerce.modules.wishlist.repository.WishlistRepository;
import com.sport.ecommerce.modules.wishlist.service.WishlistService;
import com.sport.ecommerce.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository    wishlistRepository;
    private final ProductRepository     productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    // ── Query operations ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getMyWishlist() {
        Long userId = getCurrentUser().getId();
        List<WishlistItem> items = wishlistRepository.findByUserIdWithProduct(userId);

        if (items.isEmpty()) return List.of();

        List<Long> productIds = items.stream()
                .map(w -> w.getProduct().getId())
                .collect(Collectors.toList());

        // Batch-fetch main images (avoids N+1)
        Map<Long, String> imageMap = productImageRepository
                .findMainImagesByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImageUrl,
                        (a, b) -> a));

        // Batch-fetch total stock per product (avoids N+1)
        Map<Long, Integer> stockMap = productVariantRepository
                .sumStockByProductIds(productIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()));

        return items.stream()
                .map(w -> toResponse(w, imageMap, stockMap))
                .collect(Collectors.toList());
    }

    // ── Mutation operations ───────────────────────────────────────────────────

    @Override
    @Transactional
    public WishlistItemResponse addItem(AddToWishlistRequest request) {
        User user = getCurrentUser();

        Product product = productRepository.findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(),
                        "Product not found: " + request.getProductId()));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "Product is already in your wishlist");
        }

        WishlistItem saved = wishlistRepository.save(
                WishlistItem.builder()
                        .user(user)
                        .product(product)
                        .variantId(request.getVariantId())
                        .build());

        log.info("Wishlist item {} added: user={} product={}", saved.getId(), user.getId(), product.getId());

        // Build response without batch queries (single item)
        String imageUrl = productImageRepository
                .findByProductIdAndIsMainTrue(product.getId())
                .map(ProductImage::getImageUrl)
                .orElse(null);

        int totalStock = productVariantRepository
                .sumStockByProductIds(List.of(product.getId()))
                .stream()
                .findFirst()
                .map(row -> ((Long) row[1]).intValue())
                .orElse(0);

        return toResponse(saved, imageUrl, totalStock);
    }

    @Override
    @Transactional
    public void removeItem(Long id) {
        User user = getCurrentUser();
        WishlistItem item = wishlistRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(),
                        "Wishlist item not found: " + id));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN.value(),
                    "You do not have permission to remove this item");
        }

        wishlistRepository.delete(item);
        log.info("Wishlist item {} removed by user {}", id, user.getId());
    }

    @Override
    @Transactional
    public void clearWishlist() {
        Long userId = getCurrentUser().getId();
        wishlistRepository.deleteByUserId(userId);
        log.info("Wishlist cleared for user {}", userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getCurrentUser() {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getUser();
    }

    private WishlistItemResponse toResponse(
            WishlistItem w,
            Map<Long, String> imageMap,
            Map<Long, Integer> stockMap) {
        Long pid = w.getProduct().getId();
        return toResponse(w, imageMap.get(pid), stockMap.getOrDefault(pid, 0));
    }

    private WishlistItemResponse toResponse(WishlistItem w, String imageUrl, int totalStock) {
        Product p = w.getProduct();
        return WishlistItemResponse.builder()
                .id(w.getId())
                .productId(p.getId())
                .variantId(w.getVariantId())
                .productName(p.getName())
                .productSlug(p.getSlug())
                .mainImageUrl(imageUrl)
                .price(p.getPrice())
                .discountPrice(p.getDiscountPrice())
                .totalStock(totalStock)
                .createdAt(w.getCreatedAt())
                .build();
    }
}
