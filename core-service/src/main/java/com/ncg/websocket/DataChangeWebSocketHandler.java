package com.ncg.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 处理器 — 管理所有已连接的客户端会话，
 * 并对外暴露广播消息的能力。
 *
 * 协议：所有消息均为 JSON 格式，格式为：
 *   { "type": "OVERVIEW_UPDATE" | "ALERT_NEW" | "BATCH_NEW" | "HEARTBEAT", "data": { ... }, "timestamp": "..." }
 *
 * 客户端连接地址：ws://host/ws/data-change
 */
@Component
public class DataChangeWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(DataChangeWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("[WS] 客户端连接建立，当前连接数：{}，sessionId={}", sessions.size(), session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("[WS] 客户端断开，当前连接数：{}，sessionId={}，原因={}", sessions.size(), session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
        log.warn("[WS] 连接传输异常，已移除 sessionId={}，error={}", session.getId(), exception.getMessage());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 目前客户端暂不需要主动发消息，此处可扩展心跳检测等
        log.debug("[WS] 收到客户端消息：{}，sessionId={}", message.getPayload(), session.getId());
    }

    /**
     * 向所有已连接的客户端广播消息
     *
     * @param payload 消息体（Map，会序列化为 JSON）
     */
    public void broadcast(Map<String, Object> payload) {
        if (sessions.isEmpty()) {
            return;
        }
        try {
            payload.put("timestamp", java.time.LocalDateTime.now().toString());
            String json = objectMapper.writeValueAsString(payload);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        log.warn("[WS] 发送消息失败，移除异常 sessionId={}", session.getId());
                        sessions.remove(session);
                    }
                }
            }
        } catch (Exception e) {
            log.error("[WS] 广播消息序列化失败：{}", e.getMessage());
        }
    }

    public int getActiveSessionCount() {
        return (int) sessions.stream().filter(WebSocketSession::isOpen).count();
    }
}
