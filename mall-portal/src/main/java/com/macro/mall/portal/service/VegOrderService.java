package com.macro.mall.portal.service;

import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dto.VegOrderDetailDTO;
import com.macro.mall.portal.dto.VegOrderSubmitResult;
import com.macro.mall.portal.dto.VegSubmitOrderParam;
import com.macro.mall.portal.dto.VegOrderSimple;

import java.util.List;

public interface VegOrderService {

    VegOrderSubmitResult submitOrder(UmsMember member, VegSubmitOrderParam param);

    List<VegOrderSimple> list(UmsMember member, String status, Integer pageNum, Integer pageSize);

    VegOrderDetailDTO detail(UmsMember member, Long orderId);
}
