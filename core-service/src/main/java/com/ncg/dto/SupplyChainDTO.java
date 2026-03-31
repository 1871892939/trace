package com.ncg.dto;

import lombok.Data;

import java.util.List;

/**
 * 供应链桑基图数据 DTO
 */
@Data
public class SupplyChainDTO {
    
    /**
     * 节点列表
     */
    private List<NodeDTO> nodes;
    
    /**
     * 边列表
     */
    private List<LinkDTO> links;
    
    @Data
    public static class NodeDTO {
        private String name;
    }
    
    @Data
    public static class LinkDTO {
        private String source;
        private String target;
        private Integer value;
    }
}
