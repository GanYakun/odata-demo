package com.jinyi.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实体定义DTO
 */
@Data
public class EntityDefinitionDto {

    /**
     * 实体ID
     */
    private Long id;

    /**
     * 实体名称
     */
    private String entityName;

    /**
     * 实体编码
     */
    private String entityCode;

    /**
     * 实体表名
     */
    private String tableName;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 描述
     */
    private String description;

    /**
     * 关联的应用ID
     */
    private Long appId;

    /**
     * 实体类型
     */
    private String entityType;

    /**
     * 状态
     */
    private String status;

    /**
     * 是否自动创建表
     */
    private Boolean autoCreateTable;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 扩展属性
     */
    private String properties;

    /**
     * 字段定义列表
     */
    private List<EntityFieldDto> fields;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}