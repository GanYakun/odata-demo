package com.jinyi.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 应用实体关联 - MyBatis Plus实体
 */
@Data
@TableName("application_entities")
public class ApplicationEntityRelation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("application_id")
    private Long applicationId;
    
    @TableField("entity_name")
    private String entityName;
    
    @TableField("table_name")
    private String tableName;
    
    @TableField("description")
    private String description;
    
    @TableField("is_dynamic")
    private Boolean isDynamic;
    
    @TableField("active")
    private Boolean active;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}