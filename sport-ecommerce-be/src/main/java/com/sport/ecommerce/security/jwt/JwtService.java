package com.sport.ecommerce.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sport.ecommerce.modules.user.entity.Role;
import com.sport.ecommerce.modules.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + expirationTime);

        List<String> roles =
                user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList();

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public String getEmailFromJWT(String token) {
        return JWT.decode(token).getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            DecodedJWT token = JWT.require(Algorithm.HMAC512(secretKey)).build().verify(authToken);

            Date expireAt = token.getExpiresAt();
            if (expireAt.compareTo(new Date()) > 0) {
                return true;
            }
        } catch (JWTVerificationException ex) {
            log.error("Invalid JWT token");
        }
        return false;
    }
}
