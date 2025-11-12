package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class VegOrderItemsUpdateParam {

    @ApiModelProperty("商品明细")
    @Valid
    @NotEmpty
    private List<VegOrderItemUpdateParam> items;

    private String operatorRemark;
}
