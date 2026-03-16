package com.sport.ecommerce.modules.auth.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.auth.dto.request.ChangePasswordRequest;
import com.sport.ecommerce.modules.auth.dto.request.LoginRequest;
import com.sport.ecommerce.modules.auth.dto.response.LoginResponse;
import com.sport.ecommerce.modules.auth.entity.RefreshToken;
import com.sport.ecommerce.modules.auth.service.AuthService;
import com.sport.ecommerce.modules.auth.service.RefreshTokenService;
import com.sport.ecommerce.modules.user.dto.request.CreateUserRequest;
import com.sport.ecommerce.modules.user.dto.response.UserResponse;
import com.sport.ecommerce.modules.user.entity.Role;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.mapper.UserMapper;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import com.sport.ecommerce.security.jwt.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Invalid password");
        }

        String accessToken = jwtService.generateToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(accessToken, refreshToken.getToken());
    }

    public UserResponse register(CreateUserRequest registerRequest) {
        String email = registerRequest.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "User " + " already exists with " + "email: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setStatus("ACTIVE");
        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User user = userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword()))
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "wrong old password");

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
    }

    public LoginResponse refresh(String refreshToken) {
        RefreshToken newToken = refreshTokenService.rotate(refreshToken);

        String access = jwtService.generateToken(newToken.getUser());

        return new LoginResponse(access, newToken.getToken());
    }
}
