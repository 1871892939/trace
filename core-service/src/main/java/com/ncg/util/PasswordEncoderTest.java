package com.ncg.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密工具（用于生成测试数据）
 */
public class PasswordEncoderTest {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 123456 的 BCrypt 密码
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("原始密码：" + rawPassword);
        System.out.println("BCrypt 加密后：" + encodedPassword);
        System.out.println("\n验证匹配：" + encoder.matches(rawPassword, encodedPassword));
        
        // 生成几个不同的哈希值（BCrypt 每次生成的都不同）
        System.out.println("\n多个哈希示例：");
        for (int i = 0; i < 3; i++) {
            System.out.println((i + 1) + ". " + encoder.encode(rawPassword));
        }
    }
}
