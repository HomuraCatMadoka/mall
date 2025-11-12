package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class VegOrderUpdateStatusParam {
    @ApiModelProperty(value = "订单状态：PENDING/PROCESSED/CLOSED", required = true)
    private String status;

    @ApiModelProperty("运营备注")
    private String operatorRemark;
}
