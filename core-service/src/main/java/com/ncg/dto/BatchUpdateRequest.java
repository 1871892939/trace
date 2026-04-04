package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批次编辑请求 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchUpdateRequest {

    private Long id;

    private String origin;

    private String enterprise;

    private String productionDate;
}
