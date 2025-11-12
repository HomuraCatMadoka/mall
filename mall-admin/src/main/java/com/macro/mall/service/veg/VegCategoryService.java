package com.macro.mall.service.veg;

import com.macro.mall.model.PmsVegCategory;

import java.util.List;

public interface VegCategoryService {
    List<PmsVegCategory> list(Long parentId);
    int create(PmsVegCategory category);
    int update(Long id, PmsVegCategory category);
    int delete(Long id);
}
