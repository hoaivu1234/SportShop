package com.sport.ecommerce.modules.review.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.review.dto.response.ReviewResponse;
import com.sport.ecommerce.modules.review.dto.response.ReviewSummaryResponse;
import com.sport.ecommerce.modules.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.PUBLIC_PREFIX + "/products/{productId}/reviews")
@RequiredArgsConstructor
public class PublicReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getProductReviews(productId, pageable)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getProductReviewSummary(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getProductReviewSummary(productId)));
    }
}
