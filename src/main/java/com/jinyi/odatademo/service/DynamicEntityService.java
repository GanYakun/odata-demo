package com.jinyi.odatademo.service;

import com.jinyi.odatademo.annotation.ODataField;
import com.jinyi.odatademo.dto.EntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DynamicEntityService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityRegistryService entityRegistryService;

    public List<Map<String, Object>> findAll(String entityName) {
        String tableName = entityRegistryService.getTableName(entityName);
        
        if (tableName == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }
        
        String sql = "SELECT * FROM " + tableName;
        
        // 检查是否为动态实体
        if (entityRegistryService.isDynamicEntity(entityName)) {
            return jdbcTemplate.query(sql, new DynamicEntityRowMapper(entityName));
        } else {
            Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
            if (entityClass == null) {
                throw new RuntimeException("Entity class not found: " + entityName);
            }
            return jdbcTemplate.query(sql, new EntityRowMapper(entityClass));
        }
    }

    public Map<String, Object> findById(String entityName, Object id) {
        String tableName = entityRegistryService.getTableName(entityName);
        
        if (tableName == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }
        
        String keyColumn;
        List<Map<String, Object>> results;
        
        if (entityRegistryService.isDynamicEntity(entityName)) {
            keyColumn = getDynamicKeyColumn(entityName);
            String sql = "SELECT * FROM " + tableName + " WHERE " + keyColumn + " = ?";
            results = jdbcTemplate.query(sql, new DynamicEntityRowMapper(entityName), id);
        } else {
            Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
            if (entityClass == null) {
                throw new RuntimeException("Entity class not found: " + entityName);
            }
            keyColumn = getKeyColumn(entityClass);
            String sql = "SELECT * FROM " + tableName + " WHERE " + keyColumn + " = ?";
            results = jdbcTemplate.query(sql, new EntityRowMapper(entityClass), id);
        }
        
        return results.isEmpty() ? null : results.get(0);
    }

    public Map<String, Object> create(String entityName, Map<String, Object> data) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }
        
        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        for (Field field : entityClass.getDeclaredFields()) {
            ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
            if (fieldAnnotation != null && !fieldAnnotation.key()) {
                String columnName = fieldAnnotation.name().isEmpty() ? 
                    camelToSnake(field.getName()) : fieldAnnotation.name();
                String fieldName = field.getName();
                
                if (data.containsKey(fieldName)) {
                    columns.add(columnName);
                    values.add(data.get(fieldName));
                }
            }
        }
        
        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + 
                    ") VALUES (" + String.join(", ", columns.stream().map(c -> "?").toArray(String[]::new)) + ")";
        
        jdbcTemplate.update(sql, values.toArray());
        
        // Return created entity (simplified, should get generated ID)
        return data;
    }

    public Map<String, Object> update(String entityName, Object id, Map<String, Object> data) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }
        
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        
        for (Field field : entityClass.getDeclaredFields()) {
            ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
            if (fieldAnnotation != null && !fieldAnnotation.key()) {
                String columnName = fieldAnnotation.name().isEmpty() ? 
                    camelToSnake(field.getName()) : fieldAnnotation.name();
                String fieldName = field.getName();
                
                if (data.containsKey(fieldName)) {
                    setClauses.add(columnName + " = ?");
                    values.add(data.get(fieldName));
                }
            }
        }
        
        String keyColumn = getKeyColumn(entityClass);
        values.add(id);
        
        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + 
                    " WHERE " + keyColumn + " = ?";
        
        jdbcTemplate.update(sql, values.toArray());
        
        return findById(entityName, id);
    }

    public void delete(String entityName, Object id) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }
        
        String keyColumn = getKeyColumn(entityClass);
        String sql = "DELETE FROM " + tableName + " WHERE " + keyColumn + " = ?";
        
        jdbcTemplate.update(sql, id);
    }

    private String getKeyColumn(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
            if (fieldAnnotation != null && fieldAnnotation.key()) {
                return fieldAnnotation.name().isEmpty() ? 
                    camelToSnake(field.getName()) : fieldAnnotation.name();
            }
        }
        return "id";
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private static class EntityRowMapper implements RowMapper<Map<String, Object>> {
        private final Class<?> entityClass;

        public EntityRowMapper(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            
            for (Field field : entityClass.getDeclaredFields()) {
                ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
                if (fieldAnnotation != null) {
                    String columnName = fieldAnnotation.name().isEmpty() ? 
                        camelToSnake(field.getName()) : fieldAnnotation.name();
                    String fieldName = field.getName();
                    
                    try {
                        Object value = getValueFromResultSet(rs, columnName, field.getType());
                        result.put(fieldName, value);
                    } catch (SQLException e) {
                        // Ignore if column doesn't exist
                    }
                }
            }
            
            return result;
        }

        private Object getValueFromResultSet(ResultSet rs, String columnName, Class<?> fieldType) throws SQLException {
            if (fieldType == String.class) {
                return rs.getString(columnName);
            } else if (fieldType == Long.class || fieldType == long.class) {
                return rs.getLong(columnName);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                return rs.getInt(columnName);
            } else if (fieldType == BigDecimal.class) {
                return rs.getBigDecimal(columnName);
            } else if (fieldType == LocalDateTime.class) {
                return rs.getTimestamp(columnName) != null ? rs.getTimestamp(columnName).toLocalDateTime() : null;
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                return rs.getBoolean(columnName);
            }
            return rs.getObject(columnName);
        }

        private String camelToSnake(String camelCase) {
            return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        }
    }

    private String getDynamicKeyColumn(String entityName) {
        DynamicEntityRegistrationService dynamicService = entityRegistryService.getDynamicEntityService(entityName);
        if (dynamicService != null) {
            EntityDefinition entityDef = dynamicService.getEntityDefinition(entityName);
            if (entityDef != null) {
                for (EntityDefinition.FieldDefinition field : entityDef.getFields()) {
                    if (field.isKey()) {
                        return field.getColumnName() != null && !field.getColumnName().trim().isEmpty() 
                            ? field.getColumnName() : camelToSnake(field.getFieldName());
                    }
                }
            }
        }
        return "id";
    }

    private static class DynamicEntityRowMapper implements RowMapper<Map<String, Object>> {
        private final String entityName;

        public DynamicEntityRowMapper(String entityName) {
            this.entityName = entityName;
        }

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> result = new HashMap<>();
            
            // 这里需要通过某种方式获取动态实体的字段定义
            // 为了简化，我们直接从 ResultSet 的元数据中获取所有列
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                String fieldName = snakeToCamel(columnName);
                Object value = rs.getObject(i);
                result.put(fieldName, value);
            }
            
            return result;
        }

        private String snakeToCamel(String snakeCase) {
            StringBuilder result = new StringBuilder();
            boolean capitalizeNext = false;
            
            for (char c : snakeCase.toCharArray()) {
                if (c == '_') {
                    capitalizeNext = true;
                } else {
                    if (capitalizeNext) {
                        result.append(Character.toUpperCase(c));
                        capitalizeNext = false;
                    } else {
                        result.append(Character.toLowerCase(c));
                    }
                }
            }
            
            return result.toString();
        }
    }
}