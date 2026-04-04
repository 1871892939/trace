package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.*;
import com.ncg.dto.BatchCreateRequest;
import com.ncg.dto.BatchUpdateRequest;
import com.ncg.dto.OverviewDTO;
import com.ncg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 批次管理服务
 */
@Service
public class BatchService {

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
    private DataChangeNotifier dataChangeNotifier;

    @Autowired
    private OverviewService overviewService;

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(rollbackFor = Exception.class)
    public void createBatch(BatchCreateRequest req, String operator) {
        // ① 保存批次基本信息
        BatchInfo batch = new BatchInfo();
        batch.setBatchNo(req.getBatchNo());
        batch.setOrigin(req.getOrigin());
        batch.setEnterprise(req.getEnterprise());
        if (req.getProductionDate() != null && !req.getProductionDate().isBlank()) {
            batch.setProductionDate(LocalDate.parse(req.getProductionDate()));
        }
        batch.setOperator(operator);
        batch.setCleaned(0);
        batch.setCreateTime(LocalDateTime.now());
        batchInfoMapper.insert(batch);

        Long batchId = batch.getId();
        LocalDateTime now = LocalDateTime.now();

        // ② 保存检测数据
        if (req.getDetection() != null) {
            BatchCreateRequest.DetectionDataItem d = req.getDetection();
            DetectionData detection = new DetectionData();
            detection.setBatchId(batchId);
            if (d.getPesticide() != null && !d.getPesticide().isBlank()) {
                detection.setPesticide(new BigDecimal(d.getPesticide()));
            } else {
                detection.setPesticide(BigDecimal.ZERO);
            }
            if (d.getHeavyMetal() != null && !d.getHeavyMetal().isBlank()) {
                detection.setHeavyMetal(new BigDecimal(d.getHeavyMetal()));
            } else {
                detection.setHeavyMetal(BigDecimal.ZERO);
            }
            if (d.getMicrobe() != null && !d.getMicrobe().isBlank()) {
                detection.setMicrobe(new BigDecimal(d.getMicrobe()));
            } else {
                detection.setMicrobe(BigDecimal.ZERO);
            }
            if (d.getTestTime() != null && !d.getTestTime().isBlank()) {
                detection.setTestTime(LocalDateTime.parse(d.getTestTime(), DT_FORMAT));
            } else {
                detection.setTestTime(now);
            }
            detection.setOperator(operator);
            detection.setCreateTime(now);
            detectionDataMapper.insert(detection);
        }

        // ③ 保存物流数据
        if (req.getLogistics() != null && !req.getLogistics().isEmpty()) {
            for (BatchCreateRequest.LogisticsDataItem l : req.getLogistics()) {
                LogisticsData logistics = new LogisticsData();
                logistics.setBatchId(batchId);
                if (l.getGpsLng() != null && !l.getGpsLng().isBlank()) {
                    logistics.setGpsLng(new BigDecimal(l.getGpsLng()));
                } else {
                    logistics.setGpsLng(BigDecimal.ZERO);
                }
                if (l.getGpsLat() != null && !l.getGpsLat().isBlank()) {
                    logistics.setGpsLat(new BigDecimal(l.getGpsLat()));
                } else {
                    logistics.setGpsLat(BigDecimal.ZERO);
                }
                if (l.getTemperature() != null && !l.getTemperature().isBlank()) {
                    logistics.setTemperature(new BigDecimal(l.getTemperature()));
                } else {
                    logistics.setTemperature(BigDecimal.ZERO);
                }
                if (l.getHumidity() != null && !l.getHumidity().isBlank()) {
                    logistics.setHumidity(new BigDecimal(l.getHumidity()));
                } else {
                    logistics.setHumidity(BigDecimal.ZERO);
                }
                if (l.getRecordTime() != null && !l.getRecordTime().isBlank()) {
                    logistics.setRecordTime(LocalDateTime.parse(l.getRecordTime(), DT_FORMAT));
                } else {
                    logistics.setRecordTime(now);
                }
                logistics.setOperator(operator);
                logistics.setCreateTime(now);
                logisticsDataMapper.insert(logistics);
            }
        }

        // ④ 推送最新大盘数据
        try {
            OverviewDTO overview = overviewService.getOverview();
            dataChangeNotifier.pushOverviewUpdate(overview);
        } catch (Exception ignored) {}
    }

    public void updateBatch(BatchUpdateRequest req, String operator) {
        BatchInfo batch = batchInfoMapper.selectById(req.getId());
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        if (req.getOrigin() != null) {
            batch.setOrigin(req.getOrigin());
        }
        if (req.getEnterprise() != null) {
            batch.setEnterprise(req.getEnterprise());
        }
        if (req.getProductionDate() != null && !req.getProductionDate().isBlank()) {
            batch.setProductionDate(LocalDate.parse(req.getProductionDate()));
        }
        batch.setOperator(operator);
        batch.setUpdateTime(LocalDateTime.now());
        batchInfoMapper.updateById(batch);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long batchId) {
        BatchInfo batch = batchInfoMapper.selectById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        // 删除物流数据
        logisticsDataMapper.delete(new LambdaQueryWrapper<LogisticsData>()
                .eq(LogisticsData::getBatchId, batchId));
        // 删除检测数据
        detectionDataMapper.delete(new LambdaQueryWrapper<DetectionData>()
                .eq(DetectionData::getBatchId, batchId));
        // 删除已处理的预警（脏数据）
        alertRecordMapper.delete(new LambdaQueryWrapper<AlertRecord>()
                .eq(AlertRecord::getBatchId, batchId)
                .eq(AlertRecord::getHandled, 1));
        // 删除风险评估
        riskAssessmentMapper.delete(new LambdaQueryWrapper<RiskAssessment>()
                .eq(RiskAssessment::getBatchId, batchId));
        // 删除批次
        batchInfoMapper.deleteById(batchId);
    }

    public Map<String, Object> checkBatchNoExists(String batchNo, Long excludeId) {
        LambdaQueryWrapper<BatchInfo> wrapper = new LambdaQueryWrapper<BatchInfo>()
                .eq(BatchInfo::getBatchNo, batchNo);
        if (excludeId != null) {
            wrapper.ne(BatchInfo::getId, excludeId);
        }
        long count = batchInfoMapper.selectCount(wrapper);
        return Map.of("exists", count > 0);
    }
}
