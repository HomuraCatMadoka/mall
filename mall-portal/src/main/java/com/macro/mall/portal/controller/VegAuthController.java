package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dto.*;
import com.macro.mall.portal.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Api(tags = "VegAuthController")
public class VegAuthController {

    @Autowired
    private UmsMemberService memberService;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @ApiOperation("蔬菜商城注册")
    @PostMapping("/register")
    public CommonResult<VegAuthResponse> register(@Validated @RequestBody VegRegisterParam param) {
        UmsMember member = memberService.registerWithoutAuthCode(param.getUsername(), param.getPassword(), param.getMobile());
        String token = memberService.login(param.getUsername(), param.getPassword());
        return CommonResult.success(buildAuthResponse(member, token));
    }

    @ApiOperation("蔬菜商城登录")
    @PostMapping("/login")
    public CommonResult<VegAuthResponse> login(@Validated @RequestBody VegLoginParam param) {
        String token = memberService.login(param.getUsername(), param.getPassword());
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        UmsMember member = memberService.getByUsername(param.getUsername());
        return CommonResult.success(buildAuthResponse(member, token));
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/profile")
    public CommonResult<VegProfileDTO> profile() {
        UmsMember member = memberService.getCurrentMember();
        VegProfileDTO dto = new VegProfileDTO();
        dto.setMemberId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setMobile(member.getPhone());
        return CommonResult.success(dto);
    }

    private VegAuthResponse buildAuthResponse(UmsMember member, String token) {
        VegAuthResponse response = new VegAuthResponse();
        response.setMemberId(member.getId());
        response.setUsername(member.getUsername());
        response.setMobile(member.getPhone());
        response.setToken(token);
        response.setTokenHead(tokenHead);
        return response;
    }
}
