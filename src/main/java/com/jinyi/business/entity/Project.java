package com.jinyi.business.entity;

import com.jinyi.odata.annotation.ODataEntity;
import com.jinyi.odata.annotation.ODataField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目实体
 */
@Data
@ODataEntity(name = "Projects", table = "projects")
public class Project {
    @ODataField(key = true)
    private Long id;
    
    @ODataField(nullable = false, length = 100)
    private String name;
    
    @ODataField(length = 500)
    private String description;
    
    @ODataField(nullable = false)
    private LocalDateTime startTime;
    
    @ODataField
    private LocalDateTime createdAt;
    
    @ODataField
    private LocalDateTime updatedAt;
}