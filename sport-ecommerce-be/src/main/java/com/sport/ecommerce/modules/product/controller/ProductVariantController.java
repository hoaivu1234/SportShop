package com.sport.ecommerce.modules.product.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductVariantRequest;
import com.sport.ecommerce.modules.product.dto.response.VariantResponse;
import com.sport.ecommerce.modules.product.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariants(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productVariantService.getVariantsByProductId(productId)));
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        return ResponseEntity.ok(ApiResponse.success(productVariantService.getVariantById(productId, variantId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VariantResponse>> createVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productVariantService.createVariant(productId, request)));
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productVariantService.updateVariant(productId, variantId, request)));
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable Long productId,
            @PathVariable Long variantId) {
        productVariantService.deleteVariant(productId, variantId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
