package com.sport.ecommerce.modules.product.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.category.service.CategoryService;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductImageRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductVariantRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductDetailResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import com.sport.ecommerce.modules.product.mapper.ProductMapper;
import com.sport.ecommerce.modules.product.repository.ProductImageRepository;
import com.sport.ecommerce.modules.product.repository.ProductRepository;
import com.sport.ecommerce.modules.product.repository.ProductVariantRepository;
import com.sport.ecommerce.modules.product.service.ProductService;
import com.sport.ecommerce.modules.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getProducts(ProductFilterRequest filter) {

        Specification<Product> spec = buildSpecification(filter);
        Pageable pageable = buildPageable(filter);

        Page<ProductListResponse> page = productRepository.findAll(spec, pageable)
                .map(productMapper::toListResponse);

        enrichProductData(page.getContent());

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductListResponse> getProductsForExport(ProductFilterRequest filter) {

        Specification<Product> spec = buildSpecification(filter);

        List<ProductListResponse> responses = productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(productMapper::toListResponse)
                .toList();

        enrichProductData(responses);

        return responses;
    }

    private void enrichProductData(List<ProductListResponse> products) {
        List<Long> ids = products.stream()
                .map(ProductListResponse::getId)
                .toList();

        if (ids.isEmpty()) return;

        Map<Long, String> mainImages = productImageRepository.findMainImagesByProductIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImageUrl
                ));

        Map<Long, Integer> stockMap = productVariantRepository.sumStockByProductIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).intValue()
                ));

        products.forEach(p -> {
            p.setMainImageUrl(mainImages.get(p.getId()));
            p.setTotalStock(stockMap.getOrDefault(p.getId(), 0));
        });
    }

    public List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        collectCategoryIds(categoryId, result);
        return result;
    }

    private void collectCategoryIds(Long parentId, List<Long> result) {
        result.add(parentId);

        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            collectCategoryIds(child.getId(), result);
        }
    }

    private Specification<Product> buildSpecification(ProductFilterRequest filter) {

        List<Long> categoryIds = null;

        // categorySlug (from navbar) takes priority over categoryId (from sidebar)
        if (filter.getCategorySlug() != null && !filter.getCategorySlug().isBlank()) {
            categoryIds = categoryRepository.findBySlug(filter.getCategorySlug())
                    .map(cat -> getAllCategoryIds(cat.getId()))
                    .orElse(null);
        } else if (filter.getCategoryId() != null) {
            categoryIds = getAllCategoryIds(filter.getCategoryId());
        }

        return ProductSpecification.withFilters(
                filter.getKeyword(),
                categoryIds,
                filter.getBrand(),
                filter.getStatus(),
                filter.getMinPrice(),
                filter.getMaxPrice(),
                filter.isOnSale()
        );
    }

    private Pageable buildPageable(ProductFilterRequest filter) {
        Sort sort = Sort.by(
                Sort.Direction.fromString(filter.getSortDir()),
                filter.getSortBy()
        );
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        return buildDetailResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Product not found with slug: " + slug));
        return buildDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        product.setSlug(generateUniqueSlug(request.getName(), null));
        product.setCategory(resolveCategory(request.getCategoryId()));
        Product saved = productRepository.save(product);

        saveImages(saved, request.getImages());
        saveVariants(saved, request.getVariants());

        return buildDetailResponse(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductById(id);
        productMapper.updateEntity(request, product);
        product.setSlug(generateUniqueSlug(request.getName(), id));
        product.setCategory(resolveCategory(request.getCategoryId()));
        Product saved = productRepository.save(product);

        // Replace images when the request includes them
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            productImageRepository.deleteByProductId(saved.getId());
            saveImages(saved, request.getImages());
        }

        // Merge variants — update existing, insert new, soft/hard-delete removed
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            syncVariants(saved, request.getVariants());
        }

        return buildDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProductById(id); // throws 404 if not found or already deleted
        productVariantRepository.deactivateByProductId(id); // deactivate all variants atomically
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ProductDetailResponse buildDetailResponse(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(product.getId());

        ProductDetailResponse response = productMapper.toDetailResponse(product);
        response.setImages(productMapper.toImageResponseList(images));
        response.setVariants(productMapper.toVariantResponseList(variants));
        return response;
    }

    /**
     * Saves a list of images for a product.
     * If no image is explicitly marked as main, the first one becomes main automatically.
     */
    private void saveImages(Product product, List<ProductImageRequest> images) {
        if (images == null || images.isEmpty()) return;

        boolean hasExplicitMain = images.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsMain()));

        for (int i = 0; i < images.size(); i++) {
            ProductImageRequest req = images.get(i);
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(req.getImageUrl());
            image.setIsMain(hasExplicitMain ? Boolean.TRUE.equals(req.getIsMain()) : i == 0);
            image.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : i);
            productImageRepository.save(image);
        }
    }

    private void syncVariants(Product product, List<ProductVariantRequest> requests) {
        List<ProductVariant> existing = productVariantRepository.findByProductId(product.getId());

        Map<String, ProductVariant> existingBySku = existing.stream()
                .collect(Collectors.toMap(ProductVariant::getSku, v -> v));

        Set<String> incomingSkus = requests.stream()
                .map(ProductVariantRequest::getSku)
                .collect(Collectors.toSet());

        List<ProductVariant> toSave = new ArrayList<>();
        List<ProductVariant> toDelete = new ArrayList<>();

        // INSERT + UPDATE
        for (ProductVariantRequest req : requests) {
            ProductVariant variant = existingBySku.get(req.getSku());

            if (variant != null) {
                // update
                variant.setSize(req.getSize());
                variant.setColor(req.getColor());
                variant.setPrice(req.getPrice());
                variant.setStock(req.getStock() != null ? req.getStock() : 0);
                variant.setIsActive(true);
            } else {
                // insert
                variant = new ProductVariant();
                variant.setProduct(product);
                variant.setSku(req.getSku());
                variant.setSize(req.getSize());
                variant.setColor(req.getColor());
                variant.setPrice(req.getPrice());
                variant.setStock(req.getStock() != null ? req.getStock() : 0);
                variant.setIsActive(true);
            }

            toSave.add(variant);
        }

        // DELETE / SOFT DELETE
        for (ProductVariant v : existing) {
            if (!incomingSkus.contains(v.getSku())) {
                boolean referenced = productVariantRepository.countCartItemsByVariantId(v.getId()) > 0
                        || productVariantRepository.countOrderItemsByVariantId(v.getId()) > 0;

                if (referenced) {
                    v.setIsActive(false);
                    toSave.add(v); // soft delete vẫn là update
                } else {
                    toDelete.add(v);
                }
            }
        }

        // BATCH OPERATIONS
        if (!toSave.isEmpty()) {
            productVariantRepository.saveAll(toSave);
        }

        if (!toDelete.isEmpty()) {
            productVariantRepository.deleteAll(toDelete);
        }
    }

    /** Saves a list of variants for a product. */
    private void saveVariants(Product product, List<ProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) return;

        for (ProductVariantRequest req : variants) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(req.getSku());
            variant.setSize(req.getSize());
            variant.setColor(req.getColor());
            variant.setPrice(req.getPrice());
            variant.setStock(req.getStock() != null ? req.getStock() : 0);
            productVariantRepository.save(variant);
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Product not found with id: " + id));
    }

    /**
     * Fetches and validates a category for product assignment.
     * Throws if {@code categoryId} is null, not found, or not a leaf (level-3) category.
     */
    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(),
                    "Category is required. Products must be assigned to a leaf (level-3) category.");
        }
        // Validates hierarchy level — throws BusinessException if not leaf
        categoryService.validateLeafCategory(categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Category not found with id: " + categoryId));
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = base;
        int counter = 1;
        while (excludeId == null ? productRepository.existsBySlugAndIsDeletedFalse(slug)
                : productRepository.existsBySlugAndIdNotAndIsDeletedFalse(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
