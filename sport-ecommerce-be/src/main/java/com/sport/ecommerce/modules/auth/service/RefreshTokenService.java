package com.sport.ecommerce.modules.auth.service;

import com.sport.ecommerce.modules.auth.entity.RefreshToken;
import com.sport.ecommerce.modules.user.entity.User;

public interface RefreshTokenService {
    RefreshToken rotate(String token);
    RefreshToken create(User user);
}
