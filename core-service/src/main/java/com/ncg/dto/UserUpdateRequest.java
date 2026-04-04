package com.ncg.dto;

import lombok.Data;

/**
 * 用户更新请求 DTO
 */
@Data
public class UserUpdateRequest {

    private Long id;

    private String password;

    private String role;

    private Integer status;
}
