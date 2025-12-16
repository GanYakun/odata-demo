package com.jinyi.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinyi.auth.entity.Role;
import com.jinyi.auth.entity.User;
import com.jinyi.auth.service.UserService;
import com.jinyi.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * User Management Controller
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting all users: page={}, size={}", page, size);
            
            Page<User> users = userService.getAllUsers(page, size);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
        } catch (Exception e) {
            log.error("Get all users failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            log.info("Getting user by ID: {}", id);
            
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("用户不存在"));
            }
            
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
        } catch (Exception e) {
            log.error("Get user by ID failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * Create new user
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        try {
            log.info("Creating new user: {}", user.getUsername());
            
            User createdUser = userService.createUser(user);
            
            return ResponseEntity.ok(ApiResponse.success("创建用户成功", createdUser));
        } catch (Exception e) {
            log.error("Create user failed: {}", user.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        try {
            log.info("Updating user: {}", id);
            
            User updatedUser = userService.updateUser(id, user);
            
            return ResponseEntity.ok(ApiResponse.success("更新用户成功", updatedUser));
        } catch (Exception e) {
            log.error("Update user failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            log.info("Deleting user: {}", id);
            
            userService.deleteUser(id);
            
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
        } catch (Exception e) {
            log.error("Delete user failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除用户失败: " + e.getMessage()));
        }
    }
    
    /**
     * Get user roles
     */
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<ApiResponse<List<Role>>> getUserRoles(@PathVariable Long id) {
        try {
            log.info("Getting roles for user: {}", id);
            
            List<Role> roles = userService.getUserRoles(id);
            
            return ResponseEntity.ok(ApiResponse.success("获取用户角色成功", roles));
        } catch (Exception e) {
            log.error("Get user roles failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取用户角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * Assign role to user
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            log.info("Assigning role {} to user {}", roleId, userId);
            
            userService.assignRole(userId, roleId);
            
            return ResponseEntity.ok(ApiResponse.success("分配角色成功", null));
        } catch (Exception e) {
            log.error("Assign role failed: user={}, role={}", userId, roleId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("分配角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * Remove role from user
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        try {
            log.info("Removing role {} from user {}", roleId, userId);
            
            userService.removeRole(userId, roleId);
            
            return ResponseEntity.ok(ApiResponse.success("移除角色成功", null));
        } catch (Exception e) {
            log.error("Remove role failed: user={}, role={}", userId, roleId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("移除角色失败: " + e.getMessage()));
        }
    }
    
    /**
     * Change user password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('user:update') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        try {
            log.info("Changing password for user: {}", id);
            
            userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
            
            return ResponseEntity.ok(ApiResponse.success("修改密码成功", null));
        } catch (Exception e) {
            log.error("Change password failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("修改密码失败: " + e.getMessage()));
        }
    }
    
    /**
     * Reset user password (admin function)
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody ResetPasswordRequest request) {
        try {
            log.info("Resetting password for user: {}", id);
            
            userService.resetPassword(id, request.getNewPassword());
            
            return ResponseEntity.ok(ApiResponse.success("重置密码成功", null));
        } catch (Exception e) {
            log.error("Reset password failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("重置密码失败: " + e.getMessage()));
        }
    }
    
    /**
     * Set user status
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> setUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusRequest request) {
        try {
            log.info("Setting user {} status to: {}", id, request.getStatus());
            
            userService.setUserStatus(id, request.getStatus());
            
            return ResponseEntity.ok(ApiResponse.success("设置用户状态成功", null));
        } catch (Exception e) {
            log.error("Set user status failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("设置用户状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * Unlock user account
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long id) {
        try {
            log.info("Unlocking account for user: {}", id);
            
            userService.unlockAccount(id);
            
            return ResponseEntity.ok(ApiResponse.success("解锁账户成功", null));
        } catch (Exception e) {
            log.error("Unlock account failed: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("解锁账户失败: " + e.getMessage()));
        }
    }
    
    /**
     * Change Password Request DTO
     */
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
        
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * Reset Password Request DTO
     */
    public static class ResetPasswordRequest {
        private String newPassword;
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * User Status Request DTO
     */
    public static class UserStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}