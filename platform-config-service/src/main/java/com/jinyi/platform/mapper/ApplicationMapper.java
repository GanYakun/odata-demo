package com.jinyi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.platform.entity.ApplicationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用数据访问层
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<ApplicationEntity> {
}