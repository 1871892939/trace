package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置参数实体类
 */
@Data
@TableName("config_param")
public class ConfigParam {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 参数唯一标识（程序中引用的 key）
     */
    private String paramKey;

    /**
     * 参数中文名称（展示用）
     */
    private String paramName;

    /**
     * 参数值
     */
    private String paramValue;

    /**
     * 参数类型：number / string / boolean
     */
    private String paramType;

    /**
     * 参数分组：risk（风险评分）/ anomaly（异常检测）/ alert（预警阈值）
     */
    private String paramGroup;

    /**
     * 参数说明
     */
    private String description;

    /**
     * 是否可编辑：0-不可编辑 1-可编辑
     */
    private Integer editable;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
