package com.jinyi.odatademo.entity;

import java.math.BigDecimal;
import com.jinyi.odatademo.annotation.ODataEntity;
import com.jinyi.odatademo.annotation.ODataField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ODataEntity(name = "CompleteTest", table = "complete_test")
/**
 * Complete workflow test entity
 * Generated automatically by Dynamic Entity Registration Service
 */
public class CompleteTest {

    /**
     * Primary key
     */
    @ODataField(key = true, nullable = false)
    private Long id;

    /**
     * Name field
     */
    @ODataField(nullable = false, length = 150)
    private String name;

    /**
     * Price field
     */
    @ODataField()
    private BigDecimal price;

    /**
     * Active status
     */
    @ODataField(nullable = false)
    private Boolean active;

    /**
     * Last update timestamp
     */
    @ODataField()
    private LocalDateTime updatedAt;

}
