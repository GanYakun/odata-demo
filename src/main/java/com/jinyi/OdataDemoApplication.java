package com.jinyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OData演示应用主类
 * 扫描com.jinyi包下的所有组件，包括odata框架和business业务模块
 */
@SpringBootApplication(scanBasePackages = "com.jinyi")
public class OdataDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(OdataDemoApplication.class, args);
    }
}