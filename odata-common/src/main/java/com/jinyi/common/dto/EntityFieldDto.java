package com.jinyi.common.dto;

import lombok.Data;

/**
 * 实体字段DTO
 */
@Data
public class EntityFieldDto {

    /**
     * 字段ID
     */
    private Long id;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段编码
     */
    private String fieldCode;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 描述
     */
    private String description;

    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 字段长度
     */
    private Integer fieldLength;

    /**
     * 小数位数
     */
    private Integer decimalPlaces;

    /**
     * 是否为主键
     */
    private Boolean isPrimaryKey;

    /**
     * 是否不为空
     */
    private Boolean isNotNull;

    /**
     * 是否唯一
     */
    private Boolean isUnique;

    /**
     * 是否有索引
     */
    private Boolean isIndexed;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    private String status;

    /**
     * 验证规则
     */
    private String validationRules;

    /**
     * 扩展属性
     */
    private String properties;
}