package com.ncg.schedule;

import com.ncg.dto.OverviewDTO;
import com.ncg.service.ConfigService;
import com.ncg.service.DataChangeNotifier;
import com.ncg.service.DataCleanService;
import com.ncg.service.OverviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时清洗调度器
 *
 * 使用 TaskScheduler 实现动态可调的定时清洗任务：
 *   - 通过 ConfigService 监听配置变更，参数修改后立即生效
 *   - 支持动态开启/关闭定时清洗（schedule.enabled）
 *   - 支持动态调整清洗间隔（schedule.interval，秒）
 *
 * 配置项（config_param）：
 *   - schedule.enabled  ：定时清洗开关，true=启用，false=禁用（默认 false）
 *   - schedule.interval ：清洗间隔，单位秒，默认 10 秒
 */
@Component
public class DataCleanScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataCleanScheduler.class);

    private static final String KEY_ENABLED = "schedule.enabled";
    private static final String KEY_INTERVAL = "schedule.interval";

    @Autowired
    private DataCleanService dataCleanService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private OverviewService overviewService;

    @Autowired
    private DataChangeNotifier dataChangeNotifier;

    /** 任务调度器（线程池） */
    private TaskScheduler taskScheduler;

    /** 当前已提交的未来任务引用，用于取消 */
    private ScheduledFuture<?> currentTask;

    /** 防止任务并发执行 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** 是否已开启定时调度 */
    private volatile boolean scheduled = false;

    /** ==================== 生命周期钩子 ==================== */

    @PostConstruct
    public void init() {
        // 初始化 TaskScheduler（线程池）
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("DataCleanScheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        this.taskScheduler = scheduler;

        // 注册配置变更监听
        configService.addListener(KEY_ENABLED, enabledListener);
        configService.addListener(KEY_INTERVAL, intervalListener);

        // 读取初始配置，决定是否立即启动
        boolean enabled = "true".equalsIgnoreCase(
                configService.getValue(KEY_ENABLED, "false").trim());
        if (enabled) {
            doSchedule();
        } else {
            log.info("[DataCleanScheduler] 初始状态：定时清洗已禁用（schedule.enabled=false），可在参数配置中开启");
        }
    }

    @PreDestroy
    public void destroy() {
        configService.removeListener(KEY_ENABLED, enabledListener);
        configService.removeListener(KEY_INTERVAL, intervalListener);
        if (currentTask != null) {
            currentTask.cancel(false);
        }
        log.info("[DataCleanScheduler] 已停止并清理资源");
    }

    /** ==================== 配置监听器 ==================== */

    /**
     * schedule.enabled 变更监听
     * true  → 开启定时调度
     * false → 停止定时调度
     */
    private final ConfigService.ConfigChangeListener enabledListener = (key, newValue) -> {
        boolean enabled = "true".equalsIgnoreCase(newValue.trim());
        log.info("[DataCleanScheduler] 检测到 schedule.enabled 变更：{}", enabled);
        if (enabled) {
            doSchedule();
        } else {
            doUnschedule();
        }
    };

    /**
     * schedule.interval 变更监听
     * 已在调度中 → 取消当前任务，立即用新间隔重新调度
     * 未在调度中 → 仅记录，启用时会用新间隔
     */
    private final ConfigService.ConfigChangeListener intervalListener = (key, newValue) -> {
        log.info("[DataCleanScheduler] 检测到 schedule.interval 变更：{}s", newValue);
        if (scheduled) {
            log.info("[DataCleanScheduler] 已在运行，重新调度（取消旧任务）...");
            doReschedule();
        }
    };

    /** ==================== 调度控制 ==================== */

    /**
     * 开始定时调度（首次或重新开启）
     */
    private synchronized void doSchedule() {
        if (scheduled) return;
        scheduled = true;
        submitTask();
        log.info("[DataCleanScheduler] 定时清洗已开启");
    }

    /**
     * 停止定时调度（禁用开关）
     */
    private synchronized void doUnschedule() {
        if (!scheduled) return;
        scheduled = false;
        if (currentTask != null) {
            currentTask.cancel(false);
            currentTask = null;
        }
        log.info("[DataCleanScheduler] 定时清洗已停止");
    }

    /**
     * 重新调度（间隔变更时用新间隔重新提交任务）
     */
    private synchronized void doReschedule() {
        if (!scheduled) return;
        if (currentTask != null) {
            currentTask.cancel(false);
        }
        submitTask();
    }

    /**
     * 读取当前配置的间隔（秒），解析失败时返回默认值 10
     */
    private long readIntervalSeconds() {
        try {
            String val = configService.getValue(KEY_INTERVAL, "10").trim();
            long interval = Long.parseLong(val);
            return interval > 0 ? interval : 10L;
        } catch (NumberFormatException e) {
            log.warn("[DataCleanScheduler] schedule.interval 格式错误，使用默认值 10s：{}", e.getMessage());
            return 10L;
        }
    }

    /**
     * 向 TaskScheduler 提交一个带固定延时的 Runnable
     */
    private void submitTask() {
        long intervalMs = readIntervalSeconds() * 1000L;
        log.info("[DataCleanScheduler] 提交定时任务，间隔 {}s", intervalMs / 1000);

        currentTask = taskScheduler.scheduleAtFixedRate(
                this::runOnce,
                intervalMs
        );
    }

    /** ==================== 实际执行逻辑 ==================== */

    /**
     * 实际执行一次清洗
     */
    private void runOnce() {
        if (!running.compareAndSet(false, true)) {
            log.info("[DataCleanScheduler] 上一次清洗任务仍在执行中，跳过本次触发");
            return;
        }

        try {
            int cleaned = dataCleanService.cleanAllUnprocessed();
            if (cleaned > 0) {
                log.info("[DataCleanScheduler] 本轮定时清洗完成，共处理 {} 个批次", cleaned);
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
