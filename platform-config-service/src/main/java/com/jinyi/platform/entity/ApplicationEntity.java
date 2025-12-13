package com.jinyi.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 应用实体 - MyBatis Plus实体
 */
@Data
@TableName("applications")
public class ApplicationEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("app_code")
    private String appCode;
    
    @TableField("app_name")
    private String appName;
    
    @TableField("description")
    private String description;
    
    @TableField("version")
    private String version;
    
    @TableField("active")
    private Boolean active;
    
    @TableField("base_url")
    private String baseUrl;
    
    @TableField("owner")
    private String owner;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}