package com.sport.ecommerce.modules.product.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.product.dto.request.ProductVariantRequest;
import com.sport.ecommerce.modules.product.dto.response.VariantResponse;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import com.sport.ecommerce.modules.product.mapper.ProductMapper;
import com.sport.ecommerce.modules.product.repository.ProductRepository;
import com.sport.ecommerce.modules.product.repository.ProductVariantRepository;
import com.sport.ecommerce.modules.product.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsByProductId(Long productId) {
        ensureProductExists(productId);
        return productVariantRepository.findByProductId(productId)
                .stream()
                .map(productMapper::toVariantResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getVariantById(Long productId, Long variantId) {
        ProductVariant variant = findVariantByIdAndProductId(variantId, productId);
        return productMapper.toVariantResponse(variant);
    }

    @Override
    @Transactional
    public VariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = findProductById(productId);

        if (productVariantRepository.existsBySku(request.getSku())) {
            throw new BusinessException(HttpStatus.CONFLICT.value(),
                    "SKU already exists: " + request.getSku());
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock() != null ? request.getStock() : 0);

        return productMapper.toVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request) {
        ProductVariant variant = findVariantByIdAndProductId(variantId, productId);

        if (!variant.getSku().equals(request.getSku())
                && productVariantRepository.existsBySkuAndIdNot(request.getSku(), variantId)) {
            throw new BusinessException(HttpStatus.CONFLICT.value(),
                    "SKU already exists: " + request.getSku());
        }

        variant.setSku(request.getSku());
        variant.setSize(request.getSize());
        variant.setColor(request.getColor());
        variant.setPrice(request.getPrice());
        variant.setStock(request.getStock() != null ? request.getStock() : 0);

        return productMapper.toVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = findVariantByIdAndProductId(variantId, productId);
        productVariantRepository.delete(variant);
    }

    private ProductVariant findVariantByIdAndProductId(Long variantId, Long productId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Variant not found with id: " + variantId));
        if (!variant.getProduct().getId().equals(productId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                    "Variant not found for product id: " + productId);
        }
        return variant;
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Product not found with id: " + productId));
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                    "Product not found with id: " + productId);
        }
    }
}
