package com.jinyi.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 应用实体
 * 公共实体定义，用于微服务间通信
 */
@Data
public class Application {
    private Long id;
    private String appCode;
    private String appName;
    private String description;
    private String version;
    private Boolean active;
    private String baseUrl;
    private String owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}