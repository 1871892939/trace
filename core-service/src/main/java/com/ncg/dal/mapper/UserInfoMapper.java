package com.ncg.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ncg.model.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息 Mapper 接口
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
