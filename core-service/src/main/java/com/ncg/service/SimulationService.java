package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.ncg.dal.mapper.*;
import com.ncg.dto.SimulationResponse;
import com.ncg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SimulationService {

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    @Autowired
    private DetectionDataMapper detectionDataMapper;

    @Autowired
    private LogisticsDataMapper logisticsDataMapper;

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    private static final String[] ORIGINS = {
            "北京", "上海", "广州", "深圳", "成都", "杭州", "武汉", "南京", "西安", "重庆",
            "天津", "苏州", "郑州", "长沙", "沈阳", "青岛", "宁波", "东莞", "无锡", "昆明"
    };

    private static final String[] ENTERPRISES = {
            "华润万家", "永辉超市", "盒马鲜生", "大润发", "沃尔玛中国", "家乐福中国", "麦德龙", "欧尚",
            "物美集团", "京客隆", "乐购", "卜蜂莲花", "华联综超", "北京华联", "人人乐", "中百仓储"
    };

    private static final String[] PESTICIDES = {
            "毒死蜱", "氧乐果", "克百威", "甲胺磷", "对硫磷", "甲基对硫磷", "水胺硫磷", "三唑磷",
            "多菌灵", "百菌清", "甲氰菊酯", "氯氟氰菊酯", "溴氰菊酯", "联苯菊酯", "氟虫腈", "茚虫威"
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public SimulationResponse generateData(String type, Integer count) throws JsonProcessingException {
        int total = count == null || count <= 0 ? 1 : Math.min(count, 100);
        List<String> batchNos = new ArrayList<>();
        int alertCount = 0;

        for (int i = 0; i < total; i++) {
            String batchNo = generateBatchNo(type, i);
            batchNos.add(batchNo);

            BatchInfo batch = createBatch(batchNo, type);
            batchInfoMapper.insert(batch);

            DetectionData detection = createDetection(batch.getId(), type);
            detectionDataMapper.insert(detection);

            List<LogisticsData> logisticsList = createLogistics(batch.getId(), type);
            for (LogisticsData log : logisticsList) {
                logisticsDataMapper.insert(log);
            }

            RiskAssessment risk = createRiskAssessment(batch.getId(), detection, logisticsList, type);
            riskAssessmentMapper.insert(risk);

            List<AlertRecord> alerts = createAlerts(batch.getId(), detection, logisticsList, type);
            for (AlertRecord alert : alerts) {
                alertRecordMapper.insert(alert);
                alertCount++;
            }
        }

        Map<String, Long> riskDist = getRiskDistribution();
        return new SimulationResponse(total, total, alertCount, riskDist, batchNos,
                type.equals("anomaly") ? "已生成 " + total + " 条异常模拟数据" : "已生成 " + total + " 条正常模拟数据");
    }

    private String generateBatchNo(String type, int index) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = type.equals("anomaly") ? "AB" : "NB";
        return prefix + dateStr + String.format("%04d", index + 1);
    }

    private BatchInfo createBatch(String batchNo, String type) {
        BatchInfo batch = new BatchInfo();
        batch.setBatchNo(batchNo);
        batch.setOrigin(ORIGINS[new Random().nextInt(ORIGINS.length)]);
        batch.setProductionDate(LocalDate.now().minusDays(new Random().nextInt(30)));
        batch.setEnterprise(ENTERPRISES[new Random().nextInt(ENTERPRISES.length)]);
        return batch;
    }

    private DetectionData createDetection(Long batchId, String type) {
        DetectionData detection = new DetectionData();
        detection.setBatchId(batchId);
        detection.setTestTime(LocalDateTime.now().minusHours(new Random().nextInt(72)));

        if ("anomaly".equals(type)) {
            // 异常：农残或重金属超标
            int anomalyType = new Random().nextInt(3);
            if (anomalyType == 0) {
                detection.setPesticide(new BigDecimal(String.format("%.4f", 0.5 + new Random().nextDouble() * 1.5)));
                detection.setHeavyMetal(new BigDecimal(String.format("%.4f", new Random().nextDouble() * 0.3)));
            } else if (anomalyType == 1) {
                detection.setPesticide(new BigDecimal(String.format("%.4f", new Random().nextDouble() * 0.5)));
                detection.setHeavyMetal(new BigDecimal(String.format("%.4f", 0.6 + new Random().nextDouble() * 1.0)));
            } else {
                detection.setPesticide(new BigDecimal(String.format("%.4f", 0.5 + new Random().nextDouble() * 1.5)));
                detection.setHeavyMetal(new BigDecimal(String.format("%.4f", 0.6 + new Random().nextDouble() * 1.0)));
            }
            detection.setMicrobe(new BigDecimal(String.format("%.4f", new Random().nextDouble() * 800)));
        } else {
            // 正常：所有指标在安全范围内
            detection.setPesticide(new BigDecimal(String.format("%.4f", new Random().nextDouble() * 0.4)));
            detection.setHeavyMetal(new BigDecimal(String.format("%.4f", new Random().nextDouble() * 0.4)));
            detection.setMicrobe(new BigDecimal(String.format("%.4f", 10 + new Random().nextDouble() * 90)));
        }
        return detection;
    }

    private List<LogisticsData> createLogistics(Long batchId, String type) {
        List<LogisticsData> list = new ArrayList<>();
        int records = 2 + new Random().nextInt(4);

        for (int i = 0; i < records; i++) {
            LogisticsData log = new LogisticsData();
            log.setBatchId(batchId);
            log.setGpsLng(new BigDecimal(String.format("%.6f", 73 + new Random().nextDouble() * 33)));
            log.setGpsLat(new BigDecimal(String.format("%.6f", 18 + new Random().nextDouble() * 34)));
            log.setRecordTime(LocalDateTime.now().minusHours(new Random().nextInt(72)).minusMinutes(i * 60L));

            if ("anomaly".equals(type) && new Random().nextInt(10) < 3) {
                // 异常：温度或湿度超标
                if (new Random().nextBoolean()) {
                    log.setTemperature(new BigDecimal(String.format("%.1f", 10 + new Random().nextDouble() * 15)));
                    log.setHumidity(new BigDecimal(String.format("%.1f", 40 + new Random().nextDouble() * 40)));
                } else {
                    log.setTemperature(new BigDecimal(String.format("%.1f", 2 + new Random().nextDouble() * 5)));
                    log.setHumidity(new BigDecimal(String.format("%.1f", 85 + new Random().nextDouble() * 15)));
                }
            } else {
                log.setTemperature(new BigDecimal(String.format("%.1f", 2 + new Random().nextDouble() * 6)));
                log.setHumidity(new BigDecimal(String.format("%.1f", 60 + new Random().nextDouble() * 25)));
            }
            list.add(log);
        }
        return list;
    }

    private RiskAssessment createRiskAssessment(Long batchId, DetectionData detection,
                                                  List<LogisticsData> logisticsList, String type) throws JsonProcessingException {
        RiskAssessment risk = new RiskAssessment();
        risk.setBatchId(batchId);
        risk.setAssessmentDate(LocalDate.now());

        double score;
        String level;

        if ("anomaly".equals(type)) {
            score = 60 + new Random().nextDouble() * 40;
            level = "High";
        } else {
            score = 5 + new Random().nextDouble() * 30;
            level = score <= 40 ? "Low" : "Medium";
        }

        risk.setRiskScore((int) score);
        risk.setRiskLevel(level);

        Map<String, Object> factors = new LinkedHashMap<>();
        factors.put("pesticideScore", Math.min(100, detection.getPesticide().doubleValue() * 150));
        factors.put("heavyMetalScore", Math.min(100, detection.getHeavyMetal().doubleValue() * 120));
        factors.put("microbeScore", Math.min(100, detection.getMicrobe().doubleValue() * 0.3));
        factors.put("tempAnomaly", logisticsList.stream().anyMatch(l ->
                l.getTemperature().doubleValue() > 8 || l.getTemperature().doubleValue() < 0) ? 1 : 0);
        factors.put("humidityAnomaly", logisticsList.stream().anyMatch(l ->
                l.getHumidity().doubleValue() > 85 || l.getHumidity().doubleValue() < 40) ? 1 : 0);

        risk.setFactors(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(factors));
        return risk;
    }

    private List<AlertRecord> createAlerts(Long batchId, DetectionData detection,
                                            List<LogisticsData> logisticsList, String type) {
        List<AlertRecord> alerts = new ArrayList<>();

        if ("anomaly".equals(type)) {
            if (detection.getPesticide().doubleValue() > 0.5) {
                AlertRecord a = new AlertRecord();
                a.setBatchId(batchId);
                a.setAlertType("PESTICIDE");
                a.setRiskScore(BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP));
                a.setCreateTime(LocalDateTime.now());
                a.setHandled(0);
                alerts.add(a);
            }
            if (detection.getHeavyMetal().doubleValue() > 0.5) {
                AlertRecord a = new AlertRecord();
                a.setBatchId(batchId);
                a.setAlertType("HEAVY_METAL");
                a.setRiskScore(new BigDecimal("0.85"));
                a.setCreateTime(LocalDateTime.now());
                a.setHandled(0);
                alerts.add(a);
            }
            if (detection.getMicrobe().doubleValue() > 500) {
                AlertRecord a = new AlertRecord();
                a.setBatchId(batchId);
                a.setAlertType("MICROBE");
                a.setRiskScore(new BigDecimal("0.75"));
                a.setCreateTime(LocalDateTime.now());
                a.setHandled(0);
                alerts.add(a);
            }
            boolean tempAnomaly = logisticsList.stream().anyMatch(l ->
                    l.getTemperature().doubleValue() > 8 || l.getTemperature().doubleValue() < 0);
            if (tempAnomaly) {
                AlertRecord a = new AlertRecord();
                a.setBatchId(batchId);
                a.setAlertType("TEMP");
                a.setRiskScore(new BigDecimal("0.60"));
                a.setCreateTime(LocalDateTime.now());
                a.setHandled(0);
                alerts.add(a);
            }
            boolean humidityAnomaly = logisticsList.stream().anyMatch(l ->
                    l.getHumidity().doubleValue() > 85 || l.getHumidity().doubleValue() < 40);
            if (humidityAnomaly) {
                AlertRecord a = new AlertRecord();
                a.setBatchId(batchId);
                a.setAlertType("HUMIDITY");
                a.setRiskScore(new BigDecimal("0.55"));
                a.setCreateTime(LocalDateTime.now());
                a.setHandled(0);
                alerts.add(a);
            }
        }

        return alerts;
    }

    private Map<String, Long> getRiskDistribution() {
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("Low", riskAssessmentMapper.selectCount(new LambdaQueryWrapper<RiskAssessment>()
                .eq(RiskAssessment::getRiskLevel, "Low")));
        dist.put("Medium", riskAssessmentMapper.selectCount(new LambdaQueryWrapper<RiskAssessment>()
                .eq(RiskAssessment::getRiskLevel, "Medium")));
        dist.put("High", riskAssessmentMapper.selectCount(new LambdaQueryWrapper<RiskAssessment>()
                .eq(RiskAssessment::getRiskLevel, "High")));
        return dist;
    }
}
