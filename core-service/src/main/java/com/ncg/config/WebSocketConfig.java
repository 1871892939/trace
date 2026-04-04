package com.ncg.config;

import com.ncg.websocket.DataChangeWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置
 *
 * 将 DataChangeWebSocketHandler 注册为 /ws/data-change 端点。
 * 前端通过 new WebSocket('ws://host/ws/data-change') 连接。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DataChangeWebSocketHandler dataChangeWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dataChangeWebSocketHandler, "/ws/data-change")
                .setAllowedOrigins("*");
    }
}
