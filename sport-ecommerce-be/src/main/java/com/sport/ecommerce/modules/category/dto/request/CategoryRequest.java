package com.sport.ecommerce.modules.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(max = 200, message = "Category name must not exceed 200 characters")
    private String name;

    private Long parentId;
}
