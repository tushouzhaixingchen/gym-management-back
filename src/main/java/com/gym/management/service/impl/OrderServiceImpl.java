package com.gym.management.service.impl;


import com.gym.management.dto.response.OrderResponse;
import com.gym.management.entity.Appointment;
import com.gym.management.entity.Order;
import com.gym.management.repository.OrderRepository;
import com.gym.management.service.OrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void createOrder(Appointment appointment) {
        Order order = new Order();
        order.setOrderNo("ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        order.setStoreId(appointment.getStoreId());
        order.setMemberId(appointment.getMemberId());
        order.setOrderType("appointment");
        order.setOrderAmount(appointment.getPrice());
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setPayAmount(appointment.getPrice());
        order.setPayMethod(String.valueOf(appointment.getPayMethod())); // 简单转换
        order.setPayStatus(1); // 预约支付即视为订单已支付
        order.setPayTime(LocalDateTime.now());
        order.setRemark("预约单号: " + appointment.getAppointmentNo());
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    @Override
    public List<OrderResponse> getMemberOrders(Integer memberId) {
        List<Order> list = orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        return list.stream().map(order -> {
            OrderResponse res = new OrderResponse();
            BeanUtils.copyProperties(order, res);
            return res;
        }).collect(Collectors.toList());
    }
}