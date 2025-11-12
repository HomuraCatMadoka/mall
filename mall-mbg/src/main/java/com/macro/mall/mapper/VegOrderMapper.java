package com.macro.mall.mapper;

import com.macro.mall.model.VegOrder;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface VegOrderMapper {
    int insert(VegOrder record);

    int insertSelective(VegOrder record);

    VegOrder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(VegOrder record);

    List<VegOrder> selectByMember(@Param("memberId") Long memberId, @Param("status") String status);

    List<VegOrder> selectForAdmin(@Param("memberUsername") String memberUsername, @Param("status") String status);

    int deleteByPrimaryKey(Long id);
}
