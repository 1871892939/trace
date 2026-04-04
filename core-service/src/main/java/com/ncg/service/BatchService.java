package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.AlertRecordMapper;
import com.ncg.dal.mapper.BatchInfoMapper;
import com.ncg.dal.mapper.RiskAssessmentMapper;
import com.ncg.dto.BatchCreateRequest;
import com.ncg.dto.BatchUpdateRequest;
import com.ncg.model.AlertRecord;
import com.ncg.model.BatchInfo;
import com.ncg.model.RiskAssessment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 批次管理服务
 */
@Service
public class BatchService {

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    public void createBatch(BatchCreateRequest req, String operator) {
        BatchInfo batch = new BatchInfo();
        batch.setBatchNo(req.getBatchNo());
        batch.setOrigin(req.getOrigin());
        batch.setEnterprise(req.getEnterprise());
        if (req.getProductionDate() != null && !req.getProductionDate().isBlank()) {
            batch.setProductionDate(LocalDate.parse(req.getProductionDate()));
        }
        batch.setOperator(operator);
        batch.setCreateTime(LocalDateTime.now());
        batchInfoMapper.insert(batch);
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
        // 删除该批次已处理的预警和风险评估（脏数据）
        LambdaQueryWrapper<AlertRecord> alertWrapper = new LambdaQueryWrapper<AlertRecord>()
                .eq(AlertRecord::getBatchId, batchId);
        alertRecordMapper.delete(alertWrapper);

        LambdaQueryWrapper<RiskAssessment> riskWrapper = new LambdaQueryWrapper<RiskAssessment>()
                .eq(RiskAssessment::getBatchId, batchId);
        riskAssessmentMapper.delete(riskWrapper);

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
