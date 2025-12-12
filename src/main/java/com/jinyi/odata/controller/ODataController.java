package com.jinyi.odata.controller;

import com.jinyi.odata.service.ODataQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * OData协议控制器
 * 提供标准的OData查询接口
 */
@RestController
@RequestMapping("/odata/global")
@Slf4j
public class ODataController {

    @Autowired
    private ODataQueryService odataQueryService;

    /**
     * 查询实体集合
     */
    @GetMapping(value = "/{entitySet}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> queryEntitySet(
            @PathVariable String entitySet,
            HttpServletRequest request) {
        
        try {
            // 提取查询参数
            Map<String, String> queryParams = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    queryParams.put(key, values[0]);
                }
            });

            log.info("OData query for entity: {} with params: {}", entitySet, queryParams);

            // 执行查询
            ODataQueryService.QueryResult result = odataQueryService.queryEntities(entitySet, queryParams);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("@odata.context", "$metadata#" + entitySet);
            response.put("value", result.getData());
            
            if (result.getCount() > 0) {
                response.put("@odata.count", result.getCount());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to query entity set: {}", entitySet, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", Map.of(
                "code", "QUERY_ERROR",
                "message", e.getMessage()
            ));
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取单个实体
     */
    @GetMapping(value = "/{entitySet}({key})", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getEntity(
            @PathVariable String entitySet,
            @PathVariable String key) {
        
        try {
            // 构建过滤条件查询单个实体
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("$filter", "id eq " + key);
            queryParams.put("$top", "1");

            log.info("OData get entity: {} with key: {}", entitySet, key);

            ODataQueryService.QueryResult result = odataQueryService.queryEntities(entitySet, queryParams);

            if (result.getData().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", Map.of(
                    "code", "NOT_FOUND",
                    "message", "Entity not found"
                ));
                return ResponseEntity.notFound().build();
            }

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("@odata.context", "$metadata#" + entitySet + "/$entity");
            response.putAll(result.getData().get(0));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to get entity: {} with key: {}", entitySet, key, e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", Map.of(
                "code", "QUERY_ERROR",
                "message", e.getMessage()
            ));
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取元数据
     */
    @GetMapping(value = "/$metadata", produces = "application/xml")
    public ResponseEntity<String> getMetadata() {
        // 简化的元数据实现
        String metadata = """
            <?xml version="1.0" encoding="UTF-8"?>
            <edmx:Edmx xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
                <edmx:DataServices>
                    <Schema xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="ODataDemo">
                        <EntityContainer Name="Container">
                            <EntitySet Name="Orders" EntityType="ODataDemo.Order"/>
                            <EntitySet Name="Products" EntityType="ODataDemo.Product"/>
                            <EntitySet Name="Projects" EntityType="ODataDemo.Project"/>
                        </EntityContainer>
                        <EntityType Name="Order">
                            <Key>
                                <PropertyRef Name="id"/>
                            </Key>
                            <Property Name="id" Type="Edm.Int64" Nullable="false"/>
                            <Property Name="orderNo" Type="Edm.String" MaxLength="50"/>
                            <Property Name="amount" Type="Edm.Decimal"/>
                            <Property Name="createdAt" Type="Edm.DateTimeOffset"/>
                        </EntityType>
                        <EntityType Name="Product">
                            <Key>
                                <PropertyRef Name="id"/>
                            </Key>
                            <Property Name="id" Type="Edm.Int64" Nullable="false"/>
                            <Property Name="name" Type="Edm.String" MaxLength="100"/>
                            <Property Name="description" Type="Edm.String" MaxLength="500"/>
                            <Property Name="price" Type="Edm.Decimal"/>
                            <Property Name="stock" Type="Edm.Int32"/>
                            <Property Name="createdAt" Type="Edm.DateTimeOffset"/>
                            <Property Name="updatedAt" Type="Edm.DateTimeOffset"/>
                        </EntityType>
                        <EntityType Name="Project">
                            <Key>
                                <PropertyRef Name="id"/>
                            </Key>
                            <Property Name="id" Type="Edm.Int64" Nullable="false"/>
                            <Property Name="name" Type="Edm.String" MaxLength="100"/>
                            <Property Name="description" Type="Edm.String" MaxLength="500"/>
                            <Property Name="startTime" Type="Edm.DateTimeOffset"/>
                            <Property Name="createdAt" Type="Edm.DateTimeOffset"/>
                            <Property Name="updatedAt" Type="Edm.DateTimeOffset"/>
                        </EntityType>
                    </Schema>
                </edmx:DataServices>
            </edmx:Edmx>
            """;
        
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_XML)
            .body(metadata);
    }
}