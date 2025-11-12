package com.macro.mall.portal.dto;

import lombok.Data;

@Data
public class VegAuthResponse {
    private Long memberId;
    private String username;
    private String mobile;
    private String tokenHead;
    private String token;
}
