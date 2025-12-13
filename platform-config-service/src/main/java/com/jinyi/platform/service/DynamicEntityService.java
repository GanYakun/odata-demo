package com.jinyi.platform.service;

import com.jinyi.common.dto.EntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态实体管理服务
 */
@Service
@Slf4j
public class DynamicEntityService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 存储动态注册的实体定义
    private final Map<String, EntityDefinition> dynamicEntities = new ConcurrentHashMap<>();
    
    // 存储动态实体的字段映射
    private final Map<String, Map<String, EntityDefinition.FieldDefinition>> entityFieldMappings = new ConcurrentHashMap<>();

    /**
     * 动态注册实体
     */
    public String registerEntity(EntityDefinition entityDef, Long applicationId) {
        try {
            validateEntityDefinition(entityDef);
            
            String entityName = entityDef.getEntityName();
            String tableName = entityDef.getTableName();
            
            // 检查实体是否已存在
            if (dynamicEntities.containsKey(entityName)) {
                throw new RuntimeException("Entity already exists: " + entityName);
            }
            
            // 创建数据库表
            if (entityDef.isAutoCreate()) {
                createDatabaseTable(entityDef);
            }
            
            // 注册实体到内存
            dynamicEntities.put(entityName, entityDef);
            
            // 创建字段映射
            Map<String, EntityDefinition.FieldDefinition> fieldMap = new HashMap<>();
            for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
                fieldMap.put(field.getFieldName(), field);
            }
            entityFieldMappings.put(entityName, fieldMap);
            
            String message = "Entity registered successfully: " + entityName;
            log.info("Successfully registered dynamic entity: {} -> table: {}", entityName, tableName);
            return message;
            
        } catch (Exception e) {
            log.error("Failed to register entity: {}", entityDef.getEntityName(), e);
            throw new RuntimeException("Failed to register entity: " + e.getMessage());
        }
    }

    /**
     * 获取动态实体定义
     */
    public EntityDefinition getEntityDefinition(String entityName) {
        return dynamicEntities.get(entityName);
    }

    /**
     * 获取实体字段定义
     */
    public EntityDefinition.FieldDefinition getFieldDefinition(String entityName, String fieldName) {
        Map<String, EntityDefinition.FieldDefinition> fieldMap = entityFieldMappings.get(entityName);
        return fieldMap != null ? fieldMap.get(fieldName) : null;
    }

    /**
     * 获取所有动态注册的实体
     */
    public Map<String, EntityDefinition> getAllDynamicEntities() {
        return Collections.unmodifiableMap(dynamicEntities);
    }

    /**
     * 删除动态实体
     */
    public String unregisterEntity(String entityName, boolean dropTable) {
        try {
            EntityDefinition entityDef = dynamicEntities.get(entityName);
            if (entityDef == null) {
                throw new RuntimeException("Entity not found: " + entityName);
            }
            
            // 删除数据库表（如果需要）
            if (dropTable) {
                String sql = "DROP TABLE IF EXISTS " + entityDef.getTableName();
                jdbcTemplate.execute(sql);
                log.info("Dropped table: {}", entityDef.getTableName());
            }
            
            // 从内存中移除
            dynamicEntities.remove(entityName);
            entityFieldMappings.remove(entityName);
            
            String message = "Entity unregistered successfully: " + entityName;
            if (dropTable) {
                message += " (table dropped)";
            }
            
            log.info("Successfully unregistered dynamic entity: {}", entityName);
            return message;
            
        } catch (Exception e) {
            log.error("Failed to unregister entity: {}", entityName, e);
            throw new RuntimeException("Failed to unregister entity: " + e.getMessage());
        }
    }

    /**
     * 验证实体定义
     */
    private void validateEntityDefinition(EntityDefinition entityDef) {
        if (entityDef.getEntityName() == null || entityDef.getEntityName().trim().isEmpty()) {
            throw new RuntimeException("Entity name is required");
        }
        
        if (entityDef.getTableName() == null || entityDef.getTableName().trim().isEmpty()) {
            throw new RuntimeException("Table name is required");
        }
        
        if (entityDef.getFields() == null || entityDef.getFields().isEmpty()) {
            throw new RuntimeException("At least one field is required");
        }
        
        // 检查是否有主键字段
        boolean hasKey = entityDef.getFields().stream().anyMatch(EntityDefinition.FieldDefinition::isKey);
        if (!hasKey) {
            throw new RuntimeException("At least one key field is required");
        }
        
        // 验证字段名唯一性
        Set<String> fieldNames = new HashSet<>();
        for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
            if (field.getFieldName() == null || field.getFieldName().trim().isEmpty()) {
                throw new RuntimeException("Field name is required");
            }
            
            if (!fieldNames.add(field.getFieldName())) {
                throw new RuntimeException("Duplicate field name: " + field.getFieldName());
            }
            
            if (field.getDataType() == null || field.getDataType().trim().isEmpty()) {
                throw new RuntimeException("Data type is required for field: " + field.getFieldName());
            }
        }
    }

    /**
     * 创建数据库表
     */
    private void createDatabaseTable(EntityDefinition entityDef) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(entityDef.getTableName()).append(" (");
        
        List<String> columns = new ArrayList<>();
        List<String> keyColumns = new ArrayList<>();
        
        for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
            String columnName = field.getColumnName() != null && !field.getColumnName().trim().isEmpty() 
                ? field.getColumnName() : camelToSnake(field.getFieldName());
            
            StringBuilder columnDef = new StringBuilder();
            columnDef.append(columnName).append(" ").append(getSqlType(field));
            
            if (!field.isNullable()) {
                columnDef.append(" NOT NULL");
            }
            
            if (field.isKey()) {
                keyColumns.add(columnName);
                if ("LONG".equals(field.getDataType()) || "INTEGER".equals(field.getDataType())) {
                    columnDef.append(" AUTO_INCREMENT");
                }
            }
            
            columns.add(columnDef.toString());
        }
        
        sql.append(String.join(", ", columns));
        
        if (!keyColumns.isEmpty()) {
            sql.append(", PRIMARY KEY (").append(String.join(", ", keyColumns)).append(")");
        }
        
        sql.append(")");
        
        jdbcTemplate.execute(sql.toString());
        log.info("Created table: {} with SQL: {}", entityDef.getTableName(), sql.toString());
    }

    /**
     * 获取SQL数据类型
     */
    private String getSqlType(EntityDefinition.FieldDefinition field) {
        switch (field.getDataType().toUpperCase()) {
            case "STRING":
                return "VARCHAR(" + field.getLength() + ")";
            case "LONG":
                return "BIGINT";
            case "INTEGER":
                return "INT";
            case "DECIMAL":
                return "DECIMAL(19,2)";
            case "DATETIME":
                return "DATETIME";
            case "BOOLEAN":
                return "BOOLEAN";
            default:
                return "VARCHAR(255)";
        }
    }

    /**
     * 驼峰转下划线
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 检查动态实体是否存在
     */
    public boolean isDynamicEntity(String entityName) {
        return dynamicEntities.containsKey(entityName);
    }
}