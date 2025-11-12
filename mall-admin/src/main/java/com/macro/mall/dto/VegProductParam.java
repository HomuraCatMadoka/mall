package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class VegProductParam {

    @NotNull
    private Long categoryId;

    @ApiModelProperty(value = "商品名称", required = true)
    private String name;

    private String subTitle;

    private String pic;

    @ApiModelProperty("规格描述")
    private String specDesc;

    private String unit;

    @NotNull
    private BigDecimal price;

    private Integer sort;

    private Integer recommendStatus;

    private Integer status;
}
