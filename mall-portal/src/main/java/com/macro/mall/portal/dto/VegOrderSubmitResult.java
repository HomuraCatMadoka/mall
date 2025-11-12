package com.macro.mall.portal.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VegOrderSubmitResult {
    private Long orderId;
    private String orderSn;
    private String status;
    private BigDecimal totalAmount;
}
