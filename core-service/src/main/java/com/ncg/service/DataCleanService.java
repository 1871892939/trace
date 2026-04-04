package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.algorithm.RiskScoring;
import com.ncg.algorithm.StatisticalAnomalyDetector;
import com.ncg.dal.mapper.*;
import com.ncg.model.*;
import com.ncg.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据清洗服务
 *
 * 职责：读取原始数据（batch_info / detection_data / logistics_data）
 *       → 调用算法引擎计算 → 落 risk_assessment + alert_record 表
 *
 * 数据流：
 *   原始数据 ──→ DataCleanService.cleanBatch()
 *                    ├── StatisticalAnomalyDetector.detectBatch()  → 3σ 统计异常
 *                    ├── RiskScoring.calculateRiskScore()        → 规则风险评分
 *                    ├── RiskScoring.generateRiskFactorsJson()   → 评分明细
 *                    └── 落 risk_assessment + alert_record
 */
@Service
public class DataCleanService {

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    @Autowired
    private DetectionDataMapper detectionDataMapper;

    @Autowired
    private LogisticsDataMapper logisticsDataMapper;

    @Autowired
    private RiskScoring riskScoring;

    @Autowired
    private StatisticalAnomalyDetector anomalyDetector;

    @Autowired
    private ConfigService configService;

    /**
     * 清洗指定批次（算法处理核心入口）
     * 对给定批次执行：读取原始数据 → 算法计算 → 落库
     *
     * @param batchId 批次 ID
     */
    @Transactional
    public void cleanBatch(Long batchId) {
        // ① 读取原始数据
        DetectionData detection = detectionDataMapper.selectOne(
                new LambdaQueryWrapper<DetectionData>()
                        .eq(DetectionData::getBatchId, batchId));
        if (detection == null) {
            return;
        }

        List<LogisticsData> logistics = logisticsDataMapper.selectList(
                new LambdaQueryWrapper<LogisticsData>()
                        .eq(LogisticsData::getBatchId, batchId));

        // ② 统计异常检测（3σ 建模，取最异常时刻的温湿度）
        StatisticalAnomalyDetector.BatchAnomalyResult anomalyResult =
                anomalyDetector.detectBatch(logistics);

        // 取物流中最异常的温湿度（用于 RiskScoring 评分）
        BigDecimal tempMostAnomaly = findMostAnomalyValue(logistics, true, anomalyResult);
        BigDecimal humMostAnomaly  = findMostAnomalyValue(logistics, false, anomalyResult);

        // ③ 规则风险评分
        int totalScore = riskScoring.calculateRiskScore(
                detection.getPesticide(),
                detection.getHeavyMetal(),
                detection.getMicrobe(),
                tempMostAnomaly,
                humMostAnomaly
        );
        String riskLevel = riskScoring.getRiskLevel(totalScore);
        String factors = riskScoring.generateRiskFactorsJson(
                detection.getPesticide(),
                detection.getHeavyMetal(),
                detection.getMicrobe(),
                tempMostAnomaly,
                humMostAnomaly,
                totalScore
        );

        // ④ 落 risk_assessment 表
        RiskAssessment risk = new RiskAssessment();
        risk.setBatchId(batchId);
        risk.setRiskScore(totalScore);
        risk.setRiskLevel(riskLevel);
        risk.setAssessmentDate(LocalDate.now());
        risk.setFactors(factors);
        riskAssessmentMapper.insert(risk);

        // ⑤ 生成并落 alert_record 表
        List<AlertRecord> alerts = generateAlerts(batchId, detection, anomalyResult, totalScore);
        for (AlertRecord alert : alerts) {
            alertRecordMapper.insert(alert);
        }
    }

