package com.ncg.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncg.model.BatchInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食品批次信息 Mapper 接口
 */
@Mapper
public interface BatchInfoMapper extends BaseMapper<BatchInfo> {
}
