package com.macro.mall.portal.controller;

import com.github.pagehelper.PageInfo;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.dto.VegOrderDetailDTO;
import com.macro.mall.portal.dto.VegOrderSimple;
import com.macro.mall.portal.dto.VegOrderSubmitResult;
import com.macro.mall.portal.dto.VegSubmitOrderParam;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.portal.service.VegOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/veg/orders")
@Api(tags = "VegOrderController")
public class VegOrderController {

    @Autowired
    private VegOrderService vegOrderService;
    @Autowired
    private UmsMemberService memberService;

    @ApiOperation("提交蔬菜订单")
    @PostMapping
    public CommonResult<VegOrderSubmitResult> submit(@Validated @RequestBody VegSubmitOrderParam param) {
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(vegOrderService.submitOrder(member, param));
    }

    @ApiOperation("订单列表")
    @GetMapping
    public CommonResult<CommonPage<VegOrderSimple>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(CommonPage.restPage(vegOrderService.list(member, status, pageNum, pageSize)));
    }

    @ApiOperation("订单详情")
    @GetMapping("/{orderId}")
    public CommonResult<VegOrderDetailDTO> detail(@PathVariable Long orderId) {
        UmsMember member = memberService.getCurrentMember();
        return CommonResult.success(vegOrderService.detail(member, orderId));
    }
}
