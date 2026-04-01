package com.ncg.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据录入请求 DTO
 */
@Data
public class DataEntryRequest {
    
    /**
     * 批次信息
     */
    private String batchNo;          // 批次编号
    private String origin;           // 产地编码
    private LocalDate productionDate; // 生产日期
    private String enterprise;       // 所属企业
    
    /**
     * 检测数据
     */
    private BigDecimal pesticide;    // 农残值
    private BigDecimal heavyMetal;   // 重金属值
    private BigDecimal microbe;      // 微生物值
    private LocalDateTime testTime;  // 检测时间
    
    /**
     * 物流数据
     */
    private BigDecimal temperature;  // 温度
    private BigDecimal humidity;     // 湿度
    private BigDecimal gpsLng;       // GPS 经度
    private BigDecimal gpsLat;       // GPS 纬度
    private LocalDateTime recordTime; // 采集时间
}