    /**
     * 找出物流记录中温湿度异常分最高的那个值
     */
    private BigDecimal findMostAnomalyValue(List<LogisticsData> records,
                                            boolean isTemp,
                                            StatisticalAnomalyDetector.BatchAnomalyResult anomalyResult) {
        if (records == null || records.isEmpty()) {
            return isTemp ? BigDecimal.ZERO : new BigDecimal("55");
        }

        List<Double> values = records.stream()
                .map(r -> (isTemp ? r.getTemperature() : r.getHumidity()).doubleValue())
                .toList();

        double mean = anomalyDetector.calculateMean(values);
        double std  = anomalyDetector.calculateStdDev(values, mean);

        // 找出异常分最高的记录，返回其对应值
        BigDecimal result = isTemp ? records.get(0).getTemperature() : records.get(0).getHumidity();
        double maxScore = 0.0;

        for (LogisticsData r : records) {
            double v = (isTemp ? r.getTemperature() : r.getHumidity()).doubleValue();
            double score = anomalyDetector.calculateAnomalyScore(v, mean, std);
            if (score > maxScore) {
                maxScore = score;
                result = isTemp ? r.getTemperature() : r.getHumidity();
            }
        }
        return result;
    }

    /**
     * 根据算法结果生成预警记录列表
     */
    private List<AlertRecord> generateAlerts(Long batchId,
                                              DetectionData detection,
                                              StatisticalAnomalyDetector.BatchAnomalyResult anomalyResult,
                                              int riskScore) {
        List<AlertRecord> alerts = new ArrayList<>();

        java.math.BigDecimal pesticideLimit = configService.getNumericValue("limit.pesticide", new java.math.BigDecimal("0.5"));
        java.math.BigDecimal heavyMetalLimit = configService.getNumericValue("limit.heavy_metal", new java.math.BigDecimal("0.1"));
        java.math.BigDecimal microbeLimit = configService.getNumericValue("limit.microbe", new java.math.BigDecimal("200"));
        int compositeThreshold = configService.getValue("alert.composite.threshold", "70") != null
                ? Integer.parseInt(configService.getValue("alert.composite.threshold", "70")) : 70;

        // 规则触发：农残超标
        if (detection.getPesticide().compareTo(pesticideLimit) > 0) {
            alerts.add(makeAlert(batchId, "PESTICIDE", java.math.BigDecimal.ONE.setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        // 规则触发：重金属超标
        if (detection.getHeavyMetal().compareTo(heavyMetalLimit) > 0) {
            alerts.add(makeAlert(batchId, "HEAVY_METAL", new java.math.BigDecimal("0.85")));
        }

        // 规则触发：微生物超标
        if (detection.getMicrobe().compareTo(microbeLimit) > 0) {
            alerts.add(makeAlert(batchId, "MICROBE", new java.math.BigDecimal("0.75")));
        }

        // 统计异常触发：温度 3σ 异常
        if (anomalyResult.tempAnomaly()) {
            alerts.add(makeAlert(batchId, "TEMP",
                    java.math.BigDecimal.valueOf(anomalyResult.tempAnomalyScore()).setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        // 统计异常触发：湿度 3σ 异常
        if (anomalyResult.humAnomaly()) {
            alerts.add(makeAlert(batchId, "HUMIDITY",
                    java.math.BigDecimal.valueOf(anomalyResult.humAnomalyScore()).setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        // 高风险无具体预警时，触发综合预警
        if (riskScore > compositeThreshold && alerts.isEmpty()) {
            alerts.add(makeAlert(batchId, "COMPOSITE",
                    java.math.BigDecimal.valueOf(riskScore / 100.0).setScale(2, java.math.RoundingMode.HALF_UP)));
        }

        return alerts;
    }

    private AlertRecord makeAlert(Long batchId, String alertType, BigDecimal riskScore) {
        AlertRecord a = new AlertRecord();
        a.setBatchId(batchId);
        a.setAlertType(alertType);
        a.setRiskScore(riskScore);
        a.setCreateTime(LocalDateTime.now());
        a.setHandled(0);
        return a;
    }

    /**
     * 批量清洗所有未处理的批次
     * 用于首次导入历史数据后批量计算，或定时增量清洗
     *
     * @return 本次清洗的批次数量
     */
    @Transactional
    public int cleanAllUnprocessed() {
        List<BatchInfo> batches = batchInfoMapper.selectList(null);
        int count = 0;
        for (BatchInfo batch : batches) {
            long alreadyExists = riskAssessmentMapper.selectCount(
                    new LambdaQueryWrapper<RiskAssessment>()
                            .eq(RiskAssessment::getBatchId, batch.getId()));
            if (alreadyExists > 0) {
                continue;
            }
            try {
                cleanBatch(batch.getId());
                count++;
            } catch (Exception ignored) {
                // 单条失败不影响其他批次
            }
        }
        return count;
    }
}
