package com.sport.ecommerce.modules.product.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.product.dto.request.ProductImageRequest;
import com.sport.ecommerce.modules.product.dto.response.ImageResponse;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.mapper.ProductMapper;
import com.sport.ecommerce.modules.product.repository.ProductImageRepository;
import com.sport.ecommerce.modules.product.repository.ProductRepository;
import com.sport.ecommerce.modules.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getImagesByProductId(Long productId) {
        ensureProductExists(productId);
        return productImageRepository.findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(productMapper::toImageResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ImageResponse getImageById(Long productId, Long imageId) {
        ProductImage image = findImageByIdAndProductId(imageId, productId);
        return productMapper.toImageResponse(image);
    }

    @Override
    @Transactional
    public ImageResponse createImage(Long productId, ProductImageRequest request) {
        Product product = findProductById(productId);

        // If this image is marked as main, unset any existing main image
        if (Boolean.TRUE.equals(request.getIsMain())) {
            clearMainImage(productId);
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(request.getImageUrl());
        image.setIsMain(request.getIsMain() != null ? request.getIsMain() : false);
        image.setSortOrder(request.getSortOrder());

        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional
    public ImageResponse updateImage(Long productId, Long imageId, ProductImageRequest request) {
        ProductImage image = findImageByIdAndProductId(imageId, productId);

        // If setting as main, unset any existing main image (excluding this one)
        if (Boolean.TRUE.equals(request.getIsMain()) && !Boolean.TRUE.equals(image.getIsMain())) {
            clearMainImage(productId);
        }

        image.setImageUrl(request.getImageUrl());
        image.setIsMain(request.getIsMain() != null ? request.getIsMain() : false);
        image.setSortOrder(request.getSortOrder());

        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = findImageByIdAndProductId(imageId, productId);
        productImageRepository.delete(image);
    }

    @Override
    @Transactional
    public ImageResponse setMainImage(Long productId, Long imageId) {
        ensureProductExists(productId);
        clearMainImage(productId);

        ProductImage image = findImageByIdAndProductId(imageId, productId);
        image.setIsMain(true);

        return productMapper.toImageResponse(productImageRepository.save(image));
    }

    private void clearMainImage(Long productId) {
        productImageRepository.findByProductIdAndIsMainTrue(productId)
                .ifPresent(img -> {
                    img.setIsMain(false);
                    productImageRepository.save(img);
                });
    }

    private ProductImage findImageByIdAndProductId(Long imageId, Long productId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(),
                        "Image not found with id: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND.value(),
                    "Image not found for product id: " + productId);
        }
        return image;
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
