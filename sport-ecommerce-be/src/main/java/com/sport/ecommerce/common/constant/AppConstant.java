package com.sport.ecommerce.common.constant;

public final class AppConstant {

    private AppConstant() {}

    public static final String API_PREFIX = "/api/v1";
    public static final String SECRET_KEY = "sport-ecommerce-key";
    public static final long EXPIRATION_TIME = 15 * 60 * 1000;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
}
