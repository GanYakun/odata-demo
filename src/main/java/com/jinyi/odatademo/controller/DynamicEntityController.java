package com.jinyi.odatademo.controller;

import com.jinyi.odatademo.dto.EntityDefinition;
import com.jinyi.odatademo.service.DynamicEntityRegistrationService;
import com.jinyi.odatademo.service.EntityRegistryService;
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

    /**
     * 注册新的动态实体
     */
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerEntity(@RequestBody EntityDefinition entityDef) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = dynamicEntityService.registerEntity(entityDef);
            
            response.put("success", true);
            response.put("message", result);
            response.put("entityName", entityDef.getEntityName());
            response.put("tableName", entityDef.getTableName());
            
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
            @RequestParam(defaultValue = "false") boolean dropTable) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String result = dynamicEntityService.unregisterEntity(entityName, dropTable);
            
            response.put("success", true);
            response.put("message", result);
            response.put("entityName", entityName);
            response.put("tableDropped", dropTable);
            
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
}