package com.macro.mall.service.veg;

import com.macro.mall.dto.VegProductParam;
import com.macro.mall.model.PmsVegProduct;

import java.util.List;

public interface VegProductService {
    int create(VegProductParam param);
    int update(Long id, VegProductParam param);
    int delete(Long id);
    List<PmsVegProduct> list(Long categoryId, String keyword, Integer pageNum, Integer pageSize);
    PmsVegProduct detail(Long id);
}
