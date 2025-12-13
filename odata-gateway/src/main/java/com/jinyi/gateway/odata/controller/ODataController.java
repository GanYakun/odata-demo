package com.jinyi.gateway.odata.controller;

import com.jinyi.gateway.odata.service.ODataService;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * OData协议控制器 - 网关版本
 * 基于应用代码提供OData服务，通过平台配置服务获取数据
 */
@RestController
@RequestMapping("")  // 移除/odata前缀，由API Gateway处理路由
@Slf4j
public class ODataController {

    @Autowired
    private ODataService odataService;

    /**
     * 获取应用的服务文档
     */
    @GetMapping("/{appCode}")
    public ResponseEntity<Map<String, Object>> getServiceDocument(@PathVariable String appCode) {
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            
            Map<String, Object> serviceDoc = new HashMap<>();
            serviceDoc.put("@odata.application", appCode);
            serviceDoc.put("@odata.context", "$metadata");
            
            List<Map<String, Object>> entitySets = new ArrayList<>();
            for (ApplicationEntity entity : entities) {
                if (entity.getActive()) {
                    Map<String, Object> entitySet = new HashMap<>();
                    entitySet.put("kind", "EntitySet");
                    entitySet.put("name", entity.getEntityName());
                    entitySet.put("title", entity.getDescription() != null ? entity.getDescription() : entity.getEntityName());
                    entitySet.put("url", entity.getEntityName());
                    entitySets.add(entitySet);
                }
            }
            
            serviceDoc.put("value", entitySets);
            
            log.info("Generated service document for application: {}", appCode);
            return ResponseEntity.ok(serviceDoc);
            
        } catch (Exception e) {
            log.error("Failed to generate service document for application: {}", appCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取实体集合数据
     */
    @GetMapping("/{appCode}/{entityName}")
    public ResponseEntity<Map<String, Object>> getEntitySet(
            @PathVariable String appCode,
            @PathVariable String entityName,
            @RequestParam Map<String, String> queryParams) {
        
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否存在于应用中
            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            boolean entityExists = entities.stream()
                    .anyMatch(e -> e.getEntityName().equals(entityName) && e.getActive());
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 获取表名
            String tableName = odataService.getTableName(appCode, entityName);
            if (tableName == null) {
                return ResponseEntity.notFound().build();
            }

            // 执行查询
            Map<String, Object> result = odataService.executeQuery(appCode, entityName, tableName, queryParams);
            
            log.info("Executed OData query for {}:{}", appCode, entityName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to execute OData query for {}:{}", appCode, entityName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取单个实体数据
     */
    @GetMapping("/{appCode}/{entityName}({key})")
    public ResponseEntity<Map<String, Object>> getEntity(
            @PathVariable String appCode,
            @PathVariable String entityName,
            @PathVariable String key,
            @RequestParam Map<String, String> queryParams) {
        
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否存在于应用中
            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            boolean entityExists = entities.stream()
                    .anyMatch(e -> e.getEntityName().equals(entityName) && e.getActive());
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 获取表名
            String tableName = odataService.getTableName(appCode, entityName);
            if (tableName == null) {
                return ResponseEntity.notFound().build();
            }

            // 添加key过滤条件
            queryParams.put("$filter", "id eq " + key);
            queryParams.put("$top", "1");

            // 执行查询
            Map<String, Object> result = odataService.executeQuery(appCode, entityName, tableName, queryParams);
            
            // 如果查询结果为空，返回404
            @SuppressWarnings("unchecked")
            List<Object> values = (List<Object>) result.get("value");
            if (values == null || values.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 返回单个实体（不是数组）
            Map<String, Object> singleResult = new HashMap<>();
            singleResult.put("@odata.application", appCode);
            singleResult.put("@odata.context", "$metadata#" + entityName + "/$entity");
            singleResult.putAll((Map<String, Object>) values.get(0));
            
            log.info("Retrieved single entity {}:{} with key: {}", appCode, entityName, key);
            return ResponseEntity.ok(singleResult);
            
        } catch (Exception e) {
            log.error("Failed to retrieve entity {}:{} with key: {}", appCode, entityName, key, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建实体数据
     */
    @PostMapping("/{appCode}/{entityName}")
    public ResponseEntity<Map<String, Object>> createEntity(
            @PathVariable String appCode,
            @PathVariable String entityName,
            @RequestBody Map<String, Object> entityData) {
        
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否存在于应用中
            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            boolean entityExists = entities.stream()
                    .anyMatch(e -> e.getEntityName().equals(entityName) && e.getActive());
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 获取表名
            String tableName = odataService.getTableName(appCode, entityName);
            if (tableName == null) {
                return ResponseEntity.notFound().build();
            }

            // 执行创建操作
            Map<String, Object> result = odataService.executeUpdate(appCode, entityName, tableName, "CREATE", entityData);
            
            log.info("Created entity in {}:{}", appCode, entityName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to create entity in {}:{}", appCode, entityName, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新实体数据
     */
    @PutMapping("/{appCode}/{entityName}({key})")
    public ResponseEntity<Map<String, Object>> updateEntity(
            @PathVariable String appCode,
            @PathVariable String entityName,
            @PathVariable String key,
            @RequestBody Map<String, Object> entityData) {
        
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否存在于应用中
            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            boolean entityExists = entities.stream()
                    .anyMatch(e -> e.getEntityName().equals(entityName) && e.getActive());
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 获取表名
            String tableName = odataService.getTableName(appCode, entityName);
            if (tableName == null) {
                return ResponseEntity.notFound().build();
            }

            // 添加key到数据中
            entityData.put("key", key);

            // 执行更新操作
            Map<String, Object> result = odataService.executeUpdate(appCode, entityName, tableName, "UPDATE", entityData);
            
            log.info("Updated entity {}:{} with key: {}", appCode, entityName, key);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Failed to update entity {}:{} with key: {}", appCode, entityName, key, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除实体数据
     */
    @DeleteMapping("/{appCode}/{entityName}({key})")
    public ResponseEntity<Void> deleteEntity(
            @PathVariable String appCode,
            @PathVariable String entityName,
            @PathVariable String key) {
        
        try {
            Application app = odataService.getApplication(appCode);
            if (app == null) {
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否存在于应用中
            List<ApplicationEntity> entities = odataService.getApplicationEntities(appCode);
            boolean entityExists = entities.stream()
                    .anyMatch(e -> e.getEntityName().equals(entityName) && e.getActive());
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 获取表名
            String tableName = odataService.getTableName(appCode, entityName);
            if (tableName == null) {
                return ResponseEntity.notFound().build();
            }

            // 执行删除操作
            Map<String, Object> deleteData = new HashMap<>();
            deleteData.put("key", key);
            Map<String, Object> result = odataService.executeUpdate(appCode, entityName, tableName, "DELETE", deleteData);
            
            // 检查删除结果
            Boolean success = (Boolean) result.get("success");
            if (Boolean.TRUE.equals(success)) {
                log.info("Deleted entity {}:{} with key: {}", appCode, entityName, key);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Failed to delete entity {}:{} with key: {}", appCode, entityName, key, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}