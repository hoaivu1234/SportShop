package com.sport.ecommerce.modules.product.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductImageRequest;
import com.sport.ecommerce.modules.product.dto.response.ImageResponse;
import com.sport.ecommerce.modules.product.service.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageResponse>>> getImages(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.getImagesByProductId(productId)));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ImageResponse>> getImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.getImageById(productId, imageId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImageResponse>> createImage(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productImageService.createImage(productId, request)));
    }

    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ImageResponse>> updateImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @Valid @RequestBody ProductImageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.updateImage(productId, imageId, request)));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{imageId}/main")
    public ResponseEntity<ApiResponse<ImageResponse>> setMainImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.setMainImage(productId, imageId)));
    }
}
