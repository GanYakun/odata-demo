package com.jinyi.business.entity;

import com.jinyi.odata.annotation.ODataEntity;
import com.jinyi.odata.annotation.ODataField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用实体关联表
 * 记录每个应用下的实体信息
 */
@Data
@ODataEntity(name = "ApplicationEntities", table = "application_entities")
public class ApplicationEntity {
    
    @ODataField(key = true)
    private Long id;
    
    @ODataField(nullable = false)
    private Long applicationId;  // 关联的应用ID
    
    @ODataField(nullable = false, length = 100)
    private String entityName;  // 实体名称
    
    @ODataField(nullable = false, length = 100)
    private String tableName;  // 对应的数据库表名
    
    @ODataField(length = 500)
    private String description;  // 实体描述
    
    @ODataField(nullable = false)
    private Boolean isDynamic;  // 是否为动态实体
    
    @ODataField(nullable = false)
    private Boolean active;  // 是否激活
    
    @ODataField
    private LocalDateTime createdAt;
    
    @ODataField
    private LocalDateTime updatedAt;
}