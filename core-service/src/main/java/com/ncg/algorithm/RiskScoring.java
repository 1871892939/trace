package com.ncg.algorithm;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于规则加权的食品安全风险评分算法
 * 
 * 设计思路：摒弃复杂机器学习，采用监管规则驱动的加权评分机制
 * 评分范围：0-100 分
 * 风险等级：Low(0-40) / Medium(41-70) / High(71-100)
 */
@Component
public class RiskScoring {
    
    // ==================== 权重配置（可根据监管政策调整）====================
    
    /**
     * 检测指标权重（总和 100）
     */
    private static final double WEIGHT_PESTICIDE = 35.0;    // 农残权重 35%
    private static final double WEIGHT_HEAVY_METAL = 35.0;  // 重金属权重 35%
    private static final double WEIGHT_MICROBE = 30.0;      // 微生物权重 30%
    
    /**
     * 物流环境权重（总和 100）
     */
    private static final double WEIGHT_TEMP = 60.0;         // 温度权重 60%
    private static final double WEIGHT_HUMIDITY = 40.0;     // 湿度权重 40%
    
    // ==================== 阈值标准（根据食品安全国家标准）====================
    
    /**
     * 农残限量阈值（mg/kg）- 参考 GB 2763
     */
    private static final BigDecimal PESTICIDE_LIMIT = new BigDecimal("0.5");
    
    /**
     * 重金属限量阈值（mg/kg）- 参考 GB 2762
     */
    private static final BigDecimal HEAVY_METAL_LIMIT = new BigDecimal("0.1");
    
    /**
     * 微生物限量阈值（CFU/g）- 参考 GB 29921
     */
    private static final BigDecimal MICROBE_LIMIT = new BigDecimal("200");
    
    /**
     * 冷链食品适宜温度范围（℃）
     */
    private static final BigDecimal TEMP_MIN = new BigDecimal("0");
    private static final BigDecimal TEMP_MAX = new BigDecimal("10");
    
    /**
     * 适宜湿度范围（%）
     */
    private static final BigDecimal HUMIDITY_MIN = new BigDecimal("40");
    private static final BigDecimal HUMIDITY_MAX = new BigDecimal("70");
    
    // ==================== 风险等级阈值 ====================
    
    private static final int RISK_LOW_THRESHOLD = 40;
    private static final int RISK_HIGH_THRESHOLD = 70;
    
    /**
     * 计算综合风险评分
     * 
     * @param pesticide 农残值
     * @param heavyMetal 重金属值
     * @param microbe 微生物值
     * @param temperature 温度
     * @param humidity 湿度
     * @return 风险总分 (0-100)
     */
    public int calculateRiskScore(BigDecimal pesticide, BigDecimal heavyMetal, 
                                   BigDecimal microbe, BigDecimal temperature, 
                                   BigDecimal humidity) {
        
        // 1. 计算检测指标风险分（0-100）
        int detectionScore = calculateDetectionRisk(pesticide, heavyMetal, microbe);
        
        // 2. 计算物流环境风险分（0-100）
        int logisticsScore = calculateLogisticsRisk(temperature, humidity);
        
        // 3. 加权计算综合风险分（检测 70% + 物流 30%）
        int totalScore = (int) Math.round(detectionScore * 0.7 + logisticsScore * 0.3);
        
        return Math.min(totalScore, 100); // 不超过 100
    }
    
    /**
     * 计算检测指标风险分
     */
    private int calculateDetectionRisk(BigDecimal pesticide, BigDecimal heavyMetal, BigDecimal microbe) {
        // 农残风险分（0-100）
        int pesticideScore = calculateSingleScore(pesticide, PESTICIDE_LIMIT);
        
        // 重金属风险分（0-100）
        int heavyMetalScore = calculateSingleScore(heavyMetal, HEAVY_METAL_LIMIT);
        
        // 微生物风险分（0-100）
        int microbeScore = calculateSingleScore(microbe, MICROBE_LIMIT);
        
        // 加权求和
        double totalScore = pesticideScore * (WEIGHT_PESTICIDE / 100.0)
                          + heavyMetalScore * (WEIGHT_HEAVY_METAL / 100.0)
                          + microbeScore * (WEIGHT_MICROBE / 100.0);
        
        return (int) Math.round(totalScore);
    }
    
    /**
     * 计算物流环境风险分
     */
    private int calculateLogisticsRisk(BigDecimal temperature, BigDecimal humidity) {
        // 温度风险分（0-100）
        int tempScore = calculateRangeScore(temperature, TEMP_MIN, TEMP_MAX);
        
        // 湿度风险分（0-100）
        int humidityScore = calculateRangeScore(humidity, HUMIDITY_MIN, HUMIDITY_MAX);
        
        // 加权求和
        double totalScore = tempScore * (WEIGHT_TEMP / 100.0) 
                          + humidityScore * (WEIGHT_HUMIDITY / 100.0);
        
        return (int) Math.round(totalScore);
    }
    
