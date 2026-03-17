package com.sport.ecommerce.modules.product.service;

import com.sport.ecommerce.modules.product.dto.request.ProductImageRequest;
import com.sport.ecommerce.modules.product.dto.response.ImageResponse;

import java.util.List;

public interface ProductImageService {

    List<ImageResponse> getImagesByProductId(Long productId);

    ImageResponse getImageById(Long productId, Long imageId);

    ImageResponse createImage(Long productId, ProductImageRequest request);

    ImageResponse updateImage(Long productId, Long imageId, ProductImageRequest request);

    void deleteImage(Long productId, Long imageId);

    ImageResponse setMainImage(Long productId, Long imageId);
}
