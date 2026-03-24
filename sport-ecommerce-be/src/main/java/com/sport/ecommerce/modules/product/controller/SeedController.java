package com.sport.ecommerce.modules.product.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.product.dto.response.SeedResponse;
import com.sport.ecommerce.modules.product.service.SeedProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(AppConstant.API_PREFIX + "/admin")
@RequiredArgsConstructor
public class SeedController {

    private static final int MAX_SEED_COUNT = 100;

    private final SeedProductService seedProductService;

    /**
     * Generates random sport products and persists them to the database.
     * Images are uploaded to Cloudinary before the product is created.
     *
     * @param numberOfProducts number of products to generate (default 20, max 100)
     */
    @PostMapping("/seed-products")
    public ResponseEntity<ApiResponse<SeedResponse>> seedProducts(
            @RequestParam(defaultValue = "20") int numberOfProducts) {

        int count = Math.min(Math.max(numberOfProducts, 1), MAX_SEED_COUNT);
        log.info("Seeding {} products…", count);

        long start = System.currentTimeMillis();
        SeedProductService.SeedResult result = seedProductService.seed(count);
        long durationMs = System.currentTimeMillis() - start;

        SeedResponse response = SeedResponse.of(
                result.created(),
                result.failed(),
                durationMs,
                result.errors()
        );

        log.info("Seed complete — created={}, failed={}, duration={}ms",
                response.getCreated(), response.getFailed(), response.getDurationMs());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
