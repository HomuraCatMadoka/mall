package com.macro.mall.portal.dto;

import com.macro.mall.model.VegOrder;
import com.macro.mall.model.VegOrderItem;
import lombok.Data;

import java.util.List;

@Data
public class VegOrderDetailDTO {
    private VegOrder order;
    private List<VegOrderItem> items;
}
