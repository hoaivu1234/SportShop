package com.sport.ecommerce.modules.category.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.category.dto.request.CategoryRequest;
import com.sport.ecommerce.modules.category.dto.response.CategoryResponse;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.mapper.CategoryMapper;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategories(Pageable pageable) {
        return PageResponse.of(
                categoryRepository.findAll(pageable).map(categoryMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategoriesWithChildren() {
        return categoryMapper.toResponseList(categoryRepository.findAllRootCategoriesWithChildren());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryById(id);
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Category not found with slug: " + slug));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);
        category.setSlug(generateUniqueSlug(request.getName(), null));

        if (request.getParentId() != null) {
            category.setParent(findCategoryById(request.getParentId()));
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);

        String newSlug = generateUniqueSlug(request.getName(), id);
        categoryMapper.updateEntity(request, category);
        category.setSlug(newSlug);

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Category cannot be its own parent");
            }
            category.setParent(findCategoryById(request.getParentId()));
        } else {
            category.setParent(null);
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        if (!category.getChildren().isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT.value(), "Cannot delete category with subcategories");
        }
        categoryRepository.delete(category);
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Category not found with id: " + id));
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = base;
        int counter = 1;
        while (excludeId == null ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
