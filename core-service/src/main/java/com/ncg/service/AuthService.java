package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.UserInfoMapper;
import com.ncg.dto.LoginRequest;
import com.ncg.dto.LoginResponse;
import com.ncg.model.UserInfo;
import com.ncg.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * BCrypt 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 初始化测试用户（开发环境用）
     */
    @javax.annotation.PostConstruct
    public void initTestUsers() {
        try {
            // 检查是否已有 admin 用户
            UserInfo admin = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, "admin")
            );
            
            if (admin == null) {
                // 创建测试用户
                UserInfo adminUser = new UserInfo();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("123456"));
                adminUser.setRole("admin");
                adminUser.setStatus(1);
                userInfoMapper.insert(adminUser);
                
                UserInfo supervisor = new UserInfo();
                supervisor.setUsername("supervisor01");
                supervisor.setPassword(passwordEncoder.encode("123456"));
                supervisor.setRole("supervisor");
                supervisor.setStatus(1);
                userInfoMapper.insert(supervisor);
                
                System.out.println("✅ 测试用户已自动创建：admin/123456, supervisor01/123456");
            }
        } catch (Exception e) {
            System.err.println("⚠️ 创建测试用户失败（可能表还未创建）: " + e.getMessage());
        }
    }
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（含 Token）
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户信息
        UserInfo user = userInfoMapper.selectOne(
            new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUsername, request.getUsername())
        );
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        
        // 5. 将 Token 存入 Redis（用于登出时使 Token 失效）
        String redisKey = "token:" + user.getUsername();
        redisTemplate.opsForValue().set(redisKey, token, 2, TimeUnit.HOURS);
        
        return new LoginResponse(token, user.getRole(), user.getUsername());
    }
    
    /**
     * 用户登出
     * 
     * @param token Token
     */
    public void logout(String token) {
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                // 从 Redis 删除 Token，使其失效
                String redisKey = "token:" + username;
                redisTemplate.delete(redisKey);
            }
        } catch (Exception e) {
            // 忽略异常
        }
    }
    
    /**
     * 刷新 Token
     * 
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String oldToken) {
        if (jwtUtil.validateToken(oldToken)) {
            return jwtUtil.refreshToken(oldToken);
        }
        return null;
    }
}
