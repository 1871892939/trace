package com.ncg.algorithm;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 基于规则加权的食品安全风险评分算法
 *
 * 设计思路：摒弃复杂机器学习，采用监管规则驱动的加权评分机制
 * 评分范围：0-100 分
 * 风险等级：Low(0-40) / Medium(41-70) / High(71-100)
 */
@Component
public class RiskScoring {

    // ==================== 权重配置（检测 70% + 物流 30%）====================

    private static final double WEIGHT_PESTICIDE = 35.0;
    private static final double WEIGHT_HEAVY_METAL = 35.0;
    private static final double WEIGHT_MICROBE = 30.0;

    private static final double WEIGHT_TEMP = 60.0;
    private static final double WEIGHT_HUMIDITY = 40.0;

    // ==================== 国标阈值 ====================

    /** 农残限量阈值（mg/kg）- GB 2763 */
    private static final BigDecimal PESTICIDE_LIMIT = new BigDecimal("0.5");

    /** 重金属限量阈值（mg/kg）- GB 2762 */
    private static final BigDecimal HEAVY_METAL_LIMIT = new BigDecimal("0.1");

    /** 微生物限量阈值（CFU/g）- GB 29921 */
    private static final BigDecimal MICROBE_LIMIT = new BigDecimal("200");

    /** 冷链适宜温度（℃） */
    private static final BigDecimal TEMP_MIN = new BigDecimal("0");
    private static final BigDecimal TEMP_MAX = new BigDecimal("10");

    /** 适宜湿度（%） */
    private static final BigDecimal HUMIDITY_MIN = new BigDecimal("40");
    private static final BigDecimal HUMIDITY_MAX = new BigDecimal("70");

    // ==================== 风险等级阈值 ====================

    private static final int RISK_LOW_THRESHOLD = 40;
    private static final int RISK_HIGH_THRESHOLD = 70;

    /**
     * 计算综合风险评分
     *
     * @param pesticide  农残值
     * @param heavyMetal 重金属值
     * @param microbe     微生物值
     * @param tempMax     物流温度（取该批次中偏离最大的那条记录）
     * @param humMax      物流湿度（取该批次中偏离最大的那条记录）
     * @return 综合风险分 (0-100)
     */
    public int calculateRiskScore(BigDecimal pesticide, BigDecimal heavyMetal,
                                  BigDecimal microbe, BigDecimal tempMax, BigDecimal humMax) {

        int detectionScore = calculateDetectionRisk(pesticide, heavyMetal, microbe);
        int logisticsScore  = calculateLogisticsRisk(tempMax, humMax);

        int total = (int) Math.round(detectionScore * 0.7 + logisticsScore * 0.3);
        return Math.min(total, 100);
    }

    /**
     * 兼容旧接口（单条物流记录）
     */
    public int calculateRiskScore(BigDecimal pesticide, BigDecimal heavyMetal,
                                  BigDecimal microbe, BigDecimal temperature, BigDecimal humidity) {
        return calculateRiskScore(pesticide, heavyMetal, microbe, temperature, humidity);
    }

    // ==================== 检测指标评分 ====================

    private int calculateDetectionRisk(BigDecimal pesticide, BigDecimal heavyMetal, BigDecimal microbe) {
        double score = calculateSingleScore(pesticide, PESTICIDE_LIMIT) * (WEIGHT_PESTICIDE / 100.0)
                     + calculateSingleScore(heavyMetal, HEAVY_METAL_LIMIT) * (WEIGHT_HEAVY_METAL / 100.0)
                     + calculateSingleScore(microbe, MICROBE_LIMIT) * (WEIGHT_MICROBE / 100.0);
        return (int) Math.round(score);
    }

    /**
     * 超标型指标计分
     * - 未超标 → 0 分
     * - 超标 1 倍内 → 线性 0-60 分
     * - 超标 1-3 倍 → 加速 60-90 分
     * - 超标 3 倍以上 → 91-100 分
     */
    public int calculateSingleScore(BigDecimal value, BigDecimal limit) {
        if (value.compareTo(limit) <= 0) {
            return 0;
        }
        double ratio = value.divide(limit, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        if (ratio <= 2.0) {
            return (int) Math.round(ratio * 30);
        } else if (ratio <= 4.0) {
            return 60 + (int) Math.round((ratio - 2.0) * 15);
        } else {
            return Math.min(100, 90 + (int) Math.round((ratio - 4.0) * 5));
        }
    }

    // ==================== 物流环境评分 ====================

    private int calculateLogisticsRisk(BigDecimal temperature, BigDecimal humidity) {
        double score = calculateRangeScore(temperature, TEMP_MIN, TEMP_MAX) * (WEIGHT_TEMP / 100.0)
                     + calculateRangeScore(humidity, HUMIDITY_MIN, HUMIDITY_MAX) * (WEIGHT_HUMIDITY / 100.0);
        return (int) Math.round(score);
    }

    /**
     * 范围型指标计分
     * - 在范围内 → 0 分
     * - 范围外 → 按相对偏离度计分，上限 100 分
     */
    public int calculateRangeScore(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
            return 0;
        }
        BigDecimal range = max.subtract(min);
        BigDecimal deviation;
        if (value.compareTo(min) < 0) {
            deviation = min.subtract(value).divide(range, 4, BigDecimal.ROUND_HALF_UP);
        } else {
            deviation = value.subtract(max).divide(range, 4, BigDecimal.ROUND_HALF_UP);
        }
        return Math.min(100, (int) Math.round(deviation.doubleValue() * 50));
    }

    // ==================== 风险等级判定 ====================

    public String getRiskLevel(int totalScore) {
        if (totalScore <= RISK_LOW_THRESHOLD) {
            return "Low";
        } else if (totalScore <= RISK_HIGH_THRESHOLD) {
            return "Medium";
        } else {
            return "High";
        }
    }

    // ==================== 评分明细 JSON ====================

    /**
     * 生成风险因素明细 JSON
     * 包含各项指标得分，供落库 risk_assessment.factors 使用
     */
    public String generateRiskFactorsJson(BigDecimal pesticide, BigDecimal heavyMetal,
                                          BigDecimal microbe, BigDecimal tempMax, BigDecimal humMax,
                                          int totalScore) {
        int ps  = calculateSingleScore(pesticide, PESTICIDE_LIMIT);
        int hs  = calculateSingleScore(heavyMetal, HEAVY_METAL_LIMIT);
        int ms  = calculateSingleScore(microbe, MICROBE_LIMIT);
        int ts  = calculateRangeScore(tempMax, TEMP_MIN, TEMP_MAX);
        int hms = calculateRangeScore(humMax, HUMIDITY_MIN, HUMIDITY_MAX);

        return String.format(
            "{\"total_score\":%d,\"risk_level\":\"%s\"," +
            "\"detection\":{\"pesticide_score\":%d,\"heavy_metal_score\":%d,\"microbe_score\":%d}," +
            "\"logistics\":{\"temperature_score\":%d,\"humidity_score\":%d}}",
            totalScore, getRiskLevel(totalScore), ps, hs, ms, ts, hms
        );
    }
}
