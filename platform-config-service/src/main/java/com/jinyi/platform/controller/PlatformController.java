package com.jinyi.platform.controller;

import com.jinyi.platform.service.DynamicEntityService;
import com.jinyi.platform.service.QueryExecutionService;
import com.jinyi.platform.service.ApplicationService;
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