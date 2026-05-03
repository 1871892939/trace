package com.ncg.web.controller;

import com.ncg.dto.BatchQueryDTO;
import com.ncg.dto.TraceChainDTO;
import com.ncg.service.TraceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 溯源管理控制器
 */
@RestController
@RequestMapping("/api/trace")
@CrossOrigin(origins = "*")
public class TraceController {

    @Autowired
    private TraceService traceService;

    @GetMapping("/batch/query")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "trace:query")
    public Map<String, Object> queryBatches(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String alertType) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<BatchQueryDTO> data = traceService.queryBatches(keyword, riskLevel, alertType);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/chain/{batchId}")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "trace:chain")
    public Map<String, Object> getTraceChain(@PathVariable Long batchId) {
        Map<String, Object> result = new HashMap<>();
        try {
            TraceChainDTO data = traceService.getTraceChain(batchId);
            if (data == null) {
                result.put("code", 404);
                result.put("message", "批次不存在");
                result.put("data", null);
            } else {
                result.put("code", 200);
                result.put("message", "success");
                result.put("data", data);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取溯源链失败：" + e.getMessage());
        }
        return result;
    }
}
