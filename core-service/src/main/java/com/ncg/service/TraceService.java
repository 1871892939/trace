package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.*;
import com.ncg.dto.BatchQueryDTO;
import com.ncg.dto.TraceChainDTO;
import com.ncg.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TraceService {

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

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<BatchQueryDTO> queryBatches(String keyword, String riskLevel, String alertType) {
        LambdaQueryWrapper<BatchInfo> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(BatchInfo::getBatchNo, keyword)
                    .or()
                    .like(BatchInfo::getOrigin, keyword)
                    .or()
                    .like(BatchInfo::getEnterprise, keyword));
        }

        List<BatchInfo> batches = batchInfoMapper.selectList(wrapper.orderByDesc(BatchInfo::getId));

        return batches.stream().map(batch -> {
            BatchQueryDTO dto = new BatchQueryDTO();
            dto.setId(batch.getId());
            dto.setBatchNo(batch.getBatchNo());
            dto.setOrigin(batch.getOrigin());
            dto.setEnterprise(batch.getEnterprise());
            dto.setProductionDate(batch.getProductionDate() != null ? batch.getProductionDate().toString() : "");

            RiskAssessment risk = riskAssessmentMapper.selectOne(
                    new LambdaQueryWrapper<RiskAssessment>()
                            .eq(RiskAssessment::getBatchId, batch.getId())
                            .orderByDesc(RiskAssessment::getId).last("LIMIT 1"));
            if (risk != null) {
                dto.setRiskLevel(risk.getRiskLevel());
                dto.setRiskScore(risk.getRiskScore());
                if (riskLevel != null && !riskLevel.isBlank() && !riskLevel.equals(risk.getRiskLevel())) {
                    return null;
                }
            } else {
                dto.setRiskLevel("Unknown");
                dto.setRiskScore(null);
            }

            LambdaQueryWrapper<AlertRecord> alertWrapper = new LambdaQueryWrapper<AlertRecord>()
                    .eq(AlertRecord::getBatchId, batch.getId());
            if (alertType != null && !alertType.isBlank()) {
                alertWrapper.eq(AlertRecord::getAlertType, alertType);
            }
            AlertRecord alert = alertRecordMapper.selectOne(alertWrapper.orderByDesc(AlertRecord::getCreateTime).last("LIMIT 1"));
            if (alert != null) {
                dto.setHasAlert(true);
                dto.setAlertType(alert.getAlertType());
                dto.setHandled(alert.getHandled() == 1);
            } else {
                dto.setHasAlert(false);
                dto.setAlertType(null);
                dto.setHandled(null);
            }

            dto.setCreateTime(batch.getProductionDate() != null ? batch.getProductionDate().toString() : "");

            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    public TraceChainDTO getTraceChain(Long batchId) {
        TraceChainDTO dto = new TraceChainDTO();

        BatchInfo batch = batchInfoMapper.selectById(batchId);
        if (batch == null) {
            return null;
        }

        TraceChainDTO.BatchInfoNode batchNode = new TraceChainDTO.BatchInfoNode();
        batchNode.setId(batch.getId());
        batchNode.setBatchNo(batch.getBatchNo());
        batchNode.setOrigin(batch.getOrigin());
        batchNode.setEnterprise(batch.getEnterprise());
        batchNode.setProductionDate(batch.getProductionDate() != null ? batch.getProductionDate().toString() : "");
        dto.setBatch(batchNode);

        DetectionData detection = detectionDataMapper.selectOne(
                new LambdaQueryWrapper<DetectionData>()
                        .eq(DetectionData::getBatchId, batchId)
                        .orderByDesc(DetectionData::getId).last("LIMIT 1"));
        if (detection != null) {
            TraceChainDTO.DetectionNode detNode = new TraceChainDTO.DetectionNode();
            detNode.setId(detection.getId());
            detNode.setPesticide(detection.getPesticide());
            detNode.setHeavyMetal(detection.getHeavyMetal());
            detNode.setMicrobe(detection.getMicrobe());
            detNode.setTestTime(detection.getTestTime() != null ? detection.getTestTime().format(DT_FORMAT) : "");
            dto.setDetection(detNode);
        }

        List<LogisticsData> logisticsList = logisticsDataMapper.selectList(
                new LambdaQueryWrapper<LogisticsData>()
                        .eq(LogisticsData::getBatchId, batchId)
                        .orderByAsc(LogisticsData::getId));
        List<TraceChainDTO.LogisticsNode> logisticsNodes = logisticsList.stream().map(l -> {
            TraceChainDTO.LogisticsNode node = new TraceChainDTO.LogisticsNode();
            node.setId(l.getId());
            node.setGpsLng(l.getGpsLng());
            node.setGpsLat(l.getGpsLat());
            node.setTemperature(l.getTemperature());
            node.setHumidity(l.getHumidity());
            node.setRecordTime(l.getRecordTime() != null ? l.getRecordTime().format(DT_FORMAT) : "");
            return node;
        }).collect(Collectors.toList());
        dto.setLogistics(logisticsNodes);

        RiskAssessment risk = riskAssessmentMapper.selectOne(
                new LambdaQueryWrapper<RiskAssessment>()
                        .eq(RiskAssessment::getBatchId, batchId)
                        .orderByDesc(RiskAssessment::getId).last("LIMIT 1"));
        if (risk != null) {
            TraceChainDTO.RiskNode riskNode = new TraceChainDTO.RiskNode();
            riskNode.setId(risk.getId());
            riskNode.setRiskLevel(risk.getRiskLevel());
            riskNode.setRiskScore(risk.getRiskScore());
            riskNode.setAssessmentDate(risk.getAssessmentDate() != null ? risk.getAssessmentDate().toString() : "");
            riskNode.setFactors(risk.getFactors());
            dto.setRisk(riskNode);
        }

        List<AlertRecord> alerts = alertRecordMapper.selectList(
                new LambdaQueryWrapper<AlertRecord>()
                        .eq(AlertRecord::getBatchId, batchId)
                        .orderByDesc(AlertRecord::getCreateTime));
        List<TraceChainDTO.AlertNode> alertNodes = alerts.stream().map(a -> {
            TraceChainDTO.AlertNode node = new TraceChainDTO.AlertNode();
            node.setId(a.getId());
            node.setAlertType(a.getAlertType());
            node.setRiskScore(a.getRiskScore());
            node.setCreateTime(a.getCreateTime() != null ? a.getCreateTime().format(DT_FORMAT) : "");
            node.setHandled(a.getHandled() == 1);
            return node;
        }).collect(Collectors.toList());
        dto.setAlerts(alertNodes);

        return dto;
    }
}
