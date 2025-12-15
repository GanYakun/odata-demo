package com.jinyi.platform.controller;

import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDataDto;
import com.jinyi.platform.service.EntityDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 实体数据控制器
 */
@RestController
@RequestMapping("/platform/entity-data")
@Slf4j
public class EntityDataController {

    @Autowired
    private EntityDataService entityDataService;

    /**
     * 创建实体数据
     */
    @PostMapping("/app/{appId}/entity/{entityCode}")
    public ApiResponse<EntityDataDto> createData(
            @PathVariable Long appId,
            @PathVariable String entityCode,
            @RequestBody Map<String, Object> data,
            @RequestParam(required = false) String createdBy) {
        try {
            EntityDataDto result = entityDataService.createData(appId, entityCode, data, createdBy);
            return ApiResponse.success("Data created successfully", result);
        } catch (Exception e) {
            log.error("Failed to create data for entity: {} in app: {}", entityCode, appId, e);
            return ApiResponse.error("Failed to create data: " + e.getMessage());
        }
    }

    /**
     * 根据记录ID获取数据
     */
    @GetMapping("/entity/{entityId}/record/{recordId}")
    public ApiResponse<EntityDataDto> getDataByRecordId(@PathVariable Long entityId, @PathVariable String recordId) {
        try {
            EntityDataDto result = entityDataService.getDataByRecordId(entityId, recordId);
            if (result == null) {
                return ApiResponse.error("Data record not found");
            }
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get data by record id: {} in entity: {}", recordId, entityId, e);
            return ApiResponse.error("Failed to get data: " + e.getMessage());
        }
    }

    /**
     * 获取实体数据列表
     */
    @GetMapping("/app/{appId}/entity/{entityCode}")
    public ApiResponse<List<EntityDataDto>> getDataList(
            @PathVariable Long appId,
            @PathVariable String entityCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<EntityDataDto> result = entityDataService.getDataByAppAndEntity(appId, entityCode, page, size);
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to get data list for entity: {} in app: {}", entityCode, appId, e);
            return ApiResponse.error("Failed to get data list: " + e.getMessage());
        }
    }

    /**
     * 更新实体数据
     */
    @PutMapping("/{id}")
    public ApiResponse<EntityDataDto> updateData(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data,
            @RequestParam(required = false) String updatedBy) {
        try {
            EntityDataDto result = entityDataService.updateData(id, data, updatedBy);
            return ApiResponse.success("Data updated successfully", result);
        } catch (Exception e) {
            log.error("Failed to update data: {}", id, e);
            return ApiResponse.error("Failed to update data: " + e.getMessage());
        }
    }

    /**
     * 根据记录ID更新数据
     */
    @PutMapping("/entity/{entityId}/record/{recordId}")
    public ApiResponse<EntityDataDto> updateDataByRecordId(
            @PathVariable Long entityId,
            @PathVariable String recordId,
            @RequestBody Map<String, Object> data,
            @RequestParam(required = false) String updatedBy) {
        try {
            EntityDataDto result = entityDataService.updateDataByRecordId(entityId, recordId, data, updatedBy);
            return ApiResponse.success("Data updated successfully", result);
        } catch (Exception e) {
            log.error("Failed to update data by record id: {} in entity: {}", recordId, entityId, e);
            return ApiResponse.error("Failed to update data: " + e.getMessage());
        }
    }

    /**
     * 删除实体数据
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteData(@PathVariable Long id) {
        try {
            entityDataService.deleteData(id);
            return ApiResponse.success("Data deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete data: {}", id, e);
            return ApiResponse.error("Failed to delete data: " + e.getMessage());
        }
    }

    /**
     * 根据记录ID删除数据
     */
    @DeleteMapping("/entity/{entityId}/record/{recordId}")
    public ApiResponse<Void> deleteDataByRecordId(@PathVariable Long entityId, @PathVariable String recordId) {
        try {
            entityDataService.deleteDataByRecordId(entityId, recordId);
            return ApiResponse.success("Data deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete data by record id: {} in entity: {}", recordId, entityId, e);
            return ApiResponse.error("Failed to delete data: " + e.getMessage());
        }
    }

    /**
     * 查询实体数据（支持OData查询参数）
     */
    @GetMapping("/app/{appId}/entity/{entityCode}/query")
    public ApiResponse<Map<String, Object>> queryEntityData(
            @PathVariable Long appId,
            @PathVariable String entityCode,
            @RequestParam Map<String, String> queryParams) {
        try {
            Map<String, Object> result = entityDataService.queryEntityData(appId, entityCode, queryParams);
            return ApiResponse.success("Success", result);
        } catch (Exception e) {
            log.error("Failed to query entity data for: {} in app: {}", entityCode, appId, e);
            return ApiResponse.error("Failed to query data: " + e.getMessage());
        }
    }
}