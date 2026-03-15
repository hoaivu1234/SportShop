package com.sport.ecommerce.security.userdetails;

import com.sport.ecommerce.common.enums.UserRole;
import com.sport.ecommerce.modules.user.entity.User;
import com.sport.ecommerce.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailFetchRoles(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email));

        Collection<SimpleGrantedAuthority> authorities =
                user.getRoles()
                        .stream()
                        .map(role ->
                                new SimpleGrantedAuthority(role.getName()))
                        .toList();

        return new CustomUserDetails(user, authorities);
    }
}
