package com.sport.ecommerce.modules.category.service;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.category.dto.request.CategoryRequest;
import com.sport.ecommerce.modules.category.dto.response.CategoryResponse;
import com.sport.ecommerce.modules.category.dto.response.CategoryTreeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    PageResponse<CategoryResponse> getCategories(Pageable pageable);

    List<CategoryTreeResponse> getCategoryTree();

    List<CategoryResponse> getAllFlat();

    CategoryResponse getCategoryById(Long id);

    PageResponse<CategoryResponse> getCategoriesExcludeChildren(Pageable pageable);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
