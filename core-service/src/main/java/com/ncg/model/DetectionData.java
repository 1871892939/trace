package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检测数据实体类
 */
@Data
@TableName("detection_data")
public class DetectionData {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 批次 ID
     */
    private Long batchId;
    
    /**
     * 农残值
     */
    private BigDecimal pesticide;
    
    /**
     * 重金属值
     */
    private BigDecimal heavyMetal;
    
    /**
     * 微生物值
     */
    private BigDecimal microbe;
    
    /**
     * 检测时间
     */
    private LocalDateTime testTime;
}
