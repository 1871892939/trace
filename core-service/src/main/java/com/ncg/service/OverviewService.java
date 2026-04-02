package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.*;
import com.ncg.dto.OverviewDTO;
import com.ncg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OverviewService {

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

    private static final DateTimeFormatter TREND_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    public OverviewDTO getOverview() {
        OverviewDTO dto = new OverviewDTO();

        // 基础统计
        dto.setTotalBatches(batchInfoMapper.selectCount(null));
        Long totalAlerts = alertRecordMapper.selectCount(null);
        dto.setTotalAlerts(totalAlerts);
        dto.setUnhandledAlerts(alertRecordMapper.selectCount(
                new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getHandled, 0)));

        // 风险分布
        Map<String, Long> riskDist = new LinkedHashMap<>();
        riskDist.put("Low", riskAssessmentMapper.selectCount(
                new LambdaQueryWrapper<RiskAssessment>().eq(RiskAssessment::getRiskLevel, "Low")));
        riskDist.put("Medium", riskAssessmentMapper.selectCount(
                new LambdaQueryWrapper<RiskAssessment>().eq(RiskAssessment::getRiskLevel, "Medium")));
        riskDist.put("High", riskAssessmentMapper.selectCount(
                new LambdaQueryWrapper<RiskAssessment>().eq(RiskAssessment::getRiskLevel, "High")));
        dto.setRiskDistribution(riskDist);

        // 风险评分直方图
        dto.setRiskScoreHistogram(buildRiskHistogram());

        // 预警类型分布
        Map<String, Long> alertTypeDist = new LinkedHashMap<>();
        String[] alertTypes = {"TEMP", "HUMIDITY", "PESTICIDE", "HEAVY_METAL", "MICROBE", "COMPOSITE"};
        for (String type : alertTypes) {
            alertTypeDist.put(type, alertRecordMapper.selectCount(
                    new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getAlertType, type)));
        }
        dto.setAlertTypeDistribution(alertTypeDist);

        // 近30天趋势
        buildTrendData(dto);

        // 物流统计
        buildLogisticsStats(dto);

        // 产地/企业统计
        dto.setTotalOrigins(batchInfoMapper.selectCount(null)); // 简化：按批次计数
        dto.setTotalEnterprises(batchInfoMapper.selectCount(null));

        // 批次详情（溯源链）
        dto.setBatchDetails(buildBatchDetails());

        return dto;
    }

    private List<Long> buildRiskHistogram() {
        List<Long> histogram = Arrays.asList(0L, 0L, 0L, 0L, 0L);
        List<RiskAssessment> risks = riskAssessmentMapper.selectList(
                new LambdaQueryWrapper<RiskAssessment>()
                        .between(RiskAssessment::getRiskScore, 0, 100)
                        .orderByDesc(RiskAssessment::getId)
                        .last("LIMIT 1000"));
        for (RiskAssessment r : risks) {
            int score = r.getRiskScore();
            int idx = score <= 20 ? 0 : score <= 40 ? 1 : score <= 60 ? 2 : score <= 80 ? 3 : 4;
            histogram.set(idx, histogram.get(idx) + 1);
        }
        return histogram;
    }

    private void buildTrendData(OverviewDTO dto) {
        List<String> labels = new ArrayList<>();
        List<Long> batchTrend = new ArrayList<>();
        List<Long> alertTrend = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(TREND_DATE_FORMAT);
            labels.add(dateStr);

            long batchCount = batchInfoMapper.selectCount(
                    new LambdaQueryWrapper<BatchInfo>()
                            .ge(BatchInfo::getProductionDate, date)
                            .lt(BatchInfo::getProductionDate, date.plusDays(1)));
            batchTrend.add(batchCount);

            long alertCount = alertRecordMapper.selectCount(
                    new LambdaQueryWrapper<AlertRecord>()
                            .ge(AlertRecord::getCreateTime, date.atStartOfDay())
                            .lt(AlertRecord::getCreateTime, date.plusDays(1).atStartOfDay()));
            alertTrend.add(alertCount);
        }

        dto.setTrendLabels(labels);
        dto.setBatchTrend(batchTrend);
        dto.setAlertTrend(alertTrend);
    }

    private void buildLogisticsStats(OverviewDTO dto) {
        List<LogisticsData> logs = logisticsDataMapper.selectList(
                new LambdaQueryWrapper<LogisticsData>().last("LIMIT 1000"));

        if (logs.isEmpty()) {
            dto.setAvgTemperature(BigDecimal.ZERO);
            dto.setAvgHumidity(BigDecimal.ZERO);
            dto.setTempAnomalyCount(0L);
            dto.setHumidityAnomalyCount(0L);
            return;
        }

        BigDecimal sumTemp = logs.stream().map(LogisticsData::getTemperature)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumHum = logs.stream().map(LogisticsData::getHumidity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setAvgTemperature(sumTemp.divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP));
        dto.setAvgHumidity(sumHum.divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP));

        long tempAnomaly = logs.stream()
                .filter(l -> l.getTemperature().doubleValue() > 8 || l.getTemperature().doubleValue() < 0)
                .count();
        long humAnomaly = logs.stream()
                .filter(l -> l.getHumidity().doubleValue() > 85 || l.getHumidity().doubleValue() < 40)
                .count();

        dto.setTempAnomalyCount(tempAnomaly);
        dto.setHumidityAnomalyCount(humAnomaly);
    }

    private List<OverviewDTO.BatchDetail> buildBatchDetails() {
        List<BatchInfo> batches = batchInfoMapper.selectList(
                new LambdaQueryWrapper<BatchInfo>()
                        .orderByDesc(BatchInfo::getId)
                        .last("LIMIT 50"));

        List<OverviewDTO.BatchDetail> details = new ArrayList<>();

        for (BatchInfo batch : batches) {
            OverviewDTO.BatchDetail info = new OverviewDTO.BatchDetail();
            info.setId(batch.getId());
            info.setBatchNo(batch.getBatchNo());
            info.setOrigin(batch.getOrigin());
            info.setEnterprise(batch.getEnterprise());
            info.setProductionDate(batch.getProductionDate() != null ? batch.getProductionDate().toString() : "");

            RiskAssessment risk = riskAssessmentMapper.selectOne(
                    new LambdaQueryWrapper<RiskAssessment>()
                            .eq(RiskAssessment::getBatchId, batch.getId())
                            .orderByDesc(RiskAssessment::getId).last("LIMIT 1"));
            if (risk != null) {
                info.setRiskLevel(risk.getRiskLevel());
                info.setRiskScore(risk.getRiskScore());
            }

            AlertRecord alert = alertRecordMapper.selectOne(
                    new LambdaQueryWrapper<AlertRecord>()
                            .eq(AlertRecord::getBatchId, batch.getId())
                            .orderByDesc(AlertRecord::getCreateTime).last("LIMIT 1"));
            if (alert != null) {
                info.setHasAlert(true);
                info.setAlertType(alert.getAlertType());
            } else {
                info.setHasAlert(false);
                info.setAlertType(null);
            }

            details.add(info);
        }

        return details;
    }
}
