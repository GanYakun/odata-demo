package com.jinyi.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.auth.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Permission Data Access Layer
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * Find permission by permission code
     */
    @Select("SELECT * FROM permissions WHERE permission_code = #{permissionCode} AND deleted = 0")
    Permission findByPermissionCode(@Param("permissionCode") String permissionCode);
}