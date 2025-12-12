package com.jinyi.business.entity;

import com.jinyi.odata.annotation.ODataEntity;
import com.jinyi.odata.annotation.ODataField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应用实体
 * 用于管理不同的业务应用
 */
@Data
@ODataEntity(name = "Applications", table = "applications")
public class Application {
    
    @ODataField(key = true)
    private Long id;
    
    @ODataField(nullable = false, length = 50)
    private String appCode;  // 应用代码，唯一标识
    
    @ODataField(nullable = false, length = 100)
    private String appName;  // 应用名称
    
    @ODataField(length = 500)
    private String description;  // 应用描述
    
    @ODataField(nullable = false, length = 50)
    private String version;  // 应用版本
    
    @ODataField(nullable = false)
    private Boolean active;  // 是否激活
    
    @ODataField(length = 200)
    private String baseUrl;  // 应用基础URL
    
    @ODataField(length = 100)
    private String owner;  // 应用负责人
    
    @ODataField
    private LocalDateTime createdAt;
    
    @ODataField
    private LocalDateTime updatedAt;
}