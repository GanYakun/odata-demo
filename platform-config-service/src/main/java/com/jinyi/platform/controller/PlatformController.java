package com.jinyi.platform.controller;

import com.jinyi.platform.service.ApplicationService;
import com.jinyi.platform.service.DynamicEntityService;
import com.jinyi.platform.service.QueryExecutionService;
import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDefinition;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 平台配置控制器
 */
@RestController
@RequestMapping("/platform")
@Slf4j
public class PlatformController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DynamicEntityService dynamicEntityService;

    @Autowired
    private QueryExecutionService queryExecutionService;

    /**
     * 创建应用
     */
    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<Application>> createApplication(@RequestBody Application application) {
        try {
            Application created = applicationService.createApplication(application);
            return ResponseEntity.ok(ApiResponse.success("Application created successfully", created));
        } catch (Exception e) {
            log.error("Failed to create application", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取所有应用
     */
    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<Application>>> getAllApplications() {
        try {
            List<Application> applications = applicationService.getAllApplications();
            return ResponseEntity.ok(ApiResponse.success(applications));
        } catch (Exception e) {
            log.error("Failed to get applications", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据ID获取应用
     */
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApiResponse<Application>> getApplicationById(@PathVariable Long id) {
        try {
            Optional<Application> application = applicationService.getApplicationById(id);
            if (application.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(application.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to get application by id: {}", id, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据应用代码获取应用
     */
    @GetMapping("/applications/code/{appCode}")
    public ResponseEntity<ApiResponse<Application>> getApplicationByCode(@PathVariable String appCode) {
        try {
            Optional<Application> application = applicationService.getApplicationByCode(appCode);
            if (application.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(application.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Failed to get application by code: {}", appCode, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新应用
     */
    @PutMapping("/applications/{id}")
    public ResponseEntity<ApiResponse<Application>> updateApplication(@PathVariable Long id, @RequestBody Application application) {
        try {
            application.setId(id);
            Application updated = applicationService.updateApplication(application);
            return ResponseEntity.ok(ApiResponse.success("Application updated successfully", updated));
        } catch (Exception e) {
            log.error("Failed to update application: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除应用
     */
    @DeleteMapping("/applications/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);
            return ResponseEntity.ok(ApiResponse.success("Application deleted successfully", null));
        } catch (Exception e) {
            log.error("Failed to delete application: {}", id, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取应用下的所有实体
     */
    @GetMapping("/applications/{id}/entities")
    public ResponseEntity<ApiResponse<List<ApplicationEntity>>> getApplicationEntities(@PathVariable Long id) {
        try {
            List<ApplicationEntity> entities = applicationService.getApplicationEntities(id);
            return ResponseEntity.ok(ApiResponse.success(entities));
        } catch (Exception e) {
            log.error("Failed to get application entities: {}", id, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 根据应用代码获取应用下的所有实体
     */
    @GetMapping("/applications/code/{appCode}/entities")
    public ResponseEntity<ApiResponse<List<ApplicationEntity>>> getApplicationEntitiesByCode(@PathVariable String appCode) {
        try {
            List<ApplicationEntity> entities = applicationService.getApplicationEntitiesByCode(appCode);
            return ResponseEntity.ok(ApiResponse.success(entities));
        } catch (Exception e) {
            log.error("Failed to get application entities by code: {}", appCode, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 在指定应用中注册新的动态实体
     */
    @PostMapping("/applications/{appId}/dynamic-entities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerDynamicEntity(
            @PathVariable Long appId,
            @RequestBody EntityDefinition entityDef) {
        
        try {
            // 验证应用是否存在
            if (!applicationService.existsById(appId)) {
                return ResponseEntity.status(404).body(ApiResponse.error("Application not found: " + appId));
            }

            // 注册动态实体
            String result = dynamicEntityService.registerEntity(entityDef, appId);
            
            // 将实体关联到应用
            ApplicationEntity appEntity = applicationService.addEntityToApplication(
                    appId, entityDef.getEntityName(), entityDef.getTableName(), 
                    entityDef.getDescription(), true);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("applicationId", appId);
            responseData.put("entityName", entityDef.getEntityName());
            responseData.put("tableName", entityDef.getTableName());
            responseData.put("applicationEntity", appEntity);
            
            log.info("Dynamic entity registered successfully in application {}: {}", appId, entityDef.getEntityName());
            return ResponseEntity.ok(ApiResponse.success(result, responseData));
            
        } catch (Exception e) {
            log.error("Failed to register dynamic entity in application {}: {}", appId, entityDef.getEntityName(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取应用中的动态实体定义
     */
    @GetMapping("/applications/{appId}/dynamic-entities/{entityName}")
    public ResponseEntity<ApiResponse<EntityDefinition>> getDynamicEntityDefinition(
            @PathVariable Long appId,
            @PathVariable String entityName) {
        
        try {
            // 验证应用是否存在
            if (!applicationService.existsById(appId)) {
                return ResponseEntity.status(404).body(ApiResponse.error("Application not found: " + appId));
            }

            // 验证实体是否属于该应用
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            boolean entityExists = appEntities.stream()
                    .anyMatch(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic());
            
            if (!entityExists) {
                return ResponseEntity.status(404).body(ApiResponse.error("Dynamic entity not found in application: " + entityName));
            }

            // 获取动态实体定义
            EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(entityName);
            
            if (entityDef == null) {
                return ResponseEntity.status(404).body(ApiResponse.error("Entity definition not found: " + entityName));
            }
            
            return ResponseEntity.ok(ApiResponse.success(entityDef));
            
        } catch (Exception e) {
            log.error("Failed to get dynamic entity definition in application {}: {}", appId, entityName, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 检查动态实体是否存在
     */
    @GetMapping("/applications/{appId}/dynamic-entities/{entityName}/exists")
    public ResponseEntity<ApiResponse<Boolean>> isDynamicEntityExists(@PathVariable Long appId, @PathVariable String entityName) {
        try {
            // 验证应用是否存在
            if (!applicationService.existsById(appId)) {
                return ResponseEntity.status(404).body(ApiResponse.error("Application not found: " + appId));
            }

            // 检查实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            boolean exists = appEntities.stream()
                    .anyMatch(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic());
            
            return ResponseEntity.ok(ApiResponse.success(exists));
            
        } catch (Exception e) {
            log.error("Failed to check dynamic entity existence in application {}: {}", appId, entityName, e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 从应用中删除动态实体
     */
    @DeleteMapping("/applications/{appId}/dynamic-entities/{entityName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteDynamicEntity(
            @PathVariable Long appId,
            @PathVariable String entityName,
            @RequestParam(defaultValue = "false") boolean dropTable) {
        
        try {
            // 验证应用是否存在
            if (!applicationService.existsById(appId)) {
                return ResponseEntity.status(404).body(ApiResponse.error("Application not found: " + appId));
            }

            // 验证实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            boolean entityExists = appEntities.stream()
                    .anyMatch(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic());
            
            if (!entityExists) {
                return ResponseEntity.status(404).body(ApiResponse.error("Dynamic entity not found in application: " + entityName));
            }

            // 从动态实体服务中注销实体
            String result = dynamicEntityService.unregisterEntity(entityName, dropTable);
            
            // 从应用中移除实体关联
            applicationService.removeEntityFromApplication(appId, entityName);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("applicationId", appId);
            responseData.put("entityName", entityName);
            responseData.put("tableDropped", dropTable);
            
            log.info("Dynamic entity unregistered successfully from application {}: {}", appId, entityName);
            return ResponseEntity.ok(ApiResponse.success(result, responseData));
            
        } catch (Exception e) {
            log.error("Failed to unregister dynamic entity from application {}: {}", appId, entityName, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 执行数据库查询
     */
    @PostMapping("/query/execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeQuery(@RequestBody Map<String, Object> queryRequest) {
        try {
            String appCode = (String) queryRequest.get("appCode");
            String entityName = (String) queryRequest.get("entityName");
            String tableName = (String) queryRequest.get("tableName");
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams = (Map<String, String>) queryRequest.get("queryParams");

            Map<String, Object> result = queryExecutionService.executeQuery(appCode, entityName, tableName, queryParams);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Failed to execute query", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 执行数据库更新
     */
    @PostMapping("/query/update")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeUpdate(@RequestBody Map<String, Object> updateRequest) {
        try {
            String appCode = (String) updateRequest.get("appCode");
            String entityName = (String) updateRequest.get("entityName");
            String tableName = (String) updateRequest.get("tableName");
            String operation = (String) updateRequest.get("operation");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) updateRequest.get("data");

            Map<String, Object> result = queryExecutionService.executeUpdate(appCode, entityName, tableName, operation, data);
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            log.error("Failed to execute update", e);
            return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
        }
    }
}