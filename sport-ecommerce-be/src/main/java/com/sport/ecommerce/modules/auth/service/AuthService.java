package com.sport.ecommerce.modules.auth.service;

import com.sport.ecommerce.modules.auth.dto.request.ChangePasswordRequest;
import com.sport.ecommerce.modules.auth.dto.request.LoginRequest;
import com.sport.ecommerce.modules.auth.dto.response.LoginResponse;
import com.sport.ecommerce.modules.user.dto.request.CreateUserRequest;
import com.sport.ecommerce.modules.user.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserResponse register(CreateUserRequest registerRequest);
    void changePassword(ChangePasswordRequest req);
    LoginResponse refresh(String refreshToken);
}
