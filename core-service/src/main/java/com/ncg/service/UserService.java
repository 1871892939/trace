package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ncg.dal.mapper.UserInfoMapper;
import com.ncg.dto.UserDTO;
import com.ncg.dto.UserUpdateRequest;
import com.ncg.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户管理服务（仅管理员可用）
 */
@Service
public class UserService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private UserInfoMapper userInfoMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 查询用户列表
     */
    public List<UserDTO> listUsers(String keyword, String role, Integer status) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(UserInfo::getUsername, keyword);
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(UserInfo::getRole, role);
        }
        if (status != null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        wrapper.orderByAsc(UserInfo::getRole, UserInfo::getId);

        return userInfoMapper.selectList(wrapper)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 创建用户（管理员用）
     */
    @Transactional
    public void createUser(String username, String password, String role) {
        if (username == null || username.isBlank() || username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("用户名长度需在 3-20 个字符之间");
        }
        if (password == null || password.length() < 6 || password.length() > 20) {
            throw new RuntimeException("密码长度需在 6-20 个字符之间");
        }
        if (role == null || (!role.equals("admin") && !role.equals("supervisor"))) {
            throw new RuntimeException("角色必须为 admin 或 supervisor");
        }

        UserInfo existing = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, username));
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }

        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(1);
        userInfoMapper.insert(user);
    }

    /**
     * 更新用户信息（管理员用）
     */
    @Transactional
    public void updateUser(UserUpdateRequest req) {
        UserInfo user = userInfoMapper.selectById(req.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().length() < 6 || req.getPassword().length() > 20) {
                throw new RuntimeException("密码长度需在 6-20 个字符之间");
            }
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getRole() != null && !req.getRole().isBlank()) {
            if (!req.getRole().equals("admin") && !req.getRole().equals("supervisor")) {
                throw new RuntimeException("角色必须为 admin 或 supervisor");
            }
            user.setRole(req.getRole());
        }

        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
        }

        userInfoMapper.updateById(user);
    }

    /**
     * 删除用户（管理员用）
     */
    @Transactional
    public void deleteUser(Long id) {
        UserInfo user = userInfoMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        userInfoMapper.deleteById(id);
    }

    /**
     * 获取当前登录用户的简单信息
     */
    public UserDTO getCurrentUser(String username) {
        UserInfo user = userInfoMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, username));
        if (user == null) return null;
        return toDTO(user);
    }

    private UserDTO toDTO(UserInfo u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setRole(u.getRole());
        dto.setRoleName("admin".equals(u.getRole()) ? "管理员" : "监管员");
        dto.setStatus(u.getStatus());
        dto.setStatusName(u.getStatus() == 1 ? "启用" : "禁用");
        dto.setCreateTime(u.getCreateTime() != null ? u.getCreateTime().format(DT_FORMAT) : "");
        return dto;
    }
}
