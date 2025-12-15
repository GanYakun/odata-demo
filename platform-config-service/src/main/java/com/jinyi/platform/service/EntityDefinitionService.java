package com.jinyi.platform.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jinyi.common.dto.EntityDefinitionDto;
import com.jinyi.common.dto.EntityFieldDto;
import com.jinyi.common.entity.EntityDefinitionTable;
import com.jinyi.common.entity.EntityFieldDefinition;
import com.jinyi.platform.mapper.EntityDefinitionMapper;
import com.jinyi.platform.mapper.EntityFieldDefinitionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 实体定义服务
 */
@Service
@Slf4j
public class EntityDefinitionService {

    @Autowired
    private EntityDefinitionMapper entityDefinitionMapper;

    @Autowired
    private EntityFieldDefinitionMapper entityFieldDefinitionMapper;

    @Autowired
    private DatabaseTableService databaseTableService;

    /**
     * 创建实体定义
     */
    @Transactional
    public EntityDefinitionDto createEntity(EntityDefinitionDto dto) {
        log.info("Creating entity definition: {}", dto.getEntityName());

        // 检查实体名称是否已存在
        EntityDefinitionTable existing = entityDefinitionMapper.selectByEntityName(dto.getEntityName(), dto.getAppId());
        if (existing != null) {
            throw new RuntimeException("Entity name already exists: " + dto.getEntityName());
        }

        // 创建实体定义
        EntityDefinitionTable entity = new EntityDefinitionTable();
        BeanUtils.copyProperties(dto, entity);
        
        // 设置默认值
        if (entity.getEntityCode() == null) {
            entity.setEntityCode(generateEntityCode());
        }
        if (entity.getTableName() == null) {
            entity.setTableName("dyn_" + entity.getEntityCode().toLowerCase());
        }
        if (entity.getEntityType() == null) {
            entity.setEntityType("DYNAMIC");
        }
        if (entity.getStatus() == null) {
            entity.setStatus("ACTIVE");
        }
        if (entity.getVersion() == null) {
            entity.setVersion(1);
        }
        if (entity.getAutoCreateTable() == null) {
            entity.setAutoCreateTable(true);
        }
        entity.setTableCreated(false);
        entity.setCreatedBy("system");
        entity.setUpdatedBy("system");

        entityDefinitionMapper.insert(entity);

        // 创建字段定义
        if (dto.getFields() != null && !dto.getFields().isEmpty()) {
            for (int i = 0; i < dto.getFields().size(); i++) {
                EntityFieldDto fieldDto = dto.getFields().get(i);
                EntityFieldDefinition field = new EntityFieldDefinition();
                BeanUtils.copyProperties(fieldDto, field);
                
                field.setEntityId(entity.getId());
                if (field.getFieldCode() == null) {
                    field.setFieldCode(field.getFieldName().toUpperCase());
                }
                if (field.getDbType() == null) {
                    field.setDbType(mapFieldTypeToDbType(field.getFieldType()));
                }
                if (field.getStatus() == null) {
                    field.setStatus("ACTIVE");
                }
                if (field.getSortOrder() == null) {
                    field.setSortOrder(i + 1);
                }
                field.setCreatedBy("system");
                field.setUpdatedBy("system");

                entityFieldDefinitionMapper.insert(field);
            }
        }

        // 如果需要自动创建表，则创建数据库表
        if (entity.getAutoCreateTable()) {
            try {
                List<EntityFieldDefinition> fields = entityFieldDefinitionMapper.selectByEntityId(entity.getId());
                databaseTableService.createDynamicTable(entity.getTableName(), fields);
                
                // 更新表创建状态
                entity.setTableCreated(true);
                entityDefinitionMapper.updateById(entity);
                
                log.info("Database table created successfully: {}", entity.getTableName());
            } catch (Exception e) {
                log.error("Failed to create database table: {}", entity.getTableName(), e);
                throw new RuntimeException("Failed to create database table: " + e.getMessage());
            }
        }

        return getEntityById(entity.getId());
    }

    /**
     * 根据ID获取实体定义
     */
    public EntityDefinitionDto getEntityById(Long id) {
        EntityDefinitionTable entity = entityDefinitionMapper.selectById(id);
        if (entity == null) {
            return null;
        }

        EntityDefinitionDto dto = new EntityDefinitionDto();
        BeanUtils.copyProperties(entity, dto);

        // 获取字段定义
        List<EntityFieldDefinition> fields = entityFieldDefinitionMapper.selectByEntityId(id);
        List<EntityFieldDto> fieldDtos = fields.stream().map(field -> {
            EntityFieldDto fieldDto = new EntityFieldDto();
            BeanUtils.copyProperties(field, fieldDto);
            return fieldDto;
        }).collect(Collectors.toList());
        dto.setFields(fieldDtos);

        return dto;
    }

