package com.ncg.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncg.model.AlertRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预警记录 Mapper 接口
 */
@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {
}
