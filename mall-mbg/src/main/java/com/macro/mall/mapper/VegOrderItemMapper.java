package com.macro.mall.mapper;

import com.macro.mall.model.VegOrderItem;
import java.util.List;

public interface VegOrderItemMapper {
    int insert(VegOrderItem record);

    int insertSelective(VegOrderItem record);

    int insertBatch(List<VegOrderItem> records);

    List<VegOrderItem> selectByOrderId(Long orderId);

    int deleteByOrderId(Long orderId);
}
