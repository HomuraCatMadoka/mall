package com.macro.mall.service.veg;

import com.github.pagehelper.PageInfo;
import com.macro.mall.dto.VegOrderItemUpdateParam;
import com.macro.mall.dto.VegOrderItemsUpdateParam;
import com.macro.mall.dto.VegOrderUpdateStatusParam;
import com.macro.mall.model.VegOrder;
import com.macro.mall.model.VegOrderItem;

import java.util.List;

public interface VegOrderService {
    PageInfo<VegOrder> list(String memberUsername, String status, Integer pageNum, Integer pageSize);

    VegOrder get(Long id);

    List<VegOrderItem> items(Long orderId);

    int updateStatus(Long id, VegOrderUpdateStatusParam param);

    int updateItems(Long id, VegOrderItemsUpdateParam param);
}
