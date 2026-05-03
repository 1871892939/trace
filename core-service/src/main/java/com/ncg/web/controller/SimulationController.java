package com.ncg.web.controller;

import com.ncg.dto.SimulationResponse;
import com.ncg.service.SimulationService;
import com.ncg.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据模拟控制器
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {
    
    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractOperator(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null && !username.isBlank()) {
                    return username;
                }
            }
        } catch (Exception ignored) {}
        return "system";
    }

    /**
     * 生成模拟数据
     *
     * @param type  数据类型：normal - 正常数据，anomaly - 异常数据
     * @param count 模拟数量
     * @return 模拟结果
     */
    @PostMapping("/generate")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "simulation:generate")
    public Map<String, Object> generate(@RequestParam("type") String type,
                                        @RequestParam(value = "count", defaultValue = "1") Integer count,
                                        @RequestParam(value = "clean", defaultValue = "true") Boolean clean,
                                        HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (type == null || type.isEmpty()) {
                type = "normal";
            }
            if (!type.equals("normal") && !type.equals("anomaly")) {
                result.put("code", 400);
                result.put("message", "type 参数无效，请使用 normal 或 anomaly");
                return result;
            }
            if (count <= 0 || count > 100) {
                result.put("code", 400);
                result.put("message", "count 参数需在 1-100 之间");
                return result;
            }

            String operator = extractOperator(request);
            String typeLabel = "anomaly".equals(type) ? "异常" : "正常";
            String description = "数据模拟生成 " + count + " 条" + typeLabel + "批次";
            SimulationResponse response = simulationService.generateData(type, count, clean, operator, description);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", response);

        } catch (Exception e) {
            // 记录详细错误日志，但不暴露给前端
            logger.error("模拟数据生成失败 - type: {}, count: {}, error: {}", type, count, e.getMessage(), e);
            result.put("code", 500);
            result.put("message", "模拟数据生成失败，请稍后重试");
        }

        return result;
    }
}
