package com.jinyi.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 应用实体关联
 * 公共实体定义，用于微服务间通信
 */
@Data
public class ApplicationEntity {
    private Long id;
    private Long applicationId;
    private String entityName;
    private String tableName;
    private String description;
    private Boolean isDynamic;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}