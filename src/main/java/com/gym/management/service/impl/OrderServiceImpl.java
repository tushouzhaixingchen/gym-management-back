package com.gym.management.service.impl;

import com.gym.management.dto.response.OrderResponse;
import com.gym.management.entity.Appointment;
import com.gym.management.entity.CourseBooking;
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
        // 使用时间戳（毫秒）+ 随机数生成唯一订单号
        String orderNo = "ORD" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
        order.setOrderNo(orderNo);
        order.setStoreId(appointment.getStoreId());
        order.setMemberId(appointment.getMemberId());
        order.setOrderType("appointment");
        order.setOrderAmount(appointment.getPrice());
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setPayAmount(appointment.getPrice());
        order.setPayMethod(String.valueOf(appointment.getPayMethod()));
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setRemark("预约单号: " + appointment.getAppointmentNo());
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }

    @Override
    public void createOrderFromCourseBooking(CourseBooking booking) {
        Order order = new Order();
        // 使用时间戳（毫秒）+ 随机数生成唯一订单号
        String orderNo = "ORD" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
        order.setOrderNo(orderNo);
        order.setStoreId(booking.getStoreId());
        order.setMemberId(booking.getMemberId());
        order.setOrderType("course");
        order.setOrderAmount(booking.getPrice());
        order.setDiscountAmount(java.math.BigDecimal.ZERO);
        order.setPayAmount(booking.getPrice());
        order.setPayMethod(String.valueOf(booking.getPayMethod()));
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setRemark("课程报名单号: " + booking.getBookingNo());
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
