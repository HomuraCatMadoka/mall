package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsVegProduct;
import com.macro.mall.portal.service.VegProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/veg/products")
@Api(tags = "VegProductController")
public class VegProductController {

    @Autowired
    private VegProductService vegProductService;

    @ApiOperation("获取商品列表")
    @GetMapping
    public CommonResult<CommonPage<PmsVegProduct>> list(@RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return CommonResult.success(CommonPage.restPage(vegProductService.list(categoryId, keyword, pageNum, pageSize)));
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public CommonResult<PmsVegProduct> detail(@PathVariable Long id) {
        return CommonResult.success(vegProductService.detail(id));
    }
}
