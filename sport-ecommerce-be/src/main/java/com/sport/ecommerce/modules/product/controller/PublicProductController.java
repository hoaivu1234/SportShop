package com.sport.ecommerce.modules.product.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductDetailResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;
import com.sport.ecommerce.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Read-only product endpoints accessible without authentication.
 * Always scoped to ACTIVE products — status param is ignored.
 */
@RestController
@RequestMapping(AppConstant.PUBLIC_PREFIX + "/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categorySlug,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean onSale,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setKeyword(keyword);
        filter.setCategoryId(categoryId);
        filter.setCategorySlug(categorySlug);
        filter.setBrand(brand);
        filter.setStatus("ACTIVE");   // always enforced for public access
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setOnSale(onSale);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(filter)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }
}
