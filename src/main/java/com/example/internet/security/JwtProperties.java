package com.example.internet.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项，见 application.yml 中 jwt 前缀
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HMAC-SHA256 签名密钥，不少于 32 字节 */
    private String secret;

    /** Token 有效期（分钟） */
    private long expireMinutes = 720;
}
