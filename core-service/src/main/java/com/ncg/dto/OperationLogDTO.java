package com.ncg.dto;

import lombok.Data;

/**
 * 操作日志列表 DTO
 */
@Data
public class OperationLogDTO {

    private Long id;

    private String username;

    private String role;

    private String roleName;

    private String operationType;

    private String operationTypeName;

    private String module;

    private String description;

    private String method;

    private String requestUrl;

    private String status;

    private String statusName;

    private String errorMsg;

    private String ipAddress;

    private String operator;

    /**
     * 批次编号
     */
    private String batchNo;

    /**
     * 批次新增时间
     */
    private String batchCreateTime;

    /**
     * 批次修改时间
     */
    private String batchUpdateTime;

    private String operateTime;
}
