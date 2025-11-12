package com.macro.mall.controller.veg;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsVegCategory;
import com.macro.mall.service.veg.VegCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veg/category")
@Api(tags = "VegCategoryController")
public class VegCategoryController {

    @Autowired
    private VegCategoryService vegCategoryService;

    @ApiOperation("分类列表")
    @GetMapping("/list")
    public CommonResult<CommonPage<PmsVegCategory>> list(@RequestParam(required = false) Long parentId,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "50") Integer pageSize) {
        List<PmsVegCategory> data = vegCategoryService.list(parentId);
        return CommonResult.success(CommonPage.restPage(data));
    }

    @ApiOperation("创建分类")
    @PostMapping("/create")
    public CommonResult<Integer> create(@RequestBody PmsVegCategory category) {
        return CommonResult.success(vegCategoryService.create(category));
    }

    @ApiOperation("更新分类")
    @PostMapping("/update/{id}")
    public CommonResult<Integer> update(@PathVariable Long id, @RequestBody PmsVegCategory category) {
        return CommonResult.success(vegCategoryService.update(id, category));
    }

    @ApiOperation("删除分类")
    @PostMapping("/delete/{id}")
    public CommonResult<Integer> delete(@PathVariable Long id) {
        return CommonResult.success(vegCategoryService.delete(id));
    }
}
