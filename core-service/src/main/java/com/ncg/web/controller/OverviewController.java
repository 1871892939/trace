package com.ncg.web.controller;

import com.ncg.dto.OverviewDTO;
import com.ncg.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 大盘概览控制器
 */
@RestController
@RequestMapping("/api/overview")
@CrossOrigin(origins = "*")
public class OverviewController {

    @Autowired
    private OverviewService overviewService;

    /**
     * 获取系统大盘概览数据
     *
     * @return 聚合后的概览数据
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();

        try {
            OverviewDTO data = overviewService.getOverview();

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取概览数据失败：" + e.getMessage());
        }

        return result;
    }
}
