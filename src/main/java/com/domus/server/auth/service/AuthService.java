package com.domus.server.auth.service;

import com.domus.server.auth.dto.request.LoginRequest;
import com.domus.server.auth.dto.response.AuthResponse;
import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.exception.UnauthorizedException;
import com.domus.server.common.security.AuthUser;
import com.domus.server.common.security.JwtService;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.mapper.UserMapper;
import com.domus.server.user.repository.UserRepository;
import java.util.Map;
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
    private final AuditLogService auditLogService;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserRepository userRepository,
        UserMapper userMapper,
        AuditLogService auditLogService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            AuthUser authUser = (AuthUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            ).getPrincipal();

            UserResponse user = userRepository.findById(authUser.getId())
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

            auditLogService.record(
                authUser.getId(),
                "AUTH",
                authUser.getId().toString(),
                AuditAction.LOGIN,
                "User logged in successfully.",
                null,
                null,
                Map.of(
                    "email", authUser.getUsername(),
                    "roles", authUser.getRoles(),
                    "permissions", authUser.getPermissions()
                )
            );

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
