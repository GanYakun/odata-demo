package com.jinyi.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Role Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "roles")
@TableName("roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String roleCode;
    
    @Column(nullable = false, length = 100)
    private String roleName;
    
    @Column(length = 200)
    private String description;
    
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
    
    @Column(name = "is_system")
    private Boolean isSystem = false; // Whether it is a system role
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    // Role permissions (JPA association, not stored in database)
    @Transient
    @TableField(exist = false)
    private Set<Permission> permissions;
}