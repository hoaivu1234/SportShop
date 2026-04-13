package com.sport.ecommerce.config.cache;

public final class CacheNames {

    private CacheNames() {}

    // Category caches
    public static final String CATEGORY_TREE    = "category:tree";
    public static final String CATEGORY_LEVEL   = "category:level";
    public static final String CATEGORY_FLAT    = "category:flat";
    public static final String CATEGORY_BY_ID   = "category:id";
    public static final String CATEGORY_BY_SLUG = "category:slug";

    // Product caches
    public static final String PRODUCT_BY_ID   = "product:id";
    public static final String PRODUCT_BY_SLUG = "product:slug";
}
