package com.jinyi.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 平台配置服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PlatformConfigServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlatformConfigServiceApplication.class, args);
    }
}