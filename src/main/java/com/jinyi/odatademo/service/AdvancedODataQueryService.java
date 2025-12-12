package com.jinyi.odatademo.service;

import com.jinyi.odatademo.annotation.ODataField;
import com.jinyi.odatademo.dto.EntityDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AdvancedODataQueryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityRegistryService entityRegistryService;

    @Autowired
    private ODataQueryService basicQueryService;

    @Autowired
    private DynamicEntityRegistrationService dynamicEntityService;

    /**
     * 支持更高级的 OData 查询功能
     */
    public ODataQueryService.QueryResult advancedQuery(String entityName, Map<String, String> queryParams) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }

        // 处理高级过滤功能
        String filter = queryParams.get("$filter");
        if (StringUtils.hasText(filter)) {
            filter = processAdvancedFilter(entityClass, filter);
            queryParams.put("$filter", filter);
        }

        // 使用基础查询服务执行查询
        return basicQueryService.queryEntities(entityName, queryParams);
    }

    /**
     * 处理高级过滤功能，支持 OData 函数
     */
    private String processAdvancedFilter(Class<?> entityClass, String filter) {
        // 支持的 OData 字符串函数
        filter = processStringFunctions(filter);
        
        // 支持的 OData 数学函数
        filter = processMathFunctions(filter);
        
        // 支持的 OData 日期函数
        filter = processDateFunctions(filter);
        
        return filter;
    }

    /**
     * 处理字符串函数
     * 支持: contains, startswith, endswith, length, tolower, toupper, trim, substring
     */
    private String processStringFunctions(String filter) {
        // contains(field, 'value') -> field LIKE '%value%'
        Pattern containsPattern = Pattern.compile("contains\\s*\\(\\s*(\\w+)\\s*,\\s*'([^']*)'\\s*\\)");
        Matcher matcher = containsPattern.matcher(filter);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);
            matcher.appendReplacement(result, field + " LIKE '%" + value + "%'");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // startswith(field, 'value') -> field LIKE 'value%'
        Pattern startswithPattern = Pattern.compile("startswith\\s*\\(\\s*(\\w+)\\s*,\\s*'([^']*)'\\s*\\)");
        matcher = startswithPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);
            matcher.appendReplacement(result, field + " LIKE '" + value + "%'");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // endswith(field, 'value') -> field LIKE '%value'
        Pattern endswithPattern = Pattern.compile("endswith\\s*\\(\\s*(\\w+)\\s*,\\s*'([^']*)'\\s*\\)");
        matcher = endswithPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            String value = matcher.group(2);
            matcher.appendReplacement(result, field + " LIKE '%" + value + "'");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // length(field) -> CHAR_LENGTH(field)
        Pattern lengthPattern = Pattern.compile("length\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = lengthPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "CHAR_LENGTH(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // tolower(field) -> LOWER(field)
        Pattern tolowerPattern = Pattern.compile("tolower\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = tolowerPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "LOWER(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // toupper(field) -> UPPER(field)
        Pattern toupperPattern = Pattern.compile("toupper\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = toupperPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "UPPER(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // trim(field) -> TRIM(field)
        Pattern trimPattern = Pattern.compile("trim\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = trimPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "TRIM(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        return filter;
    }

    /**
     * 处理数学函数
     * 支持: round, floor, ceiling
     */
    private String processMathFunctions(String filter) {
        // round(field) -> ROUND(field)
        Pattern roundPattern = Pattern.compile("round\\s*\\(\\s*(\\w+)\\s*\\)");
        Matcher matcher = roundPattern.matcher(filter);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "ROUND(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // floor(field) -> FLOOR(field)
        Pattern floorPattern = Pattern.compile("floor\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = floorPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "FLOOR(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // ceiling(field) -> CEILING(field)
        Pattern ceilingPattern = Pattern.compile("ceiling\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = ceilingPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "CEILING(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        return filter;
    }

    /**
     * 处理日期函数
     * 支持: year, month, day, hour, minute, second
     */
    private String processDateFunctions(String filter) {
        // year(field) -> YEAR(field)
        Pattern yearPattern = Pattern.compile("year\\s*\\(\\s*(\\w+)\\s*\\)");
        Matcher matcher = yearPattern.matcher(filter);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "YEAR(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // month(field) -> MONTH(field)
        Pattern monthPattern = Pattern.compile("month\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = monthPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "MONTH(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // day(field) -> DAY(field)
        Pattern dayPattern = Pattern.compile("day\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = dayPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "DAY(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        // hour(field) -> HOUR(field)
        Pattern hourPattern = Pattern.compile("hour\\s*\\(\\s*(\\w+)\\s*\\)");
        matcher = hourPattern.matcher(filter);
        result = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            matcher.appendReplacement(result, "HOUR(" + field + ")");
        }
        matcher.appendTail(result);
        filter = result.toString();

        return filter;
    }

    /**
     * 获取实体的聚合统计信息
     */
    public Map<String, Object> getAggregateStats(String entityName, String field) {
        String tableName = entityRegistryService.getTableName(entityName);
        Class<?> entityClass = entityRegistryService.getEntityClass(entityName);
        
        if (tableName == null || entityClass == null) {
            throw new RuntimeException("Entity not found: " + entityName);
        }

        String columnName = getColumnName(entityClass, field);
        if (columnName == null) {
            throw new RuntimeException("Field not found: " + field);
        }

        String sql = String.format(
            "SELECT COUNT(*) as count, MIN(%s) as min, MAX(%s) as max, AVG(%s) as avg, SUM(%s) as sum FROM %s",
            columnName, columnName, columnName, columnName, tableName
        );

        return jdbcTemplate.queryForMap(sql);
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

    /**
     * 获取动态实体名称
     */
    public Set<String> getDynamicEntityNames() {
        return dynamicEntityService.getAllDynamicEntities().keySet();
    }

    /**
     * 获取动态实体定义
     */
    public EntityDefinition getDynamicEntityDefinition(String entityName) {
        return dynamicEntityService.getEntityDefinition(entityName);
    }
}