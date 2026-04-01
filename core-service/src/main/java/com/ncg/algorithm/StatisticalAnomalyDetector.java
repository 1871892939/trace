package com.ncg.algorithm;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基于 3σ原则（拉依达准则）的统计异常检测
 * 
 * 设计思路：无需训练，直接基于历史数据统计特性判定异常
 * 原理：正态分布下，99.73% 的数据落在均值±3 倍标准差范围内
 * 应用：温度、湿度、检测值等指标的动态基线异常检测
 */
@Component
public class StatisticalAnomalyDetector {
    
    /**
     * 3σ阈值系数
     */
    private static final double SIGMA_COEFFICIENT = 3.0;
    
    /**
     * 2σ阈值系数（用于预警）
     */
    private static final double WARNING_SIGMA_COEFFICIENT = 2.0;
    
    /**
     * 检测单个值是否异常（3σ原则）
     * 
     * @param value 待检测值
     * @param mean 历史均值
     * @param stdDev 历史标准差
     * @return true-异常，false-正常
     */
    public boolean isAnomaly(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            // 标准差为 0 或负数，无法判断
            return false;
        }
        
        double deviation = Math.abs(value - mean);
        return deviation > (SIGMA_COEFFICIENT * stdDev);
    }
    
    /**
     * 检测单个值是否处于预警状态（2σ原则）
     * 
     * @param value 待检测值
     * @param mean 历史均值
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
     * 计算一组数据的均值
     * 
     * @param values 数据列表
     * @return 均值
     */
    public double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        
        return sum / values.size();
    }
    
    /**
     * 计算一组数据的标准差
     * 
     * @param values 数据列表
     * @return 标准差
     */
    public double calculateStdDev(List<Double> values, double mean) {
        if (values == null || values.isEmpty() || values.size() < 2) {
            return 0.0;
        }
        
        double sumSquaredDiff = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        // 样本标准差（除以 n-1）
        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }
    
    /**
     * 批量检测异常并返回异常分数（0-1）
     * 
     * @param value 待检测值
     * @param mean 历史均值
     * @param stdDev 历史标准差
     * @return 异常分数（越接近 1 越异常）
     */
    public double calculateAnomalyScore(double value, double mean, double stdDev) {
        if (stdDev <= 0) {
            return 0.0;
        }
        
        double deviation = Math.abs(value - mean);
        double sigmaLevel = deviation / stdDev;
        
        // 将σ水平映射到 0-1 范围
        // 0-2σ: 0-0.3 分（正常范围）
        // 2-3σ: 0.3-0.7 分（预警范围）
        // 3σ以上：0.7-1.0 分（异常范围）
        
        if (sigmaLevel <= WARNING_SIGMA_COEFFICIENT) {
            return sigmaLevel / WARNING_SIGMA_COEFFICIENT * 0.3;
        } else if (sigmaLevel <= SIGMA_COEFFICIENT) {
            return 0.3 + (sigmaLevel - WARNING_SIGMA_COEFFICIENT) / 
                    (SIGMA_COEFFICIENT - WARNING_SIGMA_COEFFICIENT) * 0.4;
        } else {
            return Math.min(1.0, 0.7 + (sigmaLevel - SIGMA_COEFFICIENT) * 0.1);
        }
    }
    
    /**
     * 检测 BigDecimal 类型数据
     * 
     * @param value 待检测值
     * @param mean 历史均值
     * @param stdDev 历史标准差
     * @return 异常分数
     */
    public double detect(BigDecimal value, double mean, double stdDev) {
        return calculateAnomalyScore(value.doubleValue(), mean, stdDev);
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
}
