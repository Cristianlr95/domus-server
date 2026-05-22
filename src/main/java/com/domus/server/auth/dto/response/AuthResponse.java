package com.domus.server.auth.dto.response;

import com.domus.server.user.dto.response.UserResponse;

public record AuthResponse(
    String tokenType,
    String accessToken,
    String refreshToken,
    long expiresIn,
    long refreshExpiresIn,
    UserResponse user
) {
}
