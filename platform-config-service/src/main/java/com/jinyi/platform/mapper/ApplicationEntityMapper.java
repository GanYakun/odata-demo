package com.jinyi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.platform.entity.ApplicationEntityRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用实体关联数据访问层
 */
@Mapper
public interface ApplicationEntityMapper extends BaseMapper<ApplicationEntityRelation> {
}