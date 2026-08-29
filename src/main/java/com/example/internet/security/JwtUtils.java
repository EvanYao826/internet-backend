package com.example.internet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验工具
 */
@Component
public class JwtUtils {

    private static final String CLAIM_UID = "uid";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpireMinutes() * 60 * 1000);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_UID, userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token，无效或过期时抛出 JwtException
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        Number uid = claims.get(CLAIM_UID, Number.class);
        return uid == null ? null : uid.longValue();
    }

    /**
     * 校验 Token 是否有效（签名正确且未过期）
     */
    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
