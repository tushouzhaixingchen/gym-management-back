package com.gym.management.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${gym.jwt.secret}")
    private String secret;

    @Value("${gym.jwt.expiration}")
    private Long expiration; // 建议改为 Long，防止溢出，单位毫秒

    /**
     * 获取签名密钥
     * 使用 HS256 算法，密钥长度建议至少 256 bit (32字符)
     */
    private Key getSigningKey() {
        // 确保 secret 足够长，如果配置太短，Keys.hmacShaKeyFor 会抛异常
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 Token
     * @param userId 用户ID (管理员/教练/会员的主键)
     * @param account 账号标识 (用户名或手机号)
     * @param userType 用户类型 (ADMIN, COACH, MEMBER)
     */
    public String generateToken(Integer userId, String account, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))       // 标准主题：存用户ID
                .claim("account", account)                // 自定义声明：账号
                .claim("userType", userType)              // 自定义声明：用户类型 (关键!)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * 从 Token 中获取账号 (用户名/手机)
     */
    public String getAccountFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("account", String.class);
    }

    /**
     * 从 Token 中获取用户类型 (ADMIN/COACH/MEMBER)
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userType", String.class);
    }

    /**
     * 验证 Token 是否有效
     * @return true: 有效; false: 过期或签名错误
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 辅助方法：解析 Claims，统一处理异常
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 即使过期，我们也希望能拿到 Claims (比如为了提示用户“令牌已过期”)
            return e.getClaims();
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }
}