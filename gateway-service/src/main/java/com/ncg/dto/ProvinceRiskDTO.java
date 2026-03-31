package com.ncg.dto;

import lombok.Data;

/**
 * 省份风险等级 DTO
 */
@Data
public class ProvinceRiskDTO {
    
    /**
     * 省份名称
     */
    private String name;
    
    /**
     * 风险值 (0-1)
     */
    private Double value;
    
    public ProvinceRiskDTO(String name, Double value) {
        this.name = name;
        this.value = value;
    }
}
