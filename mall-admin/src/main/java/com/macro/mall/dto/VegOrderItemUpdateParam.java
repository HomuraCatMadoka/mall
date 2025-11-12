package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class VegOrderItemUpdateParam {

    @NotNull
    private Long productId;

    @ApiModelProperty(value = "商品名称", required = true)
    private String productName;

    private String specDesc;

    private String unit;

    @NotNull
    private BigDecimal price;

    @Min(1)
    private Integer quantity;
}
