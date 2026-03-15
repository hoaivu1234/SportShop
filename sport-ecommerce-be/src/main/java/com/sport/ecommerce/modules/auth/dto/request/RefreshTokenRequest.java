package com.sport.ecommerce.modules.auth.dto.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
