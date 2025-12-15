package com.jinyi.platform.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinyi.common.dto.EntityDataDto;
import com.jinyi.common.dto.EntityDefinitionDto;
import com.jinyi.common.entity.EntityDataStorage;
import com.jinyi.platform.mapper.EntityDataStorageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实体数据服务
 */
@Service
@Slf4j
public class EntityDataService {

    @Autowired
    private EntityDataStorageMapper entityDataStorageMapper;

    @Autowired
    private EntityDefinitionService entityDefinitionService;

    /**
     * 创建实体数据
     */
    @Transactional
    public EntityDataDto createData(Long appId, String entityCode, Map<String, Object> data, String createdBy) {
        log.info("Creating entity data for entity: {} in app: {}", entityCode, appId);

        // 获取实体定义
        EntityDefinitionDto entityDef = entityDefinitionService.getEntityByCode(entityCode, appId);
        if (entityDef == null) {
            throw new RuntimeException("Entity not found: " + entityCode);
        }

        // 验证数据
        validateEntityData(entityDef, data);

        // 创建数据记录
        EntityDataStorage dataStorage = new EntityDataStorage();
        dataStorage.setEntityId(entityDef.getId());
        dataStorage.setEntityCode(entityCode);
        dataStorage.setAppId(appId);
        dataStorage.setRecordId(generateRecordId());
        // 使用UTF-8编码序列化JSON，确保中文字符正确处理
        dataStorage.setDataJson(JSON.toJSONString(data, com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringKeyAsString, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat));
        dataStorage.setVersion(1);
        dataStorage.setStatus("ACTIVE");
        dataStorage.setCreatedBy(createdBy != null ? createdBy : "system");
        dataStorage.setUpdatedBy(createdBy != null ? createdBy : "system");

        entityDataStorageMapper.insert(dataStorage);

        return convertToDto(dataStorage);
    }

    /**
     * 根据记录ID获取数据
     */
    public EntityDataDto getDataByRecordId(Long entityId, String recordId) {
        EntityDataStorage dataStorage = entityDataStorageMapper.selectByRecordId(entityId, recordId);
        if (dataStorage == null) {
            return null;
        }
        return convertToDto(dataStorage);
    }

