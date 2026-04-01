package com.ncg.service;

import com.ncg.algorithm.RiskScoring;
import com.ncg.algorithm.StatisticalAnomalyDetector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 加权评分算法与统计异常检测测试
 */
@SpringBootTest
public class RiskEvaluationTest {
    
    @Autowired
    private RiskScoring riskScoring;
    
    @Autowired
    private StatisticalAnomalyDetector anomalyDetector;
    
    /**
     * 测试加权风险评分算法
     */
    @Test
    public void testRiskScoring() {
        System.out.println("========== 加权风险评分测试 ==========");
        
        // 测试用例 1：正常数据（各项指标合格）
        int score1 = riskScoring.calculateRiskScore(
            new BigDecimal("0.3"),   // 农残（合格）
            new BigDecimal("0.05"),  // 重金属（合格）
            new BigDecimal("100"),   // 微生物（合格）
            new BigDecimal("5"),     // 温度（适宜 0-10℃）
            new BigDecimal("55")     // 湿度（适宜 40-70%）
        );
        System.out.println("正常样本风险分：" + score1);
        System.out.println("风险等级：" + riskScoring.getRiskLevel(score1));
        
        // 测试用例 2：农残超标
        int score2 = riskScoring.calculateRiskScore(
            new BigDecimal("1.2"),   // 农残超标 2.4 倍
            new BigDecimal("0.05"),  // 重金属合格
            new BigDecimal("100"),   // 微生物合格
            new BigDecimal("5"),     // 温度适宜
            new BigDecimal("55")     // 湿度适宜
        );
        System.out.println("\n农残超标样本风险分：" + score2);
        System.out.println("风险等级：" + riskScoring.getRiskLevel(score2));
        
        // 测试用例 3：多重超标
        int score3 = riskScoring.calculateRiskScore(
            new BigDecimal("2.5"),   // 农残严重超标
            new BigDecimal("0.3"),   // 重金属超标
            new BigDecimal("500"),   // 微生物超标
            new BigDecimal("18"),    // 温度偏高
            new BigDecimal("80")     // 湿度偏高
        );
        System.out.println("\n多重超标样本风险分：" + score3);
        System.out.println("风险等级：" + riskScoring.getRiskLevel(score3));
        
        // 生成风险因素 JSON
        String factorsJson = riskScoring.generateRiskFactorsJson(
            new BigDecimal("1.2"),
            new BigDecimal("0.05"),
            new BigDecimal("100"),
            new BigDecimal("5"),
            new BigDecimal("55"),
            score2
        );
        System.out.println("\n风险因素 JSON:" + factorsJson);
        
        System.out.println("===========================================\n");
    }
    
    /**
     * 测试统计异常检测（3σ原则）
     */
    @Test
    public void testStatisticalAnomalyDetection() {
        System.out.println("========== 统计异常检测测试 ==========");
        
        // 模拟历史温度数据（均值约 25℃，标准差约 2℃）
        List<Double> historyTemps = Arrays.asList(
            24.5, 25.0, 24.8, 25.2, 25.5, 
            24.3, 25.1, 24.9, 25.3, 24.7,
            25.4, 24.6, 25.0, 25.2, 24.8
        );
        
        double mean = anomalyDetector.calculateMean(historyTemps);
        double stdDev = anomalyDetector.calculateStdDev(historyTemps, mean);
        
        System.out.println("历史温度均值：" + mean);
        System.out.println("历史温度标准差：" + stdDev);
        System.out.println("正常范围：[" + (mean - 3 * stdDev) + ", " + (mean + 3 * stdDev) + "]");
        
        // 测试正常值
        double normalTemp = 25.1;
        double normalScore = anomalyDetector.detect(new BigDecimal(normalTemp), mean, stdDev);
        System.out.println("\n当前温度 " + normalTemp + "℃:");
        System.out.println("异常分数：" + normalScore);
        System.out.println("判定：" + anomalyDetector.getAnomalyLevel(normalScore));
        
        // 测试预警值（2σ以上）
        double warningTemp = 28.5;
        double warningScore = anomalyDetector.detect(new BigDecimal(warningTemp), mean, stdDev);
        System.out.println("\n当前温度 " + warningTemp + "℃:");
        System.out.println("异常分数：" + warningScore);
        System.out.println("判定：" + anomalyDetector.getAnomalyLevel(warningScore));
        
        // 测试异常值（3σ以上）
        double anomalyTemp = 32.0;
        double anomalyScore = anomalyDetector.detect(new BigDecimal(anomalyTemp), mean, stdDev);
        System.out.println("\n当前温度 " + anomalyTemp + "℃:");
        System.out.println("异常分数：" + anomalyScore);
        System.out.println("判定：" + anomalyDetector.getAnomalyLevel(anomalyScore));
        
        System.out.println("===========================================\n");
    }
}
