package com.ncg.algorithm;

import com.ncg.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 基于规则加权的食品安全风险评分算法
 *
 * 设计思路：摒弃复杂机器学习，采用监管规则驱动的加权评分机制
 * 评分范围：0-100 分
 * 风险等级：Low(0-40) / Medium(41-70) / High(71-100)
 *
 * 所有阈值和权重均从 ConfigService 动态读取，不再硬编码。
 */
@Component
public class RiskScoring {

    @Autowired
    private ConfigService configService;

    // ==================== 检测指标权重（默认静态值，ConfigService 未就绪时兜底）====================

    private double getWeightPesticide() {
        return parseDouble(configService.getValue("risk.weight.pesticide"), 35.0);
    }

    private double getWeightHeavyMetal() {
        return parseDouble(configService.getValue("risk.weight.heavy_metal"), 35.0);
    }

    private double getWeightMicrobe() {
        return parseDouble(configService.getValue("risk.weight.microbe"), 30.0);
    }

    private double getWeightTemp() {
        return parseDouble(configService.getValue("risk.weight.temp"), 60.0);
    }

    private double getWeightHumidity() {
        return parseDouble(configService.getValue("risk.weight.humidity"), 40.0);
    }

    // ==================== 国标限量阈值（从配置读取）====================

    private BigDecimal getPesticideLimit() {
        return parseNumeric(configService.getValue("limit.pesticide"), new BigDecimal("0.5"));
    }

    private BigDecimal getHeavyMetalLimit() {
        return parseNumeric(configService.getValue("limit.heavy_metal"), new BigDecimal("0.1"));
    }

    private BigDecimal getMicrobeLimit() {
        return parseNumeric(configService.getValue("limit.microbe"), new BigDecimal("200"));
    }

    private BigDecimal getTempMin() {
        return parseNumeric(configService.getValue("limit.temp.min"), BigDecimal.ZERO);
    }

    private BigDecimal getTempMax() {
        return parseNumeric(configService.getValue("limit.temp.max"), new BigDecimal("10"));
    }

    private BigDecimal getHumidityMin() {
        return parseNumeric(configService.getValue("limit.humidity.min"), new BigDecimal("40"));
    }

    private BigDecimal getHumidityMax() {
        return parseNumeric(configService.getValue("limit.humidity.max"), new BigDecimal("70"));
    }

    // ==================== 风险等级阈值（从配置读取）====================

    private int getRiskLowThreshold() {
        return parseInt(configService.getValue("risk.low.threshold"), 40);
    }

    private int getRiskHighThreshold() {
        return parseInt(configService.getValue("risk.high.threshold"), 70);
    }

    /**
     * 计算综合风险评分
     *
     * @param pesticide  农残值
     * @param heavyMetal 重金属值
     * @param microbe    微生物值
     * @param tempMax    物流温度（取该批次中偏离最大的那条记录）
     * @param humMax     物流湿度（取该批次中偏离最大的那条记录）
     * @return 综合风险分 (0-100)
     */
    public int calculateRiskScore(BigDecimal pesticide, BigDecimal heavyMetal,
                                  BigDecimal microbe, BigDecimal tempMax, BigDecimal humMax) {

        double detectionWeight = parseDouble(configService.getValue("risk.weight.detection"), 70.0) / 100.0;
        double logisticsWeight = 1.0 - detectionWeight;

        int detectionScore = calculateDetectionRisk(pesticide, heavyMetal, microbe);
        int logisticsScore = calculateLogisticsRisk(tempMax, humMax);

        int total = (int) Math.round(detectionScore * detectionWeight + logisticsScore * logisticsWeight);
        return Math.min(total, 100);
    }

    // ==================== 检测指标评分 ====================

    private int calculateDetectionRisk(BigDecimal pesticide, BigDecimal heavyMetal, BigDecimal microbe) {
        double score = calculateSingleScore(pesticide, getPesticideLimit()) * (getWeightPesticide() / 100.0)
                     + calculateSingleScore(heavyMetal, getHeavyMetalLimit()) * (getWeightHeavyMetal() / 100.0)
                     + calculateSingleScore(microbe, getMicrobeLimit()) * (getWeightMicrobe() / 100.0);
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
        double ratio = value.divide(limit, 4, RoundingMode.HALF_UP).doubleValue();
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
        double score = calculateRangeScore(temperature, getTempMin(), getTempMax()) * (getWeightTemp() / 100.0)
                     + calculateRangeScore(humidity, getHumidityMin(), getHumidityMax()) * (getWeightHumidity() / 100.0);
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
            deviation = min.subtract(value).divide(range, 4, RoundingMode.HALF_UP);
        } else {
            deviation = value.subtract(max).divide(range, 4, RoundingMode.HALF_UP);
        }
        return Math.min(100, (int) Math.round(deviation.doubleValue() * 50));
    }

    // ==================== 风险等级判定 ====================

    public String getRiskLevel(int totalScore) {
        if (totalScore <= getRiskLowThreshold()) {
            return "Low";
        } else if (totalScore <= getRiskHighThreshold()) {
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
        int ps  = calculateSingleScore(pesticide, getPesticideLimit());
        int hs  = calculateSingleScore(heavyMetal, getHeavyMetalLimit());
        int ms  = calculateSingleScore(microbe, getMicrobeLimit());
        int ts  = calculateRangeScore(tempMax, getTempMin(), getTempMax());
        int hms = calculateRangeScore(humMax, getHumidityMin(), getHumidityMax());

        return String.format(
            "{\"total_score\":%d,\"risk_level\":\"%s\"," +
            "\"detection\":{\"pesticide_score\":%d,\"heavy_metal_score\":%d,\"microbe_score\":%d}," +
            "\"logistics\":{\"temperature_score\":%d,\"humidity_score\":%d}}",
            totalScore, getRiskLevel(totalScore), ps, hs, ms, ts, hms
        );
    }

    private double parseDouble(String s, double defaultVal) {
        if (s == null) return defaultVal;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return defaultVal; }
    }

    private int parseInt(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
    }

    private BigDecimal parseNumeric(String s, BigDecimal defaultVal) {
        if (s == null) return defaultVal;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return defaultVal; }
    }
}