    /**
     * 根据应用和实体获取数据列表
     */
    public List<EntityDataDto> getDataByAppAndEntity(Long appId, String entityCode, int page, int size) {
        // 使用分页查询
        Page<EntityDataStorage> pageObj = new Page<>(page, size);
        QueryWrapper<EntityDataStorage> wrapper = new QueryWrapper<>();
        wrapper.eq("app_id", appId)
               .eq("entity_code", entityCode)
               .eq("deleted", false)
               .orderByDesc("created_at");

        Page<EntityDataStorage> result = entityDataStorageMapper.selectPage(pageObj, wrapper);
        
        return result.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 更新实体数据
     */
    @Transactional
    public EntityDataDto updateData(Long id, Map<String, Object> data, String updatedBy) {
        EntityDataStorage dataStorage = entityDataStorageMapper.selectById(id);
        if (dataStorage == null) {
            throw new RuntimeException("Data record not found: " + id);
        }

        // 获取实体定义进行验证
        EntityDefinitionDto entityDef = entityDefinitionService.getEntityById(dataStorage.getEntityId());
        if (entityDef != null) {
            validateEntityData(entityDef, data);
        }

        // 更新数据
        dataStorage.setDataJson(JSON.toJSONString(data, com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringKeyAsString, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat));
        dataStorage.setVersion(dataStorage.getVersion() + 1);
        dataStorage.setUpdatedBy(updatedBy != null ? updatedBy : "system");
        dataStorage.setUpdatedAt(LocalDateTime.now());

        entityDataStorageMapper.updateById(dataStorage);

        return convertToDto(dataStorage);
    }

    /**
     * 根据记录ID更新数据
     */
    @Transactional
    public EntityDataDto updateDataByRecordId(Long entityId, String recordId, Map<String, Object> data, String updatedBy) {
        EntityDataStorage dataStorage = entityDataStorageMapper.selectByRecordId(entityId, recordId);
        if (dataStorage == null) {
            throw new RuntimeException("Data record not found: " + recordId);
        }

        return updateData(dataStorage.getId(), data, updatedBy);
    }

    /**
     * 删除实体数据
     */
    @Transactional
    public void deleteData(Long id) {
        EntityDataStorage dataStorage = entityDataStorageMapper.selectById(id);
        if (dataStorage == null) {
            throw new RuntimeException("Data record not found: " + id);
        }

        entityDataStorageMapper.deleteById(id);
    }

    /**
     * 根据记录ID删除数据
     */
    @Transactional
    public void deleteDataByRecordId(Long entityId, String recordId) {
        EntityDataStorage dataStorage = entityDataStorageMapper.selectByRecordId(entityId, recordId);
        if (dataStorage == null) {
            throw new RuntimeException("Data record not found: " + recordId);
        }

        entityDataStorageMapper.deleteById(dataStorage.getId());
    }

    /**
     * 查询实体数据（支持OData查询参数）
     */
    public Map<String, Object> queryEntityData(Long appId, String entityCode, Map<String, String> queryParams) {
        log.info("Query parameters received: {}", queryParams);
        
        // 获取实体定义
        EntityDefinitionDto entityDef = entityDefinitionService.getEntityByCode(entityCode, appId);
        if (entityDef == null) {
            throw new RuntimeException("Entity not found: " + entityCode);
        }

        // 解析查询参数
        int top = queryParams.containsKey("$top") ? Integer.parseInt(queryParams.get("$top")) : 50;
        int skip = queryParams.containsKey("$skip") ? Integer.parseInt(queryParams.get("$skip")) : 0;
        String filter = queryParams.get("$filter");
        String orderby = queryParams.get("$orderby");
        String select = queryParams.get("$select");
        boolean count = queryParams.containsKey("$count") && "true".equals(queryParams.get("$count"));
        boolean stats = queryParams.containsKey("$stats") && "true".equals(queryParams.get("$stats"));
        
        log.debug("Query parameters - stats: {}, statsParam: {}", stats, queryParams.get("$stats"));

        // 构建查询条件
        QueryWrapper<EntityDataStorage> wrapper = new QueryWrapper<>();
        wrapper.eq("app_id", appId)
               .eq("entity_code", entityCode)
               .eq("deleted", false);

        // 应用过滤条件
        if (filter != null && !filter.isEmpty()) {
            log.debug("Filter condition: {}", filter);
            applyFilterCondition(wrapper, filter);
        }

        // 应用排序
        if (orderby != null && !orderby.isEmpty()) {
            log.debug("Order by condition: {}", orderby);
            applyOrderByCondition(wrapper, orderby);
        } else {
            wrapper.orderByDesc("created_at");
        }

        // 分页查询
        long pageNum = (skip / top) + 1;
        Page<EntityDataStorage> pageObj = new Page<>(pageNum, top);
        Page<EntityDataStorage> result = entityDataStorageMapper.selectPage(pageObj, wrapper);

        // 转换结果
        List<Map<String, Object>> dataList = result.getRecords().stream()
                .map(storage -> {
                    Map<String, Object> dataMap = JSON.parseObject(storage.getDataJson(), Map.class, com.alibaba.fastjson2.JSONReader.Feature.SupportAutoType);
                    // 添加系统字段
                    dataMap.put("id", storage.getRecordId());
                    dataMap.put("_created_at", storage.getCreatedAt());
                    dataMap.put("_updated_at", storage.getUpdatedAt());
                    dataMap.put("_version", storage.getVersion());
                    return dataMap;
                })
                .collect(Collectors.toList());

        // 应用字段选择
        if (select != null && !select.isEmpty()) {
            log.debug("Select fields: {}", select);
            String[] selectedFields = select.split(",");
            Set<String> fieldSet = Arrays.stream(selectedFields)
                    .map(String::trim)
                    .collect(Collectors.toSet());
            
            // 过滤字段
            dataList = dataList.stream()
                    .map(dataMap -> {
                        Map<String, Object> filteredMap = new HashMap<>();
                        // 总是包含id字段
                        if (dataMap.containsKey("id")) {
                            filteredMap.put("id", dataMap.get("id"));
                        }
                        // 添加选择的字段
                        for (String field : fieldSet) {
                            if (dataMap.containsKey(field)) {
                                filteredMap.put(field, dataMap.get(field));
                            }
                        }
                        return filteredMap;
                    })
                    .collect(Collectors.toList());
        }

        // 构建返回结果
        Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("@odata.context", "$metadata#" + entityCode);
        
        // 如果请求统计信息，返回统计数据
        if (stats) {
            log.debug("Generating stats data for {} records", dataList.size());
            Map<String, Object> statsData = generateStatsData(dataList, entityDef);
            log.debug("Generated stats data: {}", statsData);
            resultMap.put("@odata.stats", statsData);
        }
        
        resultMap.put("value", dataList);

        if (count) {
            resultMap.put("@odata.count", result.getTotal());
        }

        return resultMap;
    }

    /**
     * 验证实体数据
     */
    private void validateEntityData(EntityDefinitionDto entityDef, Map<String, Object> data) {
        // 简化验证：检查必填字段
        entityDef.getFields().forEach(field -> {
            if (Boolean.TRUE.equals(field.getIsNotNull()) && !data.containsKey(field.getFieldName())) {
                throw new RuntimeException("Required field missing: " + field.getFieldName());
            }
        });
    }

    /**
     * 生成记录ID
     */
    private String generateRecordId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换为DTO
     */
    private EntityDataDto convertToDto(EntityDataStorage dataStorage) {
        EntityDataDto dto = new EntityDataDto();
        BeanUtils.copyProperties(dataStorage, dto);
        
        // 解析JSON数据，确保UTF-8编码正确处理
        if (dataStorage.getDataJson() != null) {
            Map<String, Object> dataMap = JSON.parseObject(dataStorage.getDataJson(), Map.class, com.alibaba.fastjson2.JSONReader.Feature.SupportAutoType);
            dto.setData(dataMap);
        }
        
        return dto;
    }

    /**
     * 应用过滤条件
     * 支持基本的OData过滤操作符：eq, ne, gt, ge, lt, le, contains, startswith, endswith
     */
    private void applyFilterCondition(QueryWrapper<EntityDataStorage> wrapper, String filter) {
        try {
            // 简单的过滤条件解析
            // 支持格式：fieldName operator value
            // 例如：price gt 3000, name eq 'test', contains(name,'phone')
            
            filter = filter.trim();
            
            // 处理contains函数
            if (filter.startsWith("contains(")) {
                parseContainsFilter(wrapper, filter);
                return;
            }
            
            // 处理startswith函数
            if (filter.startsWith("startswith(")) {
                parseStartsWithFilter(wrapper, filter);
                return;
            }
            
            // 处理endswith函数
            if (filter.startsWith("endswith(")) {
                parseEndsWithFilter(wrapper, filter);
                return;
            }
            
            // 处理基本比较操作符
            parseBasicFilter(wrapper, filter);
            
        } catch (Exception e) {
            log.warn("Failed to parse filter condition: {}, error: {}", filter, e.getMessage());
            // 如果解析失败，不应用过滤条件，返回所有数据
        }
    }

    /**
     * 解析基本过滤条件：fieldName operator value
     */
    private void parseBasicFilter(QueryWrapper<EntityDataStorage> wrapper, String filter) {
        // 支持的操作符
        String[] operators = {"ge", "le", "gt", "lt", "ne", "eq"};
        
        for (String op : operators) {
            if (filter.contains(" " + op + " ")) {
                String[] parts = filter.split(" " + op + " ", 2);
                if (parts.length == 2) {
                    String fieldName = parts[0].trim();
                    String value = parts[1].trim();
                    
                    // 移除字符串值的引号
                    if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    applyJsonFilter(wrapper, fieldName, op, value);
                    return;
                }
            }
        }
    }

    /**
     * 解析contains函数：contains(fieldName, 'value')
     */
    private void parseContainsFilter(QueryWrapper<EntityDataStorage> wrapper, String filter) {
        // contains(name,'phone') -> name contains 'phone'
        String content = filter.substring(9, filter.length() - 1); // 移除contains()
        String[] parts = content.split(",", 2);
        if (parts.length == 2) {
            String fieldName = parts[0].trim();
            String value = parts[1].trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            applyJsonFilter(wrapper, fieldName, "contains", value);
        }
    }

    /**
     * 解析startswith函数：startswith(fieldName, 'value')
     */
    private void parseStartsWithFilter(QueryWrapper<EntityDataStorage> wrapper, String filter) {
        String content = filter.substring(11, filter.length() - 1); // 移除startswith()
        String[] parts = content.split(",", 2);
        if (parts.length == 2) {
            String fieldName = parts[0].trim();
            String value = parts[1].trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            applyJsonFilter(wrapper, fieldName, "startswith", value);
        }
    }

    /**
     * 解析endswith函数：endswith(fieldName, 'value')
     */
    private void parseEndsWithFilter(QueryWrapper<EntityDataStorage> wrapper, String filter) {
        String content = filter.substring(9, filter.length() - 1); // 移除endswith()
        String[] parts = content.split(",", 2);
        if (parts.length == 2) {
            String fieldName = parts[0].trim();
            String value = parts[1].trim();
            if (value.startsWith("'") && value.endsWith("'")) {
                value = value.substring(1, value.length() - 1);
            }
            applyJsonFilter(wrapper, fieldName, "endswith", value);
        }
    }

    /**
     * 应用JSON字段过滤条件
     * 使用MySQL的JSON函数查询JSON字段
     */
    private void applyJsonFilter(QueryWrapper<EntityDataStorage> wrapper, String fieldName, String operator, String value) {
        String jsonPath = "$.\"" + fieldName + "\"";
        
        switch (operator.toLowerCase()) {
            case "eq":
                // 尝试数字比较，如果失败则使用字符串比较
                if (isNumeric(value)) {
                    wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) = {1}", jsonPath, value);
                } else {
                    wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, {0})) = {1}", jsonPath, value);
                }
                break;
            case "ne":
                if (isNumeric(value)) {
                    wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) != {1}", jsonPath, value);
                } else {
                    wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, {0})) != {1}", jsonPath, value);
                }
                break;
            case "gt":
                wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) > {1}", jsonPath, value);
                break;
            case "ge":
                wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) >= {1}", jsonPath, value);
                break;
            case "lt":
                wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) < {1}", jsonPath, value);
                break;
            case "le":
                wrapper.apply("CAST(JSON_EXTRACT(data_json, {0}) AS DECIMAL(20,2)) <= {1}", jsonPath, value);
                break;
            case "contains":
                wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, {0})) LIKE CONCAT('%', {1}, '%')", jsonPath, value);
                break;
            case "startswith":
                wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, {0})) LIKE CONCAT({1}, '%')", jsonPath, value);
                break;
            case "endswith":
                wrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(data_json, {0})) LIKE CONCAT('%', {1})", jsonPath, value);
                break;
            default:
                log.warn("Unsupported filter operator: {}", operator);
        }
    }

    /**
     * 检查字符串是否为数字
     */
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 应用排序条件
     * 支持格式：fieldName [asc|desc], fieldName1 asc, fieldName2 desc
     */
    private void applyOrderByCondition(QueryWrapper<EntityDataStorage> wrapper, String orderby) {
        try {
            // 解析排序条件，支持多个字段排序
            String[] orderFields = orderby.split(",");
            
            for (String orderField : orderFields) {
                orderField = orderField.trim();
                
                String fieldName;
                boolean isAsc = true; // 默认升序
                
                if (orderField.endsWith(" desc")) {
                    fieldName = orderField.substring(0, orderField.length() - 5).trim();
                    isAsc = false;
                } else if (orderField.endsWith(" asc")) {
                    fieldName = orderField.substring(0, orderField.length() - 4).trim();
                    isAsc = true;
                } else {
                    fieldName = orderField;
                    isAsc = true;
                }
                
                // 使用JSON函数进行排序
                String jsonPath = "$.\"" + fieldName + "\"";
                
                if (isAsc) {
                    wrapper.orderByAsc("JSON_EXTRACT(data_json, '" + jsonPath + "')");
                } else {
                    wrapper.orderByDesc("JSON_EXTRACT(data_json, '" + jsonPath + "')");
                }
            }
            
        } catch (Exception e) {
            log.warn("Failed to parse orderby condition: {}, error: {}", orderby, e.getMessage());
            // 如果解析失败，使用默认排序
            wrapper.orderByDesc("created_at");
        }
    }

    /**
     * 生成统计数据
     * 包括记录数、数字字段的统计信息等
     */
    private Map<String, Object> generateStatsData(List<Map<String, Object>> dataList, EntityDefinitionDto entityDef) {
        Map<String, Object> stats = new HashMap<>();
        
        // 基本统计
        stats.put("totalRecords", dataList.size());
        
        if (dataList.isEmpty()) {
            return stats;
        }
        
        // 字段统计
        Map<String, Object> fieldStats = new HashMap<>();
        
        // 遍历实体字段定义，为数字字段生成统计
        for (var field : entityDef.getFields()) {
            String fieldName = field.getFieldName();
            String fieldType = field.getFieldType();
            
            // 只为数字字段生成统计
            if ("INTEGER".equals(fieldType) || "LONG".equals(fieldType) || "DECIMAL".equals(fieldType)) {
                Map<String, Object> numStats = generateNumericFieldStats(dataList, fieldName);
                if (!numStats.isEmpty()) {
                    fieldStats.put(fieldName, numStats);
                }
            }
            
            // 为字符串字段生成基本统计
            if ("STRING".equals(fieldType) || "TEXT".equals(fieldType)) {
                Map<String, Object> strStats = generateStringFieldStats(dataList, fieldName);
                if (!strStats.isEmpty()) {
                    fieldStats.put(fieldName, strStats);
                }
            }
        }
        
        stats.put("fieldStats", fieldStats);
        
        return stats;
    }

    /**
     * 生成数字字段统计
     */
    private Map<String, Object> generateNumericFieldStats(List<Map<String, Object>> dataList, String fieldName) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Double> values = dataList.stream()
                .map(data -> data.get(fieldName))
                .filter(Objects::nonNull)
                .map(value -> {
                    try {
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        } else {
                            return Double.parseDouble(value.toString());
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (values.isEmpty()) {
            return stats;
        }
        
        // 计算统计值
        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double avg = sum / values.size();
        double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        
        stats.put("count", values.size());
        stats.put("sum", sum);
        stats.put("average", avg);
        stats.put("min", min);
        stats.put("max", max);
        
        return stats;
    }

    /**
     * 生成字符串字段统计
     */
    private Map<String, Object> generateStringFieldStats(List<Map<String, Object>> dataList, String fieldName) {
        Map<String, Object> stats = new HashMap<>();
        
        List<String> values = dataList.stream()
                .map(data -> data.get(fieldName))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        
        if (values.isEmpty()) {
            return stats;
        }
        
        // 计算统计值
        long uniqueCount = values.stream().distinct().count();
        int minLength = values.stream().mapToInt(String::length).min().orElse(0);
        int maxLength = values.stream().mapToInt(String::length).max().orElse(0);
        double avgLength = values.stream().mapToInt(String::length).average().orElse(0);
        
        stats.put("count", values.size());
        stats.put("uniqueCount", uniqueCount);
        stats.put("minLength", minLength);
        stats.put("maxLength", maxLength);
        stats.put("averageLength", avgLength);
        
        return stats;
    }
}