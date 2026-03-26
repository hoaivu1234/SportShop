package com.sport.ecommerce.modules.review.service;

import com.sport.ecommerce.modules.review.dto.request.CreateReviewRequest;
import com.sport.ecommerce.modules.review.dto.request.UpdateReviewRequest;
import com.sport.ecommerce.modules.review.dto.response.ReviewResponse;
import com.sport.ecommerce.modules.review.dto.response.ReviewSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    /** Creates a new review. The authenticated user may review each product only once. */
    ReviewResponse createReview(CreateReviewRequest request);

    /** Updates the rating and/or comment of a review. Only the review owner may update. */
    ReviewResponse updateReview(Long id, UpdateReviewRequest request);

    /**
     * Deletes a review. Both the review owner and ROLE_ADMIN users are allowed.
     */
    void deleteReview(Long id);

    /** Returns paginated reviews for a product, newest first. */
    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    /** Returns the aggregate rating summary (average, count, distribution) for a product. */
    ReviewSummaryResponse getProductReviewSummary(Long productId);

    /** Returns paginated reviews written by the currently authenticated user. */
    Page<ReviewResponse> getMyReviews(Pageable pageable);
}
