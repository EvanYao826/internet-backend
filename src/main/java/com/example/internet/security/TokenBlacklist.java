package com.example.internet.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存态 Token 黑名单：退出登录后将 Token 拦截至其自然过期。
 * 仅适用于单实例部署，集群部署可替换为 Redis 实现。
 */
@Component
public class TokenBlacklist {

    private final Map<String, Long> store = new ConcurrentHashMap<>();

    public void revoke(String token, Date expiresAt) {
        store.put(sha256(token), expiresAt.getTime());
    }

    public boolean isRevoked(String token) {
        String key = sha256(token);
        Long expiresAt = store.get(key);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) {
            store.remove(key);
            return false;
        }
        return true;
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }
}
