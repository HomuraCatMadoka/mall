package com.macro.mall.portal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VegRegisterParam {

    @ApiModelProperty(value = "用户名", required = true)
    @NotBlank
    private String username;

    @ApiModelProperty(value = "登录密码", required = true)
    @NotBlank
    private String password;

    @ApiModelProperty(value = "手机号", required = true)
    @NotBlank
    private String mobile;
}
