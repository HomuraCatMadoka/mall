package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.PmsVegCategoryMapper;
import com.macro.mall.model.PmsVegCategory;
import com.macro.mall.portal.service.VegCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VegCategoryServiceImpl implements VegCategoryService {

    @Autowired
    private PmsVegCategoryMapper categoryMapper;

    @Override
    public List<PmsVegCategory> list(Long parentId) {
        if (parentId == null) {
            return categoryMapper.selectAll();
        }
        return categoryMapper.selectByParentId(parentId);
    }
}
