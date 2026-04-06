package com.sport.ecommerce.modules.product.service;

import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;

public interface ProductSearchService {
    PageResponse<ProductListResponse> search(ProductFilterRequest filter);
}
