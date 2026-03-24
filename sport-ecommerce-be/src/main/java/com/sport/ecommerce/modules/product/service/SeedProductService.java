package com.sport.ecommerce.modules.product.service;

import com.sport.ecommerce.infrastructure.cloudinary.CloudinaryService;
import com.sport.ecommerce.modules.category.entity.Category;
import com.sport.ecommerce.modules.category.repository.CategoryRepository;
import com.sport.ecommerce.modules.product.dto.request.ProductImageRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductRequest;
import com.sport.ecommerce.modules.product.dto.request.ProductVariantRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedProductService {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;
    private final CategoryRepository categoryRepository;

    private static final String CLOUDINARY_FOLDER = "sport-shop/products";

    private static final String[] BRANDS = {
            "Nike", "Adidas", "Under Armour", "Puma", "New Balance",
            "Reebok", "Asics", "Saucony", "Brooks", "Mizuno"
    };

    private static final String[] PRODUCT_TYPES = {
            "Running Shoes", "Training Shoes", "Basketball Shoes", "Football Cleats",
            "Tennis Shoes", "Hiking Boots", "Cycling Shoes", "Compression Shorts",
            "Performance T-Shirt", "Sports Jacket", "Training Shorts", "Yoga Pants",
            "Sports Bra", "Gym Gloves", "Resistance Bands", "Jump Rope",
            "Foam Roller", "Sports Socks", "Water Bottle", "Sports Bag"
    };

    private static final String[] SIZES = {"XS", "S", "M", "L", "XL", "XXL"};
    private static final String[] SHOE_SIZES = {"6", "7", "8", "9", "10", "11", "12"};
    private static final String[] COLORS = {
            "Black", "White", "Red", "Blue", "Navy", "Gray", "Green",
            "Orange", "Yellow", "Pink", "Purple", "Maroon"
    };

    // Picsum Photos gives reliable random images — each seed produces a unique image
    private static final String IMAGE_URL_TEMPLATE = "https://picsum.photos/seed/%s/800/600";

    private final Random random = new Random();

    /**
     * Seeds {@code count} random sport products into the database.
     *
     * @param count number of products to generate
     * @return list of error messages for any products that failed; empty if all succeeded
     */
    public SeedResult seed(int count) {
        List<Long> categoryIds = fetchCategoryIds();
        List<String> errors = new ArrayList<>();
        int created = 0;

        for (int i = 0; i < count; i++) {
            try {
                ProductRequest request = buildRandomProduct(categoryIds);
                productService.createProduct(request);
                created++;
                log.debug("Seeded product {}/{}: {}", i + 1, count, request.getName());
            } catch (Exception ex) {
                String msg = "Product #" + (i + 1) + ": " + ex.getMessage();
                log.warn("Seed failed — {}", msg);
                errors.add(msg);
            }
        }

        return new SeedResult(created, errors);
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private ProductRequest buildRandomProduct(List<Long> categoryIds) {
        String brand = randomFrom(BRANDS);
        String type = randomFrom(PRODUCT_TYPES);
        String name = brand + " " + type + " " + randomSuffix();

        BigDecimal basePrice = randomPrice(29, 199);

        ProductRequest req = new ProductRequest();
        req.setName(name);
        req.setDescription("Premium " + type.toLowerCase() + " by " + brand + ". Designed for performance and comfort.");
        req.setBrand(brand);
        req.setPrice(basePrice);
        req.setStatus("ACTIVE");

        if (!categoryIds.isEmpty()) {
            req.setCategoryId(categoryIds.get(random.nextInt(categoryIds.size())));
        }

        req.setImages(buildImages());
        req.setVariants(buildVariants(type, basePrice));

        return req;
    }

    private List<ProductImageRequest> buildImages() {
        String seed = UUID.randomUUID().toString().substring(0, 8);
        String imageUrl = uploadToCloudinary(String.format(IMAGE_URL_TEMPLATE, seed));

        ProductImageRequest img = new ProductImageRequest();
        img.setImageUrl(imageUrl);
        img.setIsMain(true);
        img.setSortOrder(0);
        return List.of(img);
    }

    private String uploadToCloudinary(String sourceUrl) {
        try {
            return cloudinaryService.uploadFromUrl(sourceUrl, CLOUDINARY_FOLDER);
        } catch (Exception ex) {
            log.warn("Cloudinary upload failed ({}), using source URL as fallback: {}", ex.getMessage(), sourceUrl);
            // Fall back to the original URL so product creation doesn't fail entirely
            return sourceUrl;
        }
    }

    private List<ProductVariantRequest> buildVariants(String type, BigDecimal basePrice) {
        boolean isShoe = type.toLowerCase().contains("shoe") || type.toLowerCase().contains("boot")
                || type.toLowerCase().contains("cleat");
        String[] sizePool = isShoe ? SHOE_SIZES : SIZES;

        List<ProductVariantRequest> variants = new ArrayList<>();
        String color = randomFrom(COLORS);

        // Pick 2–4 random sizes
        int numSizes = 2 + random.nextInt(3);
        for (int i = 0; i < numSizes; i++) {
            String size = sizePool[random.nextInt(sizePool.length)];
            String sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            BigDecimal variantPrice = basePrice.add(BigDecimal.valueOf(random.nextInt(21) - 10))
                    .max(BigDecimal.ONE);

            ProductVariantRequest v = new ProductVariantRequest();
            v.setSku(sku);
            v.setSize(size);
            v.setColor(color);
            v.setPrice(variantPrice.setScale(2, RoundingMode.HALF_UP));
            v.setStock(random.nextInt(51));  // 0–50
            variants.add(v);
        }

        return variants;
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    private List<Long> fetchCategoryIds() {
        try {
            return categoryRepository.findAll().stream()
                    .map(Category::getId)
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not fetch categories, products will have no category: {}", ex.getMessage());
            return List.of();
        }
    }

    private String randomFrom(String[] arr) {
        return arr[random.nextInt(arr.length)];
    }

    private BigDecimal randomPrice(int min, int max) {
        int cents = (min * 100) + random.nextInt((max - min) * 100);
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String randomSuffix() {
        return String.valueOf(2000 + random.nextInt(25));
    }

    // ── Result record ────────────────────────────────────────────────────────

    public record SeedResult(int created, List<String> errors) {
        public int failed() { return errors.size(); }
    }
}
