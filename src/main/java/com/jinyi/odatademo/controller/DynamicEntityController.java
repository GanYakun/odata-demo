package com.jinyi.odatademo.controller;

import com.jinyi.odatademo.dto.EntityDefinition;
import com.jinyi.odatademo.service.DynamicEntityRegistrationService;
import com.jinyi.odatademo.service.EntityRegistryService;
import com.jinyi.odatademo.service.EntityFileGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/entities")
@Slf4j
public class DynamicEntityController {

    @Autowired
    private DynamicEntityRegistrationService dynamicEntityService;

    @Autowired
    private EntityRegistryService entityRegistryService;

    @Autowired
    private EntityFileGeneratorService entityFileGeneratorService;

    /**
     * 注册新的动态实体
     */
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerEntity(
            @RequestBody EntityDefinition entityDef,
            @RequestParam(defaultValue = "true") boolean generateJavaFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = dynamicEntityService.registerEntity(entityDef, generateJavaFile);
            
            response.put("success", true);
            response.put("message", result);
            response.put("entityName", entityDef.getEntityName());
            response.put("tableName", entityDef.getTableName());
            response.put("javaFileGenerated", generateJavaFile);
            
            if (generateJavaFile) {
                response.put("javaFilePath", entityFileGeneratorService.getEntityFilePath(entityDef.getEntityName()));
            }
            
            log.info("Entity registered successfully: {}", entityDef.getEntityName());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("entityName", entityDef.getEntityName());
            
            log.error("Failed to register entity: {}", entityDef.getEntityName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取实体定义
     */
    @GetMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getEntityDefinition(@PathVariable String entityName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(entityName);
            
            if (entityDef == null) {
                response.put("success", false);
                response.put("message", "Entity not found: " + entityName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("entity", entityDef);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get entity definition: {}", entityName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取所有动态实体
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllDynamicEntities() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, EntityDefinition> entities = dynamicEntityService.getAllDynamicEntities();
            
            response.put("success", true);
            response.put("count", entities.size());
            response.put("entities", entities);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get all dynamic entities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除动态实体
     */
    @DeleteMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> unregisterEntity(
            @PathVariable String entityName,
            @RequestParam(defaultValue = "false") boolean dropTable,
            @RequestParam(defaultValue = "true") boolean deleteJavaFile) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = dynamicEntityService.unregisterEntity(entityName, dropTable, deleteJavaFile);
            
            response.put("success", true);
            response.put("message", result);
            response.put("entityName", entityName);
            response.put("tableDropped", dropTable);
            response.put("javaFileDeleted", deleteJavaFile);
            
            log.info("Entity unregistered successfully: {}", entityName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("entityName", entityName);
            
            log.error("Failed to unregister entity: {}", entityName, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取所有实体（包括静态和动态）
     */
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllEntities() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> allEntities = new HashMap<>();
            
            // 静态实体
            Map<String, Object> staticEntities = new HashMap<>();
            entityRegistryService.getEntityRegistry().forEach((name, clazz) -> {
                Map<String, Object> entityInfo = new HashMap<>();
                entityInfo.put("type", "static");
                entityInfo.put("className", clazz.getName());
                entityInfo.put("tableName", entityRegistryService.getTableName(name));
                staticEntities.put(name, entityInfo);
            });
            
            // 动态实体
            Map<String, Object> dynamicEntities = new HashMap<>();
            dynamicEntityService.getAllDynamicEntities().forEach((name, def) -> {
                Map<String, Object> entityInfo = new HashMap<>();
                entityInfo.put("type", "dynamic");
                entityInfo.put("tableName", def.getTableName());
                entityInfo.put("description", def.getDescription());
                entityInfo.put("fieldCount", def.getFields().size());
                dynamicEntities.put(name, entityInfo);
            });
            
            allEntities.put("static", staticEntities);
            allEntities.put("dynamic", dynamicEntities);
            
            response.put("success", true);
            response.put("entities", allEntities);
            response.put("totalCount", staticEntities.size() + dynamicEntities.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get all entities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证实体定义
     */
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateEntityDefinition(@RequestBody EntityDefinition entityDef) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 这里可以添加验证逻辑，但不实际创建实体
            if (entityDef.getEntityName() == null || entityDef.getEntityName().trim().isEmpty()) {
                throw new RuntimeException("Entity name is required");
            }
            
            if (entityDef.getFields() == null || entityDef.getFields().isEmpty()) {
                throw new RuntimeException("At least one field is required");
            }
            
            boolean hasKey = entityDef.getFields().stream().anyMatch(EntityDefinition.FieldDefinition::isKey);
            if (!hasKey) {
                throw new RuntimeException("At least one key field is required");
            }
            
            response.put("success", true);
            response.put("message", "Entity definition is valid");
            response.put("entityName", entityDef.getEntityName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 预览实体Java文件内容（不生成文件）
     */
    @PostMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> previewEntityFile(@RequestBody EntityDefinition entityDef) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String javaCode = entityFileGeneratorService.previewEntityFile(entityDef);
            
            response.put("success", true);
            response.put("entityName", entityDef.getEntityName());
            response.put("javaCode", javaCode);
            response.put("filePath", entityFileGeneratorService.getEntityFilePath(entityDef.getEntityName()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to preview entity file: {}", entityDef.getEntityName(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 为已注册的实体生成Java文件
     */
    @PostMapping(value = "/{entityName}/generate-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> generateEntityFile(@PathVariable String entityName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            EntityDefinition entityDef = dynamicEntityService.getEntityDefinition(entityName);
            
            if (entityDef == null) {
                response.put("success", false);
                response.put("message", "Entity not found: " + entityName);
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
            response.put("entityName", entityName);
            response.put("filePath", filePath);
            
            log.info("Generated Java file for entity: {}", entityName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to generate Java file for entity: {}", entityName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除实体的Java文件
     */
    @DeleteMapping(value = "/{entityName}/file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteEntityFile(@PathVariable String entityName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = entityFileGeneratorService.deleteEntityFile(entityName);
            
            response.put("success", true);
            response.put("message", deleted ? "Java file deleted successfully" : "Java file did not exist");
            response.put("entityName", entityName);
            response.put("fileDeleted", deleted);
            
            log.info("Deleted Java file for entity: {}", entityName);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to delete Java file for entity: {}", entityName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查实体Java文件是否存在
     */
    @GetMapping(value = "/{entityName}/file-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkEntityFileStatus(@PathVariable String entityName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean fileExists = entityFileGeneratorService.entityFileExists(entityName);
            String filePath = entityFileGeneratorService.getEntityFilePath(entityName);
            
            response.put("success", true);
            response.put("entityName", entityName);
            response.put("fileExists", fileExists);
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to check file status for entity: {}", entityName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}