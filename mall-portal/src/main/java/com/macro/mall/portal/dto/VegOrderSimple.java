package com.macro.mall.portal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class VegOrderSimple {

    private Long id;

    private String orderSn;

    private String status;

    private BigDecimal totalAmount;

    @ApiModelProperty("提交时间")
    private Date submitTime;
}
