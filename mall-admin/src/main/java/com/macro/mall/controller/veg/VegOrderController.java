package com.macro.mall.controller.veg;

import com.github.pagehelper.PageInfo;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.VegOrderItemsUpdateParam;
import com.macro.mall.dto.VegOrderUpdateStatusParam;
import com.macro.mall.model.VegOrder;
import com.macro.mall.model.VegOrderItem;
import com.macro.mall.service.veg.VegOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veg/order")
@Api(tags = "VegOrderController")
public class VegOrderController {

    @Autowired
    private VegOrderService vegOrderService;

    @ApiOperation("订单列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<VegOrder>> list(@RequestParam(required = false) String memberUsername,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "20") Integer pageSize) {
        PageInfo<VegOrder> info = vegOrderService.list(memberUsername, status, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(info));
    }

    @ApiOperation("订单详情")
    @GetMapping("/{id}")
    public CommonResult<VegOrder> detail(@PathVariable Long id) {
        return CommonResult.success(vegOrderService.get(id));
    }

    @ApiOperation("订单明细")
    @GetMapping("/{id}/items")
    public CommonResult<List<VegOrderItem>> items(@PathVariable Long id) {
        return CommonResult.success(vegOrderService.items(id));
    }

    @ApiOperation("更新订单状态")
    @PostMapping("/updateStatus/{id}")
    public CommonResult<Integer> updateStatus(@PathVariable Long id,
                                              @Validated @RequestBody VegOrderUpdateStatusParam param) {
        return CommonResult.success(vegOrderService.updateStatus(id, param));
    }

    @ApiOperation("更新订单商品")
    @PostMapping("/updateItems/{id}")
    public CommonResult<Integer> updateItems(@PathVariable Long id,
                                             @Validated @RequestBody VegOrderItemsUpdateParam param) {
        return CommonResult.success(vegOrderService.updateItems(id, param));
    }
}
