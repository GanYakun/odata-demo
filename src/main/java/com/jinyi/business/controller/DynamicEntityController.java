package com.jinyi.business.controller;

import com.jinyi.business.entity.ApplicationEntity;
import com.jinyi.business.service.ApplicationService;
import com.jinyi.odata.dynamic.EntityDefinition;
import com.jinyi.odata.dynamic.DynamicEntityRegistrationService;
import com.jinyi.odata.dynamic.EntityFileGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基于应用的动态实体管理控制器
 * 所有实体操作都必须在应用上下文中完成
 */
@RestController
@RequestMapping("/api/applications/{appId}/dynamic-entities")
@Slf4j
public class DynamicEntityController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DynamicEntityRegistrationService dynamicEntityService;

    @Autowired
    private EntityFileGeneratorService entityFileGeneratorService;

    /**
     * 在指定应用中注册新的动态实体
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerEntityInApplication(
            @PathVariable Long appId,
            @RequestBody EntityDefinition entityDef,
            @RequestParam(defaultValue = "true") boolean generateJavaFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 注册动态实体
            String result = dynamicEntityService.registerEntity(entityDef, generateJavaFile, appId);
            
            // 将实体关联到应用
            ApplicationEntity appEntity = applicationService.addEntityToApplication(
                    appId, entityDef.getEntityName(), entityDef.getTableName(), 
                    entityDef.getDescription(), true);
            
            response.put("success", true);
            response.put("message", result);
            response.put("applicationId", appId);
            response.put("entityName", entityDef.getEntityName());
            response.put("tableName", entityDef.getTableName());
            response.put("javaFileGenerated", generateJavaFile);
            response.put("applicationEntity", appEntity);
            
            if (generateJavaFile) {
                response.put("javaFilePath", entityFileGeneratorService.getEntityFilePath(entityDef.getEntityName()));
            }
            
            log.info("Dynamic entity registered successfully in application {}: {}", appId, entityDef.getEntityName());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("applicationId", appId);
            response.put("entityName", entityDef.getEntityName());
            
            log.error("Failed to register dynamic entity in application {}: {}", appId, entityDef.getEntityName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取应用中的动态实体定义
     */
    @GetMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getEntityDefinitionInApplication(
            @PathVariable Long appId,
            @PathVariable String entityName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 验证实体是否属于该应用
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            Optional<ApplicationEntity> appEntity = appEntities.stream()
                    .filter(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic())
                    .findFirst();
            
            if (appEntity.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dynamic entity not found in application: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 获取动态实体定义
            EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(entityName);
            
            if (entityDef == null) {
                response.put("success", false);
                response.put("message", "Entity definition not found: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("applicationId", appId);
            response.put("entity", entityDef);
            response.put("applicationEntity", appEntity.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get dynamic entity definition in application {}: {}", appId, entityName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取应用中的所有动态实体
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllDynamicEntitiesInApplication(@PathVariable Long appId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 获取应用下的所有动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            List<ApplicationEntity> dynamicEntities = appEntities.stream()
                    .filter(ApplicationEntity::getIsDynamic)
                    .toList();

            // 获取详细的实体定义
            Map<String, Object> entitiesWithDefinitions = new HashMap<>();
            for (ApplicationEntity appEntity : dynamicEntities) {
                EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(appEntity.getEntityName());
                Map<String, Object> entityInfo = new HashMap<>();
                entityInfo.put("applicationEntity", appEntity);
                entityInfo.put("entityDefinition", entityDef);
                entitiesWithDefinitions.put(appEntity.getEntityName(), entityInfo);
            }
            
            response.put("success", true);
            response.put("applicationId", appId);
            response.put("count", dynamicEntities.size());
            response.put("entities", entitiesWithDefinitions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get dynamic entities in application: {}", appId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 从应用中删除动态实体
     */
    @DeleteMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> unregisterEntityFromApplication(
            @PathVariable Long appId,
            @PathVariable String entityName,
            @RequestParam(defaultValue = "false") boolean dropTable,
            @RequestParam(defaultValue = "true") boolean deleteJavaFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 验证实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            Optional<ApplicationEntity> appEntity = appEntities.stream()
                    .filter(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic())
                    .findFirst();
            
            if (appEntity.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dynamic entity not found in application: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 从动态实体服务中注销实体
            String result = dynamicEntityService.unregisterEntity(entityName, dropTable, deleteJavaFile);
            
            // 从应用中移除实体关联
            applicationService.removeEntityFromApplication(appId, entityName);
            
            response.put("success", true);
            response.put("message", result);
            response.put("applicationId", appId);
            response.put("entityName", entityName);
            response.put("tableDropped", dropTable);
            response.put("javaFileDeleted", deleteJavaFile);
            
            log.info("Dynamic entity unregistered successfully from application {}: {}", appId, entityName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("applicationId", appId);
            response.put("entityName", entityName);
            
            log.error("Failed to unregister dynamic entity from application {}: {}", appId, entityName, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 预览应用中的动态实体Java文件内容（不生成文件）
     */
    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> previewEntityFileInApplication(
            @PathVariable Long appId,
            @RequestBody EntityDefinition entityDef) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String javaCode = entityFileGeneratorService.previewEntityFile(entityDef);
            
            response.put("success", true);
            response.put("applicationId", appId);
            response.put("entityName", entityDef.getEntityName());
            response.put("javaCode", javaCode);
            response.put("filePath", entityFileGeneratorService.getEntityFilePath(entityDef.getEntityName()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to preview entity file in application {}: {}", appId, entityDef.getEntityName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 为应用中的动态实体生成Java文件
     */
    @PostMapping(value = "/{entityName}/generate-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> generateEntityFileInApplication(
            @PathVariable Long appId,
            @PathVariable String entityName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 验证实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            Optional<ApplicationEntity> appEntity = appEntities.stream()
                    .filter(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic())
                    .findFirst();
            
            if (appEntity.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dynamic entity not found in application: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 获取实体定义
            EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(entityName);
            if (entityDef == null) {
                response.put("success", false);
                response.put("message", "Entity definition not found: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 检查文件是否已存在
            boolean fileExists = entityFileGeneratorService.entityFileExists(entityName);
            if (fileExists) {
                response.put("success", false);
                response.put("message", "Java file already exists for entity: " + entityName);
                response.put("filePath", entityFileGeneratorService.getEntityFilePath(entityName));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            String filePath = entityFileGeneratorService.generateEntityFile(entityDef);
            
            response.put("success", true);
            response.put("message", "Java file generated successfully");
            response.put("applicationId", appId);
            response.put("entityName", entityName);
            response.put("filePath", filePath);
            
            log.info("Generated Java file for dynamic entity {} in application {}", entityName, appId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to generate Java file for entity {} in application {}: {}", entityName, appId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除应用中动态实体的Java文件
     */
    @DeleteMapping(value = "/{entityName}/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteEntityFileInApplication(
            @PathVariable Long appId,
            @PathVariable String entityName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 验证实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            Optional<ApplicationEntity> appEntity = appEntities.stream()
                    .filter(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic())
                    .findFirst();
            
            if (appEntity.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dynamic entity not found in application: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            boolean deleted = entityFileGeneratorService.deleteEntityFile(entityName);
            
            response.put("success", true);
            response.put("message", deleted ? "Java file deleted successfully" : "Java file did not exist");
            response.put("applicationId", appId);
            response.put("entityName", entityName);
            response.put("fileDeleted", deleted);
            
            log.info("Deleted Java file for dynamic entity {} in application {}", entityName, appId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to delete Java file for entity {} in application {}: {}", entityName, appId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查应用中动态实体Java文件状态
     */
    @GetMapping(value = "/{entityName}/file-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkEntityFileStatusInApplication(
            @PathVariable Long appId,
            @PathVariable String entityName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationById(appId).isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found: " + appId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // 验证实体是否属于该应用且为动态实体
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntities(appId);
            Optional<ApplicationEntity> appEntity = appEntities.stream()
                    .filter(ae -> ae.getEntityName().equals(entityName) && ae.getIsDynamic())
                    .findFirst();
            
            if (appEntity.isEmpty()) {
                response.put("success", false);
                response.put("message", "Dynamic entity not found in application: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            boolean fileExists = entityFileGeneratorService.entityFileExists(entityName);
            String filePath = entityFileGeneratorService.getEntityFilePath(entityName);
            
            response.put("success", true);
            response.put("applicationId", appId);
            response.put("entityName", entityName);
            response.put("fileExists", fileExists);
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to check file status for entity {} in application {}: {}", entityName, appId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}