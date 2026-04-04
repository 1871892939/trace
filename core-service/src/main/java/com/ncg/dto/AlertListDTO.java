package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 预警列表项 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertListDTO {

    private Long id;
    private Long batchId;
    private String batchNo;
    private String origin;
    private String enterprise;
    private String alertType;
    private BigDecimal riskScore;
    private String createTime;
    private Boolean handled;
}
