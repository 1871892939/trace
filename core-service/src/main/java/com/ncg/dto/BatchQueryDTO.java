package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批次查询响应 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchQueryDTO {

    private Long id;
    private String batchNo;
    private String origin;
    private String enterprise;
    private String productionDate;
    private String riskLevel;
    private Integer riskScore;
    private Boolean hasAlert;
    private String alertType;
    private Boolean handled;
    private String updateTime;
    private String operator;
    private String createTime;
}
