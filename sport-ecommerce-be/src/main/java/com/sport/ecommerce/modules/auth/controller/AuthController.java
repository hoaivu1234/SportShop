package com.sport.ecommerce.modules.auth.controller;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.common.dto.response.ApiResponse;
import com.sport.ecommerce.modules.auth.dto.request.ChangePasswordRequest;
import com.sport.ecommerce.modules.auth.dto.request.LoginRequest;
import com.sport.ecommerce.modules.auth.dto.request.RefreshTokenRequest;
import com.sport.ecommerce.modules.auth.dto.response.LoginResponse;
import com.sport.ecommerce.modules.auth.dto.response.RegisterResponse;
import com.sport.ecommerce.modules.auth.service.AuthService;
import com.sport.ecommerce.modules.user.dto.request.CreateUserRequest;
import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.API_PREFIX + "/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(loginRequest)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody CreateUserRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(authService.register(registerRequest)));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.noContent());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody(required = false) RefreshTokenRequest refreshTokenRequest) {

        String token = null;
        boolean isCookieAuth = false;

        // 1. Try request body (email/password users)
        if (refreshTokenRequest != null && StringUtils.hasText(refreshTokenRequest.getRefreshToken())) {
            token = refreshTokenRequest.getRefreshToken();
        }

        // 2. Fall back to HttpOnly cookie (OAuth2 users)
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refresh_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        isCookieAuth = true;
                        break;
                    }
                }
            }
        }

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "No refresh token provided"));
        }

        LoginResponse loginResponse = authService.refresh(token);

        // If the original request used a cookie, rotate the cookies in the response
        if (isCookieAuth) {
            addTokenCookie(response, "access_token", loginResponse.getAccessToken(), jwtExpiration / 1000);
            addTokenCookie(response, "refresh_token", loginResponse.getRefreshToken(), REFRESH_TOKEN_MAX_AGE_SECONDS);
        }

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    private void addTokenCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
