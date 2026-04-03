package com.gym.management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    /**
     * JWT 签名密钥
     * 建议：生产环境务必使用复杂的随机字符串，并存储在环境变量或配置中心
     * 对应 application.yml: gym.jwt.secret
     */
    @Value("${gym.jwt.secret:GymManagementSystemSecretKey2026VeryLongAndSecureString!@#}")
    private String secret;

    /**
     * JWT 过期时间 (单位：毫秒)
     * 默认 7 天 (7 * 24 * 60 * 60 * 1000 = 604800000)
     * 对应 application.yml: gym.jwt.expiration
     */
    @Value("${gym.jwt.expiration:604800000}")
    private long expiration;

    /**
     * JWT 签发者标识
     * 对应 application.yml: gym.jwt.issuer
     */
    @Value("${gym.jwt.issuer:gym-management-system}")
    private String issuer;

    // Getter 方法
    public String getSecret() {
        return secret;
    }

    public long getExpiration() {
        return expiration;
    }

    public String getIssuer() {
        return issuer;
    }
}