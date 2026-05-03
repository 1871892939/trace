package com.ncg.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 全局异常处理器
 *
 * 功能说明：
 *  - 限流异常（FlowException）：当前 QPS 超过阈值，返回 429 Too Many Requests
 *  - 熔断异常（DegradeException）：接口被熔断降级，返回 503 Service Unavailable
 *  - 系统兜底异常（BlockException）：其他 Sentinel 限流场景，返回 429
 *
 * 所有被 @SentinelResource 注解保护的资源，触发限流/熔断时均在此处统一处理，
 * 保证前端收到的 JSON 格式与其他 Controller 方法一致（code/message/data 结构）。
 */
@RestControllerAdvice
public class SentinelExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SentinelExceptionHandler.class);

    /**
     * 限流异常处理
     * 触发场景：接口 QPS 瞬时超过阈值（QPS=10），多余请求直接被拒绝
     */
    @ExceptionHandler(FlowException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, Object> handleFlowException(FlowException e) {
        log.warn("[Sentinel] 触发限流 - 类型: {}, 描述: {}",
                e.getClass().getSimpleName(), e.getMessage());
        return buildResponse(429, "请求过于频繁，请稍后重试", null);
    }

    /**
     * 熔断降级异常处理
     * 触发场景：接口慢调用比例 / 异常比例超过阈值，进入熔断状态
     */
    @ExceptionHandler(DegradeException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, Object> handleDegradeException(DegradeException e) {
        log.warn("[Sentinel] 触发熔断 - 类型: {}, 描述: {}",
                e.getClass().getSimpleName(), e.getMessage());
        return buildResponse(503, "服务暂时不可用，请稍后重试", null);
    }

    /**
     * Sentinel 通用阻塞异常兜底处理
     * 兜住所有 BlockException 子类（FlowException / DegradeException 之外的边界情况）
     */
    @ExceptionHandler(BlockException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, Object> handleBlockException(BlockException e) {
        log.warn("[Sentinel] 触发 Sentinel 阻断 - 类型: {}, 描述: {}",
                e.getClass().getSimpleName(), e.getMessage());
        return buildResponse(429, "系统繁忙，请稍后重试", null);
    }

    private Map<String, Object> buildResponse(int code, String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("message", message);
        result.put("data", data);
        return result;
    }
}
