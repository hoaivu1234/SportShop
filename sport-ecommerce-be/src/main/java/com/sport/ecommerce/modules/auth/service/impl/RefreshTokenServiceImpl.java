package com.sport.ecommerce.modules.auth.service.impl;

import com.sport.ecommerce.exception.custom.BusinessException;
import com.sport.ecommerce.modules.auth.entity.RefreshToken;
import com.sport.ecommerce.modules.auth.repository.RefreshTokenRepository;
import com.sport.ecommerce.modules.auth.service.RefreshTokenService;
import com.sport.ecommerce.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken rotate(String token) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(token).orElseThrow();
        refreshTokenRepository.delete(oldToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(oldToken.getUser());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setExpiryDate(LocalDate.now().plusDays(7).atStartOfDay());

        return refreshTokenRepository.save(newToken);
    }

    @Override
    public RefreshToken create(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDate.now().plusDays(7).atStartOfDay());
        return refreshTokenRepository.save(refreshToken);
    }
}