    /**
     * 根据应用ID获取实体列表
     */
    public List<EntityDefinitionDto> getEntitiesByAppId(Long appId) {
        List<EntityDefinitionTable> entities = entityDefinitionMapper.selectByAppId(appId);
        return entities.stream().map(entity -> {
            EntityDefinitionDto dto = new EntityDefinitionDto();
            BeanUtils.copyProperties(entity, dto);
            
            // 获取字段定义
            List<EntityFieldDefinition> fields = entityFieldDefinitionMapper.selectByEntityId(entity.getId());
            List<EntityFieldDto> fieldDtos = fields.stream().map(field -> {
                EntityFieldDto fieldDto = new EntityFieldDto();
                BeanUtils.copyProperties(field, fieldDto);
                return fieldDto;
            }).collect(Collectors.toList());
            dto.setFields(fieldDtos);
            
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 根据实体编码获取实体定义
     */
    public EntityDefinitionDto getEntityByCode(String entityCode, Long appId) {
        EntityDefinitionTable entity = entityDefinitionMapper.selectByEntityCode(entityCode, appId);
        if (entity == null) {
            return null;
        }
        return getEntityById(entity.getId());
    }

    /**
     * 根据实体名称获取实体定义
     */
    public EntityDefinitionDto getEntityByName(String entityName, Long appId) {
        EntityDefinitionTable entity = entityDefinitionMapper.selectByEntityName(entityName, appId);
        if (entity == null) {
            return null;
        }
        return getEntityById(entity.getId());
    }

    /**
     * 更新实体定义
     */
    @Transactional
    public EntityDefinitionDto updateEntity(Long id, EntityDefinitionDto dto) {
        EntityDefinitionTable entity = entityDefinitionMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("Entity not found: " + id);
        }

        // 更新实体基本信息
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "createdBy");
        entity.setUpdatedBy("system");
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVersion(entity.getVersion() + 1);

        entityDefinitionMapper.updateById(entity);

        // 更新字段定义（简化处理：删除旧字段，插入新字段）
        if (dto.getFields() != null) {
            // 删除旧字段
            QueryWrapper<EntityFieldDefinition> wrapper = new QueryWrapper<>();
            wrapper.eq("entity_id", id);
            entityFieldDefinitionMapper.delete(wrapper);

            // 插入新字段
            for (int i = 0; i < dto.getFields().size(); i++) {
                EntityFieldDto fieldDto = dto.getFields().get(i);
                EntityFieldDefinition field = new EntityFieldDefinition();
                BeanUtils.copyProperties(fieldDto, field);
                
                field.setEntityId(id);
                if (field.getFieldCode() == null) {
                    field.setFieldCode(field.getFieldName().toUpperCase());
                }
                if (field.getDbType() == null) {
                    field.setDbType(mapFieldTypeToDbType(field.getFieldType()));
                }
                if (field.getStatus() == null) {
                    field.setStatus("ACTIVE");
                }
                if (field.getSortOrder() == null) {
                    field.setSortOrder(i + 1);
                }
                field.setCreatedBy("system");
                field.setUpdatedBy("system");

                entityFieldDefinitionMapper.insert(field);
            }
        }

        return getEntityById(id);
    }

    /**
     * 删除实体定义
     */
    @Transactional
    public void deleteEntity(Long id, boolean dropTable) {
        EntityDefinitionTable entity = entityDefinitionMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("Entity not found: " + id);
        }

        // 删除字段定义
        QueryWrapper<EntityFieldDefinition> wrapper = new QueryWrapper<>();
        wrapper.eq("entity_id", id);
        entityFieldDefinitionMapper.delete(wrapper);

        // 删除实体定义
        entityDefinitionMapper.deleteById(id);

        // 如果需要删除表
        if (dropTable && entity.getTableCreated()) {
            try {
                databaseTableService.dropTable(entity.getTableName());
                log.info("Database table dropped successfully: {}", entity.getTableName());
            } catch (Exception e) {
                log.error("Failed to drop database table: {}", entity.getTableName(), e);
            }
        }
    }

    /**
     * 生成实体编码
     */
    private String generateEntityCode() {
        return "ENT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 映射字段类型到数据库类型
     */
    private String mapFieldTypeToDbType(String fieldType) {
        switch (fieldType.toUpperCase()) {
            case "STRING":
                return "VARCHAR";
            case "INTEGER":
                return "INT";
            case "LONG":
                return "BIGINT";
            case "DECIMAL":
                return "DECIMAL";
            case "BOOLEAN":
                return "BOOLEAN";
            case "DATETIME":
                return "DATETIME";
            case "TEXT":
                return "TEXT";
            case "JSON":
                return "JSON";
            default:
                return "VARCHAR";
        }
    }
}