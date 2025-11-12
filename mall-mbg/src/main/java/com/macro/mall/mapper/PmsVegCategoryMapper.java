package com.macro.mall.mapper;

import com.macro.mall.model.PmsVegCategory;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PmsVegCategoryMapper {
    int insert(PmsVegCategory record);

    int insertSelective(PmsVegCategory record);

    int updateByPrimaryKeySelective(PmsVegCategory record);

    int deleteByPrimaryKey(Long id);

    PmsVegCategory selectByPrimaryKey(Long id);

    List<PmsVegCategory> selectByParentId(@Param("parentId") Long parentId);

    List<PmsVegCategory> selectAll();
}
