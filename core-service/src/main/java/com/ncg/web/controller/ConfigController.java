package com.ncg.web.controller;

import com.ncg.dto.ConfigParamDTO;
import com.ncg.dto.ConfigUpdateRequest;
import com.ncg.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/list")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "config:list")
    public Map<String, Object> getConfigList(@RequestParam(required = false) String group) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ConfigParamDTO> list = configService.getByGroup(group);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询配置失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/groups")
    public Map<String, Object> getGroups() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> groups = configService.getAllGroups();
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", groups);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分组失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    @com.alibaba.csp.sentinel.annotation.SentinelResource(value = "config:update")
    public Map<String, Object> updateConfig(@RequestBody ConfigUpdateRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean ok = configService.updateParam(req);
            if (ok) {
                result.put("code", 200);
                result.put("message", "更新成功");
            } else {
                result.put("code", 400);
                result.put("message", "参数不存在或不可编辑");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }
}
