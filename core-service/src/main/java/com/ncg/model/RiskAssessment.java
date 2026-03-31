package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 风险评估实体类
 */
@Data
@TableName("risk_assessment")
public class RiskAssessment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 批次 ID
     */
    private Long batchId;
    
    /**
     * 风险等级：Low/Medium/High
     */
    private String riskLevel;
    
    /**
     * 评估日期
     */
    private LocalDate assessmentDate;
    
    /**
     * 风险因素 (JSON 字符串)
     */
    private String factors;
}
