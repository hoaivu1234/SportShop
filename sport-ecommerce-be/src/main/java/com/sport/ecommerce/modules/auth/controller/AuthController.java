package com.sport.ecommerce.modules.auth.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.auth.dto.request.ChangePasswordRequest;
import com.sport.ecommerce.modules.auth.dto.request.LoginRequest;
import com.sport.ecommerce.modules.auth.dto.response.LoginResponse;
import com.sport.ecommerce.modules.auth.service.AuthService;
import com.sport.ecommerce.modules.user.dto.request.CreateUserRequest;
import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequest)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody CreateUserRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authService.register(registerRequest)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
    }
}
