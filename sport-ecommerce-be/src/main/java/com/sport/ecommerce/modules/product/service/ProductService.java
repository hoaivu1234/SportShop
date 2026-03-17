package com.sport.ecommerce.modules.product.service;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductDetailResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;

public interface ProductService {

    PageResponse<ProductListResponse> getProducts(ProductFilterRequest filter);

    ProductDetailResponse getProductById(Long id);

    ProductDetailResponse getProductBySlug(String slug);

    ProductDetailResponse createProduct(ProductRequest request);

    ProductDetailResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
