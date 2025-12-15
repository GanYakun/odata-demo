package com.jinyi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.common.entity.EntityFieldDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实体字段定义Mapper
 */
@Mapper
public interface EntityFieldDefinitionMapper extends BaseMapper<EntityFieldDefinition> {

    /**
     * 根据实体ID查询字段定义
     */
    @Select("SELECT * FROM entity_field_definitions WHERE entity_id = #{entityId} AND deleted = 0 ORDER BY sort_order, created_at")
    List<EntityFieldDefinition> selectByEntityId(@Param("entityId") Long entityId);

    /**
     * 根据字段名称查询字段定义
     */
    @Select("SELECT * FROM entity_field_definitions WHERE entity_id = #{entityId} AND field_name = #{fieldName} AND deleted = 0")
    EntityFieldDefinition selectByFieldName(@Param("entityId") Long entityId, @Param("fieldName") String fieldName);
}