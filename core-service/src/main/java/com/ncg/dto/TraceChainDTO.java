package com.ncg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 溯源链完整响应 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TraceChainDTO {

    /** 批次基本信息 */
    private BatchInfoNode batch;

    /** 检测数据节点 */
    private DetectionNode detection;

    /** 物流轨迹节点 */
    private List<LogisticsNode> logistics;

    /** 风险评估节点 */
    private RiskNode risk;

    /** 预警记录节点 */
    private List<AlertNode> alerts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchInfoNode {
        private Long id;
        private String batchNo;
        private String origin;
        private String enterprise;
        private String productionDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetectionNode {
        private Long id;
        private BigDecimal pesticide;
        private BigDecimal heavyMetal;
        private BigDecimal microbe;
        private String testTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogisticsNode {
        private Long id;
        private BigDecimal gpsLng;
        private BigDecimal gpsLat;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private String recordTime;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RiskNode {
        private Long id;
        private String riskLevel;
        private Integer riskScore;
        private String assessmentDate;
        private String factors;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlertNode {
        private Long id;
        private String alertType;
        private BigDecimal riskScore;
        private String createTime;
        private Boolean handled;
    }
}
