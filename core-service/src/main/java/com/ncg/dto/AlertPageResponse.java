package com.ncg.dto;

import com.ncg.model.AlertRecord;
import lombok.Data;

import java.util.List;

/**
 * 预警列表分页响应 DTO
 */
@Data
public class AlertPageResponse {
    
    /**
     * 预警列表
     */
    private List<AlertRecord> list;
    
    /**
     * 总数
     */
    private Long total;
    
    public AlertPageResponse(List<AlertRecord> list, Long total) {
        this.list = list;
        this.total = total;
    }
}
