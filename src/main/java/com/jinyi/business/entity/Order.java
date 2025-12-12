package com.jinyi.business.entity;

import com.jinyi.odata.annotation.ODataEntity;
import com.jinyi.odata.annotation.ODataField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
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