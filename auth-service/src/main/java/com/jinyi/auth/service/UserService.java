package com.jinyi.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinyi.auth.entity.Role;
import com.jinyi.auth.entity.User;
import com.jinyi.auth.entity.UserRole;
import com.jinyi.auth.mapper.RoleMapper;
import com.jinyi.auth.mapper.UserMapper;
import com.jinyi.auth.mapper.UserRoleMapper;
import com.jinyi.auth.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User Management Service
 */
@Service
@Slf4j
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private PasswordUtil passwordUtil;
    
    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(int page, int size) {
        Page<User> pageRequest = new Page<>(page, size);
        return userMapper.selectPage(pageRequest, new QueryWrapper<User>().eq("deleted", false));
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    /**
     * Create new user
     */
    @Transactional
    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());
        
        // Check if username already exists
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (StringUtils.hasText(user.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", user.getEmail()).eq("deleted", false);
            if (userMapper.selectOne(emailQuery) != null) {
                throw new RuntimeException("Email already exists: " + user.getEmail());
            }
        }
        
        // Encrypt password
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordUtil.encode(user.getPassword()));
        }
        
        // Set default values
        user.setStatus("ACTIVE");
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(false);
        
        userMapper.insert(user);
        
        // Assign default USER role if no roles specified
        assignDefaultRole(user.getId());
        
        log.info("User created successfully: {}", user.getUsername());
        return user;
    }
    
    /**
     * Update user
     */
    @Transactional
    public User updateUser(Long id, User userUpdate) {
        log.info("Updating user: {}", id);
        
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            throw new RuntimeException("User not found: " + id);
        }
        
        // Check username uniqueness if changed
        if (StringUtils.hasText(userUpdate.getUsername()) && 
            !userUpdate.getUsername().equals(existingUser.getUsername())) {
            if (userMapper.findByUsername(userUpdate.getUsername()) != null) {
                throw new RuntimeException("Username already exists: " + userUpdate.getUsername());
            }
            existingUser.setUsername(userUpdate.getUsername());
        }
        
        // Check email uniqueness if changed
        if (StringUtils.hasText(userUpdate.getEmail()) && 
            !userUpdate.getEmail().equals(existingUser.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", userUpdate.getEmail()).eq("deleted", false);
            if (userMapper.selectOne(emailQuery) != null) {
                throw new RuntimeException("Email already exists: " + userUpdate.getEmail());
            }
            existingUser.setEmail(userUpdate.getEmail());
        }
        
        // Update other fields
        if (StringUtils.hasText(userUpdate.getRealName())) {
            existingUser.setRealName(userUpdate.getRealName());
        }
        if (StringUtils.hasText(userUpdate.getPhone())) {
            existingUser.setPhone(userUpdate.getPhone());
        }
        if (StringUtils.hasText(userUpdate.getStatus())) {
            existingUser.setStatus(userUpdate.getStatus());
        }
        
        // Update password if provided
        if (StringUtils.hasText(userUpdate.getPassword())) {
            existingUser.setPassword(passwordUtil.encode(userUpdate.getPassword()));
            existingUser.setPasswordChangeTime(LocalDateTime.now());
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(existingUser);
        
        log.info("User updated successfully: {}", id);
        return existingUser;
    }
    
    /**
     * Delete user (soft delete)
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("User not found: " + id);
        }
        
        // Soft delete
        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // Remove user roles
        QueryWrapper<UserRole> userRoleQuery = new QueryWrapper<>();
        userRoleQuery.eq("user_id", id);
        userRoleMapper.delete(userRoleQuery);
        
        log.info("User deleted successfully: {}", id);
    }
    
    /**
     * Get user roles
     */
    public List<Role> getUserRoles(Long userId) {
        return userMapper.findRolesByUserId(userId);
    }
    
    /**
     * Assign role to user
     */
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        
        // Check if user exists
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        // Check if role exists
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("Role not found: " + roleId);
        }
        
        // Check if assignment already exists
        QueryWrapper<UserRole> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("role_id", roleId);
        if (userRoleMapper.selectOne(query) != null) {
            log.warn("User {} already has role {}", userId, roleId);
            return;
        }
        
        // Create assignment
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);
        
        log.info("Role assigned successfully: user={}, role={}", userId, roleId);
    }
    
    /**
     * Remove role from user
     */
    @Transactional
    public void removeRole(Long userId, Long roleId) {
        log.info("Removing role {} from user {}", roleId, userId);
        
        QueryWrapper<UserRole> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("role_id", roleId);
        userRoleMapper.delete(query);
        
        log.info("Role removed successfully: user={}, role={}", userId, roleId);
    }
    
    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        // Verify old password
        if (!passwordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        
        // Update password
        user.setPassword(passwordUtil.encode(newPassword));
        user.setPasswordChangeTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }
    
    /**
     * Reset user password (admin function)
     */
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("Resetting password for user: {}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        user.setPassword(passwordUtil.encode(newPassword));
        user.setPasswordChangeTime(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("Password reset successfully for user: {}", userId);
    }
    
    /**
     * Enable/Disable user account
     */
    @Transactional
    public void setUserStatus(Long userId, String status) {
        log.info("Setting user {} status to: {}", userId, status);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("User status updated successfully: user={}, status={}", userId, status);
    }
    
    /**
     * Unlock user account
     */
    @Transactional
    public void unlockAccount(Long userId) {
        log.info("Unlocking account for user: {}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found: " + userId);
        }
        
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        log.info("Account unlocked successfully for user: {}", userId);
    }
    
    /**
     * Assign default USER role to new user
     */
    private void assignDefaultRole(Long userId) {
        // Find USER role
        QueryWrapper<Role> roleQuery = new QueryWrapper<>();
        roleQuery.eq("role_code", "USER").eq("deleted", false);
        Role userRole = roleMapper.selectOne(roleQuery);
        
        if (userRole != null) {
            assignRole(userId, userRole.getId());
        } else {
            log.warn("Default USER role not found, skipping role assignment for user: {}", userId);
        }
    }
}