package com.macro.mall.portal.service;

import com.macro.mall.model.PmsVegCategory;

import java.util.List;

public interface VegCategoryService {
    List<PmsVegCategory> list(Long parentId);
}
