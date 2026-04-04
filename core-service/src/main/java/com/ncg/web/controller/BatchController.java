package com.ncg.web.controller;

import com.ncg.dto.BatchCreateRequest;
import com.ncg.dto.BatchUpdateRequest;
import com.ncg.service.BatchService;
import com.ncg.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 批次管理控制器
 */
@RestController
@RequestMapping("/api/batch")
@CrossOrigin(origins = "*")
public class BatchController {

    @Autowired
    private BatchService batchService;

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

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody BatchCreateRequest req, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String operator = extractOperator(request);
            batchService.createBatch(req, operator);
            result.put("code", 200);
            result.put("message", "批次录入成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody BatchUpdateRequest req, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String operator = extractOperator(request);
            batchService.updateBatch(req, operator);
            result.put("code", 200);
            result.put("message", "批次更新成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{batchId}")
    public Map<String, Object> delete(@PathVariable Long batchId) {
        Map<String, Object> result = new HashMap<>();
        try {
            batchService.deleteBatch(batchId);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/check-batch-no")
    public Map<String, Object> checkBatchNo(
            @RequestParam String batchNo,
            @RequestParam(required = false) Long excludeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = batchService.checkBatchNoExists(batchNo, excludeId);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
