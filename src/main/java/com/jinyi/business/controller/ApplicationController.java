package com.jinyi.business.controller;

import com.jinyi.business.entity.Application;
import com.jinyi.business.entity.ApplicationEntity;
import com.jinyi.platform.service.ApplicationService;
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
 * 应用管理控制器
 * 提供应用的创建、管理和实体关联功能
 */
@RestController
@RequestMapping("/api/applications")
@Slf4j
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建新应用
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createApplication(@RequestBody Application application) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Application created = applicationService.createApplication(application);
            
            response.put("success", true);
            response.put("message", "Application created successfully");
            response.put("application", created);
            
            log.info("Application created successfully: {}", created.getAppCode());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to create application: {}", application.getAppCode(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取所有应用
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllApplications() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Application> applications = applicationService.getAllApplications();
            
            response.put("success", true);
            response.put("count", applications.size());
            response.put("applications", applications);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get all applications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取应用
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getApplicationById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Application> application = applicationService.getApplicationById(id);
            
            if (application.isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("application", application.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get application by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据应用代码获取应用
     */
    @GetMapping(value = "/code/{appCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getApplicationByCode(@PathVariable String appCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Application> application = applicationService.getApplicationByCode(appCode);
            
            if (application.isEmpty()) {
                response.put("success", false);
                response.put("message", "Application not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("application", application.get());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get application by code: {}", appCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新应用
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateApplication(@PathVariable Long id, @RequestBody Application application) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            application.setId(id);
            Application updated = applicationService.updateApplication(application);
            
            response.put("success", true);
            response.put("message", "Application updated successfully");
            response.put("application", updated);
            
            log.info("Application updated successfully: {}", updated.getAppCode());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to update application: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 删除应用
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteApplication(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            applicationService.deleteApplication(id);
            
            response.put("success", true);
            response.put("message", "Application deleted successfully");
            
            log.info("Application deleted successfully: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to delete application: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 为应用添加实体
     */
    @PostMapping(value = "/{id}/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addEntityToApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> entityInfo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String entityName = (String) entityInfo.get("entityName");
            String tableName = (String) entityInfo.get("tableName");
            String description = (String) entityInfo.get("description");
            Boolean isDynamic = (Boolean) entityInfo.getOrDefault("isDynamic", false);
            
            ApplicationEntity appEntity = applicationService.addEntityToApplication(
                    id, entityName, tableName, description, isDynamic);
            
            response.put("success", true);
            response.put("message", "Entity added to application successfully");
            response.put("entity", appEntity);
            
            log.info("Entity {} added to application {} successfully", entityName, id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to add entity to application: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * 获取应用下的所有实体
     */
    @GetMapping(value = "/{id}/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getApplicationEntities(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ApplicationEntity> entities = applicationService.getApplicationEntities(id);
            
            response.put("success", true);
            response.put("count", entities.size());
            response.put("entities", entities);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get application entities: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据应用代码获取应用下的所有实体
     */
    @GetMapping(value = "/code/{appCode}/entities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getApplicationEntitiesByCode(@PathVariable String appCode) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ApplicationEntity> entities = applicationService.getApplicationEntitiesByCode(appCode);
            
            response.put("success", true);
            response.put("count", entities.size());
            response.put("entities", entities);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to get application entities by code: {}", appCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 从应用中移除实体
     */
    @DeleteMapping(value = "/{id}/entities/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> removeEntityFromApplication(
            @PathVariable Long id,
            @PathVariable String entityName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            applicationService.removeEntityFromApplication(id, entityName);
            
            response.put("success", true);
            response.put("message", "Entity removed from application successfully");
            
            log.info("Entity {} removed from application {} successfully", entityName, id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            
            log.error("Failed to remove entity from application: {} - {}", id, entityName, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}