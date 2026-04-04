package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.*;
import com.ncg.dto.SimulationResponse;
import com.ncg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    @Autowired
    private DataCleanService dataCleanService;

    @Autowired
    private OperationLogService operationLogService;

    private static final String[] ORIGINS = {
            "北京", "上海", "广州", "深圳", "成都", "杭州", "武汉", "南京", "西安", "重庆",
            "天津", "苏州", "郑州", "长沙", "沈阳", "青岛", "宁波", "东莞", "无锡", "昆明"
    };

    private static final String[] ENTERPRISES = {
            "华润万家", "永辉超市", "盒马鲜生", "大润发", "沃尔玛中国", "家乐福中国", "麦德龙", "欧尚",
            "物美集团", "京客隆", "乐购", "卜蜂莲花", "华联综超", "北京华联", "人人乐", "中百仓储"
    };

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成模拟数据
     *
     * @param type        数据类型：normal / anomaly
     * @param count       模拟数量
     * @param cleanEnabled 是否实时数据清洗（开→调用算法引擎落 risk_assessment / alert_record，关→仅原始数据）
     * @return 模拟结果
     */
    @Transactional
    public SimulationResponse generateData(String type, Integer count, boolean cleanEnabled,
                                            String operator, String description) {
        int total = count == null || count <= 0 ? 1 : Math.min(count, 100);
        List<String> batchNos = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            String batchNo = generateBatchNo(type, i);
            batchNos.add(batchNo);

            // ① 原始数据落库
            BatchInfo batch = createBatch(batchNo, type);
            batch.setOperator(operator);
            batch.setCreateTime(LocalDateTime.now());
            batchInfoMapper.insert(batch);

            DetectionData detection = createDetection(batch.getId(), type);
            detectionDataMapper.insert(detection);

            List<LogisticsData> logisticsList = createLogistics(batch.getId(), type);
            for (LogisticsData log : logisticsList) {
                logisticsDataMapper.insert(log);
            }

            // ② 实时清洗（算法引擎处理，可选）
            if (cleanEnabled) {
                dataCleanService.cleanBatch(batch.getId());
            }
            // ③ 保存数据模拟操作日志
            OperationLog simLog = new OperationLog();
            simLog.setUsername(operator != null ? operator : "system");
            simLog.setRole("");
            simLog.setOperationType("CREATE");
            simLog.setModule("批次管理");
            simLog.setDescription(description != null ? description : ("数据模拟生成 " + total + " 条" + ("anomaly".equals(type) ? "异常" : "正常") + "批次"));
            simLog.setMethod("POST");
            simLog.setRequestUrl("/api/simulation/generate");
            simLog.setRequestParams("{type:" + type + ", count:" + total + ", clean:" + cleanEnabled + "}");
            simLog.setStatus("SUCCESS");
            simLog.setIpAddress("internal");
            simLog.setOperator(operator != null ? operator : "system");
            simLog.setBatchNo(batchNos.isEmpty() ? null : batchNos.get(i));
            simLog.setOperateTime(LocalDateTime.now());
            operationLogService.saveLog(simLog);
        }

        // ③ 统计（从已清洗的表中查询）
        Map<String, Long> riskDist = getRiskDistribution();
        long alertCount = alertRecordMapper.selectCount(null);



        return new SimulationResponse(
                total,
                total,
                alertCount,
                riskDist,
                batchNos,
                type.equals("anomaly")
                        ? "已生成 " + total + " 条异常模拟数据，已完成实时清洗"
                        : "已生成 " + total + " 条正常模拟数据，已完成实时清洗"
        );
    }

    private String generateBatchNo(String type, int index) {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        String prefix = type.equals("anomaly") ? "AB" : "NB";
        String randomSuffix = String.format("%03d", new Random().nextInt(1000));
        return prefix + dateStr + String.format("%04d", index + 1) + randomSuffix;
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
                if (new Random().nextBoolean()) {
                    log.setTemperature(new BigDecimal(String.format("%.1f", 10 + new Random().nextDouble() * 15)));
                    log.setHumidity(new BigDecimal(String.format("%.1f", 40 + new Random().nextDouble() * 40)));
                } else {
                    log.setTemperature(new BigDecimal(String.format("%.1f", -3 + new Random().nextDouble() * 3)));
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

    private Map<String, Long> getRiskDistribution() {
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("Low",    queryRiskCount("Low"));
        dist.put("Medium", queryRiskCount("Medium"));
        dist.put("High",   queryRiskCount("High"));
        return dist;
    }

    private long queryRiskCount(String riskLevel) {
        return riskAssessmentMapper.selectCount(
                new LambdaQueryWrapper<RiskAssessment>()
                        .eq(RiskAssessment::getRiskLevel, riskLevel));
    }
}
