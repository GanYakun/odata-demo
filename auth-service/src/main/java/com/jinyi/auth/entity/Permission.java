package com.jinyi.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Permission Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "permissions")
@TableName("permissions")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String permissionCode;
    
    @Column(nullable = false, length = 100)
    private String permissionName;
    
    @Column(length = 200)
    private String description;
    
    @Column(length = 50)
    private String resource; // Resource identifier, e.g.: user, role, application
    
    @Column(length = 50)
    private String action; // Action identifier, e.g.: create, read, update, delete
    
    @Column(length = 20)
    private String type = "API"; // API, MENU, BUTTON
    
    @Column(length = 20)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE
    
    @Column(name = "is_system")
    private Boolean isSystem = false; // Whether it is a system permission
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    @Column(name = "deleted")
    private Boolean deleted = false;
}