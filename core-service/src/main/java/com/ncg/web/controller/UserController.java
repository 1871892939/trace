package com.ncg.web.controller;

import com.ncg.dto.UserDTO;
import com.ncg.dto.UserUpdateRequest;
import com.ncg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器（仅管理员可访问）
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Map<String, Object> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<UserDTO> list = userService.listUsers(keyword, role, status);
            result.put("code", 200);
            result.put("message", "success");
            result.put("data", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询用户列表失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> createUser(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.createUser(body.get("username"), body.get("password"), body.get("role"));
            result.put("code", 200);
            result.put("message", "创建成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> updateUser(@RequestBody UserUpdateRequest req) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.updateUser(req);
            result.put("code", 200);
            result.put("message", "更新成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.deleteUser(id);
            result.put("code", 200);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
