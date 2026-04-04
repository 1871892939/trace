package com.ncg.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncg.dal.mapper.ConfigParamMapper;
import com.ncg.model.ConfigParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统配置参数初始化器
 *
 * 应用启动时检查 config_param 表是否有数据，
 * 若为空则写入 SQL 脚本中定义的全部默认值。
 * 同时将配置同步到 Redis 缓存中预热。
 */
@Component
public class ConfigInitializer implements ApplicationRunner {

    @Autowired
    private ConfigParamMapper configParamMapper;

    @Autowired(required = false)
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        long count = configParamMapper.selectCount(null);
        if (count > 0) {
            warmupCache();
            return;
        }

        List<ConfigParam> defaults = buildDefaults();
        for (ConfigParam p : defaults) {
            configParamMapper.insert(p);
        }

        warmupCache();
    }

    private void warmupCache() {
        if (stringRedisTemplate == null) return;
        List<ConfigParam> all = configParamMapper.selectList(null);
        for (ConfigParam p : all) {
            stringRedisTemplate.opsForValue().set(
                    "config:" + p.getParamKey(), p.getParamValue());
        }
    }

    private List<ConfigParam> buildDefaults() {
        return List.of(
                cfg("risk.low.threshold", "低风险阈值", "40", "number", "risk",
                        "风险评分低于此值判定为 Low 等级"),
                cfg("risk.high.threshold", "高风险阈值", "70", "number", "risk",
                        "风险评分高于此值判定为 High 等级"),
                cfg("risk.weight.detection", "检测指标权重（%）", "70", "number", "risk",
                        "检测指标（农残+重金属+微生物）在总分中的权重占比"),
                cfg("risk.weight.pesticide", "农残权重（%）", "35", "number", "risk",
                        "农残在检测指标内的权重占比"),
                cfg("risk.weight.heavy_metal", "重金属权重（%）", "35", "number", "risk",
                        "重金属在检测指标内的权重占比"),
                cfg("risk.weight.microbe", "微生物权重（%）", "30", "number", "risk",
                        "微生物在检测指标内的权重占比"),
                cfg("risk.weight.temp", "温度权重（%）", "60", "number", "risk",
                        "温度在物流指标内的权重占比"),
                cfg("risk.weight.humidity", "湿度权重（%）", "40", "number", "risk",
                        "湿度在物流指标内的权重占比"),
                cfg("limit.pesticide", "农残限量阈值（mg/kg）", "0.5", "number", "risk",
                        "GB 2763 农残超标判定阈值"),
                cfg("limit.heavy_metal", "重金属限量阈值（mg/kg）", "0.1", "number", "risk",
                        "GB 2762 重金属超标判定阈值"),
                cfg("limit.microbe", "微生物限量阈值（CFU/g）", "200", "number", "risk",
                        "GB 29921 微生物超标判定阈值"),
                cfg("limit.temp.min", "冷链最低温度（℃）", "0", "number", "risk",
                        "冷链适宜温度下限"),
                cfg("limit.temp.max", "冷链最高温度（℃）", "10", "number", "risk",
                        "冷链适宜温度上限"),
                cfg("limit.humidity.min", "适宜湿度下限（%）", "40", "number", "risk",
                        "物流适宜湿度下限"),
                cfg("limit.humidity.max", "适宜湿度上限（%）", "70", "number", "risk",
                        "物流适宜湿度上限"),
                cfg("anomaly.sigma.warning", "预警σ系数", "2.0", "number", "anomaly",
                        "2σ 预警阈值系数（超过 2σ 触发预警）", 0),
                cfg("anomaly.sigma.critical", "异常σ系数", "3.0", "number", "anomaly",
                        "3σ 异常阈值系数（超过 3σ 判定为异常）", 0),
                cfg("alert.score.urgent", "紧急告警分数", "0.8", "number", "alert",
                        "风险分数≥此值判定为紧急告警（0-1）"),
                cfg("alert.score.serious", "严重告警分数", "0.5", "number", "alert",
                        "风险分数≥此值判定为严重告警（0-1）"),
                cfg("alert.composite.threshold", "综合预警触发分数", "70", "number", "alert",
                        "风险评分高于此值且无具体类型预警时，触发综合预警")
        );
    }

    private ConfigParam cfg(String key, String name, String value, String type,
                            String group, String desc) {
        return cfg(key, name, value, type, group, desc, 1);
    }

    private ConfigParam cfg(String key, String name, String value, String type,
                            String group, String desc, int editable) {
        ConfigParam p = new ConfigParam();
        p.setParamKey(key);
        p.setParamName(name);
        p.setParamValue(value);
        p.setParamType(type);
        p.setParamGroup(group);
        p.setDescription(desc);
        p.setEditable(editable);
        p.setCreateTime(LocalDateTime.now());
        return p;
    }
}
