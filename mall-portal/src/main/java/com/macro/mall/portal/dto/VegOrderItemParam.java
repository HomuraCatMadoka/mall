package com.macro.mall.portal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class VegOrderItemParam {

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull
    private Long productId;

    @ApiModelProperty(value = "数量", required = true)
    @Min(1)
    private Integer quantity;
}
