package com.ncg.dto;

import lombok.Data;

import java.util.List;

/**
 * WebSocket 实时消息 DTO
 */
@Data
public class WebSocketMessage {
    
    /**
     * 消息类型：trend/alert/config
     */
    private String type;
    
    /**
     * 消息数据
     */
    private Object data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public WebSocketMessage(String type, Object data, Long timestamp) {
        this.type = type;
        this.data = data;
        this.timestamp = timestamp;
    }
}
