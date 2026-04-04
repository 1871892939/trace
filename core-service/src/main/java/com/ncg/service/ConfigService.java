package com.ncg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.ConfigParamMapper;
import com.ncg.dto.ConfigParamDTO;
import com.ncg.dto.ConfigUpdateRequest;
import com.ncg.model.ConfigParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务
 *
 * 职责：配置的 CRUD + Redis 缓存 + 配置变更监听
 * 约定：所有算法类通过 ConfigService.getValue() 读取参数，而非直接读 DB
 */
@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CACHE_PREFIX = "config:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Autowired
    private ConfigParamMapper configParamMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 配置变更监听器
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        void onChange(String key, String newValue);
    }

    private final Map<String, List<ConfigChangeListener>> listeners = new ConcurrentHashMap<>();

    private static final String GLOBAL_LISTENER_KEY = "__global__";

    /**
     * 注册配置变更监听器
     *
     * @param key 监听的配置 key，为 GLOBAL_LISTENER_KEY 时监听所有 key 的变更
     * @param listener 变更回调
     */
    public void addListener(String key, ConfigChangeListener listener) {
        listeners.computeIfAbsent(normalizeKey(key), k -> Collections.synchronizedList(new ArrayList<>())).add(listener);
    }

    /**
     * 移除指定监听器
     */
    public void removeListener(String key, ConfigChangeListener listener) {
        List<ConfigChangeListener> list = listeners.get(normalizeKey(key));
        if (list != null) {
            list.remove(listener);
        }
    }

    private String normalizeKey(String key) {
        return key == null ? GLOBAL_LISTENER_KEY : key;
    }

    private void notifyListeners(String key, String newValue) {
        // 先通知精确匹配的监听器
        List<ConfigChangeListener> exact = listeners.get(key);
        if (exact != null) {
            for (ConfigChangeListener l : new ArrayList<>(exact)) {
                try {
                    l.onChange(key, newValue);
                } catch (Exception e) {
                    log.warn("[ConfigService] 监听器异常 key={}：{}", key, e.getMessage());
                }
            }
        }
        // 再通知全局监听器
        List<ConfigChangeListener> global = listeners.get(GLOBAL_LISTENER_KEY);
        if (global != null) {
            for (ConfigChangeListener l : new ArrayList<>(global)) {
                try {
                    l.onChange(key, newValue);
                } catch (Exception e) {
                    log.warn("[ConfigService] 全局监听器异常：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 按分组查询配置列表
     */
    public List<ConfigParamDTO> getByGroup(String group) {
        LambdaQueryWrapper<ConfigParam> wrapper = new LambdaQueryWrapper<>();
        if (group != null && !group.isBlank()) {
            wrapper.eq(ConfigParam::getParamGroup, group);
        }
        wrapper.orderByAsc(ConfigParam::getParamGroup, ConfigParam::getId);
        return configParamMapper.selectList(wrapper)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 按 key 查单个配置（优先 Redis 缓存）
     */
    public ConfigParam getByKey(String key) {
        String cacheKey = CACHE_PREFIX + key;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            ConfigParam p = new ConfigParam();
            p.setParamKey(key);
            p.setParamValue(cached);
            return p;
        }

        ConfigParam param = configParamMapper.selectOne(
                new LambdaQueryWrapper<ConfigParam>()
                        .eq(ConfigParam::getParamKey, key));
        if (param != null) {
            stringRedisTemplate.opsForValue().set(
                    cacheKey, param.getParamValue(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return param;
    }

    /**
     * 按 String key 读取配置值（找不到返回 null）
     */
    public String getValue(String key) {
        ConfigParam p = getByKey(key);
        return p != null ? p.getParamValue() : null;
    }

    /**
     * 按 String key 读取配置值（找不到返回默认值）
     */
    public String getValue(String key, String defaultValue) {
        String v = getValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * 按 numeric key 读取配置值（找不到返回 null）
     */
    public BigDecimal getNumericValue(String key) {
        String v = getValue(key);
        if (v == null) return null;
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 按 numeric key 读取配置值（找不到返回默认值）
     */
    public BigDecimal getNumericValue(String key, BigDecimal defaultValue) {
        BigDecimal v = getNumericValue(key);
        return v != null ? v : defaultValue;
    }

    /**
     * 更新配置值（同时写 DB + 刷 Redis 缓存 + 通知监听器）
     */
    @Transactional
    public boolean updateParam(ConfigUpdateRequest req) {
        ConfigParam param = configParamMapper.selectById(req.getId());
        if (param == null || param.getEditable() == 0) {
            return false;
        }

        String oldKey = param.getParamKey();
        param.setParamValue(req.getParamValue());
        param.setUpdateTime(LocalDateTime.now());
        int rows = configParamMapper.updateById(param);

        if (rows > 0) {
            stringRedisTemplate.opsForValue().set(
                    CACHE_PREFIX + oldKey, req.getParamValue(), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            // 通知所有监听该 key 的监听器
            notifyListeners(oldKey, req.getParamValue());
        }
        return rows > 0;
    }

    /**
     * 获取所有分组 key（用于前端标签页切换）
     */
    public List<String> getAllGroups() {
        List<ConfigParam> all = configParamMapper.selectList(null);
        return all.stream()
                .map(ConfigParam::getParamGroup)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private ConfigParamDTO toDTO(ConfigParam p) {
        ConfigParamDTO dto = new ConfigParamDTO();
        dto.setId(p.getId());
        dto.setParamKey(p.getParamKey());
        dto.setParamName(p.getParamName());
        dto.setParamValue(p.getParamValue());
        dto.setParamType(p.getParamType());
        dto.setParamGroup(p.getParamGroup());
        dto.setDescription(p.getDescription());
        dto.setEditable(p.getEditable());
        dto.setUpdateTime(p.getUpdateTime() != null ? p.getUpdateTime().format(DT_FORMAT) : "");
        return dto;
    }
}
