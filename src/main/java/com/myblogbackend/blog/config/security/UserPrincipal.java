package com.myblogbackend.blog.config.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myblogbackend.blog.models.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

public class UserPrincipal implements OAuth2User, UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private String name;

    private final String email;
    @JsonIgnore
    private final String password;
    private Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(final UserEntity user, final Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.build(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public UserPrincipal(final UUID id,
                         final String email,
                         final String password,
                         final String name,
                         final Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.authorities = authorities;
    }

    public static UserPrincipal build(final UserEntity userEntity) {
        List<GrantedAuthority> authorities = userEntity.getRoles()
                .stream()
                .map(role ->
                        new SimpleGrantedAuthority(role.getName().name())
                ).collect(Collectors.toList());

        return new UserPrincipal(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getName(),
                authorities
        );
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPrincipal user = (UserPrincipal) o;
        return Objects.equals(id, user.id);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }
}