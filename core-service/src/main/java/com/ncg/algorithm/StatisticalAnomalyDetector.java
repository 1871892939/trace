package com.ncg.algorithm;

import com.ncg.service.ConfigService;
import com.ncg.model.LogisticsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基于 3σ 原则（拉依达准则）的统计异常检测
 *
 * 设计思路：无需训练，直接基于历史数据统计特性判定异常
 * 原理：正态分布下，99.73% 的数据落在 μ±3σ 范围内
 * 应用：温度、湿度等物流指标及检测值的动态基线异常检测
 *
 * σ 系数从 ConfigService 动态读取，不再硬编码。
 */
@Component
public class StatisticalAnomalyDetector {

    @Autowired
    private ConfigService configService;

    /** 3σ 阈值系数 */
    private double getSigmaCoefficient() {
        return parseDouble(configService.getValue("anomaly.sigma.critical"), 3.0);
    }

    /** 2σ 阈值系数（用于预警） */
    private double getWarningSigmaCoefficient() {
        return parseDouble(configService.getValue("anomaly.sigma.warning"), 2.0);
    }

    // ==================== 基础统计 ====================

    public double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    public double calculateStdDev(List<Double> values, double mean) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }
        double sumSquaredDiff = 0.0;
        for (double v : values) {
            double diff = v - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }

    // ==================== 单值检测 ====================

    public boolean isAnomaly(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return false;
        }
        double deviation = Math.abs(value - mean);
        return deviation > (getSigmaCoefficient() * stdDev);
    }

    public boolean isWarning(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return false;
        }
        double deviation = Math.abs(value - mean);
        return deviation > (getWarningSigmaCoefficient() * stdDev);
    }

    /**
     * 异常分数（0-1），越接近 1 越异常
     * - 0-2σ: 0-0.3 分（正常范围）
     * - 2-3σ: 0.3-0.7 分（预警范围）
     * - 3σ以上: 0.7-1.0 分（异常范围）
     */
    public double calculateAnomalyScore(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return 0.0;
        }
        double sigmaLevel = Math.abs(value - mean) / stdDev;
        double warnSigma = getWarningSigmaCoefficient();
        double critSigma = getSigmaCoefficient();

        if (sigmaLevel <= warnSigma) {
            return sigmaLevel / warnSigma * 0.3;
        } else if (sigmaLevel <= critSigma) {
            return 0.3 + (sigmaLevel - warnSigma) / (critSigma - warnSigma) * 0.4;
        } else {
            return Math.min(1.0, 0.7 + (sigmaLevel - critSigma) * 0.1);
        }
    }

    public double detect(BigDecimal value, double mean, double stdDev) {
        return calculateAnomalyScore(value.doubleValue(), mean, stdDev);
    }

    // ==================== 批量检测（核心方法） ====================

    public BatchAnomalyResult detectBatch(List<LogisticsData> records) {
        if (records == null || records.isEmpty()) {
            return new BatchAnomalyResult(0.0, 0.0, false, false, 0.0);
        }

        List<Double> temps = records.stream()
                .map(r -> r.getTemperature().doubleValue())
                .toList();
        List<Double> hums = records.stream()
                .map(r -> r.getHumidity().doubleValue())
                .toList();

        double tempMean = calculateMean(temps);
        double tempStd = calculateStdDev(temps, tempMean);
        double humMean = calculateMean(hums);
        double humStd  = calculateStdDev(hums, humMean);

        double maxTempScore = temps.stream()
                .mapToDouble(t -> calculateAnomalyScore(t, tempMean, tempStd))
                .max().orElse(0.0);
        double maxHumScore  = hums.stream()
                .mapToDouble(h -> calculateAnomalyScore(h, humMean, humStd))
                .max().orElse(0.0);

        boolean tempAnomaly = temps.stream().anyMatch(t -> isAnomaly(t, tempMean, tempStd));
        boolean humAnomaly  = hums.stream().anyMatch(h -> isAnomaly(h, humMean, humStd));

        double compositeScore = Math.max(maxTempScore, maxHumScore);

        return new BatchAnomalyResult(
                maxTempScore,
                maxHumScore,
                tempAnomaly,
                humAnomaly,
                compositeScore
        );
    }

    public String getAnomalyLevel(double score) {
        if (score < 0.3) {
            return "NORMAL";
        } else if (score < 0.7) {
            return "WARNING";
        } else {
            return "ANOMALY";
        }
    }

    // ==================== 批量检测结果 ====================

    public record BatchAnomalyResult(
            double tempAnomalyScore,
            double humAnomalyScore,
            boolean tempAnomaly,
            boolean humAnomaly,
            double compositeScore
    ) {}

    private double parseDouble(String s, double defaultVal) {
        if (s == null) return defaultVal;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return defaultVal; }
    }
}
