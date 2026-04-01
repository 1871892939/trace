package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 数据模拟响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationResponse {

    /**
     * 生成数量
     */
    private Integer generated;

    /**
     * 批次统计
     */
    private Integer batchCount;

    /**
     * 预警统计
     */
    private Integer alertCount;

    /**
     * 风险分布：{ Low: 数量, Medium: 数量, High: 数量 }
     */
    private Map<String, Long> riskDistribution;

    /**
     * 生成的批次编号列表
     */
    private List<String> batchNos;

    /**
     * 操作详情
     */
    private String message;
}
