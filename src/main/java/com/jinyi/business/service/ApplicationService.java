package com.jinyi.business.service;

import com.jinyi.business.entity.Application;
import com.jinyi.business.entity.ApplicationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 应用管理服务
 * 负责应用的创建、管理和实体关联
 */
@Service
@Slf4j
public class ApplicationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建新应用
     */
    @Transactional
    public Application createApplication(Application app) {
        // 检查应用代码是否已存在
        if (existsByAppCode(app.getAppCode())) {
            throw new RuntimeException("Application code already exists: " + app.getAppCode());
        }

        String sql = """
            INSERT INTO applications (app_code, app_name, description, version, active, base_url, owner, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, app.getAppCode());
            ps.setString(2, app.getAppName());
            ps.setString(3, app.getDescription());
            ps.setString(4, app.getVersion());
            ps.setBoolean(5, app.getActive() != null ? app.getActive() : true);
            ps.setString(6, app.getBaseUrl());
            ps.setString(7, app.getOwner());
            ps.setObject(8, now);
            ps.setObject(9, now);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        app.setId(id);
        app.setCreatedAt(now);
        app.setUpdatedAt(now);

        log.info("Created application: {} (ID: {})", app.getAppCode(), id);
        return app;
    }

    /**
     * 获取所有应用
     */
    public List<Application> getAllApplications() {
        String sql = "SELECT * FROM applications ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new ApplicationRowMapper());
    }

    /**
     * 根据ID获取应用
     */
    public Optional<Application> getApplicationById(Long id) {
        String sql = "SELECT * FROM applications WHERE id = ?";
        List<Application> results = jdbcTemplate.query(sql, new ApplicationRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 根据应用代码获取应用
     */
    public Optional<Application> getApplicationByCode(String appCode) {
        String sql = "SELECT * FROM applications WHERE app_code = ?";
        List<Application> results = jdbcTemplate.query(sql, new ApplicationRowMapper(), appCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 更新应用
     */
    @Transactional
    public Application updateApplication(Application app) {
        String sql = """
            UPDATE applications 
            SET app_name = ?, description = ?, version = ?, active = ?, base_url = ?, owner = ?, updated_at = ?
            WHERE id = ?
            """;

        LocalDateTime now = LocalDateTime.now();
        int updated = jdbcTemplate.update(sql,
                app.getAppName(), app.getDescription(), app.getVersion(),
                app.getActive(), app.getBaseUrl(), app.getOwner(), now, app.getId());

        if (updated == 0) {
            throw new RuntimeException("Application not found: " + app.getId());
        }

        app.setUpdatedAt(now);
        log.info("Updated application: {} (ID: {})", app.getAppCode(), app.getId());
        return app;
    }

    /**
     * 删除应用
     */
    @Transactional
    public void deleteApplication(Long id) {
        // 先删除关联的实体
        jdbcTemplate.update("DELETE FROM application_entities WHERE application_id = ?", id);
        
        // 再删除应用
        int deleted = jdbcTemplate.update("DELETE FROM applications WHERE id = ?", id);
        if (deleted == 0) {
            throw new RuntimeException("Application not found: " + id);
        }

        log.info("Deleted application with ID: {}", id);
    }

    /**
     * 为应用添加实体关联
     */
    @Transactional
    public ApplicationEntity addEntityToApplication(Long applicationId, String entityName, String tableName, 
                                                   String description, boolean isDynamic) {
        // 检查应用是否存在
        if (!existsById(applicationId)) {
            throw new RuntimeException("Application not found: " + applicationId);
        }

        // 检查实体是否已关联到该应用
        if (entityExistsInApplication(applicationId, entityName)) {
            throw new RuntimeException("Entity already exists in application: " + entityName);
        }

        String sql = """
            INSERT INTO application_entities (application_id, entity_name, table_name, description, is_dynamic, active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, applicationId);
            ps.setString(2, entityName);
            ps.setString(3, tableName);
            ps.setString(4, description);
            ps.setBoolean(5, isDynamic);
            ps.setBoolean(6, true);
            ps.setObject(7, now);
            ps.setObject(8, now);
            return ps;
        }, keyHolder);

        ApplicationEntity appEntity = new ApplicationEntity();
        appEntity.setId(keyHolder.getKey().longValue());
        appEntity.setApplicationId(applicationId);
        appEntity.setEntityName(entityName);
        appEntity.setTableName(tableName);
        appEntity.setDescription(description);
        appEntity.setIsDynamic(isDynamic);
        appEntity.setActive(true);
        appEntity.setCreatedAt(now);
        appEntity.setUpdatedAt(now);

        log.info("Added entity {} to application {}", entityName, applicationId);
        return appEntity;
    }

    /**
     * 获取应用下的所有实体
     */
    public List<ApplicationEntity> getApplicationEntities(Long applicationId) {
        String sql = "SELECT * FROM application_entities WHERE application_id = ? AND active = true ORDER BY created_at";
        return jdbcTemplate.query(sql, new ApplicationEntityRowMapper(), applicationId);
    }

    /**
     * 获取应用下的所有实体（根据应用代码）
     */
    public List<ApplicationEntity> getApplicationEntitiesByCode(String appCode) {
        String sql = """
            SELECT ae.* FROM application_entities ae
            JOIN applications a ON ae.application_id = a.id
            WHERE a.app_code = ? AND ae.active = true
            ORDER BY ae.created_at
            """;
        return jdbcTemplate.query(sql, new ApplicationEntityRowMapper(), appCode);
    }

    /**
     * 从应用中移除实体
     */
    @Transactional
    public void removeEntityFromApplication(Long applicationId, String entityName) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM application_entities WHERE application_id = ? AND entity_name = ?",
                applicationId, entityName);
        
        if (deleted == 0) {
            throw new RuntimeException("Entity not found in application: " + entityName);
        }

        log.info("Removed entity {} from application {}", entityName, applicationId);
    }

    /**
     * 检查应用代码是否存在
     */
    private boolean existsByAppCode(String appCode) {
        String sql = "SELECT COUNT(*) FROM applications WHERE app_code = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, appCode);
        return count != null && count > 0;
    }

    /**
     * 检查应用ID是否存在
     */
    private boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM applications WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    /**
     * 检查实体是否已关联到应用
     */
    private boolean entityExistsInApplication(Long applicationId, String entityName) {
        String sql = "SELECT COUNT(*) FROM application_entities WHERE application_id = ? AND entity_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, applicationId, entityName);
        return count != null && count > 0;
    }

    /**
     * 应用行映射器
     */
    private static class ApplicationRowMapper implements RowMapper<Application> {
        @Override
        public Application mapRow(ResultSet rs, int rowNum) throws SQLException {
            Application app = new Application();
            app.setId(rs.getLong("id"));
            app.setAppCode(rs.getString("app_code"));
            app.setAppName(rs.getString("app_name"));
            app.setDescription(rs.getString("description"));
            app.setVersion(rs.getString("version"));
            app.setActive(rs.getBoolean("active"));
            app.setBaseUrl(rs.getString("base_url"));
            app.setOwner(rs.getString("owner"));
            app.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            app.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return app;
        }
    }

    /**
     * 应用实体行映射器
     */
    private static class ApplicationEntityRowMapper implements RowMapper<ApplicationEntity> {
        @Override
        public ApplicationEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApplicationEntity appEntity = new ApplicationEntity();
            appEntity.setId(rs.getLong("id"));
            appEntity.setApplicationId(rs.getLong("application_id"));
            appEntity.setEntityName(rs.getString("entity_name"));
            appEntity.setTableName(rs.getString("table_name"));
            appEntity.setDescription(rs.getString("description"));
            appEntity.setIsDynamic(rs.getBoolean("is_dynamic"));
            appEntity.setActive(rs.getBoolean("active"));
            appEntity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            appEntity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return appEntity;
        }
    }
}