package com.sport.ecommerce.modules.product.service;

import com.sport.ecommerce.modules.product.dto.request.ProductVariantRequest;
import com.sport.ecommerce.modules.product.dto.response.VariantResponse;

import java.util.List;

public interface ProductVariantService {

    List<VariantResponse> getVariantsByProductId(Long productId);

    VariantResponse getVariantById(Long productId, Long variantId);

    VariantResponse createVariant(Long productId, ProductVariantRequest request);

    VariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request);

    void deleteVariant(Long productId, Long variantId);
}
