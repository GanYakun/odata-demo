package com.jinyi.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jinyi.platform.entity.ApplicationEntityRelation;
import com.jinyi.platform.mapper.ApplicationMapper;
import com.jinyi.platform.mapper.ApplicationEntityMapper;
import com.jinyi.common.entity.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 应用管理服务
 */
@Service
@Slf4j
public class ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationEntityMapper applicationEntityMapper;

    /**
     * 创建新应用
     */
    @Transactional
    public Application createApplication(Application app) {
        // 检查应用代码是否已存在
        QueryWrapper<com.jinyi.platform.entity.ApplicationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_code", app.getAppCode());
        if (applicationMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("Application code already exists: " + app.getAppCode());
        }

        com.jinyi.platform.entity.ApplicationEntity entity = new com.jinyi.platform.entity.ApplicationEntity();
        BeanUtils.copyProperties(app, entity);
        entity.setActive(app.getActive() != null ? app.getActive() : true);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        applicationMapper.insert(entity);
        
        Application result = new Application();
        BeanUtils.copyProperties(entity, result);
        
        log.info("Created application: {} (ID: {})", app.getAppCode(), entity.getId());
        return result;
    }

    /**
     * 获取所有应用
     */
    public List<Application> getAllApplications() {
        List<com.jinyi.platform.entity.ApplicationEntity> entities = applicationMapper.selectList(null);
        return entities.stream()
                .map(this::convertToCommon)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取应用
     */
    public Optional<Application> getApplicationById(Long id) {
        com.jinyi.platform.entity.ApplicationEntity entity = applicationMapper.selectById(id);
        return entity != null ? Optional.of(convertToCommon(entity)) : Optional.empty();
    }

    /**
     * 根据应用代码获取应用
     */
    public Optional<Application> getApplicationByCode(String appCode) {
        QueryWrapper<com.jinyi.platform.entity.ApplicationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_code", appCode);
        com.jinyi.platform.entity.ApplicationEntity entity = applicationMapper.selectOne(queryWrapper);
        return entity != null ? Optional.of(convertToCommon(entity)) : Optional.empty();
    }

    /**
     * 更新应用
     */
    @Transactional
    public Application updateApplication(Application app) {
        com.jinyi.platform.entity.ApplicationEntity entity = applicationMapper.selectById(app.getId());
        if (entity == null) {
            throw new RuntimeException("Application not found: " + app.getId());
        }
        
        BeanUtils.copyProperties(app, entity, "id", "createdAt");
        entity.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(entity);
        
        Application result = new Application();
        BeanUtils.copyProperties(entity, result);
        
        log.info("Updated application: {} (ID: {})", app.getAppCode(), app.getId());
        return result;
    }

    /**
     * 删除应用
     */
    @Transactional
    public void deleteApplication(Long id) {
        // 先删除关联的实体
        QueryWrapper<ApplicationEntityRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_id", id);
        applicationEntityMapper.delete(queryWrapper);
        
        // 再删除应用
        int deleted = applicationMapper.deleteById(id);
        if (deleted == 0) {
            throw new RuntimeException("Application not found: " + id);
        }

        log.info("Deleted application with ID: {}", id);
    }

    /**
     * 为应用添加实体关联
     */
    @Transactional
    public com.jinyi.common.entity.ApplicationEntity addEntityToApplication(Long applicationId, String entityName, 
                                                         String tableName, String description, boolean isDynamic) {
        // 检查应用是否存在
        if (applicationMapper.selectById(applicationId) == null) {
            throw new RuntimeException("Application not found: " + applicationId);
        }

        // 检查实体是否已关联到该应用
        QueryWrapper<ApplicationEntityRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_id", applicationId).eq("entity_name", entityName);
        if (applicationEntityMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("Entity already exists in application: " + entityName);
        }

        ApplicationEntityRelation relation = new ApplicationEntityRelation();
        relation.setApplicationId(applicationId);
        relation.setEntityName(entityName);
        relation.setTableName(tableName);
        relation.setDescription(description);
        relation.setIsDynamic(isDynamic);
        relation.setActive(true);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setUpdatedAt(LocalDateTime.now());

        applicationEntityMapper.insert(relation);
        
        com.jinyi.common.entity.ApplicationEntity result = new com.jinyi.common.entity.ApplicationEntity();
        BeanUtils.copyProperties(relation, result);
        
        log.info("Added entity {} to application {}", entityName, applicationId);
        return result;
    }

    /**
     * 获取应用下的所有实体
     */
    public List<com.jinyi.common.entity.ApplicationEntity> getApplicationEntities(Long applicationId) {
        QueryWrapper<ApplicationEntityRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_id", applicationId).eq("active", true);
        List<ApplicationEntityRelation> relations = applicationEntityMapper.selectList(queryWrapper);
        
        return relations.stream()
                .map(this::convertEntityToCommon)
                .collect(Collectors.toList());
    }

    /**
     * 根据应用代码获取应用下的所有实体
     */
    public List<com.jinyi.common.entity.ApplicationEntity> getApplicationEntitiesByCode(String appCode) {
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
        QueryWrapper<ApplicationEntityRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_id", applicationId).eq("entity_name", entityName);
        
        int deleted = applicationEntityMapper.delete(queryWrapper);
        if (deleted == 0) {
            throw new RuntimeException("Entity not found in application: " + entityName);
        }

        log.info("Removed entity {} from application {}", entityName, applicationId);
    }

    /**
     * 检查应用是否存在
     */
    public boolean existsById(Long id) {
        return applicationMapper.selectById(id) != null;
    }

    /**
     * 转换实体到公共实体
     */
    private Application convertToCommon(com.jinyi.platform.entity.ApplicationEntity entity) {
        Application app = new Application();
        BeanUtils.copyProperties(entity, app);
        return app;
    }

    /**
     * 转换实体关联到公共实体关联
     */
    private com.jinyi.common.entity.ApplicationEntity convertEntityToCommon(ApplicationEntityRelation relation) {
        com.jinyi.common.entity.ApplicationEntity entity = new com.jinyi.common.entity.ApplicationEntity();
        BeanUtils.copyProperties(relation, entity);
        return entity;
    }
}