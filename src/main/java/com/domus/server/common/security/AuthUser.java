package com.domus.server.common.security;

import com.domus.server.user.entity.RoleName;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthUser implements UserDetails {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Set<RoleName> roles;
    private final Set<String> permissions;

    public AuthUser(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String passwordHash,
        boolean active,
        Set<RoleName> roles,
        Set<String> permissions
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roles = roles;
        this.permissions = permissions;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Set<RoleName> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .forEach(authorities::add);
        permissions.stream()
            .map(SimpleGrantedAuthority::new)
            .forEach(authorities::add);
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
