package com.sport.ecommerce.modules.product.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductListResponse> getProducts(ProductFilterRequest filter) {
        Specification<Product> spec = ProductSpecification.withFilters(
                filter.getKeyword(),
                filter.getCategoryId(),
                filter.getBrand(),
                filter.getStatus(),
                filter.getMinPrice(),
                filter.getMaxPrice()
        );

        Sort sort = Sort.by(Sort.Direction.fromString(filter.getSortDir()), filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<ProductListResponse> page = productRepository.findAll(spec, pageable)
                .map(productMapper::toListResponse);

        List<Long> ids = page.getContent().stream().map(ProductListResponse::getId).toList();

        if (!ids.isEmpty()) {
            // Batch load main images — one query instead of N queries
            Map<Long, String> mainImages = productImageRepository.findMainImagesByProductIds(ids)
                    .stream()
                    .collect(Collectors.toMap(img -> img.getProduct().getId(), ProductImage::getImageUrl));

            // Batch load total stock — one query instead of N queries
            Map<Long, Integer> stockMap = productVariantRepository.sumStockByProductIds(ids)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> ((Number) row[0]).longValue(),
                            row -> ((Number) row[1]).intValue()
                    ));

            page.getContent().forEach(p -> {
                p.setMainImageUrl(mainImages.get(p.getId()));
                p.setTotalStock(stockMap.getOrDefault(p.getId(), 0));
            });
        }

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        return buildDetailResponse(findProductById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
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

        // Replace variants when the request includes them
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            productVariantRepository.deleteByProductId(saved.getId());
            saveVariants(saved, request.getVariants());
        }

        return buildDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ProductDetailResponse buildDetailResponse(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

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
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Product not found with id: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Category not found with id: " + categoryId));
    }

    private String generateUniqueSlug(String name, Long excludeId) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = base;
        int counter = 1;
        while (excludeId == null ? productRepository.existsBySlug(slug)
                : productRepository.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + counter++;
        }
        return slug;
    }
}
