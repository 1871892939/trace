package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ncg.dal.mapper.AlertRecordMapper;
import com.ncg.dal.mapper.BatchInfoMapper;
import com.ncg.dto.AlertDashboardDTO;
import com.ncg.dto.AlertListDTO;
import com.ncg.model.AlertRecord;
import com.ncg.model.BatchInfo;
import com.ncg.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    @Autowired
    private ConfigService configService;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    public List<AlertListDTO> queryAlerts(String keyword, String alertType, Boolean handled) {
        Integer handledValue = null;
        if (handled != null) {
            handledValue = handled ? 1 : 0;
        }
        List<AlertRecord> alerts = alertRecordMapper.selectList(
                new LambdaQueryWrapper<AlertRecord>()
                        .eq(alertType != null && !alertType.isBlank(),
                                AlertRecord::getAlertType, alertType)
                        .eq(handled != null,
                                AlertRecord::getHandled, handledValue)
                        .orderByDesc(AlertRecord::getCreateTime)
                        .last("LIMIT 200"));

        List<Long> batchIds = alerts.stream()
                .map(AlertRecord::getBatchId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, BatchInfo> batchMap = batchIds.isEmpty() ? Collections.emptyMap() :
                batchInfoMapper.selectList(
                        new LambdaQueryWrapper<BatchInfo>().in(BatchInfo::getId, batchIds))
                        .stream().collect(Collectors.toMap(BatchInfo::getId, b -> b));

        return alerts.stream().map(alert -> {
            AlertListDTO dto = new AlertListDTO();
            dto.setId(alert.getId());
            dto.setBatchId(alert.getBatchId());
            dto.setAlertType(alert.getAlertType());
            dto.setRiskScore(alert.getRiskScore());
            dto.setCreateTime(alert.getCreateTime() != null ? alert.getCreateTime().format(DT_FORMAT) : "");
            dto.setHandled(alert.getHandled() == 1);

            BatchInfo batch = batchMap.get(alert.getBatchId());
            if (batch != null) {
                dto.setBatchNo(batch.getBatchNo());
                dto.setOrigin(batch.getOrigin());
                dto.setEnterprise(batch.getEnterprise());
            }

            boolean matchesKeyword = (keyword == null || keyword.isBlank())
                    || (dto.getBatchNo() != null && dto.getBatchNo().contains(keyword))
                    || (dto.getOrigin() != null && dto.getOrigin().contains(keyword))
                    || (dto.getEnterprise() != null && dto.getEnterprise().contains(keyword));

            return matchesKeyword ? dto : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean handleAlert(Long alertId) {
        int rows = alertRecordMapper.update(null,
                new LambdaUpdateWrapper<AlertRecord>()
                        .eq(AlertRecord::getId, alertId)
                        .set(AlertRecord::getHandled, 1));
        return rows > 0;
    }

    public AlertDashboardDTO getDashboard() {
        AlertDashboardDTO dto = new AlertDashboardDTO();

        Long total = alertRecordMapper.selectCount(null);
        Long handled = alertRecordMapper.selectCount(
                new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getHandled, 1));
        Long unhandled = alertRecordMapper.selectCount(
                new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getHandled, 0));

        dto.setTotalCount(total);
        dto.setHandledCount(handled);
        dto.setUnhandledCount(unhandled);

        if (total > 0) {
            String rate = handled * 100.0 / total + "%";
            dto.setHandleRate(rate.substring(0, Math.min(rate.indexOf("."), rate.length() - 1)) + "%");
        } else {
            dto.setHandleRate("0%");
        }

        Map<String, Long> typeDist = new LinkedHashMap<>();
        String[] types = {"TEMP", "HUMIDITY", "PESTICIDE", "HEAVY_METAL", "MICROBE", "COMPOSITE"};
        for (String t : types) {
            typeDist.put(t, alertRecordMapper.selectCount(
                    new LambdaQueryWrapper<AlertRecord>().eq(AlertRecord::getAlertType, t)));
        }
        dto.setTypeDistribution(typeDist);

        List<AlertRecord> allAlerts = alertRecordMapper.selectList(null);

        BigDecimal urgentThreshold = configService.getNumericValue("alert.score.urgent", new BigDecimal("0.8"));
        BigDecimal seriousThreshold = configService.getNumericValue("alert.score.serious", new BigDecimal("0.5"));

        long urgent = allAlerts.stream()
                .filter(a -> a.getRiskScore() != null && a.getRiskScore().compareTo(urgentThreshold) >= 0)
                .count();
        long serious = allAlerts.stream()
                .filter(a -> a.getRiskScore() != null
                        && a.getRiskScore().compareTo(seriousThreshold) >= 0
                        && a.getRiskScore().compareTo(urgentThreshold) < 0)
                .count();
        long normal = allAlerts.stream()
                .filter(a -> a.getRiskScore() == null || a.getRiskScore().compareTo(seriousThreshold) < 0)
                .count();
        dto.setLevelDistribution(new AlertDashboardDTO.LevelDistribution(urgent, serious, normal));

        buildWeekTrend(dto);

        buildTopBatches(dto);

        buildHandleTimeStats(dto, allAlerts);

        buildRecentUnhandled(dto);

        return dto;
    }

    private void buildWeekTrend(AlertDashboardDTO dto) {
        List<String> labels = new ArrayList<>();
        List<Long> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            labels.add(day.format(D_FORMAT));
            long count = alertRecordMapper.selectCount(
                    new LambdaQueryWrapper<AlertRecord>()
                            .ge(AlertRecord::getCreateTime, day.toLocalDate().atStartOfDay())
                            .lt(AlertRecord::getCreateTime, day.toLocalDate().plusDays(1).atStartOfDay()));
            trend.add(count);
        }
        dto.setTrendLabels(labels);
        dto.setWeekTrend(trend);
    }

    private void buildTopBatches(AlertDashboardDTO dto) {
        List<AlertRecord> allAlerts = alertRecordMapper.selectList(null);
        Map<Long, Long> batchCountMap = allAlerts.stream()
                .collect(Collectors.groupingBy(AlertRecord::getBatchId, Collectors.counting()));

        List<Map.Entry<Long, Long>> sorted = batchCountMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        List<Long> batchIds = sorted.stream().map(Map.Entry::getKey).collect(Collectors.toList());
        final Map<Long, BatchInfo> batchMap = batchIds.isEmpty() ? Collections.emptyMap() :
                batchInfoMapper.selectList(
                        new LambdaQueryWrapper<BatchInfo>().in(BatchInfo::getId, batchIds))
                        .stream().collect(Collectors.toMap(BatchInfo::getId, b -> b));

        List<AlertDashboardDTO.BatchAlertStat> topList = sorted.stream().map(entry -> {
            AlertDashboardDTO.BatchAlertStat stat = new AlertDashboardDTO.BatchAlertStat();
            stat.setBatchId(entry.getKey());
            stat.setAlertCount(entry.getValue());
            BatchInfo batch = batchMap.get(entry.getKey());
            if (batch != null) {
                stat.setBatchNo(batch.getBatchNo());
                stat.setOrigin(batch.getOrigin());
                stat.setEnterprise(batch.getEnterprise());
            }
            Long unhandled = alertRecordMapper.selectCount(
                    new LambdaQueryWrapper<AlertRecord>()
                            .eq(AlertRecord::getBatchId, entry.getKey())
                            .eq(AlertRecord::getHandled, 0));
            stat.setHasUnhandle(unhandled > 0);
            return stat;
        }).collect(Collectors.toList());

        dto.setTopAlertBatches(topList);
    }

    private void buildHandleTimeStats(AlertDashboardDTO dto, List<AlertRecord> allAlerts) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthStart = now.minusDays(30).withHour(0).withMinute(0).withSecond(0);

        long todayHandle = allAlerts.stream()
                .filter(a -> a.getHandled() == 1 && a.getCreateTime() != null
                        && a.getCreateTime().isAfter(todayStart))
                .count();
        long weekHandle = allAlerts.stream()
                .filter(a -> a.getHandled() == 1 && a.getCreateTime() != null
                        && a.getCreateTime().isAfter(weekStart))
                .count();
        long monthHandle = allAlerts.stream()
                .filter(a -> a.getHandled() == 1 && a.getCreateTime() != null
                        && a.getCreateTime().isAfter(monthStart))
                .count();

        List<AlertRecord> handledWithTime = allAlerts.stream()
                .filter(a -> a.getHandled() == 1 && a.getCreateTime() != null)
                .limit(100)
                .collect(Collectors.toList());

        double avgHours = 0;
        if (!handledWithTime.isEmpty()) {
            avgHours = handledWithTime.stream()
                    .mapToLong(a -> {
                        LocalDateTime create = a.getCreateTime();
                        return java.time.Duration.between(create, now).toHours();
                    })
                    .average()
                    .orElse(0);
        }

        AlertDashboardDTO.HandleTimeStats stats = new AlertDashboardDTO.HandleTimeStats();
        stats.setTodayHandleCount(todayHandle);
        stats.setWeekHandleCount(weekHandle);
        stats.setMonthHandleCount(monthHandle);
        stats.setAvgHandleHours(Math.round(avgHours * 10.0) / 10.0);
        dto.setHandleTimeStats(stats);
    }

    private void buildRecentUnhandled(AlertDashboardDTO dto) {
        List<AlertRecord> recent = alertRecordMapper.selectList(
                new LambdaQueryWrapper<AlertRecord>()
                        .eq(AlertRecord::getHandled, 0)
                        .orderByDesc(AlertRecord::getCreateTime)
                        .last("LIMIT 5"));

        List<Long> batchIds = recent.stream()
                .map(AlertRecord::getBatchId).distinct().collect(Collectors.toList());
        final Map<Long, BatchInfo> batchMap = batchIds.isEmpty() ? Collections.emptyMap() :
                batchInfoMapper.selectList(
                        new LambdaQueryWrapper<BatchInfo>().in(BatchInfo::getId, batchIds))
                        .stream().collect(Collectors.toMap(BatchInfo::getId, b -> b));

        List<AlertListDTO> list = recent.stream().map(alert -> {
            AlertListDTO d = new AlertListDTO();
            d.setId(alert.getId());
            d.setBatchId(alert.getBatchId());
            d.setAlertType(alert.getAlertType());
            d.setRiskScore(alert.getRiskScore());
            d.setCreateTime(alert.getCreateTime() != null ? alert.getCreateTime().format(DT_FORMAT) : "");
            d.setHandled(false);
            BatchInfo batch = batchMap.get(alert.getBatchId());
            if (batch != null) {
                d.setBatchNo(batch.getBatchNo());
                d.setOrigin(batch.getOrigin());
                d.setEnterprise(batch.getEnterprise());
            }
            return d;
        }).collect(Collectors.toList());

        dto.setRecentUnhandled(list);
    }
}
