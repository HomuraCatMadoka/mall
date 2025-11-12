package com.macro.mall.portal.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.PmsVegProductMapper;
import com.macro.mall.model.PmsVegProduct;
import com.macro.mall.portal.service.VegProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VegProductServiceImpl implements VegProductService {

    @Autowired
    private PmsVegProductMapper productMapper;

    @Override
    public List<PmsVegProduct> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        List<PmsVegProduct> data;
        if (categoryId != null) {
            data = productMapper.selectByCategoryId(categoryId);
        } else if (StringUtils.hasText(keyword)) {
            data = productMapper.selectByKeyword(keyword);
        } else {
            data = productMapper.selectAll();
        }
        return data;
    }

    @Override
    public PmsVegProduct detail(Long id) {
        return productMapper.selectByPrimaryKey(id);
    }
}
