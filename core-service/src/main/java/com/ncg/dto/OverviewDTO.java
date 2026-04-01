package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 大盘概览响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverviewDTO {

    // ==================== 基础统计 ====================

    /**
     * 批次总数
     */
    private Long totalBatches;

    /**
     * 预警总数
     */
    private Long totalAlerts;

    /**
     * 未处理预警数
     */
    private Long unhandledAlerts;

    // ==================== 风险分布 ====================

    /**
     * 风险等级分布：{ Low: 数量, Medium: 数量, High: 数量 }
     */
    private Map<String, Long> riskDistribution;

    /**
     * 风险评分分布（评分区间分布，用于直方图）
     * 区间：0-20, 20-40, 40-60, 60-80, 80-100
     */
    private List<Long> riskScoreHistogram;

    // ==================== 预警类型分布 ====================

    /**
     * 预警类型分布：{ TEMP: 数量, PESTICIDE: 数量, ... }
     */
    private Map<String, Long> alertTypeDistribution;

    // ==================== 趋势数据 ====================

    /**
     * 近30天批次新增趋势（每日数量）
     */
    private List<Long> batchTrend;

    /**
     * 近30天预警趋势（每日数量）
     */
    private List<Long> alertTrend;

    /**
     * 近30天日期标签
     */
    private List<String> trendLabels;

    // ==================== 物流统计 ====================

    /**
     * 平均温度
     */
    private BigDecimal avgTemperature;

    /**
     * 平均湿度
     */
    private BigDecimal avgHumidity;

    /**
     * 温度异常次数
     */
    private Long tempAnomalyCount;

    /**
     * 湿度异常次数
     */
    private Long humidityAnomalyCount;

    // ==================== 溯源链统计 ====================

    /**
     * 涉及产地数量
     */
    private Long totalOrigins;

    /**
     * 涉及企业数量
     */
    private Long totalEnterprises;

    /**
     * 批次详情（用于溯源链展示）
     */
    private List<BatchDetail> batchDetails;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchDetail {
        private Long id;
        private String batchNo;
        private String origin;
        private String enterprise;
        private String riskLevel;
        private Integer riskScore;
        private String productionDate;
        private Boolean hasAlert;
        private String alertType;
    }
}