    /**
     * 计算单项指标风险分（超标越严重，分数越高）
     * 
     * @param value 实际检测值
     * @param limit 标准限值
     * @return 风险分 (0-100)
     */
    private int calculateSingleScore(BigDecimal value, BigDecimal limit) {
        if (value.compareTo(limit) <= 0) {
            // 未超标，0 分
            return 0;
        }
        
        // 超标倍数
        BigDecimal ratio = value.divide(limit, 2, BigDecimal.ROUND_HALF_UP);
        
        // 超标 1 倍以内：线性计分（0-60 分）
        if (ratio.compareTo(new BigDecimal("2")) <= 0) {
            return (int) Math.round(ratio.doubleValue() * 30);
        }
        // 超标 1-3 倍：加速计分（60-90 分）
        else if (ratio.compareTo(new BigDecimal("4")) <= 0) {
            return 60 + (int) Math.round((ratio.doubleValue() - 2) * 15);
        }
        // 超标 3 倍以上：直接满分（91-100 分）
        else {
            return Math.min(100, 90 + (int) Math.round((ratio.doubleValue() - 4) * 5));
        }
    }
    
    /**
     * 计算范围型指标风险分（超出范围越远，分数越高）
     * 
     * @param value 实际值
     * @param min 最小值
     * @param max 最大值
     * @return 风险分 (0-100)
     */
    private int calculateRangeScore(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
            // 在范围内，0 分
            return 0;
        }
        
        // 计算超出范围的相对距离
        BigDecimal range = max.subtract(min);
        BigDecimal deviation;
        
        if (value.compareTo(min) < 0) {
            // 低于下限
            deviation = min.subtract(value).divide(range, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            // 高于上限
            deviation = value.subtract(max).divide(range, 2, BigDecimal.ROUND_HALF_UP);
        }
        
        // 偏离度转换为分数
        double score = deviation.doubleValue() * 50;
        return Math.min(100, (int) Math.round(score));
    }
    
    /**
     * 根据总分判定风险等级
     * 
     * @param totalScore 风险总分 (0-100)
     * @return 风险等级
     */
    public String getRiskLevel(int totalScore) {
        if (totalScore <= RISK_LOW_THRESHOLD) {
            return "Low";
        } else if (totalScore <= RISK_HIGH_THRESHOLD) {
            return "Medium";
        } else {
            return "High";
        }
    }
    
    /**
     * 生成风险因素明细（JSON 格式）
     * 
     * @param pesticide 农残值
     * @param heavyMetal 重金属值
     * @param microbe 微生物值
     * @param temperature 温度
     * @param humidity 湿度
     * @param totalScore 总分
     * @return JSON 字符串
     */
    public String generateRiskFactorsJson(BigDecimal pesticide, BigDecimal heavyMetal,
                                          BigDecimal microbe, BigDecimal temperature,
                                          BigDecimal humidity, int totalScore) {
        Map<String, Object> factors = new HashMap<>();
        factors.put("total_score", totalScore);
        factors.put("risk_level", getRiskLevel(totalScore));
        
        // 检测指标明细
        Map<String, Object> detection = new HashMap<>();
        detection.put("pesticide_value", pesticide);
        detection.put("pesticide_limit", PESTICIDE_LIMIT);
        detection.put("pesticide_score", calculateSingleScore(pesticide, PESTICIDE_LIMIT));
        
        detection.put("heavy_metal_value", heavyMetal);
        detection.put("heavy_metal_limit", HEAVY_METAL_LIMIT);
        detection.put("heavy_metal_score", calculateSingleScore(heavyMetal, HEAVY_METAL_LIMIT));
        
        detection.put("microbe_value", microbe);
        detection.put("microbe_limit", MICROBE_LIMIT);
        detection.put("microbe_score", calculateSingleScore(microbe, MICROBE_LIMIT));
        
        factors.put("detection", detection);
        
        // 物流环境明细
        Map<String, Object> logistics = new HashMap<>();
        logistics.put("temperature_value", temperature);
        logistics.put("temperature_range", TEMP_MIN + "-" + TEMP_MAX);
        logistics.put("temperature_score", calculateRangeScore(temperature, TEMP_MIN, TEMP_MAX));
        
        logistics.put("humidity_value", humidity);
        logistics.put("humidity_range", HUMIDITY_MIN + "-" + HUMIDITY_MAX);
        logistics.put("humidity_score", calculateRangeScore(humidity, HUMIDITY_MIN, HUMIDITY_MAX));
        
        factors.put("logistics", logistics);
        
        // 转为 JSON 字符串（简化实现，实际可用 Jackson/Gson）
        StringBuilder json = new StringBuilder("{");
        json.append("\"total_score\":").append(totalScore).append(",");
        json.append("\"risk_level\":\"").append(getRiskLevel(totalScore)).append("\",");
        json.append("\"detection\":{");
        json.append("\"pesticide_score\":").append(calculateSingleScore(pesticide, PESTICIDE_LIMIT)).append(",");
        json.append("\"heavy_metal_score\":").append(calculateSingleScore(heavyMetal, HEAVY_METAL_LIMIT)).append(",");
        json.append("\"microbe_score\":").append(calculateSingleScore(microbe, MICROBE_LIMIT));
        json.append("},");
        json.append("\"logistics\":{");
        json.append("\"temperature_score\":").append(calculateRangeScore(temperature, TEMP_MIN, TEMP_MAX)).append(",");
        json.append("\"humidity_score\":").append(calculateRangeScore(humidity, HUMIDITY_MIN, HUMIDITY_MAX));
        json.append("}");
        json.append("}");
        
        return json.toString();
    }
}
