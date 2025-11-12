package com.macro.mall.portal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class VegSubmitOrderParam {

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "下单商品", required = true)
    @NotEmpty
    @Valid
    private List<VegOrderItemParam> items;
}
