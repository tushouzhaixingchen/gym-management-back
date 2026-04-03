package com.gym.management.service;

import com.gym.management.dto.response.OrderResponse;
import com.gym.management.entity.Appointment;

import java.util.List;

public interface OrderService {

    /**
     * 根据预约创建订单
     */
    void createOrder(Appointment appointment);

    /**
     * 获取会员订单列表
     */
    List<OrderResponse> getMemberOrders(Integer memberId);
}