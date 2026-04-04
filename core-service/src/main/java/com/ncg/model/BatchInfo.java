package com.ncg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 食品批次信息实体类
 */
@Data
@TableName("batch_info")
public class BatchInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 批次编号 (业务主键)
     */
    private String batchNo;

    /**
     * 产地编码
     */
    private String origin;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 所属企业
     */
    private String enterprise;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
