package com.jinyi.odatademo.service;

import com.jinyi.odatademo.annotation.ODataField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ODataQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityRegistryService entityRegistryService;

    public QueryResult queryEntities(String entityName, Map<String, String> queryParams) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }

        // Build SQL query
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // Handle $select
        String selectClause = buildSelectClause(entityClass, queryParams.get("$select"));
        sql.append(selectClause);
        
        sql.append(" FROM ").append(tableName);
        
        // Handle $filter
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(entityClass, queryParams.get("$filter"), parameters);
        if (StringUtils.hasText(whereClause)) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        // Handle $orderby
        String orderByClause = buildOrderByClause(entityClass, queryParams.get("$orderby"));
        if (StringUtils.hasText(orderByClause)) {
            sql.append(" ORDER BY ").append(orderByClause);
        }
        
        // Get total count for $count
        long totalCount = 0;
        if ("true".equals(queryParams.get("$count"))) {
            totalCount = getTotalCount(tableName, whereClause, parameters);
        }
        
        // Handle $skip and $top
        String limitClause = buildLimitClause(queryParams.get("$skip"), queryParams.get("$top"));
        if (StringUtils.hasText(limitClause)) {
            sql.append(limitClause);
        }
        
        log.debug("Executing SQL: {} with parameters: {}", sql.toString(), parameters);
        
        // Execute query
        List<Map<String, Object>> results = jdbcTemplate.query(
            sql.toString(), 
            new EntityRowMapper(entityClass), 
            parameters.toArray()
        );
        
        return new QueryResult(results, totalCount);
    }

    private String buildSelectClause(Class<?> entityClass, String select) {
        if (!StringUtils.hasText(select)) {
            return "*";
        }
        
        String[] fields = select.split(",");
        List<String> columns = new ArrayList<>();
        
        for (String fieldName : fields) {
            fieldName = fieldName.trim();
            String columnName = getColumnName(entityClass, fieldName);
            if (columnName != null) {
                columns.add(columnName);
            }
        }
        
        return columns.isEmpty() ? "*" : String.join(", ", columns);
    }

    private String buildWhereClause(Class<?> entityClass, String filter, List<Object> parameters) {
        if (!StringUtils.hasText(filter)) {
            return "";
        }
        
        // Simple filter parser - supports basic operations
        // Examples: name eq 'John', age gt 25, price le 100.0
        
        // Replace OData operators with SQL operators
        String sqlFilter = filter
            .replaceAll("\\beq\\b", "=")
            .replaceAll("\\bne\\b", "!=")
            .replaceAll("\\bgt\\b", ">")
            .replaceAll("\\bge\\b", ">=")
            .replaceAll("\\blt\\b", "<")
            .replaceAll("\\ble\\b", "<=")
            .replaceAll("\\band\\b", "AND")
            .replaceAll("\\bor\\b", "OR")
            .replaceAll("\\bnot\\b", "NOT");
        
        // Convert field names to column names
        sqlFilter = convertFieldNamesToColumns(entityClass, sqlFilter);
        
        // Handle string literals and parameters
        sqlFilter = processFilterParameters(sqlFilter, parameters);
        
        return sqlFilter;
    }

    private String convertFieldNamesToColumns(Class<?> entityClass, String filter) {
        for (Field field : entityClass.getDeclaredFields()) {
            ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
            if (fieldAnnotation != null) {
                String fieldName = field.getName();
                String columnName = fieldAnnotation.name().isEmpty() ? 
                    camelToSnake(fieldName) : fieldAnnotation.name();
                
                // Replace field name with column name (word boundary)
                filter = filter.replaceAll("\\b" + fieldName + "\\b", columnName);
            }
        }
        return filter;
    }

    private String processFilterParameters(String filter, List<Object> parameters) {
        // Handle string literals in single quotes
        Pattern stringPattern = Pattern.compile("'([^']*)'");
        Matcher matcher = stringPattern.matcher(filter);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            parameters.add(matcher.group(1));
            matcher.appendReplacement(result, "?");
        }
        matcher.appendTail(result);
        
        // Handle numeric literals
        Pattern numberPattern = Pattern.compile("\\b(\\d+(?:\\.\\d+)?)\\b");
        matcher = numberPattern.matcher(result.toString());
        
        StringBuffer finalResult = new StringBuffer();
        while (matcher.find()) {
            String numberStr = matcher.group(1);
            if (numberStr.contains(".")) {
                parameters.add(new BigDecimal(numberStr));
            } else {
                parameters.add(Long.parseLong(numberStr));
            }
            matcher.appendReplacement(finalResult, "?");
        }
        matcher.appendTail(finalResult);
        
        return finalResult.toString();
    }

    private String buildOrderByClause(Class<?> entityClass, String orderBy) {
        if (!StringUtils.hasText(orderBy)) {
            return "";
        }
        
        String[] orderFields = orderBy.split(",");
        List<String> orderClauses = new ArrayList<>();
        
        for (String orderField : orderFields) {
            orderField = orderField.trim();
            String direction = "ASC";
            
            if (orderField.endsWith(" desc")) {
                direction = "DESC";
                orderField = orderField.substring(0, orderField.length() - 5).trim();
            } else if (orderField.endsWith(" asc")) {
                orderField = orderField.substring(0, orderField.length() - 4).trim();
            }
            
            String columnName = getColumnName(entityClass, orderField);
            if (columnName != null) {
                orderClauses.add(columnName + " " + direction);
            }
        }
        
        return orderClauses.isEmpty() ? "" : String.join(", ", orderClauses);
    }

    private String buildLimitClause(String skip, String top) {
        StringBuilder limitClause = new StringBuilder();
        
        if (StringUtils.hasText(top)) {
            limitClause.append(" LIMIT ").append(top);
        }
        
        if (StringUtils.hasText(skip)) {
            limitClause.append(" OFFSET ").append(skip);
        }
        
        return limitClause.toString();
    }

    private long getTotalCount(String tableName, String whereClause, List<Object> parameters) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        
        if (StringUtils.hasText(whereClause)) {
            countSql.append(" WHERE ").append(whereClause);
        }
        
        Long count = jdbcTemplate.queryForObject(countSql.toString(), Long.class, parameters.toArray());
        return count != null ? count : 0;
    }

    private String getColumnName(Class<?> entityClass, String fieldName) {
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            ODataField fieldAnnotation = field.getAnnotation(ODataField.class);
            if (fieldAnnotation != null) {
                return fieldAnnotation.name().isEmpty() ? 
                    camelToSnake(fieldName) : fieldAnnotation.name();
            }
        } catch (NoSuchFieldException e) {
            log.warn("Field {} not found in entity {}", fieldName, entityClass.getSimpleName());
        }
        return null;
    }

    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    // Inner classes
    public static class QueryResult {
        private final List<Map<String, Object>> data;
        private final long totalCount;

        public QueryResult(List<Map<String, Object>> data, long totalCount) {
            this.data = data;
            this.totalCount = totalCount;
        }

        public List<Map<String, Object>> getData() {
            return data;
        }

        public long getTotalCount() {
            return totalCount;
        }
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
                        // Column doesn't exist in result set (e.g., when using $select)
                        log.debug("Column {} not found in result set", columnName);
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
}