package com.gym.management.service;

import com.gym.management.dto.response.OrderResponse;
import com.gym.management.entity.Appointment;
import com.gym.management.entity.CourseBooking;

import java.util.List;

public interface OrderService {

    /**
     * 根据预约创建订单
     */
    void createOrder(Appointment appointment);

    /**
     * 根据课程报名创建订单
     */
    void createOrderFromCourseBooking(CourseBooking booking);

    /**
     * 获取会员订单列表
     */
    List<OrderResponse> getMemberOrders(Integer memberId);
}