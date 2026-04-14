package com.sport.ecommerce.modules.product.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.common.dto.response.PageResponse;
import com.sport.ecommerce.modules.product.dto.request.ProductFilterRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductRequest;
import com.sport.ecommerce.modules.product.dto.response.ProductDetailResponse;
import com.sport.ecommerce.modules.product.dto.response.ProductListResponse;
import com.sport.ecommerce.modules.product.service.ProductSearchService;
import com.sport.ecommerce.modules.product.service.ProductService;
import com.sport.ecommerce.modules.product.service.impl.ProductSearchServiceImpl;
import com.sport.ecommerce.modules.product.util.ProductCsvWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(AppConstant.ADMIN_PREFIX + "/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductSearchService productSearchService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductListResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setKeyword(keyword);
        filter.setCategoryId(categoryId);
        filter.setBrand(brand);
        filter.setStatus(status);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(ApiResponse.success(productSearchService.search(filter)));
    }

    /**
     * Exports products matching the given filters as a UTF-8 CSV file.
     * Supports the same filter params as GET /products (keyword, categoryId, brand, status, price range).
     * Returns Content-Disposition: attachment so browsers trigger a file download.
     */
    @GetMapping("/export")
    public void exportProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            HttpServletResponse response) throws IOException {

        ProductFilterRequest filter = new ProductFilterRequest();
        filter.setKeyword(keyword);
        filter.setCategoryId(categoryId);
        filter.setBrand(brand);
        filter.setStatus(status);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);

        String filename = "products_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        List<ProductListResponse> products = productService.getProductsForExport(filter);

        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(ProductCsvWriter.BOM);           // UTF-8 BOM for Excel compatibility
            writer.write(ProductCsvWriter.header());
            writer.write("\n");
            for (ProductListResponse p : products) {
                writer.write(ProductCsvWriter.toRow(p));
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reindex")
    public ResponseEntity<ApiResponse<Void>> reindexProducts() {
        productService.reindex();
        return ResponseEntity.ok(ApiResponse.noContent());
    }
}
