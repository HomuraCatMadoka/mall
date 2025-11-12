package com.macro.mall.service.veg.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.VegProductParam;
import com.macro.mall.mapper.PmsVegProductMapper;
import com.macro.mall.model.PmsVegProduct;
import com.macro.mall.service.veg.VegProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VegProductServiceImpl implements VegProductService {

    @Autowired
    private PmsVegProductMapper productMapper;

    @Override
    public int create(VegProductParam param) {
        PmsVegProduct product = new PmsVegProduct();
        BeanUtils.copyProperties(param, product);
        return productMapper.insertSelective(product);
    }

    @Override
    public int update(Long id, VegProductParam param) {
        PmsVegProduct product = new PmsVegProduct();
        BeanUtils.copyProperties(param, product);
        product.setId(id);
        return productMapper.updateByPrimaryKeySelective(product);
    }

    @Override
    public int delete(Long id) {
        return productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<PmsVegProduct> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        if (categoryId != null) {
            return productMapper.selectByCategoryId(categoryId);
        }
        if (keyword != null && keyword.length() > 0) {
            return productMapper.selectByKeyword(keyword);
        }
        return productMapper.selectAll();
    }

    @Override
    public PmsVegProduct detail(Long id) {
        return productMapper.selectByPrimaryKey(id);
    }
}
