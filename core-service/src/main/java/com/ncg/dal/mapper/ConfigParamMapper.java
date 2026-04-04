package com.ncg.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncg.model.ConfigParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置参数 Mapper 接口
 */
@Mapper
public interface ConfigParamMapper extends BaseMapper<ConfigParam> {
}
