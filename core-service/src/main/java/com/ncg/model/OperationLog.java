package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志审计实体
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户
     */
    private String username;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 操作类型：CREATE / UPDATE / DELETE
     */
    private String operationType;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 操作描述
     */
    private String description;

    /**
     * HTTP 方法
     */
    private String method;

    /**
     * 请求路径
     */
    private String requestUrl;

    /**
     * 请求参数（脱敏后）
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 操作结果：SUCCESS / FAIL
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 客户端 IP
     */
    private String ipAddress;

    /**
     * 操作人（冗余存储）
     */
    private String operator;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;
}
