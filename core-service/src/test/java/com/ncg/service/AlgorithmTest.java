package com.ncg.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 算法功能测试类
 */
@SpringBootTest
public class AlgorithmTest {
    
    /**
     * 测试孤立森林异常检测算法
     */
    @Test
    public void testIsolationForest() {
        System.out.println("========== 孤立森林异常检测测试 ==========");
        
        // 生成训练数据（正常样本）
        double[][] trainingData = new double[100][12];
        for (int i = 0; i < 100; i++) {
            trainingData[i][0] = 25.0; // 温度
            trainingData[i][1] = 50.0; // 湿度
            trainingData[i][2] = 0.3;  // 农残
            trainingData[i][3] = 0.05; // 重金属
            trainingData[i][4] = 100.0;// 微生物
            trainingData[i][5] = 116.0;// 经度
            trainingData[i][6] = 40.0; // 纬度
            trainingData[i][7] = 12.0; // 小时
            trainingData[i][8] = 1.0;  // 季节
            trainingData[i][9] = 0.9;  // 合格率
            trainingData[i][10] = 500.0;// 批次规模
            trainingData[i][11] = 0.85;// 信用分
        }
        
        // 训练模型
        IsolationForestDetector detector = new IsolationForestDetector(trainingData);
        
        // 测试正常数据
        double[] normalFeatures = trainingData[0].clone();
        double normalScore = detector.predict(normalFeatures);
        System.out.println("正常数据异常分数：" + normalScore);
        System.out.println("是否异常：" + detector.isAnomaly(normalScore));
        
        // 测试异常数据（温度极高）
        double[] anomalyFeatures = trainingData[0].clone();
        anomalyFeatures[0] = 55.0; // 异常高温
        anomalyFeatures[2] = 2.5;  // 农残超标
        double anomalyScore = detector.predict(anomalyFeatures);
        System.out.println("异常数据异常分数：" + anomalyScore);
        System.out.println("是否异常：" + detector.isAnomaly(anomalyScore));
        
        System.out.println("===========================================\n");
    }
    
    /**
     * 测试 J48 决策树风险评估算法
     */
    @Test
    public void testJ48RiskAssessment() {
        System.out.println("========== J48 决策树风险评估测试 ==========");
        
        try {
            J48RiskAssessor assessor = new J48RiskAssessor();
            
            // 生成训练数据
            double[][] trainingData = new double[50][8];
            for (int i = 0; i < 50; i++) {
                trainingData[i][0] = 0.3 + Math.random() * 0.4; // anomaly_score
                trainingData[i][1] = 0.85 + Math.random() * 0.15; // enterprise_rate
                trainingData[i][2] = Math.random() * 3; // origin_code
                trainingData[i][3] = Math.random() * 3; // season_code
                trainingData[i][4] = Math.random() * 2; // overlimit_count
                trainingData[i][5] = 0.7 + Math.random() * 0.3; // temp_compliance
                trainingData[i][6] = 200 + Math.random() * 600; // batch_size
                
                // 标签：0-Low, 1-Medium, 2-High
                if (trainingData[i][0] > 0.6 || trainingData[i][4] > 1.5) {
                    trainingData[i][7] = 2; // High
                } else if (trainingData[i][0] > 0.4) {
                    trainingData[i][7] = 1; // Medium
                } else {
                    trainingData[i][7] = 0; // Low
                }
            }
            
            // 训练模型
            assessor.train(trainingData);
            System.out.println("J48 模型训练完成");
            
            // 测试低风险样本
            double[] lowRiskFeatures = {0.25, 0.95, 1.0, 1.0, 0.0, 0.95, 500.0};
            J48RiskAssessor.RiskLevel lowRisk = assessor.predict(lowRiskFeatures);
            System.out.println("低风险样本预测结果：" + lowRisk.getLabel());
            
            // 测试高风险样本
            double[] highRiskFeatures = {0.75, 0.70, 2.0, 2.0, 3.0, 0.50, 200.0};
            J48RiskAssessor.RiskLevel highRisk = assessor.predict(highRiskFeatures);
            System.out.println("高风险样本预测结果：" + highRisk.getLabel());
            
            // 获取概率分布
            double[] probs = assessor.getProbabilityDistribution(highRiskFeatures);
            System.out.print("概率分布：Low=" + probs[0] + ", Medium=" + probs[1] + ", High=" + probs[2]);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("===========================================\n");
    }
}
