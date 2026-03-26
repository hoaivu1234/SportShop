package com.sport.ecommerce.modules.review.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.repository.ProductRepository;
import com.sport.ecommerce.modules.review.dto.request.CreateReviewRequest;
import com.sport.ecommerce.modules.review.dto.request.UpdateReviewRequest;
import com.sport.ecommerce.modules.review.dto.response.ReviewResponse;
import com.sport.ecommerce.modules.review.dto.response.ReviewSummaryResponse;
import com.sport.ecommerce.modules.review.entity.Review;
import com.sport.ecommerce.modules.review.repository.ReviewRepository;
import com.sport.ecommerce.modules.review.service.ReviewService;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    // ── Public query operations ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        requireProductExists(productId);
        return reviewRepository.findByProductIdWithUser(productId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getProductReviewSummary(Long productId) {
        requireProductExists(productId);

        long total = reviewRepository.countByProductId(productId);
        double avg = total > 0
                ? roundHalf(reviewRepository.findAverageRatingByProductId(productId))
                : 0.0;

        // Build distribution — all 5 slots pre-filled with zero
        Map<Integer, Long> dist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) dist.put(i, 0L);
        List<Object[]> rows = reviewRepository.findRatingDistributionByProductId(productId);
        for (Object[] row : rows) {
            dist.put((Integer) row[0], (Long) row[1]);
        }

        return ReviewSummaryResponse.builder()
                .averageRating(avg)
                .totalReviews(total)
                .ratingDistribution(dist)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Pageable pageable) {
        Long userId = getCurrentUser().getId();
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    // ── Mutation operations ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        User user    = getCurrentUser();
        Product product = requireProductExists(request.getProductId());

        if (reviewRepository.existsByProductIdAndUserId(product.getId(), user.getId())) {
            throw new BusinessException(
                    HttpStatus.CONFLICT.value(),
                    "You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review {} created by user {} for product {}",
                saved.getId(), user.getId(), product.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, UpdateReviewRequest request) {
        User user   = getCurrentUser();
        Review review = requireReviewExists(id);
        requireOwner(review, user);

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        log.info("Review {} updated by user {}", saved.getId(), user.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        User user   = getCurrentUser();
        Review review = requireReviewExists(id);

        if (!isAdmin() && !review.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN.value(),
                    "You do not have permission to delete this review");
        }

        reviewRepository.delete(review);
        log.info("Review {} deleted by user {}", id, user.getId());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User getCurrentUser() {
        CustomUserDetails details = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return details.getUser();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Product requireProductExists(Long productId) {
        return productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(),
                        "Product not found: " + productId));
    }

    private Review requireReviewExists(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND.value(),
                        "Review not found: " + id));
    }

    private void requireOwner(Review review, User user) {
        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN.value(),
                    "You do not have permission to modify this review");
        }
    }

    private ReviewResponse toResponse(Review review) {
        String firstName = review.getUser().getFirstName();
        String lastName  = review.getUser().getLastName();
        String fullName  = ((firstName != null ? firstName : "") + " " +
                            (lastName  != null ? lastName  : "")).trim();
        if (fullName.isEmpty()) {
            fullName = "User #" + review.getUser().getId();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUser().getId())
                .userFullName(fullName)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /** Rounds to the nearest 0.5 (e.g. 4.37 → 4.5, 4.24 → 4.0). */
    private double roundHalf(double value) {
        return Math.round(value * 2.0) / 2.0;
    }
}
