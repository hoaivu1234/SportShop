package com.sport.ecommerce.modules.review.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.review.dto.request.CreateReviewRequest;
import com.sport.ecommerce.modules.review.dto.request.UpdateReviewRequest;
import com.sport.ecommerce.modules.review.dto.response.ReviewResponse;
import com.sport.ecommerce.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.updateReview(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.noContent());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReviewResponse> result = reviewService.getMyReviews(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
