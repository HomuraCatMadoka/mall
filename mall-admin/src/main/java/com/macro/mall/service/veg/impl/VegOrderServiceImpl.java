package com.macro.mall.service.veg.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.macro.mall.dto.VegOrderItemUpdateParam;
import com.macro.mall.dto.VegOrderItemsUpdateParam;
import com.macro.mall.dto.VegOrderUpdateStatusParam;
import com.macro.mall.mapper.VegOrderItemMapper;
import com.macro.mall.mapper.VegOrderMapper;
import com.macro.mall.model.VegOrder;
import com.macro.mall.model.VegOrderItem;
import com.macro.mall.service.veg.VegOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VegOrderServiceImpl implements VegOrderService {

    @Autowired
    private VegOrderMapper vegOrderMapper;
    @Autowired
    private VegOrderItemMapper vegOrderItemMapper;

    @Override
    public PageInfo<VegOrder> list(String memberUsername, String status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        List<VegOrder> orders = vegOrderMapper.selectForAdmin(memberUsername, status);
        return new PageInfo<>(orders);
    }

    @Override
    public VegOrder get(Long id) {
        return vegOrderMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<VegOrderItem> items(Long orderId) {
        return vegOrderItemMapper.selectByOrderId(orderId);
    }

    @Override
    @Transactional
    public int updateStatus(Long id, VegOrderUpdateStatusParam param) {
        VegOrder record = new VegOrder();
        record.setId(id);
        record.setStatus(param.getStatus());
        record.setOperatorRemark(param.getOperatorRemark());
        Date now = new Date();
        if ("PROCESSED".equalsIgnoreCase(param.getStatus())) {
            record.setHandleTime(now);
        } else if ("CLOSED".equalsIgnoreCase(param.getStatus())) {
            record.setCloseTime(now);
        }
        return vegOrderMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    @Transactional
    public int updateItems(Long id, VegOrderItemsUpdateParam param) {
        if (CollectionUtils.isEmpty(param.getItems())) {
            return 0;
        }
        vegOrderItemMapper.deleteByOrderId(id);
        List<VegOrderItem> items = param.getItems().stream().map(itemParam -> {
            VegOrderItem item = new VegOrderItem();
            item.setOrderId(id);
            item.setProductId(itemParam.getProductId());
            item.setProductName(itemParam.getProductName());
            item.setSpecDesc(itemParam.getSpecDesc());
            item.setUnit(itemParam.getUnit());
            item.setPrice(itemParam.getPrice());
            item.setQuantity(itemParam.getQuantity());
            return item;
        }).collect(Collectors.toList());
        vegOrderItemMapper.insertBatch(items);
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        VegOrder order = new VegOrder();
        order.setId(id);
        order.setTotalAmount(total);
        order.setOperatorRemark(param.getOperatorRemark());
        return vegOrderMapper.updateByPrimaryKeySelective(order);
    }
}
