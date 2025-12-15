package com.jinyi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinyi.common.entity.EntityDataStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 实体数据存储Mapper
 */
@Mapper
public interface EntityDataStorageMapper extends BaseMapper<EntityDataStorage> {

    /**
     * 根据实体ID查询数据
     */
    @Select("SELECT * FROM entity_data_storage WHERE entity_id = #{entityId} AND deleted = 0 ORDER BY created_at DESC")
    List<EntityDataStorage> selectByEntityId(@Param("entityId") Long entityId);

    /**
     * 根据记录ID查询数据
     */
    @Select("SELECT * FROM entity_data_storage WHERE entity_id = #{entityId} AND record_id = #{recordId} AND deleted = 0")
    EntityDataStorage selectByRecordId(@Param("entityId") Long entityId, @Param("recordId") String recordId);

    /**
     * 根据应用ID和实体编码查询数据
     */
    @Select("SELECT * FROM entity_data_storage WHERE app_id = #{appId} AND entity_code = #{entityCode} AND deleted = 0 ORDER BY created_at DESC")
    List<EntityDataStorage> selectByAppAndEntity(@Param("appId") Long appId, @Param("entityCode") String entityCode);
}