package com.jinyi.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.auth.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * Role Permission Association Data Access Layer
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}