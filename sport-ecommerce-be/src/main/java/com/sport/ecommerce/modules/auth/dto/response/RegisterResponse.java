package com.sport.ecommerce.modules.auth.dto.response;

import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private UserResponse user;
    private String accessToken;
    private String refreshToken;
}
