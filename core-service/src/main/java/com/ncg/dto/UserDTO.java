package com.ncg.dto;

import lombok.Data;

/**
 * 用户管理列表项 DTO
 */
@Data
public class UserDTO {

    private Long id;

    private String username;

    private String role;

    private String roleName;

    private Integer status;

    private String statusName;

    private String createTime;

    private Long loginCount;
}
