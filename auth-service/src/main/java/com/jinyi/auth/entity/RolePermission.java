package com.jinyi.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Role Permission Association Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "role_permissions")
@TableName("role_permissions")
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @Column(name = "deleted")
    private Boolean deleted = false;
}