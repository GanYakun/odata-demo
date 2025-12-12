package com.jinyi.odatademo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class DataInitService implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initOrderData();
        initProductData();
        initProjectData();
    }

    private void initOrderData() {
        try {
            // Check if data already exists
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            if (count != null && count > 0) {
                log.info("Order table already has data, skipping initialization");
                return;
            }

            // Insert test data
            jdbcTemplate.update(
                "INSERT INTO orders (order_no, amount, created_at) VALUES (?, ?, ?)",
                "ORD001", new BigDecimal("299.99"), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO orders (order_no, amount, created_at) VALUES (?, ?, ?)",
                "ORD002", new BigDecimal("159.50"), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO orders (order_no, amount, created_at) VALUES (?, ?, ?)",
                "ORD003", new BigDecimal("89.99"), LocalDateTime.now()
            );

            log.info("Order test data initialization completed");
        } catch (Exception e) {
            log.warn("Order data initialization failed: {}", e.getMessage());
        }
    }

    private void initProductData() {
        try {
            // Check if data already exists
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
            if (count != null && count > 0) {
                log.info("Product table already has data, skipping initialization");
                return;
            }

            // Insert test data
            jdbcTemplate.update(
                "INSERT INTO products (name, description, price, stock, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                "iPhone 15", "Latest Apple smartphone", new BigDecimal("7999.00"), 50, LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO products (name, description, price, stock, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                "MacBook Pro", "Professional laptop computer", new BigDecimal("15999.00"), 20, LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO products (name, description, price, stock, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                "AirPods Pro", "Active noise cancelling earphones", new BigDecimal("1999.00"), 100, LocalDateTime.now(), LocalDateTime.now()
            );

            log.info("Product test data initialization completed");
        } catch (Exception e) {
            log.warn("Product data initialization failed: {}", e.getMessage());
        }
    }

    private void initProjectData() {
        try {
            // Check if data already exists
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects", Integer.class);
            if (count != null && count > 0) {
                log.info("Project table already has data, skipping initialization");
                return;
            }

            // Insert test data
            jdbcTemplate.update(
                "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "E-Commerce Platform", "Build a modern e-commerce platform with microservices", 
                LocalDateTime.of(2024, 1, 15, 9, 0), LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "Mobile App Development", "Develop cross-platform mobile application", 
                LocalDateTime.of(2024, 3, 1, 10, 30), LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "Data Analytics Dashboard", "Create real-time analytics dashboard", 
                LocalDateTime.of(2024, 2, 10, 14, 0), LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "AI Chatbot Integration", "Integrate AI-powered chatbot into customer service", 
                LocalDateTime.of(2024, 4, 5, 11, 15), LocalDateTime.now(), LocalDateTime.now()
            );
            jdbcTemplate.update(
                "INSERT INTO projects (name, description, start_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                "Cloud Migration", "Migrate legacy systems to cloud infrastructure", 
                LocalDateTime.of(2023, 12, 1, 8, 0), LocalDateTime.now(), LocalDateTime.now()
            );

            log.info("Project test data initialization completed");
        } catch (Exception e) {
            log.warn("Project data initialization failed: {}", e.getMessage());
        }
    }
}