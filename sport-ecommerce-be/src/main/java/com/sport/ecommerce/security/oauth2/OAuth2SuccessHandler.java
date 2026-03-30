package com.sport.ecommerce.security.oauth2;

import com.sport.ecommerce.modules.auth.service.RefreshTokenService;
import com.sport.ecommerce.modules.user.entity.Role;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.repository.RoleRepository;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import com.sport.ecommerce.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 7 * 24 * 60 * 60; // 7 days

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final String frontendUrl;
    private final long jwtExpirationMs;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        User user = userRepository.findByEmailFetchRoles(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setStatus("ACTIVE");
            newUser.setPassword(UUID.randomUUID().toString());

            Role userRole = roleRepository.findByName("ROLE_CUSTOMER");
            if (userRole != null) {
                newUser.getRoles().add(userRole);
            }

            return userRepository.save(newUser);
        });

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.create(user).getToken();

        addTokenCookie(response, "access_token", accessToken, jwtExpirationMs / 1000);
        addTokenCookie(response, "refresh_token", refreshToken, REFRESH_TOKEN_MAX_AGE_SECONDS);

        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth2/redirect");
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
