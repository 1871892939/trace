package com.ncg.dto;

import lombok.Data;

/**
 * 配置参数更新请求 DTO
 */
@Data
public class ConfigUpdateRequest {

    private Long id;

    private String paramValue;
}
