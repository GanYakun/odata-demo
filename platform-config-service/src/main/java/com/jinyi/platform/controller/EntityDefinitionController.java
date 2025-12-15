package com.jinyi.platform.controller;

import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDefinitionDto;
import com.jinyi.platform.service.EntityDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实体定义控制器
 */
@RestController
@RequestMapping("/platform/entity-definitions")
@Slf4j
public class EntityDefinitionController {

    @Autowired
    private EntityDefinitionService entityDefinitionService;

    /**
     * 创建实体定义
     */
    @PostMapping
    public ApiResponse<EntityDefinitionDto> createEntity(@RequestBody EntityDefinitionDto dto) {
        try {
            EntityDefinitionDto result = entityDefinitionService.createEntity(dto);
            return ApiResponse.success("Entity created successfully", result);
        } catch (Exception e) {
            log.error("Failed to create entity", e);
            return ApiResponse.error("Failed to create entity: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取实体定义
     */
    @GetMapping("/{id}")
    public ApiResponse<EntityDefinitionDto> getEntityById(@PathVariable Long id) {
        try {
            EntityDefinitionDto result = entityDefinitionService.getEntityById(id);
            if (result == null) {
                return ApiResponse.error("Entity not found");
            }
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get entity by id: {}", id, e);
            return ApiResponse.error("Failed to get entity: " + e.getMessage());
        }
    }

    /**
     * 根据应用ID获取实体列表
     */
    @GetMapping("/app/{appId}")
    public ApiResponse<List<EntityDefinitionDto>> getEntitiesByAppId(@PathVariable Long appId) {
        try {
            List<EntityDefinitionDto> result = entityDefinitionService.getEntitiesByAppId(appId);
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get entities by app id: {}", appId, e);
            return ApiResponse.error("Failed to get entities: " + e.getMessage());
        }
    }

    /**
     * 根据实体编码获取实体定义
     */
    @GetMapping("/app/{appId}/code/{entityCode}")
    public ApiResponse<EntityDefinitionDto> getEntityByCode(@PathVariable Long appId, @PathVariable String entityCode) {
        try {
            EntityDefinitionDto result = entityDefinitionService.getEntityByCode(entityCode, appId);
            if (result == null) {
                return ApiResponse.error("Entity not found");
            }
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get entity by code: {} in app: {}", entityCode, appId, e);
            return ApiResponse.error("Failed to get entity: " + e.getMessage());
        }
    }

    /**
     * 根据实体名称获取实体定义
     */
    @GetMapping("/app/{appId}/name/{entityName}")
    public ApiResponse<EntityDefinitionDto> getEntityByName(@PathVariable Long appId, @PathVariable String entityName) {
        try {
            EntityDefinitionDto result = entityDefinitionService.getEntityByName(entityName, appId);
            if (result == null) {
                return ApiResponse.error("Entity not found");
            }
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get entity by name: {} in app: {}", entityName, appId, e);
            return ApiResponse.error("Failed to get entity: " + e.getMessage());
        }
    }

    /**
     * 更新实体定义
     */
    @PutMapping("/{id}")
    public ApiResponse<EntityDefinitionDto> updateEntity(@PathVariable Long id, @RequestBody EntityDefinitionDto dto) {
        try {
            EntityDefinitionDto result = entityDefinitionService.updateEntity(id, dto);
            return ApiResponse.success("Entity updated successfully", result);
        } catch (Exception e) {
            log.error("Failed to update entity: {}", id, e);
            return ApiResponse.error("Failed to update entity: " + e.getMessage());
        }
    }

    /**
     * 删除实体定义
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEntity(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean dropTable) {
        try {
            entityDefinitionService.deleteEntity(id, dropTable);
            return ApiResponse.success("Entity deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete entity: {}", id, e);
            return ApiResponse.error("Failed to delete entity: " + e.getMessage());
        }
    }
}