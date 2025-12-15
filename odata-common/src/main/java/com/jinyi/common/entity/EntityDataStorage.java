package com.jinyi.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 实体数据存储表
 * 使用JSON格式存储动态实体的数据
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("entity_data_storage")
public class EntityDataStorage {

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
     * 实体编码
     */
    @TableField("entity_code")
    private String entityCode;

    /**
     * 关联的应用ID
     */
    @TableField("app_id")
    private Long appId;

    /**
     * 数据记录的业务ID（用于OData查询）
     */
    @TableField("record_id")
    private String recordId;

    /**
     * 实体数据（JSON格式存储）
     */
    @TableField("data_json")
    private String dataJson;

    /**
     * 数据版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 数据状态（ACTIVE: 激活, INACTIVE: 停用, DRAFT: 草稿）
     */
    @TableField("status")
    private String status;

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