package com.ncg.schedule;

import com.ncg.dto.OverviewDTO;
import com.ncg.service.ConfigService;
import com.ncg.service.DataChangeNotifier;
import com.ncg.service.DataCleanService;
import com.ncg.service.OverviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时清洗调度器
 *
 * 定时任务：根据 config_param 中的开关和间隔配置，
 *          周期性执行 DataCleanService.cleanAllUnprocessed() ，
 *          清洗所有未清洗（cleaned=0）的批次。
 *
 * 配置项（config_param）：
 *   - schedule.enabled   ：定时清洗开关，true=启用，false=禁用（默认 false）
 *   - schedule.interval   ：清洗间隔，单位秒，默认 10 秒
 *
 * 使用 AtomicBoolean 防止上一次任务尚未执行完时重复触发。
 */
@Component
public class DataCleanScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataCleanScheduler.class);

    @Autowired
    private DataCleanService dataCleanService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private DataChangeNotifier dataChangeNotifier;

    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 每 10 秒执行一次（实际间隔由配置动态控制，见 runIfEnabled）
     */
    @Scheduled(fixedDelayString = "10000")
    public void scheduledClean() {
        runIfEnabled();
    }

    private void runIfEnabled() {
        try {
            String enabledStr = configService.getValue("schedule.enabled", "false");
            boolean enabled = "true".equalsIgnoreCase(enabledStr.trim());
            if (!enabled) {
                return;
            }
        } catch (Exception e) {
            log.warn("[DataCleanScheduler] 无法读取 schedule.enabled 配置，跳过本次执行：{}", e.getMessage());
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.info("[DataCleanScheduler] 上一次清洗任务仍在执行中，跳过本次触发");
            return;
        }

        try {
            int cleaned = dataCleanService.cleanAllUnprocessed();
            if (cleaned > 0) {
                log.info("[DataCleanScheduler] 本轮定时清洗完成，共处理 {} 个批次", cleaned);
                // 推送最新大盘数据
                try {
                    OverviewDTO overview = overviewService.getOverview();
                    dataChangeNotifier.pushOverviewUpdate(overview);
                } catch (Exception e2) {
                    log.warn("[DataCleanScheduler] 推送 WebSocket 失败：{}", e2.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[DataCleanScheduler] 定时清洗执行异常：{}", e.getMessage(), e);
        } finally {
            running.set(false);
        }
    }
}
