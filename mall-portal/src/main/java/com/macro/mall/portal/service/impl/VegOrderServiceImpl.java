package com.macro.mall.portal.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.mapper.PmsVegProductMapper;
import com.macro.mall.mapper.VegOrderItemMapper;
import com.macro.mall.mapper.VegOrderMapper;
import com.macro.mall.model.PmsVegProduct;
import com.macro.mall.model.UmsMember;
import com.macro.mall.model.VegOrder;
import com.macro.mall.model.VegOrderItem;
import com.macro.mall.portal.dto.*;
import com.macro.mall.portal.service.VegOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class VegOrderServiceImpl implements VegOrderService {

    @Autowired
    private VegOrderMapper vegOrderMapper;
    @Autowired
    private VegOrderItemMapper vegOrderItemMapper;
    @Autowired
    private PmsVegProductMapper vegProductMapper;

    private static final String STATUS_PENDING = "PENDING";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VegOrderSubmitResult submitOrder(UmsMember member, VegSubmitOrderParam param) {
        if (CollectionUtils.isEmpty(param.getItems())) {
            Asserts.fail("下单商品不能为空");
        }
        List<VegOrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (VegOrderItemParam itemParam : param.getItems()) {
            PmsVegProduct product = vegProductMapper.selectByPrimaryKey(itemParam.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                Asserts.fail("商品不可用: " + itemParam.getProductId());
            }
            Integer quantity = itemParam.getQuantity();
            if (quantity == null || quantity <= 0) {
                Asserts.fail("无效的购买数量");
            }
            VegOrderItem orderItem = new VegOrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPic(product.getPic());
            orderItem.setSpecDesc(product.getSpecDesc());
            orderItem.setUnit(product.getUnit());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItems.add(orderItem);
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            total = total.add(lineTotal);
        }
        VegOrder order = new VegOrder();
        order.setMemberId(member.getId());
        order.setMemberUsername(member.getUsername());
        order.setOrderSn(generateOrderSn());
        order.setStatus(STATUS_PENDING);
        order.setRemark(param.getRemark());
        order.setTotalAmount(total);
        order.setSubmitTime(new Date());
        vegOrderMapper.insertSelective(order);
        Long orderId = order.getId();
        for (VegOrderItem item : orderItems) {
            item.setOrderId(orderId);
        }
        vegOrderItemMapper.insertBatch(orderItems);
        VegOrderSubmitResult result = new VegOrderSubmitResult();
        result.setOrderId(orderId);
        result.setOrderSn(order.getOrderSn());
        result.setStatus(order.getStatus());
        result.setTotalAmount(order.getTotalAmount());
        return result;
    }

    @Override
    public List<VegOrderSimple> list(UmsMember member, String status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        List<VegOrder> orders = vegOrderMapper.selectByMember(member.getId(), status);
        return orders.stream().map(this::toSimple).collect(Collectors.toList());
    }

    @Override
    public VegOrderDetailDTO detail(UmsMember member, Long orderId) {
        VegOrder order = vegOrderMapper.selectByPrimaryKey(orderId);
        if (order == null || !order.getMemberId().equals(member.getId())) {
            Asserts.fail("订单不存在");
        }
        VegOrderDetailDTO detail = new VegOrderDetailDTO();
        detail.setOrder(order);
        detail.setItems(vegOrderItemMapper.selectByOrderId(orderId));
        return detail;
    }

    private VegOrderSimple toSimple(VegOrder order) {
        VegOrderSimple simple = new VegOrderSimple();
        simple.setId(order.getId());
        simple.setOrderSn(order.getOrderSn());
        simple.setStatus(order.getStatus());
        simple.setTotalAmount(order.getTotalAmount());
        simple.setSubmitTime(order.getSubmitTime());
        return simple;
    }

    private String generateOrderSn() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timePart = sdf.format(new Date());
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "VEG" + timePart + random;
    }
}
