package com.jinyi.odata.dynamic;

import lombok.Data;

import java.util.List;

/**
 * 动态实体定义
 * 用于定义动态创建的OData实体结构
 */
@Data
public class EntityDefinition {
    private String entityName;
    private String tableName;
    private String description;
    private boolean autoCreate = true;
    private List<FieldDefinition> fields;
    
    /**
     * 字段定义
     */
    @Data
    public static class FieldDefinition {
        private String fieldName;
        private String columnName;
        private String dataType; // STRING, LONG, INTEGER, DECIMAL, DATETIME, BOOLEAN
        private boolean key = false;
        private boolean nullable = true;
        private int length = 255;
        private String description;
    }
}