package com.jinyi.auth.controller;

import com.jinyi.auth.entity.Permission;
import com.jinyi.auth.entity.Role;
import com.jinyi.auth.entity.User;
import com.jinyi.auth.mapper.PermissionMapper;
import com.jinyi.auth.mapper.RoleMapper;
import com.jinyi.auth.mapper.UserMapper;
import com.jinyi.auth.util.PasswordUtil;
import com.jinyi.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Test Controller for manual data initialization
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    /**
     * Check if user exists
     */
    @GetMapping("/check-user/{username}")
    public ApiResponse<String> checkUser(@PathVariable String username) {
        try {
            User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                    .eq("username", username));
            
            if (user != null) {
                return ApiResponse.success("用户存在", "User exists: " + user.getUsername());
            } else {
                return ApiResponse.success("用户不存在", "User not found");
            }
        } catch (Exception e) {
            log.error("Failed to check user", e);
            return ApiResponse.error("检查用户失败: " + e.getMessage());
        }
    }
    
    /**
     * Initialize test data
     */
    @PostMapping("/init-data")
    public ApiResponse<String> initTestData() {
        try {
            log.info("Initializing test data...");
            
            // Create a test user
            User testUser = new User();
            testUser.setUsername("admin");
            testUser.setPassword(passwordUtil.encode("admin123"));
            testUser.setEmail("admin@jinyi.com");
            testUser.setRealName("系统管理员");
            testUser.setStatus("ACTIVE");
            testUser.setFailedLoginAttempts(0);
            testUser.setCreatedAt(LocalDateTime.now());
            testUser.setUpdatedAt(LocalDateTime.now());
            testUser.setDeleted(false);
            
            userMapper.insert(testUser);
            log.info("Created test user: admin");
            
            // Create a test role
            Role testRole = new Role();
            testRole.setRoleCode("ADMIN");
            testRole.setRoleName("管理员");
            testRole.setDescription("系统管理员角色");
            testRole.setStatus("ACTIVE");
            testRole.setIsSystem(true);
            testRole.setCreatedAt(LocalDateTime.now());
            testRole.setUpdatedAt(LocalDateTime.now());
            testRole.setDeleted(false);
            
            roleMapper.insert(testRole);
            log.info("Created test role: ADMIN");
            
            // Create a test permission
            Permission testPermission = new Permission();
            testPermission.setPermissionCode("user:read");
            testPermission.setPermissionName("查看用户");
            testPermission.setDescription("查看用户信息的权限");
            testPermission.setResource("user");
            testPermission.setAction("read");
            testPermission.setType("API");
            testPermission.setStatus("ACTIVE");
            testPermission.setIsSystem(true);
            testPermission.setCreatedAt(LocalDateTime.now());
            testPermission.setUpdatedAt(LocalDateTime.now());
            testPermission.setDeleted(false);
            
            permissionMapper.insert(testPermission);
            log.info("Created test permission: user:read");
            
            return ApiResponse.success("测试数据初始化成功", "Test data initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize test data", e);
            return ApiResponse.error("测试数据初始化失败: " + e.getMessage());
        }
    }
}