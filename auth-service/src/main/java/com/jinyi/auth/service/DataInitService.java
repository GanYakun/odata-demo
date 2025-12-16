package com.jinyi.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jinyi.auth.entity.*;
import com.jinyi.auth.mapper.*;
import com.jinyi.auth.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Data Initialization Service
 * Initialize default roles, permissions, and users
 */
@Service
@Slf4j
public class DataInitService /* implements CommandLineRunner */ {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    // @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            log.info("Starting data initialization...");
            
            // Wait a bit to ensure tables are created
            Thread.sleep(2000);
            
            initPermissions();
            initRoles();
            initUsers();
            assignRolePermissions();
            assignUserRoles();
            
            log.info("Data initialization completed successfully!");
        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage());
            // Don't fail the application startup if data initialization fails
            log.warn("Application will continue without initial data");
        }
    }
    
    /**
     * Initialize permissions
     */
    private void initPermissions() {
        try {
            log.info("Initializing permissions...");
            
            List<PermissionData> permissions = Arrays.asList(
            // User management permissions
            new PermissionData("user:create", "创建用户", "创建新用户的权限", "user", "create"),
            new PermissionData("user:read", "查看用户", "查看用户信息的权限", "user", "read"),
            new PermissionData("user:update", "更新用户", "更新用户信息的权限", "user", "update"),
            new PermissionData("user:delete", "删除用户", "删除用户的权限", "user", "delete"),
            
            // Role management permissions
            new PermissionData("role:create", "创建角色", "创建新角色的权限", "role", "create"),
            new PermissionData("role:read", "查看角色", "查看角色信息的权限", "role", "read"),
            new PermissionData("role:update", "更新角色", "更新角色信息的权限", "role", "update"),
            new PermissionData("role:delete", "删除角色", "删除角色的权限", "role", "delete"),
            
            // Permission management permissions
            new PermissionData("permission:create", "创建权限", "创建新权限的权限", "permission", "create"),
            new PermissionData("permission:read", "查看权限", "查看权限信息的权限", "permission", "read"),
            new PermissionData("permission:update", "更新权限", "更新权限信息的权限", "permission", "update"),
            new PermissionData("permission:delete", "删除权限", "删除权限的权限", "permission", "delete"),
            
            // Application management permissions
            new PermissionData("application:create", "创建应用", "创建新应用的权限", "application", "create"),
            new PermissionData("application:read", "查看应用", "查看应用信息的权限", "application", "read"),
            new PermissionData("application:update", "更新应用", "更新应用信息的权限", "application", "update"),
            new PermissionData("application:delete", "删除应用", "删除应用的权限", "application", "delete"),
            
            // Entity management permissions
            new PermissionData("entity:create", "创建实体", "创建新实体的权限", "entity", "create"),
            new PermissionData("entity:read", "查看实体", "查看实体信息的权限", "entity", "read"),
            new PermissionData("entity:update", "更新实体", "更新实体信息的权限", "entity", "update"),
            new PermissionData("entity:delete", "删除实体", "删除实体的权限", "entity", "delete"),
            
            // OData query permissions
            new PermissionData("odata:query", "OData查询", "执行OData查询的权限", "odata", "query"),
            new PermissionData("odata:create", "OData创建", "通过OData创建数据的权限", "odata", "create"),
            new PermissionData("odata:update", "OData更新", "通过OData更新数据的权限", "odata", "update"),
            new PermissionData("odata:delete", "OData删除", "通过OData删除数据的权限", "odata", "delete"),
            
            // System management permissions
            new PermissionData("system:config", "系统配置", "系统配置管理权限", "system", "config"),
            new PermissionData("system:monitor", "系统监控", "系统监控权限", "system", "monitor"),
            new PermissionData("system:log", "系统日志", "查看系统日志权限", "system", "log")
        );
        
        for (PermissionData permData : permissions) {
            QueryWrapper<Permission> query = new QueryWrapper<>();
            query.eq("permission_code", permData.code);
            
            if (permissionMapper.selectOne(query) == null) {
                Permission permission = new Permission();
                permission.setPermissionCode(permData.code);
                permission.setPermissionName(permData.name);
                permission.setDescription(permData.description);
                permission.setResource(permData.resource);
                permission.setAction(permData.action);
                permission.setType("API");
                permission.setStatus("ACTIVE");
                permission.setIsSystem(true);
                permission.setCreatedAt(LocalDateTime.now());
                permission.setUpdatedAt(LocalDateTime.now());
                permission.setDeleted(false);
                
                permissionMapper.insert(permission);
                log.info("Created permission: {}", permData.code);
            }
        }
        } catch (Exception e) {
            log.error("Failed to initialize permissions: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Initialize roles
     */
    private void initRoles() {
        log.info("Initializing roles...");
        
        List<RoleData> roles = Arrays.asList(
            new RoleData("SUPER_ADMIN", "超级管理员", "拥有所有权限的超级管理员"),
            new RoleData("ADMIN", "管理员", "拥有大部分管理权限的管理员"),
            new RoleData("USER_MANAGER", "用户管理员", "负责用户管理的管理员"),
            new RoleData("APP_MANAGER", "应用管理员", "负责应用管理的管理员"),
            new RoleData("DEVELOPER", "开发者", "开发人员角色"),
            new RoleData("USER", "普通用户", "普通用户角色")
        );
        
        for (RoleData roleData : roles) {
            QueryWrapper<Role> query = new QueryWrapper<>();
            query.eq("role_code", roleData.code);
            
            if (roleMapper.selectOne(query) == null) {
                Role role = new Role();
                role.setRoleCode(roleData.code);
                role.setRoleName(roleData.name);
                role.setDescription(roleData.description);
                role.setStatus("ACTIVE");
                role.setIsSystem(true);
                role.setCreatedAt(LocalDateTime.now());
                role.setUpdatedAt(LocalDateTime.now());
                role.setDeleted(false);
                
                roleMapper.insert(role);
                log.info("Created role: {}", roleData.code);
            }
        }
    }
    
    /**
     * Initialize users
     */
    private void initUsers() {
        log.info("Initializing users...");
        
        List<UserData> users = Arrays.asList(
            new UserData("admin", "admin123", "admin@jinyi.com", "系统管理员", "13800000001"),
            new UserData("test", "test123", "test@jinyi.com", "测试用户", "13800000002")
        );
        
        for (UserData userData : users) {
            if (userMapper.findByUsername(userData.username) == null) {
                User user = new User();
                user.setUsername(userData.username);
                user.setPassword(passwordUtil.encode(userData.password));
                user.setEmail(userData.email);
                user.setRealName(userData.realName);
                user.setPhone(userData.phone);
                user.setStatus("ACTIVE");
                user.setFailedLoginAttempts(0);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setDeleted(false);
                
                userMapper.insert(user);
                log.info("Created user: {}", userData.username);
            }
        }
    }
    
    /**
     * Assign permissions to roles
     */
    private void assignRolePermissions() {
        log.info("Assigning permissions to roles...");
        
        // Super Admin - all permissions
        assignAllPermissionsToRole("SUPER_ADMIN");
        
        // Admin - most permissions except system level
        assignPermissionsToRole("ADMIN", Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete",
            "role:read", "permission:read",
            "application:create", "application:read", "application:update", "application:delete",
            "entity:create", "entity:read", "entity:update", "entity:delete",
            "odata:query", "odata:create", "odata:update", "odata:delete"
        ));
        
        // User Manager - user management permissions
        assignPermissionsToRole("USER_MANAGER", Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete",
            "role:read", "permission:read"
        ));
        
        // App Manager - application and entity management
        assignPermissionsToRole("APP_MANAGER", Arrays.asList(
            "application:create", "application:read", "application:update", "application:delete",
            "entity:create", "entity:read", "entity:update", "entity:delete",
            "odata:query", "odata:create", "odata:update", "odata:delete"
        ));
        
        // Developer - development related permissions
        assignPermissionsToRole("DEVELOPER", Arrays.asList(
            "application:read", "entity:read", "entity:create", "entity:update",
            "odata:query", "odata:create", "odata:update", "odata:delete"
        ));
        
        // User - basic read permissions
        assignPermissionsToRole("USER", Arrays.asList(
            "application:read", "entity:read", "odata:query"
        ));
    }
    
    /**
     * Assign users to roles
     */
    private void assignUserRoles() {
        log.info("Assigning roles to users...");
        
        assignRoleToUser("admin", "SUPER_ADMIN");
        assignRoleToUser("test", "USER");
    }
    
    /**
     * Assign all permissions to a role
     */
    private void assignAllPermissionsToRole(String roleCode) {
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("role_code", roleCode));
        if (role == null) return;
        
        List<Permission> allPermissions = permissionMapper.selectList(new QueryWrapper<Permission>().eq("deleted", false));
        
        for (Permission permission : allPermissions) {
            assignPermissionToRole(role.getId(), permission.getId());
        }
    }
    
    /**
     * Assign specific permissions to a role
     */
    private void assignPermissionsToRole(String roleCode, List<String> permissionCodes) {
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("role_code", roleCode));
        if (role == null) return;
        
        for (String permissionCode : permissionCodes) {
            Permission permission = permissionMapper.selectOne(
                new QueryWrapper<Permission>().eq("permission_code", permissionCode));
            if (permission != null) {
                assignPermissionToRole(role.getId(), permission.getId());
            }
        }
    }
    
    /**
     * Assign permission to role
     */
    private void assignPermissionToRole(Long roleId, Long permissionId) {
        QueryWrapper<RolePermission> query = new QueryWrapper<>();
        query.eq("role_id", roleId).eq("permission_id", permissionId);
        
        if (rolePermissionMapper.selectOne(query) == null) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedAt(LocalDateTime.now());
            
            rolePermissionMapper.insert(rolePermission);
        }
    }
    
    /**
     * Assign role to user
     */
    private void assignRoleToUser(String username, String roleCode) {
        User user = userMapper.findByUsername(username);
        Role role = roleMapper.selectOne(new QueryWrapper<Role>().eq("role_code", roleCode));
        
        if (user != null && role != null) {
            QueryWrapper<UserRole> query = new QueryWrapper<>();
            query.eq("user_id", user.getId()).eq("role_id", role.getId());
            
            if (userRoleMapper.selectOne(query) == null) {
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(role.getId());
                userRole.setCreatedAt(LocalDateTime.now());
                
                userRoleMapper.insert(userRole);
                log.info("Assigned role {} to user {}", roleCode, username);
            }
        }
    }
    
    /**
     * Permission data holder
     */
    private static class PermissionData {
        String code, name, description, resource, action;
        
        PermissionData(String code, String name, String description, String resource, String action) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.resource = resource;
            this.action = action;
        }
    }
    
    /**
     * Role data holder
     */
    private static class RoleData {
        String code, name, description;
        
        RoleData(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }
    }
    
    /**
     * User data holder
     */
    private static class UserData {
        String username, password, email, realName, phone;
        
        UserData(String username, String password, String email, String realName, String phone) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.realName = realName;
            this.phone = phone;
        }
    }
}