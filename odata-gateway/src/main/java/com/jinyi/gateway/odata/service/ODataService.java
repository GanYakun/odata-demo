package com.jinyi.gateway.odata.service;

import com.jinyi.common.client.PlatformConfigClient;
import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDefinition;
import com.jinyi.common.dto.EntityDefinitionDto;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
     * 获取应用下的所有实体（使用新的实体定义系统）
     */
    public List<ApplicationEntity> getApplicationEntities(String appCode) {
        try {
            // 先获取应用信息
            Application app = getApplication(appCode);
            if (app == null) {
                log.warn("Application not found: {}", appCode);
                return Collections.emptyList();
            }

            // 使用新的实体定义接口
            ApiResponse<List<EntityDefinitionDto>> response = platformConfigClient.getEntityDefinitionsByAppId(app.getId());
            if (response.isSuccess() && response.getData() != null) {
                // 转换为ApplicationEntity格式
                return response.getData().stream()
                        .map(this::convertToApplicationEntity)
                        .collect(Collectors.toList());
            } else {
                log.error("Failed to get entity definitions: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to get application entities: {}", appCode, e);
        }
        return Collections.emptyList();
    }

    /**
     * 将EntityDefinitionDto转换为ApplicationEntity
     */
    private ApplicationEntity convertToApplicationEntity(EntityDefinitionDto entityDef) {
        ApplicationEntity appEntity = new ApplicationEntity();
        appEntity.setId(entityDef.getId());
        appEntity.setApplicationId(entityDef.getAppId());
        appEntity.setEntityName(entityDef.getEntityName());
        appEntity.setTableName(entityDef.getTableName());
        appEntity.setDescription(entityDef.getDescription());
        appEntity.setIsDynamic("DYNAMIC".equals(entityDef.getEntityType()));
        appEntity.setActive("ACTIVE".equals(entityDef.getStatus()));
        appEntity.setCreatedAt(entityDef.getCreatedAt());
        appEntity.setUpdatedAt(entityDef.getUpdatedAt());
        return appEntity;
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

    // ========== 新实体系统方法 ==========

    /**
     * 查询新实体系统数据
     */
    public Map<String, Object> queryNewEntityData(Long appId, String entityName, Map<String, String> queryParams) {
        try {
            // 先获取实体定义以获取正确的实体编码
            com.jinyi.common.dto.EntityDefinitionDto entityDef = getNewEntityDefinition(appId, entityName);
            if (entityDef == null) {
                log.warn("Entity definition not found for entity: {} in app: {}", entityName, appId);
                Map<String, Object> result = new HashMap<>();
                result.put("@odata.context", "$metadata#" + entityName);
                result.put("value", Collections.emptyList());
                return result;
            }

            // 使用实体编码查询数据
            ApiResponse<Map<String, Object>> response = platformConfigClient.queryEntityData(appId, entityDef.getEntityCode(), queryParams);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to query new entity data for entity: {} in app: {}", entityName, appId, e);
        }
        
        // 返回默认响应
        Map<String, Object> result = new HashMap<>();
        result.put("@odata.context", "$metadata#" + entityName);
        result.put("value", Collections.emptyList());
        return result;
    }

    /**
     * 创建新实体系统数据
     */
    public Map<String, Object> createNewEntityData(Long appId, String entityName, Map<String, Object> data) {
        try {
            // 先获取实体定义以获取正确的实体编码
            com.jinyi.common.dto.EntityDefinitionDto entityDef = getNewEntityDefinition(appId, entityName);
            if (entityDef == null) {
                throw new RuntimeException("Entity definition not found: " + entityName);
            }

            // 使用实体编码创建数据
            ApiResponse<com.jinyi.common.dto.EntityDataDto> response = platformConfigClient.createEntityData(appId, entityDef.getEntityCode(), data);
            if (response.isSuccess() && response.getData() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("@odata.context", "$metadata#" + entityName + "/$entity");
                result.put("id", response.getData().getRecordId());
                result.putAll(response.getData().getData());
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to create new entity data for entity: {} in app: {}", entityName, appId, e);
        }
        
        throw new RuntimeException("Failed to create entity data");
    }

    /**
     * 更新新实体系统数据
     */
    public Map<String, Object> updateNewEntityData(Long entityId, String recordId, Map<String, Object> data) {
        try {
            ApiResponse<com.jinyi.common.dto.EntityDataDto> response = platformConfigClient.updateEntityDataByRecordId(entityId, recordId, data);
            if (response.isSuccess() && response.getData() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", response.getData().getRecordId());
                result.putAll(response.getData().getData());
                return result;
            }
        } catch (Exception e) {
            log.error("Failed to update new entity data for record: {} in entity: {}", recordId, entityId, e);
        }
        
        throw new RuntimeException("Failed to update entity data");
    }

    /**
     * 删除新实体系统数据
     */
    public void deleteNewEntityData(Long entityId, String recordId) {
        try {
            ApiResponse<Void> response = platformConfigClient.deleteEntityDataByRecordId(entityId, recordId);
            if (!response.isSuccess()) {
                throw new RuntimeException("Failed to delete entity data: " + response.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to delete new entity data for record: {} in entity: {}", recordId, entityId, e);
            throw new RuntimeException("Failed to delete entity data");
        }
    }

    /**
     * 获取新实体系统的实体定义
     */
    public com.jinyi.common.dto.EntityDefinitionDto getNewEntityDefinition(Long appId, String entityName) {
        try {
            ApiResponse<com.jinyi.common.dto.EntityDefinitionDto> response = platformConfigClient.getEntityDefinitionByName(appId, entityName);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to get new entity definition for entity: {} in app: {}", entityName, appId, e);
        }
        return null;
    }

    /**
     * 获取应用下的所有新实体定义
     */
    public List<com.jinyi.common.dto.EntityDefinitionDto> getNewEntityDefinitions(Long appId) {
        try {
            ApiResponse<List<com.jinyi.common.dto.EntityDefinitionDto>> response = platformConfigClient.getEntityDefinitionsByAppId(appId);
            if (response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.error("Failed to get new entity definitions for app: {}", appId, e);
        }
        return Collections.emptyList();
    }
}