package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 用户名
     */
    private String username;
}
