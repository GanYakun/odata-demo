package com.jinyi.odatademo.entity;

import java.math.BigDecimal;
import com.jinyi.odatademo.annotation.ODataEntity;
import com.jinyi.odatademo.annotation.ODataField;
import lombok.Data;

@Data
@ODataEntity(name = "Users", table = "users")
/**
 * Customer information table
 * Generated automatically by Dynamic Entity Registration Service
 */
public class Users {

    /**
     * Primary key ID
     */
    @ODataField(key = true, nullable = false)
    private Long id;

    /**
     * Customer name
     */
    @ODataField(nullable = false, length = 100)
    private String name;

    /**
     * Email address
     */
    @ODataField(length = 200)
    private String email;

    /**
     * Account balance
     */
    @ODataField(nullable = false)
    private BigDecimal balance;

}
