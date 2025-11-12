package com.macro.mall.controller.veg;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.VegProductParam;
import com.macro.mall.model.PmsVegProduct;
import com.macro.mall.service.veg.VegProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veg/product")
@Api(tags = "VegProductController")
public class VegProductController {

    @Autowired
    private VegProductService vegProductService;

    @ApiOperation("商品列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsVegProduct>> list(@RequestParam(required = false) Long categoryId,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "20") Integer pageSize) {
        List<PmsVegProduct> data = vegProductService.list(categoryId, keyword, pageNum, pageSize);
        return CommonResult.success(CommonPage.restPage(data));
    }

    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public CommonResult<PmsVegProduct> detail(@PathVariable Long id) {
        return CommonResult.success(vegProductService.detail(id));
    }

    @ApiOperation("创建商品")
    @PostMapping("/create")
    public CommonResult<Integer> create(@Validated @RequestBody VegProductParam param) {
        return CommonResult.success(vegProductService.create(param));
    }

    @ApiOperation("更新商品")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @Validated @RequestBody VegProductParam param) {
        return CommonResult.success(vegProductService.update(id, param));
    }

    @ApiOperation("删除商品")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        return CommonResult.success(vegProductService.delete(id));
    }
}
