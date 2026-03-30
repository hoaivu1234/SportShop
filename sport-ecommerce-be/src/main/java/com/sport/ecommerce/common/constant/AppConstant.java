package com.sport.ecommerce.common.constant;

public final class AppConstant {

    private AppConstant() {}

    public static final String API_PREFIX = "/api/v1";
    public static final String PUBLIC_PREFIX = API_PREFIX + "/public";
    public static final String ADMIN_PREFIX = API_PREFIX + "/admin";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "desc";
}
