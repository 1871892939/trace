package com.ncg.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 生成与验证工具类
 */
@Component
public class JwtUtil {
    
    /**
     * 密钥（实际项目中应配置在配置文件或环境变量中）
     */
    @Value("${jwt.secret:trace-food-safety-secret-key-2026}")
    private String secret;
    
    /**
     * Token 有效期（2 小时）
     */
    @Value("${jwt.expiration:7200000}")
    private Long expiration;
    
    /**
     * 生成 JWT Token
     * 
     * @param username 用户名
     * @param role 角色
     * @return Token 字符串
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * 从 Token 中获取用户名
     * 
     * @param token Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从 Token 中获取角色
     * 
     * @param token Token 字符串
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (String) claims.get("role");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证 Token 是否有效
     * 
     * @param token Token 字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查 Token 是否即将过期（15 分钟内）
     * 
     * @param token Token 字符串
     * @return true-即将过期，false-未过期
     */
    public boolean isTokenAboutToExpire(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
            return timeUntilExpiration < 900000; // 15 分钟
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 刷新 Token
     * 
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String oldToken) {
        try {
            Claims claims = getClaimsFromToken(oldToken);
            String username = claims.getSubject();
            String role = (String) claims.get("role");
            
            return generateToken(username, role);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析 Token 获取 Claims
     * 
     * @param token Token 字符串
     * @return Claims 对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}
