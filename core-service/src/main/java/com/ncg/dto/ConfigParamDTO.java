package com.ncg.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 配置参数响应 DTO
 */
@Data
public class ConfigParamDTO {

    private Long id;

    private String paramKey;

    private String paramName;

    private String paramValue;

    private String paramType;

    private String paramGroup;

    private String description;

    private Integer editable;

    private String updateTime;
}
