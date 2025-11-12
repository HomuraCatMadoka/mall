package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.PmsVegCategory;
import com.macro.mall.portal.service.VegCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veg/categories")
@Api(tags = "VegCategoryController")
public class VegCategoryController {

    @Autowired
    private VegCategoryService vegCategoryService;

    @ApiOperation("获取蔬菜分类")
    @GetMapping
    public CommonResult<List<PmsVegCategory>> list(@RequestParam(required = false) Long parentId) {
        return CommonResult.success(vegCategoryService.list(parentId));
    }
}
