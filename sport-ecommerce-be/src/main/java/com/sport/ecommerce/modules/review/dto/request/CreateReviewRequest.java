package com.sport.ecommerce.modules.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @Size(min = 3, max = 2000, message = "Comment must be between 3 and 2000 characters")
    private String comment;
}
