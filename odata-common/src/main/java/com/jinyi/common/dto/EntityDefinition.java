package com.jinyi.common.dto;

import lombok.Data;
import java.util.List;

/**
 * 动态实体定义DTO
 * 用于微服务间传输实体定义信息
 */
@Data
public class EntityDefinition {
    private String entityName;
    private String tableName;
    private String description;
    private boolean autoCreate = true;
    private List<FieldDefinition> fields;

    @Data
    public static class FieldDefinition {
        private String fieldName;
        private String columnName;
        private String dataType;
        private boolean key = false;
        private boolean nullable = true;
        private int length = 255;
        private String description;
    }
}