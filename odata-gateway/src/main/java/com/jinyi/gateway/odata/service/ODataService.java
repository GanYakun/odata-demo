package com.jinyi.gateway.odata.service;

import com.jinyi.common.client.PlatformConfigClient;
import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDefinition;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OData服务 - 网关版本
 * 通过调用平台配置服务获取实体信息和执行查询
 */
@Service
@Slf4j
public class ODataService {

    @Autowired
    private PlatformConfigClient platformConfigClient;

    /**
     * 获取应用信息
     */
    public Application getApplication(String appCode) {
        try {
            ApiResponse<Application> response = platformConfigClient.getApplicationByCode(appCode);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to get application: {}", appCode, e);
        }
        return null;
    }

    /**
     * 获取应用下的所有实体
     */
    public List<ApplicationEntity> getApplicationEntities(String appCode) {
        try {
            ApiResponse<List<ApplicationEntity>> response = platformConfigClient.getApplicationEntitiesByCode(appCode);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to get application entities: {}", appCode, e);
        }
        return Collections.emptyList();
    }

    /**
     * 获取动态实体定义
     */
    public EntityDefinition getDynamicEntityDefinition(String appCode, String entityName) {
        try {
            Application app = getApplication(appCode);
            if (app != null) {
                ApiResponse<EntityDefinition> response = platformConfigClient.getDynamicEntityDefinition(app.getId(), entityName);
                if (response.isSuccess() && response.getData() != null) {
                    return response.getData();
                }
            }
        } catch (Exception e) {
            log.error("Failed to get dynamic entity definition: {}:{}", appCode, entityName, e);
        }
        return null;
    }

    /**
     * 检查动态实体是否存在
     */
    public boolean isDynamicEntity(String appCode, String entityName) {
        try {
            Application app = getApplication(appCode);
            if (app != null) {
                ApiResponse<Boolean> response = platformConfigClient.isDynamicEntityExists(app.getId(), entityName);
                return response.isSuccess() && Boolean.TRUE.equals(response.getData());
            }
        } catch (Exception e) {
            log.error("Failed to check dynamic entity existence: {}:{}", appCode, entityName, e);
        }
        return false;
    }

    /**
     * 获取实体的表名
     */
    public String getTableName(String appCode, String entityName) {
        List<ApplicationEntity> entities = getApplicationEntities(appCode);
        for (ApplicationEntity entity : entities) {
            if (entity.getEntityName().equals(entityName)) {
                return entity.getTableName();
            }
        }
        return null;
    }

    /**
     * 执行OData查询
     */
    public Map<String, Object> executeQuery(String appCode, String entityName, String tableName, Map<String, String> queryParams) {
        try {
            Map<String, Object> queryRequest = new HashMap<>();
            queryRequest.put("appCode", appCode);
            queryRequest.put("entityName", entityName);
            queryRequest.put("tableName", tableName);
            queryRequest.put("queryParams", queryParams);

            ApiResponse<Map<String, Object>> response = platformConfigClient.executeQuery(queryRequest);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to execute query for {}:{}", appCode, entityName, e);
        }
        
        // 返回默认响应
        Map<String, Object> result = new HashMap<>();
        result.put("@odata.application", appCode);
        result.put("@odata.context", "$metadata#" + entityName);
        result.put("value", Collections.emptyList());
        return result;
    }

    /**
     * 执行数据更新操作
     */
    public Map<String, Object> executeUpdate(String appCode, String entityName, String tableName, String operation, Map<String, Object> data) {
        try {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("appCode", appCode);
            updateRequest.put("entityName", entityName);
            updateRequest.put("tableName", tableName);
            updateRequest.put("operation", operation);
            updateRequest.put("data", data);

            ApiResponse<Map<String, Object>> response = platformConfigClient.executeUpdate(updateRequest);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to execute update for {}:{}", appCode, entityName, e);
        }
        
        // 返回默认响应
        Map<String, Object> result = new HashMap<>();
        result.put("@odata.application", appCode);
        result.put("@odata.context", "$metadata#" + entityName);
        result.put("message", "Operation completed");
        return result;
    }
}