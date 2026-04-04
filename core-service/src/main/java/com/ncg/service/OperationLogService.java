package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.BatchInfoMapper;
import com.ncg.dal.mapper.OperationLogMapper;
import com.ncg.dto.OperationLogDTO;
import com.ncg.model.BatchInfo;
import com.ncg.model.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志服务
 */
@Service
public class OperationLogService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    public void saveLog(OperationLog log) {
        if (log.getOperateTime() == null) {
            log.setOperateTime(LocalDateTime.now());
        }
        operationLogMapper.insert(log);
    }

    public List<OperationLogDTO> listLogs(String keyword, String operationType) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(OperationLog::getUsername, keyword)
                    .or()
                    .like(OperationLog::getDescription, keyword)
                    .or()
                    .like(OperationLog::getBatchNo, keyword));
        }
        if (operationType != null && !operationType.isBlank()) {
            wrapper.eq(OperationLog::getOperationType, operationType);
        }
        wrapper.orderByDesc(OperationLog::getOperateTime);
        wrapper.last("LIMIT 500");

        return operationLogMapper.selectList(wrapper)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private OperationLogDTO toDTO(OperationLog log) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(log.getId());
        dto.setUsername(log.getUsername());
        dto.setRole(log.getRole());
        dto.setRoleName("admin".equals(log.getRole()) ? "管理员" : "监管员");
        dto.setOperationType(log.getOperationType());
        dto.setOperationTypeName(switch (log.getOperationType()) {
            case "CREATE" -> "新增";
            case "UPDATE" -> "修改";
            case "DELETE" -> "删除";
            default -> log.getOperationType();
        });
        dto.setModule(log.getModule());
        dto.setDescription(log.getDescription());
        dto.setMethod(log.getMethod());
        dto.setRequestUrl(log.getRequestUrl());
        dto.setStatus(log.getStatus());
        dto.setStatusName("SUCCESS".equals(log.getStatus()) ? "成功" : "失败");
        dto.setErrorMsg(log.getErrorMsg());
        dto.setIpAddress(log.getIpAddress());
        dto.setOperator(log.getOperator());
        dto.setBatchNo(log.getBatchNo());
        dto.setBatchCreateTime("");
        dto.setBatchUpdateTime("");
        // 通过 batchNo 查询批次信息以获取批次创建/修改时间
        if (log.getBatchNo() != null && !log.getBatchNo().isBlank()) {
            BatchInfo batch = batchInfoMapper.selectOne(
                    new LambdaQueryWrapper<BatchInfo>()
                            .eq(BatchInfo::getBatchNo, log.getBatchNo())
                            .last("LIMIT 1"));
            if (batch != null) {
                dto.setBatchCreateTime(batch.getCreateTime() != null ? batch.getCreateTime().format(DT_FORMAT) : "");
                dto.setBatchUpdateTime(batch.getUpdateTime() != null ? batch.getUpdateTime().format(DT_FORMAT) : "");
            }
        }
        dto.setOperateTime(log.getOperateTime() != null ? log.getOperateTime().format(DT_FORMAT) : "");
        return dto;
    }
}
