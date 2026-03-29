package com.sport.ecommerce.config.security;

import com.sport.ecommerce.common.constant.AppConstant;
import com.sport.ecommerce.modules.auth.service.RefreshTokenService;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import com.sport.ecommerce.security.handler.AuthEntryPoint;
import com.sport.ecommerce.security.jwt.JwtAuthenticationFilter;
import com.sport.ecommerce.security.jwt.JwtService;
import com.sport.ecommerce.security.oauth2.OAuth2SuccessHandler;
import com.sport.ecommerce.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(this.jwtService, this.userDetailsService);
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler(UserRepository userRepository, JwtService jwtService, RefreshTokenService refreshTokenService ) {
        return new OAuth2SuccessHandler(userRepository, jwtService, refreshTokenService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                AppConstant.API_PREFIX + "/auth/**",
                                AppConstant.PUBLIC_PREFIX + "/**",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        .requestMatchers(
                                AppConstant.ADMIN_PREFIX + "/**"
                        ).hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new AuthEntryPoint())
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(authEndpoint ->
                                authEndpoint.baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection ->
                                redirection.baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2SuccessHandler(userRepository, jwtService, refreshTokenService))
                )

                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
