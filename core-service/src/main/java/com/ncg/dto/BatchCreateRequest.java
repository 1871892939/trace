package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批次录入请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchCreateRequest {

    private String batchNo;

    private String origin;

    private String enterprise;

    private String productionDate;
}
