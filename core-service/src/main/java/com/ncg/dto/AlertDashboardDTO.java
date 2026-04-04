package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 预警大盘响应 DTO
 * 与大盘概览的区别：聚焦预警业务，包含处理追踪、批次关联、预警等级细分等维度
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertDashboardDTO {

    /** 预警总数 */
    private Long totalCount;

    /** 已处理数 */
    private Long handledCount;

    /** 未处理数 */
    private Long unhandledCount;

    /** 处理率（百分比字符串，如 "75%") */
    private String handleRate;

    /** 预警类型分布：{ TEMP: 数量, ... } */
    private Map<String, Long> typeDistribution;

    /** 预警等级分布：紧急 / 重要 / 一般（根据 riskScore 区间划分） */
    private LevelDistribution levelDistribution;

    /** 近7天每日预警数量趋势 */
    private List<Long> weekTrend;

    /** 近7天日期标签 */
    private List<String> trendLabels;

    /** TOP 预警批次（触发预警最多的批次） */
    private List<BatchAlertStat> topAlertBatches;

    /** 处理时效统计：今日处理数 / 本周处理数 / 本月处理数 */
    private HandleTimeStats handleTimeStats;

    /** 未处理预警列表（最新5条） */
    private List<AlertListDTO> recentUnhandled;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LevelDistribution {
        private Long urgent;   // riskScore >= 0.8，高危
        private Long serious;  // riskScore >= 0.5
        private Long normal;   // riskScore < 0.5
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchAlertStat {
        private Long batchId;
        private String batchNo;
        private String origin;
        private String enterprise;
        private Long alertCount;
        private Boolean hasUnhandle;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HandleTimeStats {
        private Long todayHandleCount;
        private Long weekHandleCount;
        private Long monthHandleCount;
        private Double avgHandleHours;
    }
}
