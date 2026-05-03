package com.ncg.web.controller;

import com.ncg.dto.LoginRequest;
import com.ncg.dto.LoginResponse;
import com.ncg.dto.RegisterRequest;
import com.ncg.service.AuthService;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 登录响应（Token + 角色）
     */
    @PostMapping("/login")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(
            value = "auth:login",
            blockHandler = "loginBlockHandler",
            blockHandlerClass = AuthController.class
    )
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LoginResponse response = authService.login(request);
            
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", Map.of(
                "token", response.getToken(),
                "role", response.getRole(),
                "username", response.getUsername()
            ));
            
        } catch (Exception e) {
            result.put("code", 401);
            result.put("message", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 用户登出
     *
     * @param token Token（从请求头获取）
     * @return 操作结果
     */
    @PostMapping("/logout")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "auth:logout")
    public Map<String, Object> logout(@RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 去除 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            authService.logout(token);
            
            result.put("code", 200);
            result.put("message", "success");
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "登出失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 刷新 Token
     * 
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    @PostMapping("/refresh")
    public Map<String, Object> refreshToken(@RequestParam("token") String oldToken) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String newToken = authService.refreshToken(oldToken);
            
            if (newToken != null) {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", Map.of("token", newToken));
            } else {
                result.put("code", 401);
                result.put("message", "Token 已过期，请重新登录");
            }
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "刷新 Token 失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "auth:register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            authService.register(request);
            result.put("code", 200);
            result.put("message", "注册成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // ==================== Sentinel 限流兜底处理 ====================

    /**
     * login 接口的限流兜底处理方法
     * 当 QPS 超过阈值时，Sentinel 自动调用此方法而非原 login()
     */
    public static Map<String, Object> loginBlockHandler(LoginRequest request,
                                                         BlockException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("message", "登录请求过于频繁，请稍后重试");
        result.put("data", null);
        return result;
    }
}
