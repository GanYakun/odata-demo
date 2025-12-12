package com.jinyi.odatademo.dto;

import lombok.Data;

import java.util.List;

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
        private String dataType; // STRING, LONG, INTEGER, DECIMAL, DATETIME, BOOLEAN
        private boolean key = false;
        private boolean nullable = true;
        private int length = 255;
        private String description;
    }
}