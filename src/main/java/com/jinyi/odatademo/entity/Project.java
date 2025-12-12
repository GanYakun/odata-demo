package com.jinyi.odatademo.entity;

import com.jinyi.odatademo.annotation.ODataEntity;
import com.jinyi.odatademo.annotation.ODataField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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