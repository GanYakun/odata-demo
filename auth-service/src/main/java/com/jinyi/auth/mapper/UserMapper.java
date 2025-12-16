package com.jinyi.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * User Data Access Layer
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * Find user by username
     */
    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = false")
    User findByUsername(@Param("username") String username);
    
    /**
     * Find user by email
     */
    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = false")
    User findByEmail(@Param("email") String email);
    
    /**
     * Get user roles by user ID
     */
    @Select("SELECT r.* FROM roles r " +
            "INNER JOIN user_roles ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = false")
    List<com.jinyi.auth.entity.Role> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * Get user permissions by user ID
     */
    @Select("SELECT DISTINCT p.* FROM permissions p " +
            "INNER JOIN role_permissions rp ON p.id = rp.permission_id " +
            "INNER JOIN user_roles ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = false")
    List<com.jinyi.auth.entity.Permission> findPermissionsByUserId(@Param("userId") Long userId);
}