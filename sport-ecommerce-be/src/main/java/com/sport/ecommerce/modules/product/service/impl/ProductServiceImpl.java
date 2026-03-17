package com.sport.ecommerce.modules.product.service.impl;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductRequest;
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
                .map(product -> {
                    ProductListResponse response = productMapper.toListResponse(product);
                    productImageRepository.findByProductIdAndIsMainTrue(product.getId())
                            .ifPresent(img -> response.setMainImageUrl(img.getImageUrl()));
                    return response;
                });

        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long id) {
        Product product = findProductById(id);
        return buildDetailResponse(product);
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
        return buildDetailResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductById(id);
        String newSlug = generateUniqueSlug(request.getName(), id);
        productMapper.updateEntity(request, product);
        product.setSlug(newSlug);
        product.setCategory(resolveCategory(request.getCategoryId()));
        return buildDetailResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(), "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDetailResponse buildDetailResponse(Product product) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getId());
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        ProductDetailResponse response = productMapper.toDetailResponse(product);
        response.setImages(productMapper.toImageResponseList(images));
        response.setVariants(productMapper.toVariantResponseList(variants));
        return response;
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
