package com.jinyi.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数据初始化服务
 * 负责在应用启动时初始化示例数据
 */
@Service
@Slf4j
public class DataInitService implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createTables();
        initApplicationData();
        initSampleData();
    }

    private void createTables() {
        try {
            // 创建应用表
            String createApplicationsTable = """
                CREATE TABLE IF NOT EXISTS applications (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    app_code VARCHAR(50) NOT NULL UNIQUE,
                    app_name VARCHAR(100) NOT NULL,
                    description VARCHAR(500),
                    version VARCHAR(50) NOT NULL,
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    base_url VARCHAR(200),
                    owner VARCHAR(100),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            jdbcTemplate.execute(createApplicationsTable);

            // 应用实体关联表已废弃，使用新的entity_definitions系统

            // 创建示例数据表
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_no VARCHAR(50) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
            jdbcTemplate.execute(createOrdersTable);

            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    price DECIMAL(10,2) NOT NULL,
                    stock INT DEFAULT 0,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            jdbcTemplate.execute(createProductsTable);

            String createProjectsTable = """
                CREATE TABLE IF NOT EXISTS projects (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    description TEXT,
                    start_time DATETIME,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
            jdbcTemplate.execute(createProjectsTable);

            log.info("Database tables created successfully");
        } catch (Exception e) {
            log.error("Failed to create database tables: {}", e.getMessage());
        }
    }

    private void initApplicationData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications", Integer.class);
            if (count != null && count > 0) {
                log.info("Application table already has data, skipping initialization");
                return;
            }

            // 插入示例应用数据
            String sql = """
                INSERT INTO applications (app_code, app_name, description, version, active, base_url, owner, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            LocalDateTime now = LocalDateTime.now();
            
            jdbcTemplate.update(sql, "DEMO", "Demo Application", "OData Protocol Demo Application", "1.0.0", true, "/odata/DEMO", "System Admin", now, now);
            jdbcTemplate.update(sql, "ERP", "Enterprise Resource Planning", "Enterprise ERP System", "2.1.0", true, "/odata/ERP", "ERP Team", now, now);
            jdbcTemplate.update(sql, "CRM", "Customer Relationship Management", "Customer Relationship Management System", "1.5.0", true, "/odata/CRM", "CRM Team", now, now);
            
            log.info("Initialized application data");
        } catch (Exception e) {
            log.error("Failed to initialize application data: {}", e.getMessage());
        }
        
        // 应用实体关联已迁移到新系统，不再需要初始化
    }



    private void initSampleData() {
        initOrderData();
        initProductData();
        initProjectData();
    }

    private void initOrderData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            if (count != null && count > 0) {
                log.info("Order table already has data, skipping initialization");
                return;
            }

            // 插入示例订单数据
            String sql = "INSERT INTO orders (order_no, amount, created_at) VALUES (?, ?, ?)";
            
            jdbcTemplate.update(sql, "ORD001", new BigDecimal("299.99"), LocalDateTime.now().minusDays(5));
            jdbcTemplate.update(sql, "ORD002", new BigDecimal("159.50"), LocalDateTime.now().minusDays(3));
            jdbcTemplate.update(sql, "ORD003", new BigDecimal("89.99"), LocalDateTime.now().minusDays(1));
            
            log.info("Initialized order data");
        } catch (Exception e) {
            log.error("Failed to initialize order data: {}", e.getMessage());
        }
    }

    private void initProductData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
            if (count != null && count > 0) {
                log.info("Product table already has data, skipping initialization");
                return;
            }

            // 插入示例产品数据
            String sql = "INSERT INTO products (name, description, price, stock, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            LocalDateTime now = LocalDateTime.now();
            
            jdbcTemplate.update(sql, "Laptop Computer", "High Performance Office Laptop", new BigDecimal("5999.00"), 50, now, now);
            jdbcTemplate.update(sql, "Wireless Mouse", "Ergonomic Wireless Mouse", new BigDecimal("129.00"), 200, now, now);
            jdbcTemplate.update(sql, "Mechanical Keyboard", "Blue Switch Mechanical Keyboard", new BigDecimal("299.00"), 100, now, now);
            
            log.info("Initialized product data");
        } catch (Exception e) {
            log.error("Failed to initialize product data: {}", e.getMessage());
        }
    }

    private void initProjectData() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects", Integer.class);
            if (count != null && count > 0) {
                log.info("Project table already has data, skipping initialization");
                return;
            }

            // 插入示例项目数据
            String sql = "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
            LocalDateTime now = LocalDateTime.now();
            
            jdbcTemplate.update(sql, "OData Framework Development", "OData Protocol Implementation based on Spring Boot", now.minusDays(30), now, now);
            jdbcTemplate.update(sql, "Dynamic Entity System", "System supporting runtime dynamic entity creation", now.minusDays(15), now, now);
            jdbcTemplate.update(sql, "Frontend Interface Development", "Management Backend Frontend Interface Development", now.minusDays(7), now, now);
            
            log.info("Initialized project data");
        } catch (Exception e) {
            log.error("Failed to initialize project data: {}", e.getMessage());
        }
    }
}