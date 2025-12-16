package com.jinyi.auth.service;

import com.jinyi.auth.entity.Permission;
import com.jinyi.auth.entity.Role;
import com.jinyi.auth.entity.User;
import com.jinyi.auth.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring Security User Details Service Implementation
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userMapper.findByUsername(username);
        if (user == null) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        // Check account status
        if (!"ACTIVE".equals(user.getStatus())) {
            log.warn("User account is not active: {}", username);
            throw new UsernameNotFoundException("User account is not active: " + username);
        }
        
        // Check if account is locked
        if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("User account is locked: {}", username);
            throw new UsernameNotFoundException("User account is locked: " + username);
        }
        
        // Get user roles and permissions
        List<Role> roles = userMapper.findRolesByUserId(user.getId());
        List<Permission> permissions = userMapper.findPermissionsByUserId(user.getId());
        
        user.setRoles(new HashSet<>(roles));
        user.setPermissions(new HashSet<>(permissions));
        
        // Build authorities list
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role authorities (with ROLE_ prefix)
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
        }
        
        // Add specific permissions
        for (Permission permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
        }
        
        log.debug("User {} loaded with {} authorities", username, authorities.size());
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now()))
                .credentialsExpired(false)
                .disabled(!"ACTIVE".equals(user.getStatus()))
                .build();
    }
}