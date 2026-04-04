package com.ncg.service;

import com.ncg.websocket.DataChangeWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据变更通知服务
 *
 * 统一对外暴露 WebSocket 广播能力。
 * 当业务数据发生变更时（如新批次、新预警、大盘数据变化），
 * 调用对应方法将最新数据广播给所有已连接的 WebSocket 客户端。
 *
 * 客户端收到消息后格式为：
 *   { type: "OVERVIEW_UPDATE" | "ALERT_NEW" | "BATCH_NEW", data: {...}, timestamp: "..." }
 */
@Service
public class DataChangeNotifier {

    private static final Logger log = LoggerFactory.getLogger(DataChangeNotifier.class);

    @Autowired
    private DataChangeWebSocketHandler webSocketHandler;

    /**
     * 推送大盘概览数据更新
     *
     * @param overviewData 最新的大盘数据（OverviewDTO 序列化后的 Map）
     */
    public void pushOverviewUpdate(Object overviewData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "OVERVIEW_UPDATE");
        payload.put("data", overviewData);
        webSocketHandler.broadcast(payload);
        log.info("[WS] 推送 OVERVIEW_UPDATE，活跃连接数：{}", webSocketHandler.getActiveSessionCount());
    }

    /**
     * 推送新预警通知
     *
     * @param alertData 新预警的简要信息（可包含 alertId、alertType、batchNo 等）
     */
    public void pushAlertNew(Object alertData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ALERT_NEW");
        payload.put("data", alertData);
        webSocketHandler.broadcast(payload);
        log.info("[WS] 推送 ALERT_NEW，活跃连接数：{}", webSocketHandler.getActiveSessionCount());
    }

    /**
     * 推送新批次通知
     *
     * @param batchData 新批次的简要信息（可包含 batchId、batchNo 等）
     */
    public void pushBatchNew(Object batchData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "BATCH_NEW");
        payload.put("data", batchData);
        webSocketHandler.broadcast(payload);
        log.info("[WS] 推送 BATCH_NEW，活跃连接数：{}", webSocketHandler.getActiveSessionCount());
    }
}
