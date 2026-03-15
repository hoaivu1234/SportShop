package com.sport.ecommerce.security.userdetails;

import com.sport.ecommerce.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    User user;
    private Collection<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends SimpleGrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
