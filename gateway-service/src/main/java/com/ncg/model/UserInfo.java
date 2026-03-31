package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户信息实体类
 */
@Data
@TableName("user_info")
public class UserInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码 (BCrypt 加密)
     */
    private String password;
    
    /**
     * 角色：supervisor(监管员) / admin(管理员)
     */
    private String role;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
}
