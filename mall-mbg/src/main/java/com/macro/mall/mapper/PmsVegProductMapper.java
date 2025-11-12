package com.macro.mall.mapper;

import com.macro.mall.model.PmsVegProduct;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PmsVegProductMapper {
    int insert(PmsVegProduct record);

    int insertSelective(PmsVegProduct record);

    int updateByPrimaryKeySelective(PmsVegProduct record);

    int deleteByPrimaryKey(Long id);

    PmsVegProduct selectByPrimaryKey(Long id);

    List<PmsVegProduct> selectByCategoryId(@Param("categoryId") Long categoryId);

    List<PmsVegProduct> selectByKeyword(@Param("keyword") String keyword);

    List<PmsVegProduct> selectAll();
}
