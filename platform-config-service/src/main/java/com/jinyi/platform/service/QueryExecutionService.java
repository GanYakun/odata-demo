package com.jinyi.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 查询执行服务
 * 负责执行数据库查询和更新操作
 */
@Service
@Slf4j
public class QueryExecutionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 执行OData查询
     */
    public Map<String, Object> executeQuery(String appCode, String entityName, String tableName, Map<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 处理 $select
        String select = queryParams.get("$select");
        if (select != null && !select.trim().isEmpty()) {
            sql.append(select);
        } else {
            sql.append("*");
        }
        
        sql.append(" FROM ").append(tableName);
        
        List<Object> parameters = new ArrayList<>();
        
        // 处理 $filter
        String filter = queryParams.get("$filter");
        if (filter != null && !filter.trim().isEmpty()) {
            String whereClause = parseFilter(filter, parameters);
            sql.append(" WHERE ").append(whereClause);
        }
        
        // 处理 $orderby
        String orderBy = queryParams.get("$orderby");
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(parseOrderBy(orderBy));
        }
        
        // 处理 $top
        String top = queryParams.get("$top");
        if (top != null && !top.trim().isEmpty()) {
            try {
                int limit = Integer.parseInt(top);
                sql.append(" LIMIT ").append(limit);
            } catch (NumberFormatException e) {
                log.warn("Invalid $top value: {}", top);
            }
        }
        
        // 处理 $skip
        String skip = queryParams.get("$skip");
        if (skip != null && !skip.trim().isEmpty()) {
            try {
                int offset = Integer.parseInt(skip);
                sql.append(" OFFSET ").append(offset);
            } catch (NumberFormatException e) {
                log.warn("Invalid $skip value: {}", skip);
            }
        }
        
        log.debug("Executing SQL: {} with parameters: {}", sql.toString(), parameters);
        
        List<Map<String, Object>> results = jdbcTemplate.query(sql.toString(), parameters.toArray(), this::mapRow);
        
        Map<String, Object> response = new HashMap<>();
        response.put("@odata.application", appCode);
        response.put("@odata.context", "$metadata#" + entityName);
        response.put("value", results);
        
        // 处理 $count
        String count = queryParams.get("$count");
        if ("true".equalsIgnoreCase(count)) {
            int totalCount = getTotalCount(tableName, filter, parameters);
            response.put("@odata.count", totalCount);
        }
        
        return response;
    }

    /**
     * 执行数据更新操作
     */
    public Map<String, Object> executeUpdate(String appCode, String entityName, String tableName, String operation, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("@odata.application", appCode);
        response.put("@odata.context", "$metadata#" + entityName);
        
        try {
            switch (operation.toUpperCase()) {
                case "CREATE":
                    return executeCreate(tableName, data, response);
                case "UPDATE":
                    return executeUpdate(tableName, data, response);
                case "DELETE":
                    return executeDelete(tableName, data, response);
                default:
                    response.put("success", false);
                    response.put("message", "Unsupported operation: " + operation);
                    return response;
            }
        } catch (Exception e) {
            log.error("Failed to execute {} operation on {}: {}", operation, tableName, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    /**
     * 执行创建操作
     */
    private Map<String, Object> executeCreate(String tableName, Map<String, Object> data, Map<String, Object> response) {
        if (data.isEmpty()) {
            throw new RuntimeException("Entity data is required");
        }

        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!"id".equals(entry.getKey())) { // 跳过自增ID
                columns.add(entry.getKey());
                placeholders.add("?");
                values.add(convertValue(entry.getValue()));
            }
        }

        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + ") VALUES (" + String.join(", ", placeholders) + ")";
        
        log.debug("Executing INSERT: {} with values: {}", sql, values);
        jdbcTemplate.update(sql, values.toArray());

        response.put("success", true);
        response.put("message", "Entity created successfully");
        return response;
    }

    /**
     * 执行更新操作
     */
    private Map<String, Object> executeUpdate(String tableName, Map<String, Object> data, Map<String, Object> response) {
        if (data.isEmpty()) {
            throw new RuntimeException("Entity data is required");
        }

        String key = (String) data.remove("key");
        if (key == null) {
            throw new RuntimeException("Key is required for update operation");
        }

        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!"id".equals(entry.getKey())) { // 跳过ID字段
                setClauses.add(entry.getKey() + " = ?");
                values.add(convertValue(entry.getValue()));
            }
        }

        values.add(Long.parseLong(key)); // 添加WHERE条件的参数

        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE id = ?";
        
        log.debug("Executing UPDATE: {} with values: {}", sql, values);
        int updated = jdbcTemplate.update(sql, values.toArray());

        if (updated == 0) {
            throw new RuntimeException("Entity not found with key: " + key);
        }

        response.put("success", true);
        response.put("message", "Entity updated successfully");
        return response;
    }

    /**
     * 执行删除操作
     */
    private Map<String, Object> executeDelete(String tableName, Map<String, Object> data, Map<String, Object> response) {
        String key = (String) data.get("key");
        if (key == null) {
            throw new RuntimeException("Key is required for delete operation");
        }

        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        
        log.debug("Executing DELETE: {} with key: {}", sql, key);
        int deleted = jdbcTemplate.update(sql, Long.parseLong(key));
        
        response.put("success", deleted > 0);
        response.put("message", deleted > 0 ? "Entity deleted successfully" : "Entity not found");
        return response;
    }

    /**
     * 解析过滤条件
     */
    private String parseFilter(String filter, List<Object> parameters) {
        // 简化的过滤器解析，支持基本的比较操作
        String result = filter;
        
        // 处理 eq (等于)
        result = result.replaceAll("(\\w+)\\s+eq\\s+'([^']*)'", "$1 = ?");
        result = result.replaceAll("(\\w+)\\s+eq\\s+(\\d+)", "$1 = ?");
        
        // 处理 ne (不等于)
        result = result.replaceAll("(\\w+)\\s+ne\\s+'([^']*)'", "$1 != ?");
        result = result.replaceAll("(\\w+)\\s+ne\\s+(\\d+)", "$1 != ?");
        
        // 处理 gt (大于)
        result = result.replaceAll("(\\w+)\\s+gt\\s+(\\d+)", "$1 > ?");
        
        // 处理 lt (小于)
        result = result.replaceAll("(\\w+)\\s+lt\\s+(\\d+)", "$1 < ?");
        
        // 处理 contains
        result = result.replaceAll("contains\\((\\w+),\\s*'([^']*)'\\)", "$1 LIKE ?");
        
        // 提取参数值
        extractFilterParameters(filter, parameters);
        
        return result;
    }

    /**
     * 提取过滤器参数
     */
    private void extractFilterParameters(String filter, List<Object> parameters) {
        // 提取字符串值
        java.util.regex.Pattern stringPattern = java.util.regex.Pattern.compile("'([^']*)'");
        java.util.regex.Matcher stringMatcher = stringPattern.matcher(filter);
        while (stringMatcher.find()) {
            String value = stringMatcher.group(1);
            if (filter.contains("contains")) {
                parameters.add("%" + value + "%");
            } else {
                parameters.add(value);
            }
        }
        
        // 提取数字值
        java.util.regex.Pattern numberPattern = java.util.regex.Pattern.compile("\\s+(\\d+)(?=\\s|$)");
        java.util.regex.Matcher numberMatcher = numberPattern.matcher(filter.replaceAll("'[^']*'", ""));
        while (numberMatcher.find()) {
            parameters.add(Long.parseLong(numberMatcher.group(1)));
        }
    }

    /**
     * 解析排序条件
     */
    private String parseOrderBy(String orderBy) {
        return orderBy.replace(" asc", " ASC").replace(" desc", " DESC");
    }

    /**
     * 获取总数
     */
    private int getTotalCount(String tableName, String filter, List<Object> parameters) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        
        if (filter != null && !filter.trim().isEmpty()) {
            String whereClause = parseFilter(filter, new ArrayList<>(parameters));
            countSql.append(" WHERE ").append(whereClause);
        }
        
        return jdbcTemplate.queryForObject(countSql.toString(), parameters.toArray(), Integer.class);
    }

    /**
     * 映射结果行
     */
    private Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new HashMap<>();
        int columnCount = rs.getMetaData().getColumnCount();
        
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnLabel(i);
            Object value = rs.getObject(i);
            
            // 转换特殊类型
            if (value instanceof java.sql.Timestamp) {
                value = ((java.sql.Timestamp) value).toLocalDateTime();
            }
            
            row.put(columnName, value);
        }
        
        return row;
    }

    /**
     * 转换值类型
     */
    private Object convertValue(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            // 尝试解析为日期时间
            if (strValue.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) {
                try {
                    return LocalDateTime.parse(strValue, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception e) {
                    // 解析失败，保持原值
                }
            }
        }
        return value;
    }
}