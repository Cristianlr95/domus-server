package com.domus.server.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public String generateToken(AuthUser authUser) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.expirationSeconds());

        return Jwts.builder()
            .subject(authUser.getUsername())
            .claim("uid", authUser.getId().toString())
            .claim("roles", authUser.getRoles().stream().map(Enum::name).toList())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(signingKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, AuthUser authUser) {
        Claims claims = extractClaims(token);
        Date expiration = claims.getExpiration();
        return authUser.getUsername().equalsIgnoreCase(claims.getSubject())
            && expiration != null
            && expiration.toInstant().isAfter(Instant.now());
    }

    public long getExpirationSeconds() {
        return properties.expirationSeconds();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
