package com.macro.mall.service.veg.impl;

import com.macro.mall.mapper.PmsVegCategoryMapper;
import com.macro.mall.model.PmsVegCategory;
import com.macro.mall.service.veg.VegCategoryService;
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

    @Override
    public int create(PmsVegCategory category) {
        return categoryMapper.insertSelective(category);
    }

    @Override
    public int update(Long id, PmsVegCategory category) {
        category.setId(id);
        return categoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    public int delete(Long id) {
        return categoryMapper.deleteByPrimaryKey(id);
    }
}
