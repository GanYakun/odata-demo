package com.jinyi.platform.service;

import com.jinyi.common.entity.EntityFieldDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据库表管理服务
 */
@Service
@Slf4j
public class DatabaseTableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建动态表
     */
    public void createDynamicTable(String tableName, List<EntityFieldDefinition> fields) {
        log.info("Creating dynamic table: {}", tableName);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        // 添加系统字段
        sql.append("id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID', ");
        sql.append("record_id VARCHAR(32) NOT NULL UNIQUE COMMENT '记录ID', ");
        sql.append("created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', ");
        sql.append("updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', ");
        sql.append("version INT DEFAULT 1 COMMENT '版本号', ");
        sql.append("status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态', ");

        // 添加业务字段
        List<String> fieldDefinitions = fields.stream()
                .filter(field -> !"id".equalsIgnoreCase(field.getFieldName())) // 排除系统ID字段
                .map(this::buildFieldDefinition)
                .collect(Collectors.toList());

        if (!fieldDefinitions.isEmpty()) {
            sql.append(String.join(", ", fieldDefinitions));
        }

        // 添加表选项
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态实体表-").append(tableName).append("'");

        try {
            jdbcTemplate.execute(sql.toString());
            log.info("Table created successfully: {}", tableName);
        } catch (Exception e) {
            log.error("Failed to create table: {}", tableName, e);
            throw new RuntimeException("Failed to create table: " + e.getMessage());
        }
    }

    /**
     * 删除表
     */
    public void dropTable(String tableName) {
        log.info("Dropping table: {}", tableName);
        
        String sql = "DROP TABLE IF EXISTS " + tableName;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Table dropped successfully: {}", tableName);
        } catch (Exception e) {
            log.error("Failed to drop table: {}", tableName, e);
            throw new RuntimeException("Failed to drop table: " + e.getMessage());
        }
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE()";
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Failed to check table existence: {}", tableName, e);
            return false;
        }
    }

    /**
     * 构建字段定义SQL
     */
    private String buildFieldDefinition(EntityFieldDefinition field) {
        StringBuilder fieldDef = new StringBuilder();
        fieldDef.append(field.getFieldName()).append(" ");

        // 数据类型
        String dbType = field.getDbType();
        if (dbType == null) {
            dbType = mapFieldTypeToDbType(field.getFieldType());
        }

        switch (dbType.toUpperCase()) {
            case "VARCHAR":
                int length = field.getFieldLength() != null ? field.getFieldLength() : 255;
                fieldDef.append("VARCHAR(").append(length).append(")");
                break;
            case "INT":
                fieldDef.append("INT");
                break;
            case "BIGINT":
                fieldDef.append("BIGINT");
                break;
            case "DECIMAL":
                int precision = field.getFieldLength() != null ? field.getFieldLength() : 10;
                int scale = field.getDecimalPlaces() != null ? field.getDecimalPlaces() : 2;
                fieldDef.append("DECIMAL(").append(precision).append(",").append(scale).append(")");
                break;
            case "BOOLEAN":
            case "TINYINT":
                fieldDef.append("TINYINT(1)");
                break;
            case "DATETIME":
                fieldDef.append("DATETIME");
                break;
            case "TEXT":
                fieldDef.append("TEXT");
                break;
            case "JSON":
                fieldDef.append("JSON");
                break;
            default:
                fieldDef.append("VARCHAR(255)");
        }

        // 约束
        if (field.getIsNotNull() != null && field.getIsNotNull()) {
            fieldDef.append(" NOT NULL");
        }

        if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
            fieldDef.append(" DEFAULT '").append(field.getDefaultValue()).append("'");
        }

        if (field.getIsUnique() != null && field.getIsUnique()) {
            fieldDef.append(" UNIQUE");
        }

        // 添加字段注释
        String comment = field.getDisplayName() != null ? field.getDisplayName() : field.getFieldName();
        fieldDef.append(" COMMENT '").append(comment).append("'");

        return fieldDef.toString();
    }

    /**
     * 映射字段类型到数据库类型
     */
    private String mapFieldTypeToDbType(String fieldType) {
        if (fieldType == null) {
            return "VARCHAR";
        }
        
        switch (fieldType.toUpperCase()) {
            case "STRING":
                return "VARCHAR";
            case "INTEGER":
                return "INT";
            case "LONG":
                return "BIGINT";
            case "DECIMAL":
                return "DECIMAL";
            case "BOOLEAN":
                return "TINYINT";
            case "DATETIME":
                return "DATETIME";
            case "TEXT":
                return "TEXT";
            case "JSON":
                return "JSON";
            default:
                return "VARCHAR";
        }
    }
}