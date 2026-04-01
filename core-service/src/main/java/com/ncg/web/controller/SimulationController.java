package com.ncg.web.controller;

import com.ncg.dto.SimulationResponse;
import com.ncg.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据模拟控制器
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    /**
     * 生成模拟数据
     *
     * @param type  数据类型：normal - 正常数据，anomaly - 异常数据
     * @param count 模拟数量
     * @return 模拟结果
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestParam("type") String type,
                                        @RequestParam(value = "count", defaultValue = "1") Integer count) {
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

            SimulationResponse response = simulationService.generateData(type, count);

            result.put("code", 200);
            result.put("message", response.getMessage());
            result.put("data", response);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "模拟数据生成失败：" + e.getMessage());
        }

        return result;
    }
}
