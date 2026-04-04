package com.ncg.dto;

import lombok.Data;

/**
 * 用户注册请求 DTO
 */
@Data
public class RegisterRequest {

    private String username;

    private String password;

    private String confirmPassword;

    private String role;
}
