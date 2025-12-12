package com.jinyi.odatademo.entity;

import com.jinyi.odatademo.annotation.ODataEntity;
import com.jinyi.odatademo.annotation.ODataField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ODataEntity(name = "Orders", table = "orders")
public class Order {
    @ODataField(key = true)
    private Long id;
    
    @ODataField(nullable = false, length = 50)
    private String orderNo;
    
    @ODataField(nullable = false)
    private BigDecimal amount;
    
    @ODataField
    private LocalDateTime createdAt;
}
