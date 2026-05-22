package com.domus.server.auth.service;

import com.domus.server.auth.dto.request.LogoutRequest;
import com.domus.server.auth.dto.request.LoginRequest;
import com.domus.server.auth.dto.request.RefreshTokenRequest;
import com.domus.server.auth.dto.response.AuthResponse;
import com.domus.server.auth.entity.RefreshTokenEntity;
import com.domus.server.auth.repository.RefreshTokenRepository;
import com.domus.server.audit.entity.AuditAction;
import com.domus.server.audit.service.AuditLogService;
import com.domus.server.common.exception.UnauthorizedException;
import com.domus.server.common.security.AuthUser;
import com.domus.server.common.security.JwtService;
import com.domus.server.user.dto.response.UserResponse;
import com.domus.server.user.entity.UserEntity;
import com.domus.server.user.mapper.UserMapper;
import com.domus.server.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationDays;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        UserRepository userRepository,
        UserMapper userMapper,
        AuditLogService auditLogService,
        RefreshTokenRepository refreshTokenRepository,
        @Value("${security.refresh-token.expiration-days:14}") long refreshExpirationDays
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            AuthUser authUser = (AuthUser) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            ).getPrincipal();

            UserEntity userEntity = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));
            UserResponse user = userMapper.toResponse(userEntity);

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

            return buildAuthResponse(authUser, userEntity, user);
        } catch (BadCredentialsException exception) {
            throw new UnauthorizedException("Invalid credentials.");
        }
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        Instant now = Instant.now();
        if (!refreshToken.isActive(now) || !refreshToken.getUser().isActive()) {
            throw new UnauthorizedException("Invalid refresh token.");
        }

        refreshToken.setRevokedAt(now);
        UserEntity userEntity = refreshToken.getUser();
        AuthUser authUser = new AuthUser(
            userEntity.getId(),
            userEntity.getFirstName(),
            userEntity.getLastName(),
            userEntity.getEmail(),
            userEntity.getPasswordHash(),
            userEntity.isActive(),
            userEntity.getRoleNames(),
            userEntity.getPermissionCodes()
        );

        return buildAuthResponse(authUser, userEntity, userMapper.toResponse(userEntity));
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
            .filter(token -> token.getRevokedAt() == null)
            .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    private AuthResponse buildAuthResponse(AuthUser authUser, UserEntity userEntity, UserResponse user) {
        String refreshToken = generateRefreshToken();
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setId(UUID.randomUUID());
        refreshTokenEntity.setTokenHash(hashToken(refreshToken));
        refreshTokenEntity.setUser(userEntity);
        refreshTokenEntity.setExpiresAt(Instant.now().plusSeconds(refreshExpiresInSeconds()));
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(
            "Bearer",
            jwtService.generateToken(authUser),
            refreshToken,
            jwtService.getExpirationSeconds(),
            refreshExpiresInSeconds(),
            user
        );
    }

    private long refreshExpiresInSeconds() {
        return refreshExpirationDays * 24 * 60 * 60;
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available.");
        }
    }
}
