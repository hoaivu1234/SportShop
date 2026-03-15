package com.sport.ecommerce.modules.auth.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
