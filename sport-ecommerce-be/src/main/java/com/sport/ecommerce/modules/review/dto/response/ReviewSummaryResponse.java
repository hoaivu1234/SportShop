package com.sport.ecommerce.modules.review.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ReviewSummaryResponse {
    private double averageRating;
    private long totalReviews;
    /** Key = star count (1–5), value = number of reviews with that rating. */
    private Map<Integer, Long> ratingDistribution;
}
