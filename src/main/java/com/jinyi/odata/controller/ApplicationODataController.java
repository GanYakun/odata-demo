package com.jinyi.odata.controller;

import com.jinyi.business.entity.ApplicationEntity;
import com.jinyi.business.service.ApplicationService;
import com.jinyi.odata.service.ODataQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用级别的OData控制器
 * 为每个应用提供独立的OData服务和元数据
 */
@RestController
@RequestMapping("/odata/{appCode}")
@Slf4j
public class ApplicationODataController {

    @Autowired
    private ODataQueryService odataQueryService;

    @Autowired
    private ApplicationService applicationService;

    /**
     * 查询应用下的实体集合
     */
    @GetMapping(value = "/{entitySet}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> queryEntitySet(
            @PathVariable String appCode,
            @PathVariable String entitySet,
            HttpServletRequest request) {
        
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationByCode(appCode).isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", Map.of(
                    "code", "APPLICATION_NOT_FOUND",
                    "message", "Application not found: " + appCode
                ));
                return ResponseEntity.notFound().build();
            }

            // 验证实体是否属于该应用
            List<ApplicationEntity> appEntities = applicationService.getApplicationEntitiesByCode(appCode);
            boolean entityExists = appEntities.stream()
                    .anyMatch(ae -> ae.getEntityName().equals(entitySet));
            
            if (!entityExists) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", Map.of(
                    "code", "ENTITY_NOT_FOUND",
                    "message", "Entity not found in application " + appCode + ": " + entitySet
                ));
                return ResponseEntity.notFound().build();
            }

            // 提取查询参数
            Map<String, String> queryParams = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    queryParams.put(key, values[0]);
                }
            });

            log.info("OData query for application: {} entity: {} with params: {}", appCode, entitySet, queryParams);

            // 执行查询
            ODataQueryService.QueryResult result = odataQueryService.queryEntities(entitySet, queryParams);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("@odata.context", "$metadata#" + entitySet);
            response.put("@odata.application", appCode);
            response.put("value", result.getData());
            
            if (result.getCount() > 0) {
                response.put("@odata.count", result.getCount());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to query entity set: {} in application: {}", entitySet, appCode, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", Map.of(
                "code", "QUERY_ERROR",
                "message", e.getMessage()
            ));
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取应用下的单个实体
     */
    @GetMapping(value = "/{entitySet}({key})", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getEntity(
            @PathVariable String appCode,
            @PathVariable String entitySet,
            @PathVariable String key) {
        
        try {
            // 验证应用和实体
            if (applicationService.getApplicationByCode(appCode).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<ApplicationEntity> appEntities = applicationService.getApplicationEntitiesByCode(appCode);
            boolean entityExists = appEntities.stream()
                    .anyMatch(ae -> ae.getEntityName().equals(entitySet));
            
            if (!entityExists) {
                return ResponseEntity.notFound().build();
            }

            // 构建过滤条件查询单个实体
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("$filter", "id eq " + key);
            queryParams.put("$top", "1");

            log.info("OData get entity: {} with key: {} in application: {}", entitySet, key, appCode);

            ODataQueryService.QueryResult result = odataQueryService.queryEntities(entitySet, queryParams);

            if (result.getData().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("@odata.context", "$metadata#" + entitySet + "/$entity");
            response.put("@odata.application", appCode);
            response.putAll(result.getData().get(0));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get entity: {} with key: {} in application: {}", entitySet, key, appCode, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", Map.of(
                "code", "QUERY_ERROR",
                "message", e.getMessage()
            ));
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取应用的元数据
     */
    @GetMapping(value = "/$metadata", produces = "application/xml")
    public ResponseEntity<String> getApplicationMetadata(@PathVariable String appCode) {
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationByCode(appCode).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 获取应用下的所有实体
            List<ApplicationEntity> entities = applicationService.getApplicationEntitiesByCode(appCode);
            
            // 生成应用特定的元数据
            String metadata = generateApplicationMetadata(appCode, entities);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(metadata);
                
        } catch (Exception e) {
            log.error("Failed to generate metadata for application: {}", appCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取应用服务文档
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getApplicationServiceDocument(@PathVariable String appCode) {
        try {
            // 验证应用是否存在
            if (applicationService.getApplicationByCode(appCode).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 获取应用下的所有实体
            List<ApplicationEntity> entities = applicationService.getApplicationEntitiesByCode(appCode);
            
            // 构建服务文档
            Map<String, Object> serviceDocument = new HashMap<>();
            serviceDocument.put("@odata.context", "$metadata");
            serviceDocument.put("@odata.application", appCode);
            
            List<Map<String, Object>> entitySets = entities.stream()
                .map(entity -> {
                    Map<String, Object> entitySet = new HashMap<>();
                    entitySet.put("name", entity.getEntityName());
                    entitySet.put("url", entity.getEntityName());
                    entitySet.put("title", entity.getDescription());
                    entitySet.put("kind", "EntitySet");
                    return entitySet;
                })
                .collect(Collectors.toList());
            
            serviceDocument.put("value", entitySets);
            
            return ResponseEntity.ok(serviceDocument);
            
        } catch (Exception e) {
            log.error("Failed to generate service document for application: {}", appCode, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", Map.of(
                "code", "SERVICE_ERROR",
                "message", e.getMessage()
            ));
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 生成应用特定的元数据XML
     */
    private String generateApplicationMetadata(String appCode, List<ApplicationEntity> entities) {
        StringBuilder metadata = new StringBuilder();
        
        metadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        metadata.append("<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\">\n");
        metadata.append("  <edmx:DataServices>\n");
        metadata.append("    <Schema xmlns=\"http://docs.oasis-open.org/odata/ns/edm\" Namespace=\"").append(appCode).append("\">\n");
        
        // 实体容器
        metadata.append("      <EntityContainer Name=\"Container\">\n");
        for (ApplicationEntity entity : entities) {
            metadata.append("        <EntitySet Name=\"").append(entity.getEntityName())
                    .append("\" EntityType=\"").append(appCode).append(".").append(entity.getEntityName()).append("\"/>\n");
        }
        metadata.append("      </EntityContainer>\n");
        
        // 实体类型定义（简化版本）
        for (ApplicationEntity entity : entities) {
            metadata.append("      <EntityType Name=\"").append(entity.getEntityName()).append("\">\n");
            metadata.append("        <Key>\n");
            metadata.append("          <PropertyRef Name=\"id\"/>\n");
            metadata.append("        </Key>\n");
            metadata.append("        <Property Name=\"id\" Type=\"Edm.Int64\" Nullable=\"false\"/>\n");
            // 这里可以根据实际需要添加更多字段定义
            metadata.append("      </EntityType>\n");
        }
        
        metadata.append("    </Schema>\n");
        metadata.append("  </edmx:DataServices>\n");
        metadata.append("</edmx:Edmx>");
        
        return metadata.toString();
    }
}