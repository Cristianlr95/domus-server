package com.domus.server.common.security;

import com.domus.server.common.exception.UnauthorizedException;
import com.domus.server.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthUser loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCase(username)
            .map(user -> new AuthUser(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                user.getRoleNames()
            ))
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));
    }
}
