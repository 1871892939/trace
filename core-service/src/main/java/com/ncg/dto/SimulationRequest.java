package com.ncg.dto;

import lombok.Data;

/**
 * 数据模拟请求
 */
@Data
public class SimulationRequest {

    /**
     * 数据类型：normal - 正常数据，anomaly - 异常数据
     */
    private String type;

    /**
     * 模拟数量
     */
    private Integer count;
}
