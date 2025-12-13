package com.jinyi.common.client;

import com.jinyi.common.dto.ApiResponse;
import com.jinyi.common.dto.EntityDefinition;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台配置服务客户端
 * 用于Gateway调用平台配置服务
 */
@FeignClient(name = "platform-config-service", path = "/platform")
public interface PlatformConfigClient {

    /**
     * 获取所有应用
     */
    @GetMapping("/applications")
    ApiResponse<List<Application>> getAllApplications();

    /**
     * 根据应用代码获取应用
     */
    @GetMapping("/applications/code/{appCode}")
    ApiResponse<Application> getApplicationByCode(@PathVariable String appCode);

    /**
     * 获取应用下的所有实体
     */
    @GetMapping("/applications/{appId}/entities")
    ApiResponse<List<ApplicationEntity>> getApplicationEntities(@PathVariable Long appId);

    /**
     * 根据应用代码获取应用下的所有实体
     */
    @GetMapping("/applications/code/{appCode}/entities")
    ApiResponse<List<ApplicationEntity>> getApplicationEntitiesByCode(@PathVariable String appCode);

    /**
     * 获取动态实体定义
     */
    @GetMapping("/applications/{appId}/dynamic-entities/{entityName}")
    ApiResponse<EntityDefinition> getDynamicEntityDefinition(@PathVariable Long appId, @PathVariable String entityName);

    /**
     * 检查动态实体是否存在
     */
    @GetMapping("/applications/{appId}/dynamic-entities/{entityName}/exists")
    ApiResponse<Boolean> isDynamicEntityExists(@PathVariable Long appId, @PathVariable String entityName);

    /**
     * 注册动态实体
     */
    @PostMapping("/applications/{appId}/dynamic-entities")
    ApiResponse<Map<String, Object>> registerDynamicEntity(@PathVariable Long appId, @RequestBody EntityDefinition entityDef);

    /**
     * 删除动态实体
     */
    @DeleteMapping("/applications/{appId}/dynamic-entities/{entityName}")
    ApiResponse<Map<String, Object>> deleteDynamicEntity(@PathVariable Long appId, @PathVariable String entityName, 
                                                         @RequestParam(defaultValue = "false") boolean dropTable);

    /**
     * 执行数据库查询
     */
    @PostMapping("/query/execute")
    ApiResponse<Map<String, Object>> executeQuery(@RequestBody Map<String, Object> queryRequest);

    /**
     * 执行数据库更新
     */
    @PostMapping("/query/update")
    ApiResponse<Map<String, Object>> executeUpdate(@RequestBody Map<String, Object> updateRequest);
}