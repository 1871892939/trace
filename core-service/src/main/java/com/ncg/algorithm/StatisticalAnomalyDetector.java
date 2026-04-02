package com.ncg.algorithm;

import com.ncg.model.LogisticsData;
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
 * 接口层次：
 *   - 3σ 异常（isAnomaly）
 *   - 2σ 预警（isWarning）
 *   - 连续异常分数 0-1（calculateAnomalyScore）
 *   - 批量物流检测（detectBatch）
 */
@Component
public class StatisticalAnomalyDetector {

    /** 3σ 阈值系数 */
    private static final double SIGMA_COEFFICIENT = 3.0;

    /** 2σ 阈值系数（用于预警） */
    private static final double WARNING_SIGMA_COEFFICIENT = 2.0;

    // ==================== 基础统计 ====================

    /**
     * 计算均值
     */
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

    /**
     * 计算样本标准差（除以 n-1）
     */
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

    /**
     * 检测单个值是否异常（3σ 原则）
     *
     * @param value  待检测值
     * @param mean  历史均值
     * @param stdDev 历史标准差
     * @return true-异常，false-正常
     */
    public boolean isAnomaly(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return false;
        }
        double deviation = Math.abs(value - mean);
        return deviation > (SIGMA_COEFFICIENT * stdDev);
    }

    /**
     * 检测单个值是否处于预警状态（2σ 原则）
     *
     * @param value  待检测值
     * @param mean  历史均值
     * @param stdDev 历史标准差
     * @return true-预警，false-正常
     */
    public boolean isWarning(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return false;
        }
        double deviation = Math.abs(value - mean);
        return deviation > (WARNING_SIGMA_COEFFICIENT * stdDev);
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
        if (sigmaLevel <= WARNING_SIGMA_COEFFICIENT) {
            return sigmaLevel / WARNING_SIGMA_COEFFICIENT * 0.3;
        } else if (sigmaLevel <= SIGMA_COEFFICIENT) {
            return 0.3 + (sigmaLevel - WARNING_SIGMA_COEFFICIENT)
                         / (SIGMA_COEFFICIENT - WARNING_SIGMA_COEFFICIENT) * 0.4;
        } else {
            return Math.min(1.0, 0.7 + (sigmaLevel - SIGMA_COEFFICIENT) * 0.1);
        }
    }

    /**
     * BigDecimal 版本的异常分数
     */
    public double detect(BigDecimal value, double mean, double stdDev) {
        return calculateAnomalyScore(value.doubleValue(), mean, stdDev);
    }

    // ==================== 批量检测（核心方法） ====================

    /**
     * 批量物流数据统计异常检测
     * 一次性对一批物流记录做 3σ 建模，返回各维度异常分
     *
     * @param records 物流记录列表（同一批次）
     * @return 批量检测结果
     */
    public BatchAnomalyResult detectBatch(List<LogisticsData> records) {
        if (records == null || records.isEmpty()) {
            return new BatchAnomalyResult(0.0, 0.0, false, false, 0.0);
        }

        // 提取温度序列和湿度序列
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

        // 取各时刻异常分，取最大值代表整批最异常状态
        double maxTempScore = temps.stream()
                .mapToDouble(t -> calculateAnomalyScore(t, tempMean, tempStd))
                .max().orElse(0.0);
        double maxHumScore  = hums.stream()
                .mapToDouble(h -> calculateAnomalyScore(h, humMean, humStd))
                .max().orElse(0.0);

        boolean tempAnomaly = temps.stream().anyMatch(t -> isAnomaly(t, tempMean, tempStd));
        boolean humAnomaly  = hums.stream().anyMatch(h -> isAnomaly(h, humMean, humStd));

        // 综合异常分 = max(温度异常分, 湿度异常分)
        double compositeScore = Math.max(maxTempScore, maxHumScore);

        return new BatchAnomalyResult(
                maxTempScore,
                maxHumScore,
                tempAnomaly,
                humAnomaly,
                compositeScore
        );
    }

    /**
     * 获取异常等级描述
     *
     * @param score 异常分数
     * @return 等级描述
     */
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

    /**
     * 批量物流检测结果封装
     *
     * @param tempAnomalyScore  温度最大异常分 (0-1)
     * @param humAnomalyScore   湿度最大异常分 (0-1)
     * @param tempAnomaly       温度是否存在 3σ 异常
     * @param humAnomaly        湿度是否存在 3σ 异常
     * @param compositeScore    综合异常分 (0-1)
     */
    public record BatchAnomalyResult(
            double tempAnomalyScore,
            double humAnomalyScore,
            boolean tempAnomaly,
            boolean humAnomaly,
            double compositeScore
    ) {}
}
