package com.domus.server.auth.service;

import com.domus.server.auth.dto.request.LoginRequest;
import com.domus.server.auth.dto.response.AuthResponse;
import com.domus.server.common.exception.UnauthorizedException;
import com.domus.server.common.security.AuthUser;
import com.domus.server.common.security.JwtService;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.mapper.UserMapper;
import com.domus.server.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserRepository userRepository,
        UserMapper userMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            AuthUser authUser = (AuthUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            ).getPrincipal();

            UserResponse user = userRepository.findById(authUser.getId())
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

            return new AuthResponse(
                "Bearer",
                jwtService.generateToken(authUser),
                jwtService.getExpirationSeconds(),
                user
            );
        } catch (BadCredentialsException exception) {
            throw new UnauthorizedException("Invalid credentials.");
        }
    }
}
