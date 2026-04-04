package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流数据实体类
 */
@Data
@TableName("logistics_data")
public class LogisticsData {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次 ID
     */
    private Long batchId;

    /**
     * GPS 经度
     */
    private BigDecimal gpsLng;

    /**
     * GPS 纬度
     */
    private BigDecimal gpsLat;

    /**
     * 车厢温度
     */
    private BigDecimal temperature;

    /**
     * 车厢湿度
     */
    private BigDecimal humidity;

    /**
     * 采集时间戳
     */
    private LocalDateTime recordTime;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
