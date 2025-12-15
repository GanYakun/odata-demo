package com.jinyi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.common.entity.EntityDefinitionTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实体定义Mapper
 */
@Mapper
public interface EntityDefinitionMapper extends BaseMapper<EntityDefinitionTable> {

    /**
     * 根据应用ID查询实体定义
     */
    @Select("SELECT * FROM entity_definitions WHERE app_id = #{appId} AND deleted = 0 ORDER BY sort_order, created_at")
    List<EntityDefinitionTable> selectByAppId(@Param("appId") Long appId);

    /**
     * 根据实体编码查询实体定义
     */
    @Select("SELECT * FROM entity_definitions WHERE entity_code = #{entityCode} AND app_id = #{appId} AND deleted = 0")
    EntityDefinitionTable selectByEntityCode(@Param("entityCode") String entityCode, @Param("appId") Long appId);

    /**
     * 根据实体名称查询实体定义
     */
    @Select("SELECT * FROM entity_definitions WHERE entity_name = #{entityName} AND app_id = #{appId} AND deleted = 0")
    EntityDefinitionTable selectByEntityName(@Param("entityName") String entityName, @Param("appId") Long appId);
}