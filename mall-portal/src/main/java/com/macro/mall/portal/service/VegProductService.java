package com.macro.mall.portal.service;

import com.macro.mall.model.PmsVegProduct;

import java.util.List;

public interface VegProductService {

    List<PmsVegProduct> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize);

    PmsVegProduct detail(Long id);
}
