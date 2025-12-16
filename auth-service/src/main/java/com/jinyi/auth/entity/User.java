package com.jinyi.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * User Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "users")
@TableName("users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "real_name", length = 50)
    private String realName;
    
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, LOCKED
    
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;
    
    @Column(name = "password_change_time")
    private LocalDateTime passwordChangeTime;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    // User roles (JPA association, not stored in database)
    @Transient
    @TableField(exist = false)
    private Set<Role> roles;
    
    // User permissions (JPA association, not stored in database)
    @Transient
    @TableField(exist = false)
    private Set<Permission> permissions;
}