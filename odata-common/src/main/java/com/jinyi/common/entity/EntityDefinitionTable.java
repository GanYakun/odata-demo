package com.jinyi.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 实体定义表
 * 存储实体的基本信息和元数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("entity_definitions")
public class EntityDefinitionTable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 实体名称（用于OData服务）
     */
    @TableField("entity_name")
    private String entityName;

    /**
     * 实体编码（唯一标识）
     */
    @TableField("entity_code")
    private String entityCode;

    /**
     * 实体表名（数据库表名）
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 实体显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 实体描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联的应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 实体类型（STATIC: 静态实体, DYNAMIC: 动态实体）
     */
    @TableField("entity_type")
    private String entityType;

    /**
     * 实体状态（ACTIVE: 激活, INACTIVE: 停用, DRAFT: 草稿）
     */
    @TableField("status")
    private String status;

    /**
     * 是否自动创建表
     */
    @TableField("auto_create_table")
    private Boolean autoCreateTable;

    /**
     * 表是否已创建
     */
    @TableField("table_created")
    private Boolean tableCreated;

    /**
     * 实体版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("properties")
    private String properties;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}