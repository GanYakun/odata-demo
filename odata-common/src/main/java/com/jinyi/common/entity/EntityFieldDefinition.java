package com.jinyi.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 实体字段定义表
 * 存储实体字段的详细信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("entity_field_definitions")
public class EntityFieldDefinition {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联的实体ID
     */
    @TableField("entity_id")
    private Long entityId;

    /**
     * 字段名称
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 字段编码
     */
    @TableField("field_code")
    private String fieldCode;

    /**
     * 字段显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 字段描述
     */
    @TableField("description")
    private String description;

    /**
     * 字段类型（STRING, INTEGER, LONG, DECIMAL, BOOLEAN, DATETIME, TEXT, JSON）
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 数据库字段类型（VARCHAR, INT, BIGINT, DECIMAL, BOOLEAN, DATETIME, TEXT, JSON）
     */
    @TableField("db_type")
    private String dbType;

    /**
     * 字段长度
     */
    @TableField("field_length")
    private Integer fieldLength;

    /**
     * 小数位数（用于DECIMAL类型）
     */
    @TableField("decimal_places")
    private Integer decimalPlaces;

    /**
     * 是否为主键
     */
    @TableField("is_primary_key")
    private Boolean isPrimaryKey;

    /**
     * 是否不为空
     */
    @TableField("is_not_null")
    private Boolean isNotNull;

    /**
     * 是否唯一
     */
    @TableField("is_unique")
    private Boolean isUnique;

    /**
     * 是否有索引
     */
    @TableField("is_indexed")
    private Boolean isIndexed;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 字段排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 字段状态（ACTIVE: 激活, INACTIVE: 停用）
     */
    @TableField("status")
    private String status;

    /**
     * 验证规则（JSON格式）
     */
    @TableField("validation_rules")
    private String validationRules;

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