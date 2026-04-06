package com.sport.ecommerce.modules.product.service.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;
import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.product.dto.document.ProductDocument;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;
import com.sport.ecommerce.modules.product.entity.ProductImage;
import com.sport.ecommerce.modules.product.mapper.ProductMapper;
import com.sport.ecommerce.modules.product.repository.ProductImageRepository;
import com.sport.ecommerce.modules.product.repository.ProductVariantRepository;
import com.sport.ecommerce.modules.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<ProductListResponse> search(ProductFilterRequest filter) {

        // ===== 1. Resolve categoryIds (giữ logic cũ) =====
        List<Long> categoryIds = resolveCategoryIds(filter);

        // ===== 2. Build query =====
        BoolQuery.Builder bool = QueryBuilders.bool();

        // keyword (full-text)
        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            bool.must(m -> m.multiMatch(mm -> mm
                    .fields("name^3", "brand", "categorySlug")
                    .query(filter.getKeyword())
                    .fuzziness("AUTO")
            ));
        }

        // category
        if (categoryIds != null && !categoryIds.isEmpty()) {
            bool.filter(f -> f.terms(t -> t
                    .field("categoryId")
                    .terms(ts -> ts.value(
                            categoryIds.stream().map(FieldValue::of).toList()
                    ))
            ));
        }

        // brand
        if (filter.getBrand() != null) {
            bool.filter(f -> f.term(t -> t
                    .field("brand.keyword")
                    .value(filter.getBrand())
            ));
        }

        // price range
        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            bool.filter(f -> f.range(r -> r
                    .number(n -> n
                            .field("price")
                            .gte(filter.getMinPrice() != null ? filter.getMinPrice().doubleValue() : null)
                            .lte(filter.getMaxPrice() != null ? filter.getMaxPrice().doubleValue() : null)
                    )
            ));
        }

        // onSale
        if (filter.isOnSale()) {
            bool.filter(f -> f.term(t -> t.field("onSale").value(true)));
        }

        // status (always ACTIVE)
        bool.filter(f -> f.term(t -> t.field("status").value("ACTIVE")));

        // ===== 3. Sort =====
        SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                        .field(filter.getSortBy())
                        .order("asc".equalsIgnoreCase(filter.getSortDir())
                                ? SortOrder.Asc
                                : SortOrder.Desc)
                )
        );

        // ===== 4. Build query =====
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(bool.build()))
                .withPageable(PageRequest.of(filter.getPage(), filter.getSize()))
                .withSort(sortOptions)
                .build();

        // ===== 5. Execute =====
        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

        // ===== 6. Map =====
        List<ProductListResponse> content = hits.getSearchHits().stream()
                .map(hit -> productMapper.fromDocument(hit.getContent()))
                .toList();

        enrichProductData(content);

        return PageResponse.of(
                content,
                hits.getTotalHits(),
                filter.getPage(),
                filter.getSize()
        );
    }

    private List<Long> resolveCategoryIds(ProductFilterRequest filter) {

        if (filter.getCategorySlug() != null && !filter.getCategorySlug().isBlank()) {
            return categoryRepository.findBySlug(filter.getCategorySlug())
                    .map(cat -> getAllCategoryIds(cat.getId()))
                    .orElse(null);
        }

        if (filter.getCategoryId() != null) {
            return getAllCategoryIds(filter.getCategoryId());
        }

        return null;
    }

    public List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        collectCategoryIds(categoryId, result);
        return result;
    }

    private void collectCategoryIds(Long parentId, List<Long> result) {
        result.add(parentId);

        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            collectCategoryIds(child.getId(), result);
        }
    }

    private void enrichProductData(List<ProductListResponse> products) {
        List<Long> ids = products.stream()
                .map(ProductListResponse::getId)
                .toList();

        if (ids.isEmpty()) return;

        Map<Long, String> mainImages = productImageRepository.findMainImagesByProductIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImageUrl
                ));

        Map<Long, Integer> stockMap = productVariantRepository.sumStockByProductIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).intValue()
                ));

        products.forEach(p -> {
            p.setMainImageUrl(mainImages.get(p.getId()));
            p.setTotalStock(stockMap.getOrDefault(p.getId(), 0));
        });
    }
}
