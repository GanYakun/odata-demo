package com.jinyi.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jinyi.common.entity.Application;
import com.jinyi.common.entity.ApplicationEntity;
import com.jinyi.common.dto.EntityDefinitionDto;
import com.jinyi.common.dto.EntityFieldDto;
import com.jinyi.platform.mapper.ApplicationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用管理服务
 * 基于新的实体定义系统的统一应用管理
 */
@Service
@Slf4j
public class ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private EntityDefinitionService entityDefinitionService;

    /**
     * 创建新应用
     */
    @Transactional
    public Application createApplication(Application app) {
        log.info("Creating new application: {}", app.getAppCode());
        
        // 检查应用代码是否已存在
        QueryWrapper<com.jinyi.platform.entity.ApplicationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_code", app.getAppCode());
        com.jinyi.platform.entity.ApplicationEntity existing = applicationMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            throw new RuntimeException("Application code already exists: " + app.getAppCode());
        }

        // 转换为平台实体
        com.jinyi.platform.entity.ApplicationEntity entity = convertToEntity(app);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        // 保存到数据库
        applicationMapper.insert(entity);
        
        // 转换回DTO并返回
        Application result = convertToDto(entity);
        log.info("Application created successfully: {} with ID: {}", result.getAppCode(), result.getId());
        
        return result;
    }

    /**
     * 获取所有应用
     */
    public List<Application> getAllApplications() {
        log.debug("Getting all applications");
        
        List<com.jinyi.platform.entity.ApplicationEntity> entities = applicationMapper.selectList(null);
        
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取应用
     */
    public Optional<Application> getApplicationById(Long id) {
        log.debug("Getting application by ID: {}", id);
        
        com.jinyi.platform.entity.ApplicationEntity entity = applicationMapper.selectById(id);
        
        if (entity == null) {
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(entity));
    }

    /**
     * 根据应用代码获取应用
     */
    public Optional<Application> getApplicationByCode(String appCode) {
        log.debug("Getting application by code: {}", appCode);
        
        QueryWrapper<com.jinyi.platform.entity.ApplicationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_code", appCode);
        com.jinyi.platform.entity.ApplicationEntity entity = applicationMapper.selectOne(queryWrapper);
        
        if (entity == null) {
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(entity));
    }

    /**
     * 更新应用
     */
    @Transactional
    public Application updateApplication(Application app) {
        log.info("Updating application: {}", app.getId());
        
        // 检查应用是否存在
        com.jinyi.platform.entity.ApplicationEntity existing = applicationMapper.selectById(app.getId());
        if (existing == null) {
            throw new RuntimeException("Application not found: " + app.getId());
        }

        // 如果更新了应用代码，检查是否与其他应用冲突
        if (!existing.getAppCode().equals(app.getAppCode())) {
            QueryWrapper<com.jinyi.platform.entity.ApplicationEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_code", app.getAppCode());
            queryWrapper.ne("id", app.getId());
            com.jinyi.platform.entity.ApplicationEntity duplicate = applicationMapper.selectOne(queryWrapper);
            
            if (duplicate != null) {
                throw new RuntimeException("Application code already exists: " + app.getAppCode());
            }
        }

        // 更新实体
        com.jinyi.platform.entity.ApplicationEntity entity = convertToEntity(app);
        entity.setId(app.getId());
        entity.setCreatedAt(existing.getCreatedAt()); // 保持原创建时间
        entity.setUpdatedAt(LocalDateTime.now());
        
        applicationMapper.updateById(entity);
        
        // 转换回DTO并返回
        Application result = convertToDto(entity);
        log.info("Application updated successfully: {}", result.getAppCode());
        
        return result;
    }

    /**
     * 删除应用
     */
    @Transactional
    public void deleteApplication(Long id) {
        log.info("Deleting application: {}", id);
        
        // 检查应用是否存在
        com.jinyi.platform.entity.ApplicationEntity existing = applicationMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("Application not found: " + id);
        }

        // 先删除应用下的所有实体定义
        List<EntityDefinitionDto> entities = entityDefinitionService.getEntitiesByAppId(id);
        for (EntityDefinitionDto entity : entities) {
            try {
                entityDefinitionService.deleteEntity(entity.getId(), true);
                log.info("Deleted entity {} for application {}", entity.getEntityName(), id);
            } catch (Exception e) {
                log.warn("Failed to delete entity {} for application {}: {}", 
                        entity.getEntityName(), id, e.getMessage());
            }
        }
        
        // 删除应用
        applicationMapper.deleteById(id);
        log.info("Application deleted successfully: {}", id);
    }
    /**
     * 为应用添加实体
     */
    @Transactional
    public ApplicationEntity addEntityToApplication(Long applicationId, String entityName, String tableName, 
                                                   String description, boolean isDynamic) {
        log.info("Adding entity {} to application {}", entityName, applicationId);
        
        // 检查应用是否存在
        Optional<Application> app = getApplicationById(applicationId);
        if (app.isEmpty()) {
            throw new RuntimeException("Application not found: " + applicationId);
        }

        // 检查实体是否已存在
        EntityDefinitionDto existing = entityDefinitionService.getEntityByName(entityName, applicationId);
        if (existing != null) {
            throw new RuntimeException("Entity already exists in application: " + entityName);
        }

        // 创建实体定义
        EntityDefinitionDto entityDef = new EntityDefinitionDto();
        entityDef.setEntityName(entityName);
        entityDef.setEntityCode(generateEntityCode(entityName));
        entityDef.setTableName(tableName);
        entityDef.setDisplayName(entityName);
        entityDef.setDescription(description);
        entityDef.setAppId(applicationId);
        entityDef.setEntityType(isDynamic ? "DYNAMIC" : "STATIC");
        entityDef.setStatus("ACTIVE");
        entityDef.setAutoCreateTable(isDynamic);

        // 如果是动态实体，添加基本字段
        if (isDynamic) {
            entityDef.setFields(List.of(
                createBasicField("name", "NAME", "名称", "STRING", 100, true, 1),
                createBasicField("description", "DESCRIPTION", "描述", "TEXT", null, false, 2)
            ));
        }

        // 创建实体
        EntityDefinitionDto created = entityDefinitionService.createEntity(entityDef);
        
        // 转换为ApplicationEntity格式返回
        return convertToApplicationEntity(created);
    }

    /**
     * 获取应用下的所有实体
     */
    public List<ApplicationEntity> getApplicationEntities(Long applicationId) {
        log.debug("Getting application entities for app {}", applicationId);
        
        List<EntityDefinitionDto> entityDefinitions = entityDefinitionService.getEntitiesByAppId(applicationId);
        
        return entityDefinitions.stream()
                .map(this::convertToApplicationEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取应用下的所有实体（根据应用代码）
     */
    public List<ApplicationEntity> getApplicationEntitiesByCode(String appCode) {
        log.debug("Getting application entities for app code {}", appCode);
        
        Optional<Application> app = getApplicationByCode(appCode);
        if (app.isEmpty()) {
            throw new RuntimeException("Application not found: " + appCode);
        }
        
        return getApplicationEntities(app.get().getId());
    }

    /**
     * 从应用中移除实体
     */
    @Transactional
    public void removeEntityFromApplication(Long applicationId, String entityName) {
        log.info("Removing entity {} from application {}", entityName, applicationId);
        
        EntityDefinitionDto entity = entityDefinitionService.getEntityByName(entityName, applicationId);
        if (entity == null) {
            throw new RuntimeException("Entity not found in application: " + entityName);
        }

        // 删除实体定义（包括数据库表）
        entityDefinitionService.deleteEntity(entity.getId(), true);
        
        log.info("Removed entity {} from application {}", entityName, applicationId);
    }

    /**
     * 检查应用是否存在
     */
    public boolean existsById(Long id) {
        return getApplicationById(id).isPresent();
    }

    /**
     * 检查实体是否存在于应用中
     */
    public boolean entityExistsInApplication(Long applicationId, String entityName) {
        EntityDefinitionDto entity = entityDefinitionService.getEntityByName(entityName, applicationId);
        return entity != null && "ACTIVE".equals(entity.getStatus());
    }

    /**
     * 检查实体是否存在于应用中（根据应用代码）
     */
    public boolean entityExistsInApplication(String appCode, String entityName) {
        Optional<Application> app = getApplicationByCode(appCode);
        if (app.isEmpty()) {
            return false;
        }
        return entityExistsInApplication(app.get().getId(), entityName);
    }
    /**
     * 将平台实体转换为DTO
     */
    private Application convertToDto(com.jinyi.platform.entity.ApplicationEntity entity) {
        Application dto = new Application();
        dto.setId(entity.getId());
        dto.setAppCode(entity.getAppCode());
        dto.setAppName(entity.getAppName());
        dto.setDescription(entity.getDescription());
        dto.setVersion(entity.getVersion());
        dto.setActive(entity.getActive());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setOwner(entity.getOwner());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /**
     * 将DTO转换为平台实体
     */
    private com.jinyi.platform.entity.ApplicationEntity convertToEntity(Application dto) {
        com.jinyi.platform.entity.ApplicationEntity entity = new com.jinyi.platform.entity.ApplicationEntity();
        entity.setId(dto.getId());
        entity.setAppCode(dto.getAppCode());
        entity.setAppName(dto.getAppName());
        entity.setDescription(dto.getDescription());
        entity.setVersion(dto.getVersion());
        entity.setActive(dto.getActive() != null ? dto.getActive() : true);
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setOwner(dto.getOwner());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    /**
     * 将EntityDefinitionDto转换为ApplicationEntity
     */
    private ApplicationEntity convertToApplicationEntity(EntityDefinitionDto entityDef) {
        ApplicationEntity appEntity = new ApplicationEntity();
        appEntity.setId(entityDef.getId());
        appEntity.setApplicationId(entityDef.getAppId());
        appEntity.setEntityName(entityDef.getEntityName());
        appEntity.setTableName(entityDef.getTableName());
        appEntity.setDescription(entityDef.getDescription());
        appEntity.setIsDynamic("DYNAMIC".equals(entityDef.getEntityType()));
        appEntity.setActive("ACTIVE".equals(entityDef.getStatus()));
        appEntity.setCreatedAt(entityDef.getCreatedAt());
        appEntity.setUpdatedAt(entityDef.getUpdatedAt());
        return appEntity;
    }

    /**
     * 生成实体编码
     */
    private String generateEntityCode(String entityName) {
        return entityName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "");
    }

    /**
     * 创建基本字段定义
     */
    private EntityFieldDto createBasicField(String fieldName, String fieldCode, String displayName, 
                                          String fieldType, Integer length, boolean required, int sortOrder) {
        EntityFieldDto field = new EntityFieldDto();
        field.setFieldName(fieldName);
        field.setFieldCode(fieldCode);
        field.setDisplayName(displayName);
        field.setFieldType(fieldType);
        field.setFieldLength(length);
        field.setIsNotNull(required);
        field.setSortOrder(sortOrder);
        field.setStatus("ACTIVE");
        return field;
    }
}