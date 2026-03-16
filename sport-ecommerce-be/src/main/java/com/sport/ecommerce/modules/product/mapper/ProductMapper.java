package com.sport.ecommerce.modules.product.mapper;

import com.sport.ecommerce.modules.category.mapper.CategoryMapper;
import com.sport.ecommerce.modules.product.dto.response.ImageResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductDetailResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;
import com.sport.ecommerce.modules.product.dto.response.VariantResponse;
import com.sport.ecommerce.modules.product.entity.Product;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.entity.variant.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CategoryMapper.class})
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "mainImageUrl", ignore = true)
    ProductListResponse toListResponse(Product product);

    @Mapping(source = "category", target = "category")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    ProductDetailResponse toDetailResponse(Product product);

    VariantResponse toVariantResponse(ProductVariant variant);

    ImageResponse toImageResponse(ProductImage image);

    List<VariantResponse> toVariantResponseList(List<ProductVariant> variants);

    List<ImageResponse> toImageResponseList(List<ProductImage> images);
}
