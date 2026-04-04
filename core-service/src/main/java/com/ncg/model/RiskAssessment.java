package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
     * 风险等级：Low(低风险 0-40 分)/Medium(中风险 41-70 分)/High(高风险 71-100 分)
     */
    private String riskLevel;

    /**
     * 风险评分 (0-100)
     */
    private Integer riskScore;

    /**
     * 评估日期
     */
    private LocalDate assessmentDate;

    /**
     * 风险因素 (JSON 字符串，包含各项得分明细)
     */
    private String factors;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
