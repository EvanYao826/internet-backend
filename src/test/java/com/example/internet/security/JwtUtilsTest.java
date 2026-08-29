package com.example.internet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-key-0123456789abcdef0123456789abcdef");
        properties.setExpireMinutes(60);
        jwtUtils = new JwtUtils(properties);
    }

    @Test
    @DisplayName("签发后可解析出用户名与用户 ID")
    void generateAndParse() {
        String token = jwtUtils.generateToken(1L, "admin");
        Claims claims = jwtUtils.parseToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, jwtUtils.getUserId(claims));
        assertTrue(jwtUtils.validate(token));
    }

    @Test
    @DisplayName("篡改的 Token 校验失败")
    void tamperedToken() {
        String token = jwtUtils.generateToken(1L, "admin");
        // 翻转签名部分最后一个字符，保证长度不变而内容被篡改
        char last = token.charAt(token.length() - 1);
        char flipped = last == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, token.length() - 1) + flipped;
        assertFalse(jwtUtils.validate(tampered));
    }

    @Test
    @DisplayName("过期 Token 校验失败")
    void expiredToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-key-0123456789abcdef0123456789abcdef");
        properties.setExpireMinutes(-1);
        JwtUtils expiredUtils = new JwtUtils(properties);
        String token = expiredUtils.generateToken(1L, "admin");
        assertThrows(JwtException.class, () -> jwtUtils.parseToken(token));
        assertFalse(jwtUtils.validate(token));
    }
}
