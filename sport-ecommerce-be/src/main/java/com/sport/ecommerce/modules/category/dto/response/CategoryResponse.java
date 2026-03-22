package com.sport.ecommerce.modules.category.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> children;
    private Long productCount;
    private LocalDateTime createdAt;

    public CategoryResponse(
            Long id,
            String name,
            String slug,
            Long parentId,
            String parentName,
            Long productCount,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.productCount = productCount;
        this.createdAt = createdAt;
        this.parentId = parentId;
        this.parentName = parentName;
    }
}
