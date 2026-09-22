package com.shop.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责生成和解析登录令牌
 */
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire-hours:24}")
    private long expireHours;

    /**
     * 生成 JWT
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT 字符串
     */
    public String createToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expireTime)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT，非法或过期时返回 null
     */
    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return new LoginUser(
                    claims.get("userId", Long.class),
                    claims.getSubject(),
                    claims.get("role", String.class)
            );
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

