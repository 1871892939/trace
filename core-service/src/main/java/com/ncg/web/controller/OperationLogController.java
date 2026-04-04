package com.ncg.web.controller;

import com.ncg.dto.OperationLogDTO;
import com.ncg.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/operation-log")
@CrossOrigin(origins = "*")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Map<String, Object> getLogList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<OperationLogDTO> list = operationLogService.listLogs(keyword, operationType, module, status);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询日志失败：" + e.getMessage());
        }
        return result;
    }
}
