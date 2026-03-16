package com.sport.ecommerce.modules.category.service;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.category.dto.response.CategoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    PageResponse<CategoryResponse> getCategories(Pageable pageable);

    List<CategoryResponse> getRootCategoriesWithChildren();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryBySlug(String slug);
}
