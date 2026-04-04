package com.ncg.web.controller;

import com.ncg.dto.AlertDashboardDTO;
import com.ncg.dto.AlertListDTO;
import com.ncg.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警控制器
 */
@RestController
@RequestMapping("/api/alert")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping("/list")
    public Map<String, Object> getAlertList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) Boolean handled) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<AlertListDTO> data = alertService.queryAlerts(keyword, alertType, handled);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询预警列表失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/handle/{alertId}")
    public Map<String, Object> handleAlert(@PathVariable Long alertId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean ok = alertService.handleAlert(alertId);
            if (ok) {
                result.put("code", 200);
                result.put("message", "处理成功");
            } else {
                result.put("code", 404);
                result.put("message", "预警记录不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "处理失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        try {
            AlertDashboardDTO data = alertService.getDashboard();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取预警大盘失败：" + e.getMessage());
        }
        return result;
    }
}
