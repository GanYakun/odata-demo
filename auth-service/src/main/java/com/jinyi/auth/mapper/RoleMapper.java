package com.jinyi.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Role Data Access Layer
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * Find role by role code
     */
    @Select("SELECT * FROM roles WHERE role_code = #{roleCode} AND deleted = 0")
    Role findByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * Get permissions by role ID
     */
    @Select("SELECT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND rp.deleted = 0")
    List<com.jinyi.auth.entity.Permission> findPermissionsByRoleId(@Param("roleId") Long roleId);
}