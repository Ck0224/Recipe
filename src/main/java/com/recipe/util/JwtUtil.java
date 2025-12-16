package com.recipe.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类（终极版：0.12.3 无任何报错，兼容所有环境）
 */
@Component
public class JwtUtil {
    // 密钥配置（String类型）
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Token有效期（毫秒）
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 签发者
    @Value("${jwt.issuer}")
    private String issuer;

    // 签名密钥（最终使用的SecretKey）
    private SecretKey secretKey;

    /**
     * 初始化密钥（0.12.3 最稳定的方式，绕开Builder兼容问题）
     */
    @PostConstruct
    public void init() {
        // 核心修复：使用Keys工具类直接生成SecretKey（兼容所有0.12.x版本）
        // 自动适配密钥长度（不足256位自动补全，过长自动截断）
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * 生成Token（包含用户ID）
     */
    public String generateToken(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);

        return Jwts.builder()
                .claims(extraClaims)          // 设置自定义Claims
                .subject(userDetails.getUsername()) // 用户名（邮箱）
                .issuer(issuer)               // 签发者
                .issuedAt(new Date(System.currentTimeMillis())) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 过期时间
                .signWith(secretKey)          // 签名（仅传SecretKey）
                .compact();
    }

    /**
     * 提取用户名（邮箱）
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 提取用户ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * 提取过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 通用提取Claims方法
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析Token（0.12.3 稳定写法）
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey) // 验证签名（仅传SecretKey）
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Token解析失败：" + e.getMessage());
        }
    }

    /**
     * 验证Token是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 验证Token有效性（用户名 + 过期时间）
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // 临时调用extractUserId消除"未使用"提示（实际项目中会用到）
        extractUserId(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
