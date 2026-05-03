package com.ncg.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 限流与熔断规则配置
 *
 * 功能说明：
 *  - 限流规则（FlowRule）：控制接口的 QPS，防止瞬时高并发压垮系统
 *  - 熔断规则（DegradeRule）：当接口慢调用或异常比例过高时，自动熔断降级
 *
 * 接入 Sentinel Dashboard 后，可在控制台动态下发规则覆盖此处配置。
 * 本配置类仅作为规则初始化，当 Dashboard 下发规则时会自动合并。
 */
@Configuration
public class SentinelConfig {

    private static final Logger log = LoggerFactory.getLogger(SentinelConfig.class);

    /**
     * 系统级兜底资源名（对应 @SentinelResource(value = "systemDefault")）
     */
    public static final String SYSTEM_DEFAULT = "systemDefault";

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        log.info("[Sentinel] 限流与熔断规则初始化完成");
    }

    // ==================== 限流规则 ====================

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // ── 核心写接口 ── 严格限流：QPS=10
        rules.add(buildFlowRule("batch:create",      10, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("batch:update",     10, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("batch:delete",     10, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("alert:handle",      10, RuleConstant.FLOW_GRADE_QPS, 0));

        // ── 认证接口 ── QPS=20（登录接口高发刷脸攻击）
        rules.add(buildFlowRule("auth:login",       20, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("auth:register",    10, RuleConstant.FLOW_GRADE_QPS, 0));

        // ── 查询接口 ── 适度放宽：QPS=50（监管大屏高频访问）
        rules.add(buildFlowRule("overview:dashboard", 50, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("alert:dashboard",    50, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("alert:list",         30, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("trace:query",        30, RuleConstant.FLOW_GRADE_QPS, 0));
        rules.add(buildFlowRule("trace:chain",        30, RuleConstant.FLOW_GRADE_QPS, 0));

        // ── 重量级接口 ── 严格限制：QPS=5（数据模拟+数据清洗）
        rules.add(buildFlowRule("simulation:generate", 5, RuleConstant.FLOW_GRADE_QPS, 0));

        // ── 系统兜底规则 ── QPS=100（兜住所有未单独配置的资源）
        rules.add(buildFlowRule(SYSTEM_DEFAULT, 100, RuleConstant.FLOW_GRADE_QPS, 0));

        FlowRuleManager.loadRules(rules);
        log.info("[Sentinel] 限流规则加载，共 {} 条", rules.size());
    }

    /**
     * 构建一条限流规则
     *
     * @param resource         资源名（与 @SentinelResource(value="...") 对应）
     * @param count            阈值（QPS 或并发数）
     * @param grade            限流类型：QPS（FLOW_GRADE_QPS）或并发数（FLOW_THREAD）
     * @param controlBehavior  流控效果：0=直接拒绝，1=冷启动预热，2=匀速排队
     */
    private FlowRule buildFlowRule(String resource, double count, int grade, int controlBehavior) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setCount(count);
        rule.setGrade(grade);
        rule.setControlBehavior(controlBehavior);
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        return rule;
    }

    // ==================== 熔断规则 ====================

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // ── 批次录入：熔断策略 = 慢调用比例 ──
        // 慢调用比例阈值 50%，最大响应时间 2 秒，最小请求数 5
        rules.add(buildDegradeRule("batch:create", RuleConstant.DEGRADE_GRADE_RT,
                2000, 0.5, 5, 10));

        // ── 预警处理：熔断策略 = 异常比例 ──
        // 异常比例超过 50% 时熔断，最小请求数 5，熔断持续 30 秒
        rules.add(buildDegradeRule("alert:handle", RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO,
                2000, 0.5, 5, 30));

        // ── 模拟生成：熔断策略 = 慢调用比例 ──
        // 数据模拟涉及算法计算，耗时长；慢调用阈值 3 秒，比例 60%
        rules.add(buildDegradeRule("simulation:generate", RuleConstant.DEGRADE_GRADE_RT,
                3000, 0.6, 3, 30));

        // ── 溯源链查询：熔断策略 = 异常比例 ──
        // 聚合查询场景，异常比例 50%
        rules.add(buildDegradeRule("trace:chain", RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO,
                2000, 0.5, 5, 30));

        // ── 大盘接口：熔断策略 = 慢调用比例 ──
        // 监管大屏高频访问，慢调用阈值 3 秒，比例 60%
        rules.add(buildDegradeRule("overview:dashboard", RuleConstant.DEGRADE_GRADE_RT,
                3000, 0.6, 10, 30));

        DegradeRuleManager.loadRules(rules);
        log.info("[Sentinel] 熔断规则加载，共 {} 条", rules.size());
    }

    /**
     * 构建一条熔断规则
     *
     * @param resource           资源名
     * @param grade              熔断策略：RT（慢调用比例）/ 异常比例 / 异常数
     * @param count              慢调用最大响应时间（毫秒）或异常比例阈值
     * @param ratio              比例阈值（0.0~1.0）
     * @param minRequestCount    最小请求数（熔断触发前必须积累的请求量）
     * @param timeWindowSeconds   熔断持续时间（秒）
     */
    private DegradeRule buildDegradeRule(String resource, int grade, long count,
                                         double ratio, int minRequestCount,
                                         int timeWindowSeconds) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(grade);
        if (grade == RuleConstant.DEGRADE_GRADE_RT) {
            rule.setCount(count);
            rule.setSlowRatioThreshold(ratio);
        } else {
            rule.setCount(count);
        }
        rule.setMinRequestAmount(minRequestCount);
        rule.setTimeWindow(timeWindowSeconds);
        return rule;
    }
}
