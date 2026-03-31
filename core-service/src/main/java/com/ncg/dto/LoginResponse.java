package com.ncg.dto;

import lombok.Data;

/**
 * 登录响应 DTO
 */
@Data
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户角色
     */
    private String role;
    
    public LoginResponse(String token, String role) {
        this.token = token;
        this.role = role;
    }
}
