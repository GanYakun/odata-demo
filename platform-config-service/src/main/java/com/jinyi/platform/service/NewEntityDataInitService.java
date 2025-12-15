package com.jinyi.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 新实体系统数据初始化服务
 */
@Service
@Slf4j
@Order(0)
public class NewEntityDataInitService implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing new entity system database tables...");
        
        createEntityDefinitionTable();
        createEntityFieldDefinitionTable();
        createEntityDataStorageTable();
        
        log.info("New entity system database tables initialized successfully");
    }

    /**
     * 创建实体定义表
     */
    private void createEntityDefinitionTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS entity_definitions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                entity_name VARCHAR(100) NOT NULL COMMENT '实体名称',
                entity_code VARCHAR(50) NOT NULL COMMENT '实体编码',
                table_name VARCHAR(100) COMMENT '数据库表名',
                display_name VARCHAR(200) COMMENT '显示名称',
                description TEXT COMMENT '描述',
                app_id BIGINT NOT NULL COMMENT '应用ID',
                entity_type VARCHAR(20) DEFAULT 'DYNAMIC' COMMENT '实体类型',
                status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
                auto_create_table TINYINT(1) DEFAULT 1 COMMENT '是否自动创建表',
                table_created TINYINT(1) DEFAULT 0 COMMENT '表是否已创建',
                version INT DEFAULT 1 COMMENT '版本号',
                sort_order INT DEFAULT 0 COMMENT '排序',
                properties TEXT COMMENT '扩展属性',
                created_by VARCHAR(50) COMMENT '创建人',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                updated_by VARCHAR(50) COMMENT '更新人',
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
                UNIQUE KEY uk_entity_code_app (entity_code, app_id),
                KEY idx_entity_app_id (app_id),
                KEY idx_entity_status (status),
                KEY idx_entity_created_at (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体定义表'
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Entity definitions table created successfully");
        } catch (Exception e) {
            log.error("Failed to create entity definitions table", e);
        }
    }

    /**
     * 创建实体字段定义表
     */
    private void createEntityFieldDefinitionTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS entity_field_definitions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                entity_id BIGINT NOT NULL COMMENT '实体ID',
                field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
                field_code VARCHAR(50) NOT NULL COMMENT '字段编码',
                display_name VARCHAR(200) COMMENT '显示名称',
                description TEXT COMMENT '描述',
                field_type VARCHAR(20) NOT NULL COMMENT '字段类型',
                db_type VARCHAR(20) COMMENT '数据库类型',
                field_length INT COMMENT '字段长度',
                decimal_places INT COMMENT '小数位数',
                is_primary_key TINYINT(1) DEFAULT 0 COMMENT '是否主键',
                is_not_null TINYINT(1) DEFAULT 0 COMMENT '是否非空',
                is_unique TINYINT(1) DEFAULT 0 COMMENT '是否唯一',
                is_indexed TINYINT(1) DEFAULT 0 COMMENT '是否索引',
                default_value VARCHAR(500) COMMENT '默认值',
                sort_order INT DEFAULT 0 COMMENT '排序',
                status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
                validation_rules TEXT COMMENT '验证规则',
                properties TEXT COMMENT '扩展属性',
                created_by VARCHAR(50) COMMENT '创建人',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                updated_by VARCHAR(50) COMMENT '更新人',
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
                KEY idx_field_entity_id (entity_id),
                KEY idx_field_type (field_type),
                KEY idx_field_sort_order (sort_order)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体字段定义表'
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Entity field definitions table created successfully");
        } catch (Exception e) {
            log.error("Failed to create entity field definitions table", e);
        }
    }

    /**
     * 创建实体数据存储表
     */
    private void createEntityDataStorageTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS entity_data_storage (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                entity_id BIGINT NOT NULL COMMENT '实体ID',
                entity_code VARCHAR(50) NOT NULL COMMENT '实体编码',
                app_id BIGINT NOT NULL COMMENT '应用ID',
                record_id VARCHAR(32) NOT NULL COMMENT '记录ID',
                data_json LONGTEXT NOT NULL COMMENT '数据JSON',
                version INT DEFAULT 1 COMMENT '版本号',
                status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
                properties TEXT COMMENT '扩展属性',
                created_by VARCHAR(50) COMMENT '创建人',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                updated_by VARCHAR(50) COMMENT '更新人',
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
                UNIQUE KEY uk_record_id (record_id),
                KEY idx_data_entity_id (entity_id),
                KEY idx_data_app_entity (app_id, entity_code),
                KEY idx_data_status (status),
                KEY idx_data_created_at (created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实体数据存储表'
            """;
        
        try {
            jdbcTemplate.execute(sql);
            log.info("Entity data storage table created successfully");
        } catch (Exception e) {
            log.error("Failed to create entity data storage table", e);
        }
    }
}