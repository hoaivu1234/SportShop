package com.sport.ecommerce.modules.category.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.config.cache.CacheNames;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.category.dto.request.CategoryRequest;
import com.sport.ecommerce.modules.category.dto.response.CategoryResponse;
import com.sport.ecommerce.modules.category.dto.response.CategoryTreeResponse;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.mapper.CategoryMapper;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategories(Pageable pageable) {
        return PageResponse.of(
                categoryRepository.findAll(pageable).map(categoryMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_TREE)
    public List<CategoryTreeResponse> getCategoryTree() {
        return buildTree(categoryRepository.findAllFlat());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_FLAT)
    public List<CategoryResponse> getAllFlat() {
        return categoryRepository.findAllFlat();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategoriesExcludeChildren(Pageable pageable) {
        return PageResponse.of(categoryRepository.findAllCategoriesExcludeChildren(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_BY_ID, key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        return categoryMapper.toResponse(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_BY_SLUG, key = "#slug")
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Category not found with slug: " + slug));
        return categoryMapper.toResponse(category);
    }

    // ── Hierarchy-aware accessors ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_LEVEL, key = "'level1'")
    public List<CategoryResponse> getLevel1Categories() {
        return categoryMapper.toResponseList(categoryRepository.findAllLevel1());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_LEVEL, key = "'level2'")
    public List<CategoryResponse> getLevel2Categories() {
        return categoryMapper.toResponseList(categoryRepository.findAllLevel2());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheNames.CATEGORY_LEVEL, key = "'level3'")
    public List<CategoryResponse> getLevel3Categories() {
        return categoryMapper.toResponseList(categoryRepository.findAllLevel3());
    }

    /**
     * Validates that {@code categoryId} refers to a leaf (level-3) category.
     * Throws {@link BusinessException} with HTTP 400 if it is not.
     */
    @Override
    @Transactional(readOnly = true)
    public void validateLeafCategory(Long categoryId) {
        Category cat = findCategoryById(categoryId);
        int level = resolveLevel(cat);
        if (level != 3) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "\"" + cat.getName() + "\" is a " + levelLabel(level) + " category. "
                    + "Products must be assigned to a leaf (level-3) category.");
        }
    }

    // ── Mutate ────────────────────────────────────────────────────────────────

    /**
     * Creates a new leaf (level-3) category.
     * <p>Rules:
     * <ul>
     *   <li>A parent is always required — top-level and domain categories are fixed.</li>
     *   <li>The parent must be a level-2 (domain) category.</li>
     * </ul>
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORY_TREE, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_FLAT, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_LEVEL, allEntries = true)
    })
    public CategoryResponse createCategory(CategoryRequest request) {
        if (request.getParentId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "A parent category is required. Only leaf (level-3) categories can be created dynamically. "
                    + "Root and domain categories are fixed.");
        }

        Category parent = findCategoryById(request.getParentId());
        if (resolveLevel(parent) != 2) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "The parent must be a domain (level-2) category. "
                    + "\"" + parent.getName() + "\" is level-" + resolveLevel(parent) + ".");
        }

        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Category name already exists");
        }

        Category category = categoryMapper.toEntity(request);
        category.setSlug(generateUniqueSlug(request.getName(), null));
        category.setParent(parent);

        CategoryResponse response = categoryMapper.toResponse(categoryRepository.save(category));
        log.info("Created leaf category: \"{}\" under \"{}\"", response.getName(), parent.getName());
        return response;
    }

    /**
     * Updates a leaf (level-3) category.
     * <p>Rules:
     * <ul>
     *   <li>Root (level-1) and domain (level-2) categories are immutable.</li>
     *   <li>The new parent must remain a level-2 category.</li>
     * </ul>
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORY_TREE, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_FLAT, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_LEVEL, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheNames.CATEGORY_BY_SLUG, allEntries = true)
    })
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);

        int level = resolveLevel(category);
        if (level == 1) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(),
                    "Root (level-1) categories are fixed and cannot be modified.");
        }
        if (level == 2) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(),
                    "Domain (level-2) categories are fixed and cannot be modified.");
        }

        if (request.getParentId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Leaf categories must keep a parent. Remove the parent ID to detach is not allowed.");
        }

        Long currentParentId = category.getParent().getId();
        if (!request.getParentId().equals(currentParentId)) {
            Category newParent = findCategoryById(request.getParentId());
            if (resolveLevel(newParent) != 2) {
                throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                        "The parent must be a domain (level-2) category. "
                        + "\"" + newParent.getName() + "\" is level-" + resolveLevel(newParent) + ".");
            }
            category.setParent(newParent);
        }

        String newSlug = generateUniqueSlug(request.getName(), id);
        categoryMapper.updateEntity(request, category);
        category.setSlug(newSlug);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    /**
     * Deletes a leaf (level-3) category.
     * Root and domain categories cannot be deleted.
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.CATEGORY_TREE, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_FLAT, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_LEVEL, allEntries = true),
            @CacheEvict(value = CacheNames.CATEGORY_BY_ID, key = "#id"),
            @CacheEvict(value = CacheNames.CATEGORY_BY_SLUG, allEntries = true)
    })
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);

        int level = resolveLevel(category);
        if (level != 3) {
            String label = (level == 1) ? "Root (level-1)" : "Domain (level-2)";
            throw new BusinessException(HttpStatus.FORBIDDEN.value(),
                    label + " categories cannot be deleted.");
        }

        if (!category.getChildren().isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT.value(),
                    "Cannot delete a category that still has subcategories.");
        }
        categoryRepository.delete(category);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /**
     * Determines the hierarchy level of a category.
     * <ul>
     *   <li>1 — root (no parent)</li>
     *   <li>2 — domain (parent has no parent)</li>
     *   <li>3 — leaf (parent has a parent)</li>
     * </ul>
     * Loads the parent entity via the repository to avoid Hibernate proxy issues.
     */
    private int resolveLevel(Category cat) {
        if (cat.getParent() == null) return 1;

        Category parent = categoryRepository.findById(cat.getParent().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Parent category not found for: " + cat.getName()));

        return (parent.getParent() == null) ? 2 : 3;
    }

    private String levelLabel(int level) {
        return switch (level) {
            case 1 -> "root (level-1)";
            case 2 -> "domain (level-2)";
            default -> "level-" + level;
        };
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Category not found with id: " + id));
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = base;
        int counter = 1;
        while (excludeId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }

    // ── Tree builder (O(n), 3-pass) ───────────────────────────────────────────

    private List<CategoryTreeResponse> buildTree(List<CategoryResponse> flat) {
        Map<Long, CategoryTreeResponse> nodeMap = new HashMap<>();

        for (CategoryResponse item : flat) {
            long direct = item.getProductCount() != null ? item.getProductCount() : 0L;
            nodeMap.put(item.getId(), CategoryTreeResponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .slug(item.getSlug())
                    .productCount(direct)
                    .totalProductCount(direct)
                    .children(new ArrayList<>())
                    .build());
        }

        List<CategoryTreeResponse> roots = new ArrayList<>();
        for (CategoryResponse item : flat) {
            CategoryTreeResponse node = nodeMap.get(item.getId());
            if (item.getParentId() == null) {
                roots.add(node);
            } else {
                CategoryTreeResponse parent = nodeMap.get(item.getParentId());
                if (parent != null) parent.getChildren().add(node);
                else roots.add(node);
            }
        }

        roots.forEach(this::sumTotals);
        return roots;
    }

    private long sumTotals(CategoryTreeResponse node) {
        long total = node.getProductCount();
        for (CategoryTreeResponse child : node.getChildren()) total += sumTotals(child);
        node.setTotalProductCount(total);
        return total;
    }
}
