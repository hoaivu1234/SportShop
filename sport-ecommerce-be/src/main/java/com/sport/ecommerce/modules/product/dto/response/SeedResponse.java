package com.sport.ecommerce.modules.product.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SeedResponse {

    private int created;
    private int failed;
    private long durationMs;
    private List<String> errors = new ArrayList<>();

    public static SeedResponse of(int created, int failed, long durationMs, List<String> errors) {
        SeedResponse r = new SeedResponse();
        r.created = created;
        r.failed = failed;
        r.durationMs = durationMs;
        r.errors = errors;
        return r;
    }
}
