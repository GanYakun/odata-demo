package com.jinyi.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * User Role Association Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "user_roles")
@TableName("user_roles")
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @Column(name = "deleted")
    private Boolean deleted = false;
}