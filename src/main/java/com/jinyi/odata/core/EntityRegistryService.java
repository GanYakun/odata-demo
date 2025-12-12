package com.jinyi.odata.core;

import com.jinyi.odata.annotation.ODataEntity;
import com.jinyi.odata.annotation.ODataField;
import com.jinyi.odata.dynamic.DynamicEntityRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OData实体注册服务
 * 负责扫描和注册OData实体，管理实体与数据库表的映射关系
 */
@Service
@Slf4j
public class EntityRegistryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Map<String, Class<?>> entityRegistry = new ConcurrentHashMap<>();
    private final Map<String, String> entityTableMapping = new ConcurrentHashMap<>();
    
    // 动态实体注册
    private final Map<String, DynamicEntityRegistrationService> dynamicEntityServices = new ConcurrentHashMap<>();

    @PostConstruct
    public void scanAndRegisterEntities() {
        log.info("Starting OData entity scanning...");
        
        // 扫描业务实体包
        Reflections reflections = new Reflections("com.jinyi.business.entity");
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(ODataEntity.class);
        
        for (Class<?> entityClass : entityClasses) {
            registerEntity(entityClass);
        }
        
        log.info("OData entity scanning completed, registered {} entities", entityRegistry.size());
    }

    private void registerEntity(Class<?> entityClass) {
        ODataEntity annotation = entityClass.getAnnotation(ODataEntity.class);
        String entityName = annotation.name().isEmpty() ? entityClass.getSimpleName() : annotation.name();
        String tableName = annotation.table().isEmpty() ? entityClass.getSimpleName().toLowerCase() : annotation.table();
        
        entityRegistry.put(entityName, entityClass);
        entityTableMapping.put(entityName, tableName);
        
        log.info("Registered entity: {} -> table: {}", entityName, tableName);
        
        createTableIfNotExists(entityClass, tableName);
    }

    private void createTableIfNotExists(Class<?> entityClass, String tableName) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
            
            List<String> columns = new ArrayList<>();
            String primaryKey = null;
            
            for (Field field : entityClass.getDeclaredFields()) {
                ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
                if (fieldAnnotation != null) {
                    String columnName = fieldAnnotation.name().isEmpty() ? 
                        camelToSnake(field.getName()) : fieldAnnotation.name();
                    String columnType = getSqlType(field.getType(), fieldAnnotation);
                    
                    StringBuilder columnDef = new StringBuilder();
                    columnDef.append(columnName).append(" ").append(columnType);
                    
                    if (!fieldAnnotation.nullable()) {
                        columnDef.append(" NOT NULL");
                    }
                    
                    if (fieldAnnotation.key()) {
                        primaryKey = columnName;
                        if (field.getType() == Long.class || field.getType() == Integer.class) {
                            columnDef.append(" AUTO_INCREMENT");
                        }
                    }
                    
                    columns.add(columnDef.toString());
                }
            }
            
            sql.append(String.join(", ", columns));
            
            if (primaryKey != null) {
                sql.append(", PRIMARY KEY (").append(primaryKey).append(")");
            }
            
            sql.append(")");
            
            jdbcTemplate.execute(sql.toString());
            log.info("Table {} created successfully", tableName);
            
        } catch (Exception e) {
            log.error("Failed to create table {}: {}", tableName, e.getMessage());
        }
    }

    private String getSqlType(Class<?> fieldType, ODataField fieldAnnotation) {
        if (fieldType == String.class) {
            return "VARCHAR(" + fieldAnnotation.length() + ")";
        } else if (fieldType == Long.class || fieldType == long.class) {
            return "BIGINT";
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return "INT";
        } else if (fieldType == BigDecimal.class) {
            return "DECIMAL(19,2)";
        } else if (fieldType == LocalDateTime.class) {
            return "DATETIME";
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return "BOOLEAN";
        }
        return "VARCHAR(255)";
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    public Map<String, Class<?>> getEntityRegistry() {
        return Collections.unmodifiableMap(entityRegistry);
    }

    public String getTableName(String entityName) {
        return entityTableMapping.get(entityName);
    }

    public Class<?> getEntityClass(String entityName) {
        return entityRegistry.get(entityName);
    }

    /**
     * 注册动态实体
     */
    public void registerDynamicEntity(String entityName, String tableName, DynamicEntityRegistrationService service) {
        entityTableMapping.put(entityName, tableName);
        dynamicEntityServices.put(entityName, service);
        log.info("Registered dynamic entity: {} -> table: {}", entityName, tableName);
    }

    /**
     * 注销动态实体
     */
    public void unregisterDynamicEntity(String entityName) {
        entityTableMapping.remove(entityName);
        dynamicEntityServices.remove(entityName);
        log.info("Unregistered dynamic entity: {}", entityName);
    }

    /**
     * 检查是否为动态实体
     */
    public boolean isDynamicEntity(String entityName) {
        return dynamicEntityServices.containsKey(entityName);
    }

    /**
     * 获取动态实体服务
     */
    public DynamicEntityRegistrationService getDynamicEntityService(String entityName) {
        return dynamicEntityServices.get(entityName);
    }

    /**
     * 获取所有实体名称（包括动态实体）
     */
    public Set<String> getAllEntityNames() {
        Set<String> allEntities = new HashSet<>(entityRegistry.keySet());
        allEntities.addAll(dynamicEntityServices.keySet());
        return allEntities;
    }
}