package com.jinyi.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 实体数据DTO
 */
@Data
public class EntityDataDto {

    /**
     * 数据ID
     */
    private Long id;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 实体编码
     */
    private String entityCode;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 数据内容
     */
    private Map<String, Object> data;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态
     */
    private String status;

    /**
     * 扩展属性
     */
    private String properties;

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