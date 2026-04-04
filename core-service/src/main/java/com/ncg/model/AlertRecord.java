package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 */
@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次 ID
     */
    private Long batchId;

    /**
     * 预警类型：TEMP(温度异常)/HUMIDITY(湿度异常)/PESTICIDE(农残超标)/HEAVY_METAL(重金属超标)/MICROBE(微生物超标)/COMPOSITE(综合风险)
     */
    private String alertType;

    /**
     * 风险分数 (0-1)
     */
    private BigDecimal riskScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否已处理：0-未处理 1-已处理
     */
    private Integer handled;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
