package com.jinyi.odatademo.controller;

import com.jinyi.odatademo.service.EntityRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {
    
    @Autowired
    private EntityRegistryService entityRegistryService;
    
    @GetMapping("/test")
    public String test(){
        return "Hello OData!";
    }
    
    @GetMapping("/entities")
    public Map<String, Class<?>> getEntities() {
        return entityRegistryService.getEntityRegistry();
    }
}
