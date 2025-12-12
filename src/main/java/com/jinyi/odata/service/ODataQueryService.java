package com.jinyi.odata.service;

import com.jinyi.odata.annotation.ODataField;
import com.jinyi.odata.core.EntityRegistryService;
import lombok.Data;
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

/**
 * OData查询服务
 * 负责处理OData查询请求并转换为SQL查询
 */
@Service
@Slf4j
public class ODataQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityRegistryService entityRegistryService;

    /**
     * 查询结果封装类
     */
    @Data
    public static class QueryResult {
        private List<Map<String, Object>> data;
        private long count;
        private boolean hasMore;
    }

    /**
     * 查询实体数据
     */
    public QueryResult queryEntities(String entityName, Map<String, String> queryParams) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }

        // 构建SQL查询
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // 处理 $select
        String selectClause = buildSelectClause(entityClass, queryParams.get("$select"));
        sql.append(selectClause);
        
        sql.append(" FROM ").append(tableName);
        
        // 处理 $filter
        List<Object> parameters = new ArrayList<>();
        String whereClause = buildWhereClause(entityClass, queryParams.get("$filter"), parameters);
        if (StringUtils.hasText(whereClause)) {
            sql.append(" WHERE ").append(whereClause);
        }
        
        // 处理 $orderby
        String orderClause = buildOrderClause(entityClass, queryParams.get("$orderby"));
        if (StringUtils.hasText(orderClause)) {
            sql.append(" ORDER BY ").append(orderClause);
        }
        
        // 处理 $top 和 $skip
        String limitClause = buildLimitClause(queryParams.get("$top"), queryParams.get("$skip"));
        if (StringUtils.hasText(limitClause)) {
            sql.append(limitClause);
        }

        log.debug("Generated SQL: {}", sql.toString());
        log.debug("Parameters: {}", parameters);

        // 执行查询
        List<Map<String, Object>> data = jdbcTemplate.query(sql.toString(), parameters.toArray(), new MapRowMapper());
        
        // 获取总数（如果需要）
        long totalCount = 0;
        if ("true".equals(queryParams.get("$count"))) {
            totalCount = getTotalCount(tableName, whereClause, parameters);
        }

        QueryResult result = new QueryResult();
        result.setData(data);
        result.setCount(totalCount);
        result.setHasMore(false); // 简化实现
        
        return result;
    }

    private String buildSelectClause(Class<?> entityClass, String select) {
        if (!StringUtils.hasText(select)) {
            return "*";
        }
        
        // 简化实现：直接返回选择的字段
        return select;
    }

    private String buildWhereClause(Class<?> entityClass, String filter, List<Object> parameters) {
        if (!StringUtils.hasText(filter)) {
            return "";
        }
        
        // 简化实现：基本的过滤条件解析
        // 实际项目中需要完整的OData过滤表达式解析器
        return parseSimpleFilter(filter, parameters);
    }

    private String parseSimpleFilter(String filter, List<Object> parameters) {
        // 简单的过滤条件解析
        if (filter.contains(" eq ")) {
            String[] parts = filter.split(" eq ");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String value = parts[1].trim().replace("'", "");
                parameters.add(value);
                return field + " = ?";
            }
        }
        
        if (filter.contains(" gt ")) {
            String[] parts = filter.split(" gt ");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String value = parts[1].trim().replace("'", "");
                parameters.add(value);
                return field + " > ?";
            }
        }
        
        if (filter.contains(" lt ")) {
            String[] parts = filter.split(" lt ");
            if (parts.length == 2) {
                String field = parts[0].trim();
                String value = parts[1].trim().replace("'", "");
                parameters.add(value);
                return field + " < ?";
            }
        }
        
        return "";
    }

    private String buildOrderClause(Class<?> entityClass, String orderby) {
        if (!StringUtils.hasText(orderby)) {
            return "";
        }
        
        // 简化实现：直接返回排序字段
        return orderby.replace(" asc", " ASC").replace(" desc", " DESC");
    }

    private String buildLimitClause(String top, String skip) {
        StringBuilder limit = new StringBuilder();
        
        if (StringUtils.hasText(skip)) {
            limit.append(" OFFSET ").append(skip);
        }
        
        if (StringUtils.hasText(top)) {
            limit.append(" LIMIT ").append(top);
        }
        
        return limit.toString();
    }

    private long getTotalCount(String tableName, String whereClause, List<Object> parameters) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);
        
        if (StringUtils.hasText(whereClause)) {
            countSql.append(" WHERE ").append(whereClause);
        }
        
        Long count = jdbcTemplate.queryForObject(countSql.toString(), parameters.toArray(), Long.class);
        return count != null ? count : 0;
    }

    /**
     * 简单的行映射器
     */
    private static class MapRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> row = new HashMap<>();
            int columnCount = rs.getMetaData().getColumnCount();
            
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            
            return row;
        }
    }
}